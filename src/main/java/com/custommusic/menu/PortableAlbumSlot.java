package com.custommusic.menu;

import com.custommusic.item.CustomMusicDiscItem;
import com.custommusic.item.PortableJukeboxContainer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PortableAlbumSlot extends Slot {

    private final PortableJukeboxContainer container;
    private final int localIndex;

    public PortableAlbumSlot(PortableJukeboxContainer container, int localIndex, int x, int y) {
        super(container, localIndex, x, y);
        this.container = container;
        this.localIndex = localIndex;
    }

    private int getRealIndex() {
        return container.getCurrentPage() * 54 + localIndex;
    }

    @Override
    public ItemStack getItem() {
        return container.getItem(getRealIndex());
    }

    @Override
    public void set(ItemStack stack) {
        container.setItem(getRealIndex(), stack);
    }

    @Override
    public ItemStack remove(int amount) {
        return container.removeItem(getRealIndex(), amount);
    }

    @Override
    public boolean hasItem() {
        return !container.getItem(getRealIndex()).isEmpty();
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
