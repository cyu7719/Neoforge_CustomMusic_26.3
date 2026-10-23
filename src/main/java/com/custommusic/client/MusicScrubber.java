package com.custommusic.client;

import org.jetbrains.annotations.Nullable;

import com.custommusic.blockentity.JukeboxAlbumBlockEntity;
import com.custommusic.item.CustomMusicDiscItem;
import com.custommusic.item.PortableJukeboxContainer;
import com.custommusic.network.MusicSeekPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Shared helpers for the Spotify-style seek bar drawn below the jukebox
 * screens: playback resolution, m:ss countdown text, and bar rendering.
 */
public final class MusicScrubber {
    private MusicScrubber() {}

    public static final int SCRUB_GREEN = 0xFF1DB954;
    private static final int TRACK_COLOR = 0xAA1E1E1E;
    private static final int FILL_COLOR_PAUSED = 0xFF7A7A7A;
    private static final int KNOB_COLOR = 0xFFFFFFFF;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    public record Playback(boolean portable, BlockPos pos, int slot, int positionTicks, int lengthTicks, boolean paused) {}

    /** Formats a remaining tick count as m:ss (counting down as the song plays). */
    public static String remainingText(int remainingTicks) {
        int totalSeconds = Math.max(0, (remainingTicks + 19) / 20);
        return (totalSeconds / 60) + ":" + String.format("%02d", totalSeconds % 60);
    }

    /**
     * Draws the seek bar and countdown text. Shared between the portable and
     * block screens so both render identically.
     */
    public static void drawScrubber(GuiGraphicsExtractor gfx, Font font,
                                    int barX0, int barX1, int barY, int barH,
                                    float progress, String text, int textX, int textY,
                                    boolean paused) {
        if (barX1 > barX0) {
            gfx.fill(barX0, barY, barX1, barY + barH, TRACK_COLOR);
            int fillEnd = barX0 + (int) ((barX1 - barX0) * Math.max(0F, Math.min(1F, progress)));
            if (fillEnd > barX0) {
                gfx.fill(barX0, barY, fillEnd, barY + barH, paused ? FILL_COLOR_PAUSED : SCRUB_GREEN);
            }
            // Knob
            int knobX = Math.max(barX0, Math.min(fillEnd, barX1));
            gfx.fill(knobX - 1, barY - 1, knobX + 2, barY + barH + 1, KNOB_COLOR);
        }
        gfx.text(font, text, textX, textY, TEXT_COLOR, false);
    }

    /**
     * Resolves portable playback from the client menu container, whose fields
     * are kept in sync by PortableMusicStatePayload. The stack's data component
     * is not refreshed on the client, so it cannot be used for live display.
     */
    public static @Nullable Playback portablePlayback(Minecraft mc, PortableJukeboxContainer container) {
        if (container == null || !container.isPlaying() || container.getCurrentlyPlayingSlot() < 0) {
            return null;
        }
        int position;
        if (container.isPaused()) {
            position = container.getTicksSinceStart();
        } else {
            position = container.getSongStartGameTime() > 0L && mc.level != null
                ? (int) (mc.level.getGameTime() - container.getSongStartGameTime())
                : container.getTicksSinceStart();
        }
        int length = container.getSongLengthTicks();
        if (length <= 0) {
            length = songLengthTicks(container.getItem(container.getCurrentlyPlayingSlot()));
        }
        position = Math.max(0, Math.min(position, Math.max(0, length - 1)));
        return new Playback(true, BlockPos.ZERO, container.getCurrentlyPlayingSlot(),
            position, length, container.isPaused());
    }

    public static @Nullable Playback blockPlayback(Minecraft mc, JukeboxAlbumBlockEntity be) {
        if (be == null || !be.isPlaying() || be.getCurrentlyPlayingSlot() < 0) return null;
        int position;
        if (be.isPaused()) {
            position = be.getTicksSinceStart();
        } else {
            position = be.getSongStartGameTime() > 0L && mc.level != null
                ? (int) (mc.level.getGameTime() - be.getSongStartGameTime())
                : be.getTicksSinceStart();
        }
        int length = be.getSongLengthTicks();
        if (length <= 0) {
            length = songLengthTicks(be.getItem(be.getCurrentlyPlayingSlot()));
        }
        position = Math.max(0, Math.min(position, Math.max(0, length - 1)));
        return new Playback(false, be.getBlockPos(), be.getCurrentlyPlayingSlot(), position, length, be.isPaused());
    }

    private static int songLengthTicks(ItemStack stack) {
        if (stack.getItem() instanceof CustomMusicDiscItem disc) {
            return disc.getLengthInTicks();
        }
        if (stack.has(DataComponents.JUKEBOX_PLAYABLE)) {
            var opt = JukeboxSong.fromStack(stack);
            if (opt.isPresent() && opt.get().isBound()) {
                return (int) (opt.get().value().lengthInSeconds() * 20);
            }
        }
        return 0;
    }

    /** Sends a seek request for the given playback source. Returns the tick position sent, or -1. */
    public static int sendSeek(boolean portable, BlockPos pos, float fraction, int lengthTicks) {
        if (lengthTicks <= 0) return -1;
        int ticks = Math.max(0, Math.min((int) (fraction * lengthTicks), lengthTicks - 1));
        ClientPacketDistributor.sendToServer(new MusicSeekPayload(portable,
            portable ? BlockPos.ZERO : pos, ticks));
        return ticks;
    }
}
