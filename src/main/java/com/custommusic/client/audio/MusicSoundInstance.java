package com.custommusic.client.audio;

import java.util.concurrent.CompletableFuture;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class MusicSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private long startTimestampMillis;
    private final long originalStartTimestampMillis;
    private final int offsetTicks;
    private final boolean isRelative;
    private long totalPauseMillis;
    private long pauseStartMillis;
    private boolean stopped;

    public MusicSoundInstance(Identifier sound, SoundSource source, float volume, float pitch, double x, double y, double z, boolean relative, long startTimestampMillis, int offsetTicks) {
        super(sound, source, RandomSource.create());
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isRelative = relative;
        this.startTimestampMillis = startTimestampMillis;
        this.originalStartTimestampMillis = startTimestampMillis;
        this.offsetTicks = offsetTicks;
        this.attenuation = relative ? Attenuation.NONE : Attenuation.LINEAR;
        this.looping = false;
        this.stopped = false;
    }

    @Override
    public boolean isRelative() {
        return isRelative;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public long getStartTimestampMillis() {
        return startTimestampMillis;
    }

    public void setStartTimestampMillis(long startTimestampMillis) {
        this.startTimestampMillis = startTimestampMillis;
    }

    public long getOriginalStartTimestampMillis() {
        return originalStartTimestampMillis;
    }

    public long getTotalPauseMillis() {
        return totalPauseMillis;
    }

    public void setTotalPauseMillis(long totalPauseMillis) {
        this.totalPauseMillis = totalPauseMillis;
    }

    public long getPauseStartMillis() {
        return pauseStartMillis;
    }

    public void setPauseStartMillis(long pauseStartMillis) {
        this.pauseStartMillis = pauseStartMillis;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void tick() {
        if (MusicManager.isGloballyPaused()) {
            this.stopped = true;
        }
    }

    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        // Decodes the full audio into memory so we can seek to any position
        // instantly. This is what makes multiplayer join/resume synchronization
        // accurate enough to keep all clients on the same beat.
        return MusicStreamCache.getStream(sound.getPath(), startTimestampMillis, offsetTicks, soundBuffers, sound, looping);
    }
}
