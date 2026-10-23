package com.custommusic.menu;

import com.custommusic.blockentity.JukeboxAlbumBlockEntity;
import com.custommusic.item.CustomMusicDiscItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlbumSlot extends Slot {

    private final JukeboxAlbumBlockEntity be;
    private final int localIndex;

    public AlbumSlot(JukeboxAlbumBlockEntity be, int localIndex, int x, int y) {
        super(be, localIndex, x, y);
        this.be = be;
        this.localIndex = localIndex;
    }

    private int getRealIndex() {
        return be.getCurrentPage() * 54 + localIndex;
    }

    @Override
    public ItemStack getItem() {
        return be.getItem(getRealIndex());
    }

    @Override
    public void set(ItemStack stack) {
        be.setItem(getRealIndex(), stack);
    }

    @Override
    public ItemStack remove(int amount) {
        return be.removeItem(getRealIndex(), amount);
    }

    @Override
    public boolean hasItem() {
        return !be.getItem(getRealIndex()).isEmpty();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Only allow music discs to be placed in the jukebox album
        if (stack.isEmpty()) {
            return false;
        }
        
        // Allow custom music discs
        if (stack.getItem() instanceof CustomMusicDiscItem) {
            return true;
        }
        
        // Allow vanilla music discs (items that have jukebox playable component)
        if (stack.has(DataComponents.JUKEBOX_PLAYABLE)) {
            return true;
        }
        
        // Nothing else is allowed
        return false;
    }
}