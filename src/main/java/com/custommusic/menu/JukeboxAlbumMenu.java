package com.custommusic.menu;

import com.custommusic.blockentity.JukeboxAlbumBlockEntity;
import com.custommusic.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class JukeboxAlbumMenu extends AbstractContainerMenu {
    private final JukeboxAlbumBlockEntity blockEntity;
    private static final int SLOTS_PER_PAGE = 54;

    public JukeboxAlbumMenu(int containerId, Inventory playerInventory, JukeboxAlbumBlockEntity blockEntity) {
        super(ModMenuTypes.JUKEBOX_ALBUM.get(), containerId);
        this.blockEntity = blockEntity;
        
        setupJukeboxSlots();
        setupPlayerSlots(playerInventory);
    }

    private void setupJukeboxSlots() {

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {

                int index = row * 9 + col;

                this.addSlot(new AlbumSlot(
                        blockEntity,
                        index,
                        8 + col * 18,
                        18 + row * 18
                ));
            }
        }
    }
    
    private void setupPlayerSlots(Inventory playerInventory) {
        // Clear existing player slots (if any)
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

    public JukeboxAlbumMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    private static JukeboxAlbumBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        var be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof JukeboxAlbumBlockEntity jukebox) {
            return jukebox;
        }
        throw new IllegalStateException("Block entity is not a JukeboxAlbumBlockEntity");
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
        return this.blockEntity.stillValid(player);
    }

    public JukeboxAlbumBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
