package com.custommusic.client.audio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.custommusic.network.MusicCommandPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class MusicManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, MusicSoundInstance> activeSounds = new ConcurrentHashMap<>();
    private static final Map<String, ScheduledPlay> scheduled = new ConcurrentHashMap<>();
    private static boolean globallyPaused = false;

    private record ScheduledPlay(String musicId, long startAt, MusicSoundInstance instance) {}

    public static boolean isGloballyPaused() {
        return globallyPaused;
    }

    public static void handle(MusicCommandPayload payload) {
        if (payload.command() == MusicCommandPayload.Command.PLAY) {
            // Start decoding the full song into memory as soon as the command
            // arrives, so we are ready to seek to the exact sync point when the
            // scheduled start time is reached.
            MusicStreamCache.ensureLoaded(payload.sound());

            long clientNow = System.currentTimeMillis();

            // The payload's timestamp field carries the SERVER GAME TIME (ticks) at send.
            // The client's game time is kept in sync with the server by vanilla, so the
            // difference measures how long the packet spent in transit (login pipeline,
            // network latency) without any wall-clock skew between machines. Add that
            // to the song offset, then anchor playback to this client's own clock.
            long serverSendGameTime = payload.startTimestamp();
            var level = net.minecraft.client.Minecraft.getInstance().level;
            long transitTicks = 0L;
            if (level != null && serverSendGameTime > 0L) {
                transitTicks = level.getGameTime() - serverSendGameTime;
                // Guard against bogus game-time mismatches; transit should be small.
                transitTicks = Math.max(0L, Math.min(transitTicks, 1200L));
            }
            int effectiveOffset = payload.offsetTicks() + (int) transitTicks;

            LOGGER.info("MusicManager.handle PLAY id={} serverGameTime={} clientGameTime={} transitTicks={} offset={} effectiveOffset={}",
                payload.musicId(), serverSendGameTime, level != null ? level.getGameTime() : -1, transitTicks, payload.offsetTicks(), effectiveOffset);

            MusicCommandPayload local = new MusicCommandPayload(
                payload.command(), payload.musicId(), payload.sound(), payload.pos(),
                effectiveOffset, clientNow, payload.volume(), payload.pitch(), payload.relative()
            );

            if (globallyPaused) {
                stop(local.musicId());
                scheduled.put(local.musicId(), new ScheduledPlay(local.musicId(), clientNow, createInstance(local)));
            } else {
                play(local);
            }
        } else if (payload.command() == MusicCommandPayload.Command.SET_VOLUME) {
            setVolume(payload.musicId(), payload.volume());
        } else {
            stop(payload.musicId());
        }
    }

    private static MusicSoundInstance createInstance(MusicCommandPayload payload) {
        // Relative sounds are positioned relative to the listener, so they must
        // use (0,0,0) - absolute world coordinates would place the sound far away.
        double x = payload.relative() ? 0.0 : payload.pos().getX() + 0.5;
        double y = payload.relative() ? 0.0 : payload.pos().getY() + 0.5;
        double z = payload.relative() ? 0.0 : payload.pos().getZ() + 0.5;

        return new MusicSoundInstance(
            payload.sound(), SoundSource.RECORDS, payload.volume(), payload.pitch(),
            x, y, z, payload.relative(), payload.startTimestamp(), payload.offsetTicks()
        );
    }

    public static void play(MusicCommandPayload payload) {
        play(payload.musicId(), createInstance(payload));
    }

    public static void play(String musicId, Identifier sound, BlockPos pos, int offsetTicks, long startTimestamp, float volume, float pitch, boolean relative) {
        play(new MusicCommandPayload(MusicCommandPayload.Command.PLAY, musicId, sound, pos, offsetTicks, startTimestamp, volume, pitch, relative));
    }

    private static void play(String musicId, MusicSoundInstance instance) {
        // Stop any existing sound with the same id before playing the new one,
        // otherwise both will play simultaneously (e.g. duplicate on player join).
        MusicSoundInstance existing = activeSounds.remove(musicId);
        if (existing != null) {
            Minecraft.getInstance().getSoundManager().stop(existing);
        }
        scheduled.remove(musicId);
        activeSounds.put(musicId, instance);
        Minecraft.getInstance().getMusicManager().stopPlaying();
        Minecraft.getInstance().getSoundManager().play(instance);
        LOGGER.info("MusicManager.play id={} active={} replaced={}", musicId, activeSounds.size(), existing != null);
    }

    public static void stop(String musicId) {
        scheduled.remove(musicId);
        MusicSoundInstance existing = activeSounds.remove(musicId);
        if (existing != null) {
            Minecraft.getInstance().getSoundManager().stop(existing);
        }
    }

    public static void stopAll() {
        if (activeSounds.isEmpty() && scheduled.isEmpty() && !globallyPaused) {
            return;
        }
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        for (MusicSoundInstance instance : activeSounds.values()) {
            soundManager.stop(instance);
        }
        activeSounds.clear();
        scheduled.clear();
        globallyPaused = false;
        LOGGER.info("MusicManager.stopAll");
    }

    public static void setVolume(String musicId, float volume) {
        MusicSoundInstance existing = activeSounds.get(musicId);
        if (existing != null) {
            existing.setVolume(volume);
            LOGGER.info("MusicManager.setVolume id={} vol={}", musicId, volume);
        }
    }

    // Called when the game pauses or the window loses focus. Instead of relying on
    // Minecraft's global pause/resume (which can unpause our streaming music
    // unexpectedly), we stop each active sound and replay it from the right offset
    // when the game resumes.
    public static void pauseAll() {
        if (globallyPaused) {
            return;
        }
        globallyPaused = true;
        long now = System.currentTimeMillis();
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        for (MusicSoundInstance instance : activeSounds.values()) {
            if (instance != null) {
                instance.setStopped(true);
                instance.setPauseStartMillis(now);
                soundManager.stop(instance);
            }
        }
        LOGGER.info("MusicManager.pauseAll active={}", activeSounds.size());
    }

    public static void resumeAll() {
        if (!globallyPaused) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            stopAll();
            return;
        }
        if (mc.isPaused() || !mc.isWindowActive()) {
            LOGGER.info("MusicManager.resumeAll skipped: game paused={} windowActive={}", mc.isPaused(), mc.isWindowActive());
            return;
        }
        globallyPaused = false;
        long now = System.currentTimeMillis();
        SoundManager soundManager = mc.getSoundManager();
        mc.getMusicManager().stopPlaying();
        for (Map.Entry<String, MusicSoundInstance> entry : activeSounds.entrySet()) {
            MusicSoundInstance instance = entry.getValue();
            if (instance == null) continue;
            long pauseDuration = now - instance.getPauseStartMillis();
            long totalPause = instance.getTotalPauseMillis() + pauseDuration;
            instance.setTotalPauseMillis(totalPause);
            instance.setStartTimestampMillis(instance.getOriginalStartTimestampMillis() + totalPause);
            instance.setStopped(false);
            soundManager.play(instance);
            LOGGER.info("MusicManager.resumeAll id={} start={} totalPause={}", entry.getKey(), instance.getStartTimestampMillis(), totalPause);
        }
    }

    /**
     * Checks any scheduled play commands and starts them once their start time is
     * reached. This is what keeps multiple clients starting on the same beat in
     * multiplayer.
     */
    public static void tickScheduled() {
        if (globallyPaused || scheduled.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        java.util.Iterator<Map.Entry<String, ScheduledPlay>> it = scheduled.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ScheduledPlay> entry = it.next();
            ScheduledPlay sp = entry.getValue();
            if (sp.startAt <= now) {
                it.remove();
                play(sp.musicId, sp.instance);
            }
        }
    }
}
