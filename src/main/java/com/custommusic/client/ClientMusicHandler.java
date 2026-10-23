package com.custommusic.client;

import com.custommusic.client.audio.MusicManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientMusicHandler {
    private static boolean wasPaused = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            MusicManager.stopAll();
            wasPaused = false;
            return;
        }

        // Start any music that has reached its scheduled multiplayer start time.
        MusicManager.tickScheduled();

        // Pause music when the game is paused or the window is not focused (alt-tab/click out).
        boolean paused = mc.isPaused() || !mc.isWindowActive();
        if (paused != wasPaused) {
            wasPaused = paused;
            if (paused) {
                MusicManager.pauseAll();
            } else {
                MusicManager.resumeAll();
            }
        }
    }
}
