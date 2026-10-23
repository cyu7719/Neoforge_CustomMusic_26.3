package com.custommusic.loot;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

public class AddRandomItemModifier implements IGlobalLootModifier {
    public static final MapCodec<AddRandomItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            LootItemCondition.DIRECT_CODEC.listOf().fieldOf("conditions").forGetter(m -> m.conditions),
            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("items").forGetter(m -> m.items),
            Codec.INT.optionalFieldOf("priority", IGlobalLootModifier.DEFAULT_PRIORITY).forGetter(m -> m.priority)
    ).apply(inst, AddRandomItemModifier::new));

    private final List<LootItemCondition> conditions;
    private final List<Item> items;
    private final int priority;

    public AddRandomItemModifier(List<LootItemCondition> conditions, List<Item> items, int priority) {
        this.conditions = conditions;
        this.items = items;
        this.priority = priority;
    }

    @Override
    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (LootItemCondition condition : conditions) {
            if (!condition.test(context)) {
                return generatedLoot;
            }
        }

        if (items.isEmpty()) {
            return generatedLoot;
        }

        Item item = items.get(context.getRandom().nextInt(items.size()));
        generatedLoot.add(new ItemStack(item));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    public int priority() {
        return priority;
    }
}
