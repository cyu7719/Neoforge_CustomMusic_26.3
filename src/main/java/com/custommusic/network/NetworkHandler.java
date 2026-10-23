package com.custommusic.network;

import com.custommusic.blockentity.JukeboxAlbumBlockEntity;
import com.custommusic.CustomMusicMod;
import com.custommusic.init.ModItems;
import com.custommusic.init.ModSounds;
import com.custommusic.item.JukeboxAlbumBlockItem;
import com.custommusic.item.PortableJukeboxAlbumItem;
import com.custommusic.item.PortableJukeboxContainer;
import com.custommusic.JukeboxManager;
import com.custommusic.menu.PortableJukeboxAlbumMenu;
import com.custommusic.network.ButtonClickPayload;
import com.custommusic.network.JukeboxPickPayload;
import com.custommusic.network.JukeboxVolumePayload;
import com.custommusic.network.MusicStatePayload;
import com.custommusic.network.NowPlayingPayload;
import com.custommusic.network.PortableButtonClickPayload;
import com.custommusic.network.PortableMusicStatePayload;
import com.custommusic.network.PortableVolumePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class NetworkHandler {
    
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        
        registrar.playToClient(
            MusicStatePayload.TYPE,
            MusicStatePayload.STREAM_CODEC,
            (payload, context) -> {
                // Client-side handler - update the block entity with new state
                context.enqueueWork(() -> {
                    if (context.player().level().getBlockEntity(payload.pos()) instanceof JukeboxAlbumBlockEntity be) {
                        // Update the block entity state on client side
                        be.updateClientState(payload.isPlaying(), payload.isPaused(), payload.currentSlot(), payload.currentPage(), payload.ticksSinceStart(), payload.spinningStartTime(), payload.volume(), payload.songStartGameTime());
                    }
                });
            }
        );

        registrar.playToClient(
            MusicCommandPayload.TYPE,
            MusicCommandPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> dispatchMusicCommandClient(payload));
            }
        );

        registrar.playToServer(
            ButtonClickPayload.TYPE,
            ButtonClickPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    BlockEntity be = context.player().level().getBlockEntity(payload.pos());
                    ServerPlayer serverPlayer = (context.player() instanceof ServerPlayer sp) ? sp : null;
                    if (be instanceof JukeboxAlbumBlockEntity jukeboxAlbum) {
                        switch (payload.buttonType()) {
                            case PLAY_PAUSE:
                                if (jukeboxAlbum.isPlaying() && !jukeboxAlbum.isPaused()) {
                                    jukeboxAlbum.pauseMusic();
                                } else if (jukeboxAlbum.isPaused()) {
                                    JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer);
                                    jukeboxAlbum.resumeMusic();
                                } else {
                                    JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer);
                                    jukeboxAlbum.playMusic();
                                }
                                if (serverPlayer != null) {
                                    sendStateUpdate(jukeboxAlbum, serverPlayer);
                                }
                                break;
                            case NEXT_SONG:
                                JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer);
                                jukeboxAlbum.nextSong();
                                if (serverPlayer != null) {
                                    sendStateUpdate(jukeboxAlbum, serverPlayer);
                                }
                                break;
                            case PREVIOUS_SONG:
                                JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer);
                                jukeboxAlbum.previousSong();
                                if (serverPlayer != null) {
                                    sendStateUpdate(jukeboxAlbum, serverPlayer);
                                }
                                break;
                            case NEXT_PAGE:
                                jukeboxAlbum.nextPage();
                                if (serverPlayer != null) {
                                    sendStateUpdate(jukeboxAlbum, serverPlayer);
                                }
                                break;
                            case PREVIOUS_PAGE:
                                jukeboxAlbum.previousPage();
                                if (serverPlayer != null) {
                                    sendStateUpdate(jukeboxAlbum, serverPlayer);
                                }
                                break;
                            case PLAY_SLOT:
                                JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer);
                                jukeboxAlbum.playSlot(payload.slot());
                                if (serverPlayer != null) {
                                    sendStateUpdate(jukeboxAlbum, serverPlayer);
                                }
                                break;
                            case STOP_MUSIC:
                                jukeboxAlbum.stopMusic();
                                if (serverPlayer != null) {
                                    sendStateUpdate(jukeboxAlbum, serverPlayer);
                                }
                                break;
                            case SHIFT_CLICK_SLOT:
                                // Extract item to player inventory and stop music if playing
                                ItemStack itemToExtract = jukeboxAlbum.getItem(payload.slot());
                                if (!itemToExtract.isEmpty()) {
                                    // Check if this was the playing disc and stop music if needed
                                    boolean wasPlaying = jukeboxAlbum.isPlaying() && 
                                        jukeboxAlbum.getCurrentlyPlayingSlot() == payload.slot();
                                    
                                    // Try to add to player inventory first
                                    if (serverPlayer != null) {
                                        // Create a copy to avoid reference issues
                                        ItemStack itemToAdd = itemToExtract.copy();
                                        itemToAdd.setCount(1); // Only extract one item
                                        
                                        if (serverPlayer.getInventory().add(itemToAdd)) {
                                            // Successfully added to inventory, now remove from jukebox
                                            jukeboxAlbum.removeItem(payload.slot(), 1);
                                        } else {
                                            // If inventory is full, drop the item
                                            serverPlayer.drop(itemToAdd, false, Prediction.SERVER_ONLY);
                                            // Remove from jukebox since we dropped it
                                            jukeboxAlbum.removeItem(payload.slot(), 1);
                                        }
                                    }
                                    
                                    // Stop music if this was the playing disc
                                    if (wasPlaying) {
                                        jukeboxAlbum.stopMusic();
                                    }
                                }
                                break;
                        }
                    }
                });
            }
        );
        
        registrar.playToServer(
            PortableButtonClickPayload.TYPE,
            PortableButtonClickPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    // Handle portable jukebox button clicks
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        // Find the portable jukebox container in the player's open menu
                        if (serverPlayer.containerMenu instanceof PortableJukeboxAlbumMenu portableMenu) {
                            PortableJukeboxContainer container = portableMenu.getContainer();
                            if (container != null) {
                                switch (payload.buttonType()) {
                                    case PLAY_PAUSE:
                                        if (container.isPlaying() && !container.isPaused()) {
                                            container.pauseMusic();
                                        } else if (container.isPaused()) {
                                            JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer, container.getStack());
                                            JukeboxManager.stopNearbyJukeboxAlbumForPlayer(serverPlayer);
                                            container.resumeMusic();
                                        } else {
                                            JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer, container.getStack());
                                            JukeboxManager.stopNearbyJukeboxAlbumForPlayer(serverPlayer);
                                            container.playMusic();
                                        }
                                        break;
                                    case NEXT_SONG:
                                        JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer, container.getStack());
                                        JukeboxManager.stopNearbyJukeboxAlbumForPlayer(serverPlayer);
                                        container.nextSong();
                                        break;
                                    case PREVIOUS_SONG:
                                        JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer, container.getStack());
                                        JukeboxManager.stopNearbyJukeboxAlbumForPlayer(serverPlayer);
                                        container.previousSong();
                                        break;
                                    case NEXT_PAGE:
                                        container.nextPage();
                                        break;
                                    case PREVIOUS_PAGE:
                                        container.previousPage();
                                        break;
                                    case PLAY_SLOT:
                                        JukeboxManager.stopPortableJukeboxForPlayer(serverPlayer, container.getStack());
                                        JukeboxManager.stopNearbyJukeboxAlbumForPlayer(serverPlayer);
                                        container.playSlot(payload.slot());
                                        break;
                                    case STOP_MUSIC:
                                        container.stopMusic();
                                        break;
                                    case SHIFT_CLICK_SLOT:
                                        // Extract item to player inventory and stop music if playing
                                        ItemStack itemToExtract = container.getItem(payload.slot());
                                        if (!itemToExtract.isEmpty()) {
                                            // Check if this was the playing disc and stop music if needed
                                            boolean wasPlaying = container.isPlaying() && 
                                                container.getCurrentlyPlayingSlot() == payload.slot();
                                            
                                            // Try to add to player inventory first
                                            ItemStack itemToAdd = itemToExtract.copy();
                                            itemToAdd.setCount(1); // Only extract one item
                                            
                                            if (serverPlayer.getInventory().add(itemToAdd)) {
                                                // Successfully added to inventory, now remove from container
                                                container.removeItem(payload.slot(), 1);
                                            } else {
                                                // If inventory is full, drop the item
                                                serverPlayer.drop(itemToAdd, false, Prediction.SERVER_ONLY);
                                                // Remove from container since we dropped it
                                                container.removeItem(payload.slot(), 1);
                                            }
                                            
                                            // Stop music if this was the playing disc
                                            if (wasPlaying) {
                                                container.stopMusic();
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                });
            }
        );
        
        // Register PortableMusicStatePayload for client-side state updates
        registrar.playToClient(
            PortableMusicStatePayload.TYPE,
            PortableMusicStatePayload.STREAM_CODEC,
            (payload, context) -> {
                // Client-side handler - update the portable jukebox container with new state
                context.enqueueWork(() -> {
                    // Find the portable jukebox container from the player's open menu
                    if (context.player().containerMenu instanceof PortableJukeboxAlbumMenu menu) {
                        PortableJukeboxContainer container = menu.getContainer();
                        if (container != null) {
                            // Update the container state on client side
                            container.updateClientState(payload.isPlaying(), payload.isPaused(), payload.currentSlot(), payload.currentPage(), payload.ticksSinceStart(), payload.volume(), payload.songStartGameTime(), payload.songLengthTicks(), payload.pausedTicksPosition());
                        }
                    }
                });
            }
        );

        // Register JukeboxVolumePayload for client -> server block jukebox volume changes
        registrar.playToServer(
            JukeboxVolumePayload.TYPE,
            JukeboxVolumePayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    if (context.player().level().getBlockEntity(payload.pos()) instanceof JukeboxAlbumBlockEntity jukebox) {
                        jukebox.setPlayerVolume(context.player().getUUID(), payload.volume());
                    }
                });
            }
        );

        // Register PortableVolumePayload for client -> server portable jukebox volume changes
        registrar.playToServer(
            PortableVolumePayload.TYPE,
            PortableVolumePayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    if (context.player().containerMenu instanceof PortableJukeboxAlbumMenu menu) {
                        PortableJukeboxContainer container = menu.getContainer();
                        if (container != null) {
                            container.setVolume(payload.volume());
                        }
                    }
                });
            }
        );

        // Register NowPlayingPayload for vanilla "now playing" message
        registrar.playToClient(
            NowPlayingPayload.TYPE,
            NowPlayingPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                    if (minecraft.gui != null) {
                        minecraft.gui.hud.setNowPlaying(net.minecraft.network.chat.Component.literal(payload.songDescriptionJson()));
                    }
                });
            }
        );

        // Register MusicSeekPayload for client -> server scrub/seek requests
        registrar.playToServer(
            MusicSeekPayload.TYPE,
            MusicSeekPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (payload.portable()) {
                        ItemStack stack = findPlayingPortableJukebox(player);
                        if (!stack.isEmpty()) {
                            // When this stack's menu is open, go through its
                            // container so its in-memory state stays in sync;
                            // a second container would later overwrite the seek.
                            if (player.containerMenu instanceof PortableJukeboxAlbumMenu menu
                                    && menu.getContainer() != null && menu.getContainer().getStack() == stack) {
                                menu.getContainer().seekTo(payload.positionTicks());
                            } else {
                                PortableJukeboxContainer container = new PortableJukeboxContainer(stack, player.level());
                                container.seekTo(payload.positionTicks());
                                container.saveToStack(stack);
                            }
                        }
                    } else {
                        // Sanity-check the player is close enough to actually hear this block
                        if (player.distanceToSqr(payload.pos().getX() + 0.5, payload.pos().getY() + 0.5, payload.pos().getZ() + 0.5) > 96 * 96) {
                            return;
                        }
                        if (player.level().getBlockEntity(payload.pos()) instanceof JukeboxAlbumBlockEntity jukebox) {
                            jukebox.seekTo(payload.positionTicks());
                        }
                    }
                });
            }
        );

        // Register JukeboxPickPayload for client -> server empty-stack pick in inventory GUIs
        registrar.playToServer(
            JukeboxPickPayload.TYPE,
            JukeboxPickPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.hasInfiniteMaterials()) {
                        if (player.containerMenu.isValidSlotIndex(payload.slotIndex())) {
                            Slot slot = player.containerMenu.getSlot(payload.slotIndex());
                            if (slot != null && slot.hasItem() && isJukeboxLike(slot.getItem())) {
                                ItemStack stack = slot.getItem();
                                ItemStack clean = stack.getItem().getDefaultInstance().copyWithCount(stack.getMaxStackSize());
                                player.containerMenu.setCarried(clean);
                            }
                        }
                    }
                });
            }
        );
    }
    
    public static void sendStateUpdate(JukeboxAlbumBlockEntity jukeboxAlbum, ServerPlayer player) {
        MusicStatePayload payload = new MusicStatePayload(
            jukeboxAlbum.getBlockPos(),
            jukeboxAlbum.isPlaying(),
            jukeboxAlbum.isPaused(),
            jukeboxAlbum.getCurrentlyPlayingSlot(),
            jukeboxAlbum.getCurrentPage(),
            jukeboxAlbum.getTicksSinceStart(),
            jukeboxAlbum.getSpinningStartTime(),
            jukeboxAlbum.getVolumeForPlayer(player),
            jukeboxAlbum.getSongStartGameTime()
        );
        PacketDistributor.sendToPlayer(player, payload);
    }
    
    private static ItemStack findPlayingPortableJukebox(ServerPlayer player) {
        java.util.List<ItemStack> candidates = new java.util.ArrayList<>();
        candidates.add(player.getMainHandItem());
        candidates.add(player.getOffhandItem());
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            candidates.add(player.getInventory().getItem(i));
        }
        if (player.containerMenu != null) {
            candidates.add(player.containerMenu.getCarried());
        }
        for (ItemStack stack : candidates) {
            if (stack.isEmpty() || !(stack.getItem() instanceof PortableJukeboxAlbumItem)) continue;
            var data = stack.get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
            if (data != null && data.isPlaying() && data.currentlyPlayingSlot() >= 0) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void dispatchMusicCommandClient(MusicCommandPayload payload) {
        com.mojang.logging.LogUtils.getLogger().info("[CustomMusic] dispatchMusicCommandClient command={} sound={} pos={} vol={}", payload.command(), payload.sound(), payload.pos(), payload.volume());
        try {
            Class<?> clazz = Class.forName("com.custommusic.client.audio.MusicManager");
            clazz.getMethod("handle", MusicCommandPayload.class).invoke(null, payload);
        } catch (Exception e) {
            com.mojang.logging.LogUtils.getLogger().error("[CustomMusic] Failed to handle music command {} for {}", payload.command(), payload.sound(), e);
        }
    }

    public static void sendMusicCommand(ServerPlayer player, MusicCommandPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendNowPlaying(Component songDescription, ServerPlayer player) {
        NowPlayingPayload payload = new NowPlayingPayload(songDescription.getString());
        PacketDistributor.sendToPlayer(player, payload);
    }

    private static boolean isJukeboxLike(ItemStack stack) {
        return stack.getItem() instanceof JukeboxAlbumBlockItem
                || stack.getItem() instanceof PortableJukeboxAlbumItem
                || stack.getItem() == ModItems.JUKEBOX_ALBUM.get()
                || stack.getItem() == ModItems.PORTABLE_JUKEBOX_ALBUM.get();
    }
}
