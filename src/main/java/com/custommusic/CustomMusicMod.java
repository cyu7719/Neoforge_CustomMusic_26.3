package com.custommusic;

import org.slf4j.Logger;

import com.custommusic.init.ModBlockEntities;
import com.custommusic.init.ModBlocks;
import com.custommusic.init.ModCreativeTabs;
import com.custommusic.init.ModDataComponents;
import com.custommusic.init.ModItems;
import com.custommusic.init.ModMenuTypes;
import com.custommusic.init.ModRecipeSerializers;
import com.custommusic.init.ModSounds;
import com.custommusic.loot.ModLootModifiers;
import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.resource.NeoForgeReloadListeners;

@Mod(CustomMusicMod.MODID)
public class CustomMusicMod {
    public static final String MODID = "custommusic";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CustomMusicMod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);

        // Register the deferred registers to the event bus
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(ModCreativeTabs::onCreativeTabContents);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModLootModifiers.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new com.custommusic.event.JukeboxEventHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Custom Music Mod common setup complete");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Custom Music Mod server starting");

        var server = event.getServer();
        var managers = server.getServerResources().managers();
        var lootModifierManager = managers.getListener(net.neoforged.neoforge.resource.NeoForgeReloadListeners.LOOT_MODIFIERS_KEY);
        if (lootModifierManager == null) {
            LOGGER.warn("LootModifierManager not found");
        } else {
            int count = 0;
            for (var modifier : lootModifierManager.getSortedModifiers()) {
                Identifier id = lootModifierManager.getId(modifier);
                LOGGER.info("Loaded global loot modifier: {}", id);
                count++;
            }
            LOGGER.info("Total global loot modifiers loaded: {}", count);
        }

        var glmRegistry = net.neoforged.neoforge.registries.NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS;
        LOGGER.info("Registered global loot modifier serializers: {}", glmRegistry.keySet().size());
        for (Identifier id : glmRegistry.keySet()) {
            LOGGER.info("  GLM serializer: {}", id);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Persistent data cleanup removed due to 26.3 API changes
    }
}
