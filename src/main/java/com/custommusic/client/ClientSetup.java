package com.custommusic.client;

import com.custommusic.client.renderer.JukeboxAlbumBlockEntityRenderer;
import com.custommusic.client.renderer.JukeboxAlbumItemModel;
import com.custommusic.client.renderer.PortableJukeboxItemModel;
import com.custommusic.client.screen.JukeboxAlbumScreen;
import com.custommusic.client.screen.PortableJukeboxAlbumScreen;
import com.custommusic.init.ModBlockEntities;
import com.custommusic.init.ModMenuTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        try {
            registerScreen(event, ModMenuTypes.JUKEBOX_ALBUM.get(), JukeboxAlbumScreen.class, com.custommusic.menu.JukeboxAlbumMenu.class);
            registerScreen(event, ModMenuTypes.PORTABLE_JUKEBOX_ALBUM.get(), PortableJukeboxAlbumScreen.class, com.custommusic.menu.PortableJukeboxAlbumMenu.class);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static void registerScreen(RegisterMenuScreensEvent event, MenuType<?> menuType, Class<?> screenClass, Class<?> menuClass) throws Throwable {
        Class<?> screenConstructor = Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor");
        MethodHandle constructor = MethodHandles.lookup()
                .findConstructor(screenClass, MethodType.methodType(void.class, menuClass, Inventory.class, Component.class));
        Object factory = Proxy.newProxyInstance(ClientSetup.class.getClassLoader(), new Class<?>[]{screenConstructor},
                (proxy, method, args) -> {
                    if (method.getName().equals("create") && method.getParameterCount() == 3) {
                        return constructor.invoke(args[0], args[1], args[2]);
                    }
                    if (method.isDefault()) {
                        return InvocationHandler.invokeDefault(proxy, method, args);
                    }
                    return null;
                });
        Method m = event.getClass().getMethod("register", MenuType.class, screenConstructor);
        m.invoke(event, menuType, factory);
    }

    @SubscribeEvent
    public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        // Register the block entity renderer
        event.registerBlockEntityRenderer(ModBlockEntities.JUKEBOX_ALBUM.get(), JukeboxAlbumBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(Identifier.parse("custommusic:portable_jukebox"), PortableJukeboxItemModel.Unbaked.MAP_CODEC);
        event.register(Identifier.parse("custommusic:jukebox_album"), JukeboxAlbumItemModel.Unbaked.MAP_CODEC);
    }
}
