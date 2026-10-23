package com.custommusic.client;

import com.custommusic.init.ModItems;
import com.custommusic.item.JukeboxAlbumBlockItem;
import com.custommusic.item.PortableJukeboxAlbumItem;
import com.custommusic.network.JukeboxPickPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public class JukeboxPickHandler {

    @SubscribeEvent
    public static void onMouseButtonPre(ScreenEvent.MouseButtonPressed.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasInfiniteMaterials()) return;

        Screen s = event.getScreen();
        if (!(s instanceof AbstractContainerScreen<?> screen)) return;
        if (!screen.getMenu().getCarried().isEmpty()) return;

        var key = InputConstants.Type.MOUSE.getOrCreate(event.getButton());
        if (!mc.options.keyPickItem.matches(key)) return;

        Slot slot = ContainerScreenHelper.getHoveredSlot(screen);
        if (slot == null || !slot.hasItem()) return;

        ItemStack stack = slot.getItem();
        if (!isJukeboxLike(stack)) return;

        ItemStack clean = stack.getItem().getDefaultInstance().copyWithCount(stack.getMaxStackSize());
        event.setCanceled(true);
        screen.getMenu().setCarried(clean);
        ClientPacketDistributor.sendToServer(new JukeboxPickPayload(slot.index));
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasInfiniteMaterials()) return;

        Screen s = event.getScreen();
        if (!(s instanceof AbstractContainerScreen<?> screen)) return;
        if (!screen.getMenu().getCarried().isEmpty()) return;

        var key = InputConstants.getKey(event.getKeyEvent());
        if (!mc.options.keyPickItem.matches(key)) return;

        Slot slot = ContainerScreenHelper.getHoveredSlot(screen);
        if (slot == null || !slot.hasItem()) return;

        ItemStack stack = slot.getItem();
        if (!isJukeboxLike(stack)) return;

        ItemStack clean = stack.getItem().getDefaultInstance().copyWithCount(stack.getMaxStackSize());
        event.setCanceled(true);
        screen.getMenu().setCarried(clean);
        ClientPacketDistributor.sendToServer(new JukeboxPickPayload(slot.index));
    }

    private static boolean isJukeboxLike(ItemStack stack) {
        if (stack.getItem() instanceof JukeboxAlbumBlockItem
                || stack.getItem() instanceof PortableJukeboxAlbumItem) {
            return true;
        }
        return stack.getItem() == ModItems.JUKEBOX_ALBUM.get()
                || stack.getItem() == ModItems.PORTABLE_JUKEBOX_ALBUM.get();
    }
}
