package com.custommusic.item;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.custommusic.menu.PortableJukeboxAlbumMenu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class PortableJukeboxAlbumItem extends Item {
    private static final int SLOTS_PER_PAGE = 54;
    
    public PortableJukeboxAlbumItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }
    
        
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            serverPlayer.openMenu(new PortableJukeboxMenuProvider(stack));
        }
        return InteractionResult.SUCCESS;
    }
    
    // onDroppedByPlayer behavior not available in this Minecraft/NeoForge version

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slotEnum) {
        // Handle music playback when item is in inventory
        if (level.isClientSide() || !(entity instanceof Player player)) return;

        // Clean up old data components from previous versions
        if (stack.has(net.minecraft.core.component.DataComponents.CONTAINER)) {
            stack.remove(net.minecraft.core.component.DataComponents.CONTAINER);
        }
        if (stack.has(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA)) {
            stack.remove(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        }

        var data = stack.get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (data != null && data.isPlaying() && !data.isPaused()) {
            if (player instanceof ServerPlayer serverPlayer) {
                // The stack object can be replaced by a copy carrying a stale
                // data component (e.g. creative inventory syncs). Verify the
                // stack's session against the one the server tracks and heal it
                // before trusting its timestamps — must run before re-registering
                // so the tracked session is not overwritten by stale data.
                data = com.custommusic.JukeboxManager.sessionForStack(serverPlayer, stack);
                if (data == null) return;
                com.custommusic.JukeboxManager.setActivePortableJukebox(serverPlayer, stack);
            }

            // Only check for song completion on the server side. Both the
            // game-time and the wall-clock playheads must be past the end:
            // game time can run ahead of real time while the server catches up
            // ticks after a lag spike, which would otherwise skip songs early.
            if (!level.isClientSide()) {
                int songLengthTicks = data.songLengthTicks();
                long elapsed = 0L;
                if (data.songStartGameTime() > 0L) {
                    elapsed = level.getGameTime() - data.songStartGameTime();
                }
                long realElapsed = data.songStartRealTime() > 0L
                    ? (System.currentTimeMillis() - data.songStartRealTime()) / 50L
                    : elapsed;

                if (songLengthTicks > 0 && elapsed >= songLengthTicks && realElapsed >= songLengthTicks) {
                    // When this stack's own menu is open, its container tick drives
                    // auto-advance; compare playback sessions rather than object
                    // identity so a replaced stack object is still recognized.
                    boolean menuHandlesIt = player.containerMenu instanceof com.custommusic.menu.PortableJukeboxAlbumMenu menu
                        && menu.getContainer() != null
                        && com.custommusic.JukeboxManager.sameSession(
                            menu.getContainer().getStack().get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get()),
                            data);
                    if (!menuHandlesIt) {
                        // Song finished - advance immediately to the next disc
                        PortableJukeboxContainer container = new PortableJukeboxContainer(stack, level);
                        container.autoNextSong();
                        container.saveToStack(stack);
                    }
                }
            }
        }
    }
    
    private net.minecraft.ChatFormatting getCyclingColor(int ticksSinceStart) {
        // Vanilla jukebox cycles through colors every 10 ticks (0.5 seconds)
        int colorIndex = (ticksSinceStart / 10) % 8; // 8 colors in vanilla cycle
        net.minecraft.ChatFormatting[] colors = {
            net.minecraft.ChatFormatting.WHITE, net.minecraft.ChatFormatting.YELLOW, net.minecraft.ChatFormatting.LIGHT_PURPLE,
            net.minecraft.ChatFormatting.BLUE, net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.RED,
            net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.GRAY
        };
        return colors[colorIndex];
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        
        // Read from custom data component
        var data = stack.get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (data != null) {
            boolean isPlaying = data.isPlaying();
            boolean isPaused = data.isPaused();
            int maxPages = data.maxPages();
            int currentPage = data.currentPage();
            int currentlyPlayingSlot = data.currentlyPlayingSlot();
            
            // Get the items list once and reuse it
            var items = data.getItemsAsList(54 * maxPages);
            
            if (isPlaying && !isPaused && currentlyPlayingSlot >= 0) {
                // Use the items list we already retrieved
                if (currentlyPlayingSlot < items.size()) {
                    ItemStack discStack = items.get(currentlyPlayingSlot);
                    if (!discStack.isEmpty()) {
                        Component songName;
                        
                        // Try to get song name from custom music disc
                        if (discStack.getItem() instanceof com.custommusic.item.CustomMusicDiscItem customDisc) {
                            // Get song description from JukeboxSong registry (same as CustomMusicDiscItem tooltip)
                            var jukeboxSongKey = customDisc.getJukeboxSongKey();
                            Component finalSongName = Component.literal("Unknown");
                            
                            if (jukeboxSongKey != null && context.registries() != null) {
                                var optionalSong = context.registries().lookup(net.minecraft.core.registries.Registries.JUKEBOX_SONG)
                                    .flatMap(registry -> registry.get(jukeboxSongKey));
                                
                                if (optionalSong.isPresent()) {
                                    finalSongName = optionalSong.get().value().description().copy();
                                }
                            }
                            songName = finalSongName;
                        } else {
                            // For vanilla discs, use the item name
                            songName = discStack.getHoverName();
                        }
                        
                        tooltipComponents.accept(Component.literal("Playing: ").withStyle(net.minecraft.ChatFormatting.GREEN)
                            .append(songName.copy().withStyle(net.minecraft.ChatFormatting.GREEN)));
                    } else {
                        tooltipComponents.accept(Component.literal("Playing...").withStyle(net.minecraft.ChatFormatting.GREEN));
                    }
                } else {
                    tooltipComponents.accept(Component.literal("Playing...").withStyle(net.minecraft.ChatFormatting.GREEN));
                }
            } else if (isPaused) {
                tooltipComponents.accept(Component.literal("Paused").withStyle(net.minecraft.ChatFormatting.YELLOW));
            }
            
            tooltipComponents.accept(Component.literal("Page: " + (currentPage + 1) + "/" + maxPages).withStyle(net.minecraft.ChatFormatting.GRAY));
        } else {
            tooltipComponents.accept(Component.literal("Page: 1/1").withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
    
    // Menu provider for the portable jukebox
    private static class PortableJukeboxMenuProvider implements net.minecraft.world.MenuProvider {
        private final ItemStack stack;
        
        public PortableJukeboxMenuProvider(ItemStack stack) {
            this.stack = stack;
        }
        
        @Override
        public Component getDisplayName() {
            return Component.translatable("container.custommusic.portable_jukebox_album");
        }
        
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new PortableJukeboxAlbumMenu(containerId, playerInventory, new PortableJukeboxContainer(stack, player.level()));
        }
    }
}
