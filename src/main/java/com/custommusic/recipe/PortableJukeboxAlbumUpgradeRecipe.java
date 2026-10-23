package com.custommusic.recipe;

import com.custommusic.component.PortableJukeboxData;
import com.custommusic.init.ModDataComponents;
import com.custommusic.init.ModItems;
import com.custommusic.init.ModRecipeSerializers;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public class PortableJukeboxAlbumUpgradeRecipe extends CustomRecipe {
    public static final PortableJukeboxAlbumUpgradeRecipe INSTANCE = new PortableJukeboxAlbumUpgradeRecipe();
    public static final MapCodec<PortableJukeboxAlbumUpgradeRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, PortableJukeboxAlbumUpgradeRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public PortableJukeboxAlbumUpgradeRecipe() {
        super();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        // Accept 3x3 or trimmed 3x2/3x1 layouts
        if (input.width() != 3) {
            return false;
        }

        // Find the portable jukebox album — it must be present exactly once
        int albumSlot = -1;
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).is(ModItems.PORTABLE_JUKEBOX_ALBUM.get())) {
                if (albumSlot != -1) return false; // more than one album
                albumSlot = i;
            }
        }
        if (albumSlot == -1) return false;

        int totalDiamonds = 0;
        for (int i = 0; i < input.size(); i++) {
            if (i == albumSlot) continue;
            ItemStack stack = input.getItem(i);
            if (stack.is(net.minecraft.world.item.Items.DIAMOND)) {
                totalDiamonds += stack.getCount();
            } else if (!stack.isEmpty()) {
                return false; // non-diamond, non-album item
            }
        }

        // Must be at least 5 diamonds and a multiple of 5
        return totalDiamonds >= 5 && totalDiamonds % 5 == 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        // Locate the album and count total diamonds across all stacks
        int albumSlot = -1;
        int totalDiamonds = 0;
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).is(ModItems.PORTABLE_JUKEBOX_ALBUM.get())) {
                albumSlot = i;
            } else if (input.getItem(i).is(net.minecraft.world.item.Items.DIAMOND)) {
                totalDiamonds += input.getItem(i).getCount();
            }
        }
        if (albumSlot == -1) return ItemStack.EMPTY;

        ItemStack result = input.getItem(albumSlot).copy();
        final int pagesAdded = Math.max(1, totalDiamonds / 5);

        // Get existing custom data component
        var existingData = result.get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get());

        if (existingData != null) {
            // Increment maxPages while preserving all other data
            int newMaxPages = existingData.maxPages() + pagesAdded;
            int totalSlots = 54 * newMaxPages;

            // Get existing items and expand the list
            net.minecraft.core.NonNullList<ItemStack> expandedItems =
                net.minecraft.core.NonNullList.withSize(totalSlots, ItemStack.EMPTY);

            // Copy existing items to expanded list
            var oldItems = existingData.getItemsAsList(54 * existingData.maxPages());
            for (int i = 0; i < oldItems.size(); i++) {
                expandedItems.set(i, oldItems.get(i));
            }

            // Create new data with incremented pages
            com.custommusic.component.PortableJukeboxData newData =
                new com.custommusic.component.PortableJukeboxData(
                    net.minecraft.world.item.component.ItemContainerContents.fromItems(expandedItems),
                    newMaxPages,
                    existingData.currentPage(),
                    existingData.isPlaying(),
                    existingData.isPaused(),
                    existingData.currentlyPlayingSlot(),
                    existingData.songStartGameTime(),
                    existingData.songLengthTicks(),
                    existingData.pausedTicksPosition(),
                    existingData.spinningStartTime(),
                    existingData.songStartRealTime(),
                    existingData.volume()
                );

            result.set(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get(), newData);
        } else {
            // Create default data with 1 + pagesAdded pages
            com.custommusic.component.PortableJukeboxData newData =
                com.custommusic.component.PortableJukeboxData.createDefault(1 + pagesAdded);
            result.set(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get(), newData);
        }

        return result;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipeSerializers.PORTABLE_JUKEBOX_ALBUM_UPGRADE.get();
    }

    @Override
    public boolean isSpecial() {
        return true; // Don't show in recipe book
    }

    @Override
    public List<RecipeDisplay> display() {
        // Helmet pattern: "DDD" / "DAD" / "   " where D=diamond, A=album (5 diamonds = +1 page)
        return List.of(
            new ShapedCraftingRecipeDisplay(
                3, 3,
                List.of(
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(ModItems.PORTABLE_JUKEBOX_ALBUM.get())),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    SlotDisplay.Empty.INSTANCE,
                    SlotDisplay.Empty.INSTANCE,
                    SlotDisplay.Empty.INSTANCE
                ),
                new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(ModItems.PORTABLE_JUKEBOX_ALBUM.get())),
                new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.CRAFTING_TABLE))
            )
        );
    }
}
