package com.custommusic.client;

import java.lang.reflect.Field;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public class ContainerScreenHelper {
    private static final Field HOVERED_SLOT_FIELD;

    static {
        try {
            HOVERED_SLOT_FIELD = AbstractContainerScreen.class.getDeclaredField("hoveredSlot");
            HOVERED_SLOT_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Slot getHoveredSlot(AbstractContainerScreen<?> screen) {
        try {
            return (Slot) HOVERED_SLOT_FIELD.get(screen);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
