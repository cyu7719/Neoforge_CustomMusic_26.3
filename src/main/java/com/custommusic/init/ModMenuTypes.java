package com.custommusic.init;

import com.custommusic.CustomMusicMod;
import com.custommusic.menu.JukeboxAlbumMenu;
import com.custommusic.menu.PortableJukeboxAlbumMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
            DeferredRegister.create(Registries.MENU, CustomMusicMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<JukeboxAlbumMenu>> JUKEBOX_ALBUM =
            MENU_TYPES.register("jukebox_album", () ->
                    IMenuTypeExtension.create(JukeboxAlbumMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<PortableJukeboxAlbumMenu>> PORTABLE_JUKEBOX_ALBUM =
            MENU_TYPES.register("portable_jukebox_album", () ->
                    IMenuTypeExtension.create(PortableJukeboxAlbumMenu::new));
}
