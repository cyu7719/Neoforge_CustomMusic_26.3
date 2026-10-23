package com.custommusic.init;

import com.custommusic.CustomMusicMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CustomMusicMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CUSTOM_MUSIC =
            CREATIVE_MODE_TABS.register("custommusic", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.custommusic.custom_music_tab"))
                    .icon(() -> new ItemStack(ModItems.PORTABLE_JUKEBOX_ALBUM.get()))
                    .build());

    public static void onCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CUSTOM_MUSIC.key())) {
            ModItems.ITEMS.getEntries().forEach(item ->
                    event.accept(new ItemStack(item.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
        }
    }
}
