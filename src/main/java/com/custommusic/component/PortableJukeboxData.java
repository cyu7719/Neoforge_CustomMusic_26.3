package com.custommusic.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public record PortableJukeboxData(
    ItemContainerContents items,
    int maxPages,
    int currentPage,
    boolean isPlaying,
    boolean isPaused,
    int currentlyPlayingSlot,
    long songStartGameTime,
    int songLengthTicks,
    int pausedTicksPosition,
    long spinningStartTime,
    long songStartRealTime,
    float volume
) {
    public static final Codec<PortableJukeboxData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ItemContainerContents.CODEC.fieldOf("items").forGetter(PortableJukeboxData::items),
            Codec.INT.fieldOf("maxPages").forGetter(PortableJukeboxData::maxPages),
            Codec.INT.fieldOf("currentPage").forGetter(PortableJukeboxData::currentPage),
            Codec.BOOL.fieldOf("isPlaying").forGetter(PortableJukeboxData::isPlaying),
            Codec.BOOL.fieldOf("isPaused").forGetter(PortableJukeboxData::isPaused),
            Codec.INT.fieldOf("currentlyPlayingSlot").forGetter(PortableJukeboxData::currentlyPlayingSlot),
            Codec.LONG.fieldOf("songStartGameTime").forGetter(PortableJukeboxData::songStartGameTime),
            Codec.INT.fieldOf("songLengthTicks").forGetter(PortableJukeboxData::songLengthTicks),
            Codec.INT.fieldOf("pausedTicksPosition").forGetter(PortableJukeboxData::pausedTicksPosition),
            Codec.LONG.optionalFieldOf("spinningStartTime", 0L).forGetter(PortableJukeboxData::spinningStartTime),
            Codec.LONG.optionalFieldOf("songStartRealTime", 0L).forGetter(PortableJukeboxData::songStartRealTime),
            Codec.FLOAT.optionalFieldOf("volume", 0.5F).forGetter(PortableJukeboxData::volume)
        ).apply(instance, PortableJukeboxData::new)
    );

    public static PortableJukeboxData createDefault(int maxPages) {
        int totalSlots = 54 * maxPages;
        NonNullList<ItemStack> items = NonNullList.withSize(totalSlots, ItemStack.EMPTY);
        return new PortableJukeboxData(
            ItemContainerContents.fromItems(items),
            maxPages,
            0,
            false,
            false,
            -1,
            0L,
            0,
            0,
            0L,
            0L,
            0.5F
        );
    }
    
    public NonNullList<ItemStack> getItemsAsList(int size) {
        NonNullList<ItemStack> list = NonNullList.withSize(size, ItemStack.EMPTY);
        items.copyInto(list);
        return list;
    }
}
