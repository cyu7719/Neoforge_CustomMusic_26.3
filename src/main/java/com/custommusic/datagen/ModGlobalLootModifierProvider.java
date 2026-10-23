package com.custommusic.datagen;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.custommusic.CustomMusicMod;
import com.custommusic.init.ModItems;
import com.custommusic.loot.AddRandomItemModifier;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CustomMusicMod.MODID);
    }

    @Override
    protected void start() {
        // Loot modifier datagen disabled for 26.3
    }
}
