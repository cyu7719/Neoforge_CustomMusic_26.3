package com.custommusic.init;

import java.util.Set;

import com.custommusic.CustomMusicMod;
import com.custommusic.blockentity.JukeboxAlbumBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CustomMusicMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JukeboxAlbumBlockEntity>> JUKEBOX_ALBUM =
            BLOCK_ENTITIES.register("jukebox_album", () ->
                    new BlockEntityType<>(JukeboxAlbumBlockEntity::new, Set.of(ModBlocks.JUKEBOX_ALBUM.get())));
}
