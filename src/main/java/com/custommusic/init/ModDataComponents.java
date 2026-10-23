package com.custommusic.init;

import com.custommusic.CustomMusicMod;
import com.custommusic.component.PortableJukeboxData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = 
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CustomMusicMod.MODID);
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PortableJukeboxData>> PORTABLE_JUKEBOX_DATA =
        DATA_COMPONENTS.register("portable_jukebox_data",
            () -> DataComponentType.<PortableJukeboxData>builder()
                .persistent(PortableJukeboxData.CODEC)
                .build()
        );
}
