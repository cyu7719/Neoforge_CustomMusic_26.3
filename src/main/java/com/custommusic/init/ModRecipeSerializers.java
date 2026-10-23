package com.custommusic.init;

import com.custommusic.CustomMusicMod;
import com.custommusic.recipe.JukeboxAlbumUpgradeRecipe;
import com.custommusic.recipe.PortableJukeboxAlbumUpgradeRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CustomMusicMod.MODID);

    // Only upgrade recipes use custom serializers (they have special logic for adding pages)
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<JukeboxAlbumUpgradeRecipe>> JUKEBOX_ALBUM_UPGRADE =
            RECIPE_SERIALIZERS.register("jukebox_album_upgrade",
                    () -> new RecipeSerializer<>(JukeboxAlbumUpgradeRecipe.CODEC, JukeboxAlbumUpgradeRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PortableJukeboxAlbumUpgradeRecipe>> PORTABLE_JUKEBOX_ALBUM_UPGRADE =
            RECIPE_SERIALIZERS.register("portable_jukebox_album_upgrade",
                    () -> new RecipeSerializer<>(PortableJukeboxAlbumUpgradeRecipe.CODEC, PortableJukeboxAlbumUpgradeRecipe.STREAM_CODEC));
}
