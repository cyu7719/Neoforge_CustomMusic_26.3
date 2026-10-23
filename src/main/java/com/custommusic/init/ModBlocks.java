package com.custommusic.init;

import com.custommusic.CustomMusicMod;
import com.custommusic.block.JukeboxAlbumBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CustomMusicMod.MODID);

    public static final DeferredBlock<JukeboxAlbumBlock> JUKEBOX_ALBUM = BLOCKS.registerBlock("jukebox_album",
            JukeboxAlbumBlock::new,
            props -> props.strength(2.0f, 6.0f).sound(SoundType.WOOD).noOcclusion());
}
