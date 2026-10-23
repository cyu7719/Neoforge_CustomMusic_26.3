package com.custommusic.menu;

import com.custommusic.item.PortableJukeboxContainer;
import com.custommusic.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PortableJukeboxAlbumMenu extends AbstractContainerMenu {
    private final PortableJukeboxContainer container;
    private final Inventory playerInventory;
    private static final int SLOTS_PER_PAGE = 54;

    public PortableJukeboxAlbumMenu(int containerId, Inventory playerInventory, PortableJukeboxContainer container) {
        super(ModMenuTypes.PORTABLE_JUKEBOX_ALBUM.get(), containerId);
        this.container = container;
        this.playerInventory = playerInventory;
        
        setupJukeboxSlots();
        setupPlayerSlots(playerInventory);
    }

    private void setupJukeboxSlots() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                this.addSlot(new PortableAlbumSlot(
                        container,
                        index,
                        8 + col * 18,
                        18 + row * 18
                ));
            }
        }
    }
    
    private void setupPlayerSlots(Inventory playerInventory) {
        int startIndex = SLOTS_PER_PAGE;
        int endIndex = this.slots.size();
        if (endIndex > startIndex) {
            this.slots.subList(startIndex, endIndex).clear();
        }
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    public PortableJukeboxAlbumMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new PortableJukeboxContainer(findPortableJukeboxInInventory(playerInventory), playerInventory.player.level()));
    }
    
    private static ItemStack findPortableJukeboxInInventory(Inventory playerInventory) {
        // Check main hand first
        ItemStack mainHand = playerInventory.player.getMainHandItem();
        if (mainHand.getItem() instanceof com.custommusic.item.PortableJukeboxAlbumItem) {
            return mainHand;
        }
        
        // Check off hand
        ItemStack offHand = playerInventory.player.getOffhandItem();
        if (offHand.getItem() instanceof com.custommusic.item.PortableJukeboxAlbumItem) {
            return offHand;
        }
        
        // Search inventory
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (stack.getItem() instanceof com.custommusic.item.PortableJukeboxAlbumItem) {
                return stack;
            }
        }
        
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (index < SLOTS_PER_PAGE) {
                if (!this.moveItemStackTo(slotStack, SLOTS_PER_PAGE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, SLOTS_PER_PAGE, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // Portable jukebox is always valid
    }
    
    public void tick() {
        // Tick the container to handle auto-next
        container.tick();
    }

    public PortableJukeboxContainer getContainer() {
        return this.container;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Save container data back to the item stack when GUI is closed
        if (!player.level().isClientSide()) {
            // Update the player's inventory with the new item stack data
            // This will save all the disc data to the correct item stack
            updatePlayerInventory(player);
        }
    }
    
    private void updatePlayerInventory(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        
        if (mainHand.getItem() instanceof com.custommusic.item.PortableJukeboxAlbumItem) {
            saveDiscInventoryPreservingPlayback(mainHand);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, mainHand);
            return;
        }
        
        if (offHand.getItem() instanceof com.custommusic.item.PortableJukeboxAlbumItem) {
            saveDiscInventoryPreservingPlayback(offHand);
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, offHand);
            return;
        }
        
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.custommusic.item.PortableJukeboxAlbumItem) {
                saveDiscInventoryPreservingPlayback(stack);
                player.getInventory().setItem(i, stack);
                break;
            }
        }
    }

    /**
     * Saves the disc inventory from the GUI container to the target stack while
     * preserving the current playback state already on the stack. This prevents
     * closing the GUI from overwriting playback state updated by auto-next in
     * inventoryTick while the GUI was open.
     */
    private void saveDiscInventoryPreservingPlayback(ItemStack stack) {
        var currentData = stack.get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (currentData == null) {
            container.saveToStack(stack);
            return;
        }

        net.minecraft.core.NonNullList<ItemStack> items =
            net.minecraft.core.NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < container.getContainerSize(); i++) {
            items.set(i, container.getItem(i));
        }

        // Use disc inventory from the GUI container but preserve all playback state
        // from the actual stack (auto-next may have updated it while GUI was open)
        com.custommusic.component.PortableJukeboxData mergedData =
            new com.custommusic.component.PortableJukeboxData(
                net.minecraft.world.item.component.ItemContainerContents.fromItems(items),
                container.getMaxPages(),
                currentData.currentPage(),
                currentData.isPlaying(),
                currentData.isPaused(),
                currentData.currentlyPlayingSlot(),
                currentData.songStartGameTime(),
                currentData.songLengthTicks(),
                currentData.pausedTicksPosition(),
                currentData.spinningStartTime(),
                currentData.songStartRealTime(),
                currentData.volume()
            );
        stack.set(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get(), mergedData);
    }
}
