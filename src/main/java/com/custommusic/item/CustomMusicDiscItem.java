package com.custommusic.item;

import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerLevel;

public class CustomMusicDiscItem extends Item {
    private final int comparatorValue;
    private final Holder<SoundEvent> sound;
    private final int lengthInTicks;
    private static long lastHoverTime = 0;

    public CustomMusicDiscItem(int comparatorValue, Holder<SoundEvent> sound, Item.Properties properties, int lengthInTicks, ResourceKey<JukeboxSong> jukeboxSongKey) {
        super(properties);
        this.comparatorValue = comparatorValue;
        this.sound = sound;
        this.lengthInTicks = lengthInTicks;
        this.jukeboxSongKey = jukeboxSongKey;
    }
    
    private final ResourceKey<JukeboxSong> jukeboxSongKey;

    public int getComparatorValue() {
        return comparatorValue;
    }

    public Holder<SoundEvent> getSound() {
        return sound;
    }

    public int getLengthInTicks() {
        return lengthInTicks;
    }
    
    public ResourceKey<JukeboxSong> getJukeboxSongKey() {
        return jukeboxSongKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        // Vanilla JUKEBOX_PLAYABLE tooltip is suppressed via TOOLTIP_DISPLAY in ModItems,
        // so we add the song description ourselves (gray) followed by the credit (white).
        context.registries().lookup(Registries.JUKEBOX_SONG)
            .flatMap(registry -> registry.get(this.jukeboxSongKey))
            .ifPresent(song -> tooltip.accept(
                song.value().description().copy().withStyle(net.minecraft.ChatFormatting.GRAY)));

        String creditKey = this.getDescriptionId() + ".credit";
        tooltip.accept(Component.translatable(creditKey).withStyle(net.minecraft.ChatFormatting.WHITE));
    }
    
    public JukeboxPlayable getJukeboxPlayable(ItemStack stack) {
        return stack.get(DataComponents.JUKEBOX_PLAYABLE);
    }
    
    // Ensure the jukebox component is available when the item is in inventory
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, net.minecraft.world.entity.Entity entity, EquipmentSlot slot) {
        // Let vanilla handle the JukeboxPlayable component naturally
    }
    
    @Override
    public Component getName(ItemStack stack) {
        // Return the item name for creative menu hover
        return super.getName(stack);
    }
}
