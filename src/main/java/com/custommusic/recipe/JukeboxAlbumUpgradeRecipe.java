package com.custommusic.recipe;

import com.custommusic.init.ModBlockEntities;
import com.custommusic.init.ModBlocks;
import com.custommusic.init.ModRecipeSerializers;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public class JukeboxAlbumUpgradeRecipe extends CustomRecipe {
    public static final JukeboxAlbumUpgradeRecipe INSTANCE = new JukeboxAlbumUpgradeRecipe();
    public static final MapCodec<JukeboxAlbumUpgradeRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, JukeboxAlbumUpgradeRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public JukeboxAlbumUpgradeRecipe() {
        super();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }

        ItemStack center = input.getItem(1, 1);
        if (!center.is(ModBlocks.JUKEBOX_ALBUM.get().asItem())) {
            return false;
        }

        int totalDiamonds = 0;
        for (int i = 0; i < input.size(); i++) {
            if (i == 4) continue; // Skip center
            ItemStack stack = input.getItem(i);
            if (stack.is(net.minecraft.world.item.Items.DIAMOND)) {
                totalDiamonds += stack.getCount();
            } else if (!stack.isEmpty()) {
                return false; // Non-diamond item in pattern
            }
        }

        // Must be at least 8 diamonds and a multiple of 8
        return totalDiamonds >= 8 && totalDiamonds % 8 == 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack center = input.getItem(1, 1);
        ItemStack result = center.copy();

        // Calculate total diamonds
        int totalDiamonds = 0;
        for (int i = 0; i < input.size(); i++) {
            if (i == 4) continue; // Skip center
            ItemStack stack = input.getItem(i);
            if (stack.is(net.minecraft.world.item.Items.DIAMOND)) {
                totalDiamonds += stack.getCount();
            }
        }
        int pagesAdded = totalDiamonds / 8;

        var beData = result.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        net.minecraft.nbt.CompoundTag tag;

        if (beData != null) {
            tag = beData.copyTagWithoutId();
            int currentPages = tag.getIntOr("MaxPages", 0);
            if (currentPages == 0) currentPages = 1;
            tag.putInt("MaxPages", currentPages + pagesAdded);
        } else {
            tag = new net.minecraft.nbt.CompoundTag();
            tag.putInt("MaxPages", 1 + pagesAdded);
        }

        result.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
            net.minecraft.world.item.component.TypedEntityData.of(com.custommusic.init.ModBlockEntities.JUKEBOX_ALBUM.get(), tag));
        return result;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipeSerializers.JUKEBOX_ALBUM_UPGRADE.get();
    }

    @Override
    public boolean isSpecial() {
        return true; // Don't show in vanilla recipe book
    }

    @Override
    public List<RecipeDisplay> display() {
        // Return RecipeDisplay for JEI to show the upgrade recipe
        // Pattern: DDD / DAD / DDD where D=diamond, A=album
        return List.of(
            new ShapedCraftingRecipeDisplay(
                3, 3,
                List.of(
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(ModBlocks.JUKEBOX_ALBUM.get().asItem())),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND))
                ),
                new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(ModBlocks.JUKEBOX_ALBUM.get().asItem())),
                new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.CRAFTING_TABLE))
            )
        );
    }
}
