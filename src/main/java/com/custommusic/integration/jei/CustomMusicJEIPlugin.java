package com.custommusic.integration.jei;

import com.custommusic.CustomMusicMod;
import com.custommusic.component.PortableJukeboxData;
import com.custommusic.init.ModBlockEntities;
import com.custommusic.init.ModDataComponents;
import com.custommusic.init.ModItems;
import com.custommusic.recipe.JukeboxAlbumUpgradeRecipe;
import com.custommusic.recipe.PortableJukeboxAlbumUpgradeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JeiPlugin
public class CustomMusicJEIPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        // Register extensions for the real upgrade recipes so JEI's crafting
        // category displays them. The output slot is RENDER_ONLY so the album
        // is not indexed as a recipe output — this keeps the upgrade out of
        // the R (recipe) view while U (usage) on the album or diamonds still
        // finds it through the input slots.
        registration.getCraftingCategory().addExtension(
            JukeboxAlbumUpgradeRecipe.class,
            new UpgradeExtension<>(ModItems.JUKEBOX_ALBUM.get(), 8, upgradedJukeboxAlbum()));
        registration.getCraftingCategory().addExtension(
            PortableJukeboxAlbumUpgradeRecipe.class,
            new UpgradeExtension<>(ModItems.PORTABLE_JUKEBOX_ALBUM.get(), 5, upgradedPortableAlbum()));
    }

    private static ItemStack upgradedJukeboxAlbum() {
        ItemStack stack = new ItemStack(ModItems.JUKEBOX_ALBUM.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt("MaxPages", 2);
        stack.set(DataComponents.BLOCK_ENTITY_DATA,
            TypedEntityData.of(ModBlockEntities.JUKEBOX_ALBUM.get(), tag));
        stack.set(DataComponents.CUSTOM_NAME,
            Component.literal("Jukebox Album (Upgraded)"));
        return stack;
    }

    private static ItemStack upgradedPortableAlbum() {
        ItemStack stack = new ItemStack(ModItems.PORTABLE_JUKEBOX_ALBUM.get());
        stack.set(ModDataComponents.PORTABLE_JUKEBOX_DATA.get(),
            PortableJukeboxData.createDefault(2));
        stack.set(DataComponents.CUSTOM_NAME,
            Component.literal("Portable Jukebox Album (Upgraded)"));
        return stack;
    }

    private static class UpgradeExtension<R extends CraftingRecipe> implements ICraftingCategoryExtension<R> {
        private final Item albumItem;
        private final int diamondCount;
        private final ItemStack result;

        UpgradeExtension(Item albumItem, int diamondCount, ItemStack result) {
            this.albumItem = albumItem;
            this.diamondCount = diamondCount;
            this.result = result;
        }

        @Override
        public List<SlotDisplay> getIngredients(RecipeHolder<R> holder) {
            SlotDisplay diamond = new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.DIAMOND));
            SlotDisplay album = new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(albumItem));
            List<SlotDisplay> slots = new ArrayList<>(Collections.nCopies(9, SlotDisplay.Empty.INSTANCE));
            if (diamondCount == 8) {
                // Jukebox album: DDD / DAD / DDD
                for (int i = 0; i < 9; i++) {
                    slots.set(i, i == 4 ? album : diamond);
                }
            } else {
                // Portable album (helmet shape): DDD / DAD / ___
                slots.set(0, diamond);
                slots.set(1, diamond);
                slots.set(2, diamond);
                slots.set(3, diamond);
                slots.set(4, album);
                slots.set(5, diamond);
            }
            return slots;
        }

        @Override
        public int getWidth(RecipeHolder<R> holder) {
            return 3;
        }

        @Override
        public int getHeight(RecipeHolder<R> holder) {
            return 3;
        }

        @Override
        public void setRecipe(RecipeHolder<R> holder, IRecipeLayoutBuilder builder,
                              ICraftingGridHelper gridHelper, IFocusGroup focuses) {
            gridHelper.createAndSetIngredientsFromDisplays(builder, getIngredients(holder), 3, 3);
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 95, 19)
                .setOutputSlotBackground()
                .add(new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(result)));
        }
    }
}
