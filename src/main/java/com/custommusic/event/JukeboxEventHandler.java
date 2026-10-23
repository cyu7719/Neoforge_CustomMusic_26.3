package com.custommusic.event;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;

import com.custommusic.init.ModBlocks;
import com.custommusic.init.ModItems;
import com.mojang.logging.LogUtils;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

public class JukeboxEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();

    // Deferred jukebox-album sync tasks: each entry fires after N server ticks.
    private static final List<DeferredSync> deferredSyncs = new java.util.concurrent.CopyOnWriteArrayList<>();
    
    // List of all custom music discs that can drop
    private static final List<DeferredItem<? extends Item>> CUSTOM_DISCS = List.of(
        ModItems.MUSIC_DISC_BLAZING_HEARTS,
        ModItems.MUSIC_DISC_BREATHING_UNDERWATER,
        ModItems.MUSIC_DISC_ECHOES,
        ModItems.MUSIC_DISC_FRACTURES,
        ModItems.MUSIC_DISC_PARADISE,
        ModItems.MUSIC_DISC_UP,
        ModItems.MUSIC_DISC_WE_PRAY,
        ModItems.MUSIC_DISC_TWENTY,
        ModItems.MUSIC_DISC_404_NEW_ERA,
        ModItems.MUSIC_DISC_ARMAGEDDON,
        ModItems.MUSIC_DISC_BANG_BANG,
        ModItems.MUSIC_DISC_BLUE_VALENTINE,
        ModItems.MUSIC_DISC_CRAZY,
        ModItems.MUSIC_DISC_CRAZY_STUPID_LOVE,
        ModItems.MUSIC_DISC_DUN_DUN,
        ModItems.MUSIC_DISC_EARTHQUAKE,
        ModItems.MUSIC_DISC_FEARLESS,
        ModItems.MUSIC_DISC_FLOWER,
        ModItems.MUSIC_DISC_GODS,
        ModItems.MUSIC_DISC_GODS_MENU,
        ModItems.MUSIC_DISC_I_AM,
        ModItems.MUSIC_DISC_I_DO_ME,
        ModItems.MUSIC_DISC_JUMP,
        ModItems.MUSIC_DISC_LIKE_JENNIE,
        ModItems.MUSIC_DISC_MAGNETIC,
        ModItems.MUSIC_DISC_MANIAC,
        ModItems.MUSIC_DISC_MANTRA,
        ModItems.MUSIC_DISC_MONEY,
        ModItems.MUSIC_DISC_NOT_CUTE_ANYMORE,
        ModItems.MUSIC_DISC_OMG,
        ModItems.MUSIC_DISC_PERFECT_NIGHT,
        ModItems.MUSIC_DISC_PSYCHO,
        ModItems.MUSIC_DISC_REBEL_HEART,
        ModItems.MUSIC_DISC_ROCKSTAR,
        ModItems.MUSIC_DISC_SEE_THAT,
        ModItems.MUSIC_DISC_SHEESH,
        ModItems.MUSIC_DISC_SOLO,
        ModItems.MUSIC_DISC_SPAGHETTI,
        ModItems.MUSIC_DISC_SUPER_SHY,
        ModItems.MUSIC_DISC_SUPERNOVA,
        ModItems.MUSIC_DISC_WANNABE,
        ModItems.MUSIC_DISC_WHAT_IS_LOVE,
        ModItems.MUSIC_DISC_WHIPLASH,
        ModItems.MUSIC_DISC_GIRLS_WILL_BE_GIRLS,
        ModItems.MUSIC_DISC_NINE,
        ModItems.MUSIC_DISC_BABY_BLUE,
        ModItems.MUSIC_DISC_DRIP_TOO_HARD,
        ModItems.MUSIC_DISC_EL_DORADO,
        ModItems.MUSIC_DISC_GREECE,
        ModItems.MUSIC_DISC_GRENADE,
        ModItems.MUSIC_DISC_HOLD_ON_WERE_GOING_HOME,
        ModItems.MUSIC_DISC_HOT,
        ModItems.MUSIC_DISC_LEAVE_THE_DOOR_OPEN,
        ModItems.MUSIC_DISC_MEET_ME_HALFWAY,
        ModItems.MUSIC_DISC_NEVER_CHANGE,
        ModItems.MUSIC_DISC_ON_YOUR_OWN,
        ModItems.MUSIC_DISC_OWA_OWA,
        ModItems.MUSIC_DISC_RATHER_BE,
        ModItems.MUSIC_DISC_SOMEONE_LIKE_YOU,
        ModItems.MUSIC_DISC_THE_BEACH,
        ModItems.MUSIC_DISC_TALKING_TO_THE_MOON,
        ModItems.MUSIC_DISC_TENNESSEE_WHISKEY,
        ModItems.MUSIC_DISC_TIME_FLIES,
        ModItems.MUSIC_DISC_WAIT_FOR_U
    );
    
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LOGGER.info("LivingDeathEvent fired! Victim: {}, Damage Source: {}, Killer: {}", 
            event.getEntity().getType().toString(),
            event.getSource().toString(),
            event.getSource().getEntity() != null ? event.getSource().getEntity().getType().toString() : "null");
        
        // Check if the victim is a creeper
        if (event.getEntity() instanceof Creeper) {
            boolean skeletonKill = false;
            
            // Direct skeleton kill
            if (event.getSource().getEntity() instanceof Skeleton) {
                skeletonKill = true;
                LOGGER.info("Direct skeleton kill detected!");
            }
            // Explosion damage - check if the explosion source was from skeleton
            else if (event.getSource().getDirectEntity() != null && 
                     event.getSource().getEntity() instanceof Skeleton) {
                skeletonKill = true;
                LOGGER.info("Skeleton explosion kill detected!");
            }
            // Check for explosion damage where skeleton is the source
            else if (event.getSource().getEntity() instanceof Skeleton) {
                skeletonKill = true;
                LOGGER.info("Skeleton as damage source detected!");
            }
            
            if (skeletonKill) {
                LOGGER.info("Skeleton killed creeper detected! Rolling for disc drop...");
                
                // Same drop rate as vanilla music discs (1 in 100 chance)
                // TEMPORARILY increased to 1 in 5 for testing
                int roll = RANDOM.nextInt(5);
                LOGGER.info("Roll: {} (need 0 for drop)", roll);
                
                if (roll == 0) {
                    LOGGER.info("Disc drop successful! Selecting random disc...");
                    // Select a random custom disc
                    DeferredItem<? extends Item> selectedDisc = CUSTOM_DISCS.get(RANDOM.nextInt(CUSTOM_DISCS.size()));
                    
                    // Create the item stack and drop it
                    ItemStack discStack = new ItemStack(selectedDisc.get());
                    if (event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        event.getEntity().spawnAtLocation(serverLevel, discStack);
                    }
                    LOGGER.info("Dropped disc: {}", selectedDisc.get().toString());
                } else {
                    LOGGER.info("No disc drop this time (rolled {})", roll);
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerChangedDimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof com.custommusic.item.PortableJukeboxAlbumItem) {
                var data = stack.get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
                if (data != null && data.isPlaying() && !data.isPaused() && data.currentlyPlayingSlot() >= 0) {
                    com.custommusic.item.PortableJukeboxContainer container = new com.custommusic.item.PortableJukeboxContainer(stack, player.level());
                    container.resyncSoundForPlayer(player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;
        if (!(event.getTarget() instanceof ItemFrame itemFrame)) return;

        // If the item frame is empty and the player is about to place a playing portable jukebox in it,
        // pause the music first. Once the item is in the frame its inventoryTick no longer runs.
        if (itemFrame.getItem().isEmpty() && event.getHand() != null) {
            ItemStack stack = serverPlayer.getItemInHand(event.getHand());
            if (stack.getItem() instanceof com.custommusic.item.PortableJukeboxAlbumItem) {
                var data = stack.get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
                if (data != null && data.isPlaying() && !data.isPaused()) {
                    com.custommusic.item.PortableJukeboxContainer container = new com.custommusic.item.PortableJukeboxContainer(stack, serverPlayer.level());
                    container.pauseMusic();
                    container.saveToStack(stack);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        // Check player inventory for Jukebox Album items and ensure BLOCK_ENTITY_DATA is preserved
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModBlocks.JUKEBOX_ALBUM.get().asItem())) {
                var beData = stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
                if (beData == null) {
                    System.out.println("[PlayerLoggedIn] Jukebox Album missing BLOCK_ENTITY_DATA, restoring default");
                    // Add default data if missing
                    net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                    tag.putInt("MaxPages", 1);
                    tag.putInt("CurrentPage", 0);
                    stack.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
                        net.minecraft.world.item.component.TypedEntityData.of(
                            com.custommusic.init.ModBlockEntities.JUKEBOX_ALBUM.get(), tag));
                }
            }
        }

        // Sync any nearby playing jukebox album blocks so the new player hears it at the same position.
        // Defer by 40 ticks (2s) so the client has finished loading the world and its game time
        // is synced with the server — otherwise the transit-tick calculation in MusicManager
        // would be wrong and the joiner would start at a stale song position.
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Defer by 40 ticks (2s) so the client has finished loading the world and its
            // game time is synced with the server before the sync packet arrives.
            deferredSyncs.add(new DeferredSync(player, serverLevel, 40));
        }

        // Crash recovery: any portable jukebox that was playing when the world crashed should be paused.
        if (com.custommusic.JukeboxManager.isStartupPause()) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.getItem() instanceof com.custommusic.item.PortableJukeboxAlbumItem) {
                    var data = stack.get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
                    if (data != null && data.isPlaying() && !data.isPaused()) {
                        com.custommusic.item.PortableJukeboxContainer container = new com.custommusic.item.PortableJukeboxContainer(stack, player.level());
                        container.pauseMusic();
                        container.saveToStack(stack);
                        com.custommusic.JukeboxManager.clearActivePortableJukebox(player);
                    }
                }
            }
        }

        // Crash recovery only applies to the very first login after the server starts.
        // Later joiners must not pause music that the host deliberately started.
        com.custommusic.JukeboxManager.setStartupPause(false);
    }

    @SubscribeEvent
    public void onCrafting(net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
        // Consume additional diamonds after vanilla's -1 removal
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack crafted = event.getCrafting();
        // Check if this is a Jukebox Album or Portable Jukebox Album upgrade
        boolean isJukeboxAlbumUpgrade = crafted.is(ModBlocks.JUKEBOX_ALBUM.get().asItem());
        boolean isPortableUpgrade = crafted.is(ModItems.PORTABLE_JUKEBOX_ALBUM.get());

        if (!isJukeboxAlbumUpgrade && !isPortableUpgrade) return;

        var container = event.getInventory();
        if (container == null) return;

        // Calculate total diamonds in the container
        int totalDiamonds = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(net.minecraft.world.item.Items.DIAMOND)) {
                totalDiamonds += stack.getCount();
            }
        }

        // If there are diamonds remaining (vanilla only removed 1 per slot), remove the rest
        if (totalDiamonds > 0) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.is(net.minecraft.world.item.Items.DIAMOND)) {
                    container.setItem(i, ItemStack.EMPTY);
                }
            }
            System.out.println("[CraftingEvent] Consumed remaining diamonds: " + totalDiamonds + " for " + (isJukeboxAlbumUpgrade ? "Jukebox Album" : "Portable Jukebox Album"));
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        com.custommusic.JukeboxManager.onServerTick(event.getServer());

        // Tick open portable jukebox menus so pending next/previous song changes fire.
        for (var player : event.getServer().getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof com.custommusic.menu.PortableJukeboxAlbumMenu menu) {
                menu.tick();
            }
        }

        // Process deferred jukebox-album syncs.
        if (!deferredSyncs.isEmpty()) {
            for (int i = deferredSyncs.size() - 1; i >= 0; i--) {
                DeferredSync ds = deferredSyncs.get(i);
                ds.ticksRemaining--;
                if (ds.ticksRemaining <= 0) {
                    deferredSyncs.remove(i);
                    if (!ds.player.hasDisconnected()) {
                        net.minecraft.core.BlockPos ppos = ds.player.blockPosition();
                        int chunkRange = 8; // 128 blocks
                        for (int cx = -chunkRange; cx <= chunkRange; cx++) {
                            for (int cz = -chunkRange; cz <= chunkRange; cz++) {
                                net.minecraft.world.level.chunk.LevelChunk chunk = ds.level.getChunkSource().getChunkNow((ppos.getX() >> 4) + cx, (ppos.getZ() >> 4) + cz);
                                if (chunk == null) continue;
                                for (net.minecraft.world.level.block.entity.BlockEntity be : chunk.getBlockEntities().values()) {
                                    if (be instanceof com.custommusic.blockentity.JukeboxAlbumBlockEntity jukebox) {
                                        if (jukebox.isPlaying() && !jukebox.isPaused()) {
                                            jukebox.syncToPlayer(ds.player);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static final class DeferredSync {
        final net.minecraft.server.level.ServerPlayer player;
        final net.minecraft.server.level.ServerLevel level;
        int ticksRemaining;

        DeferredSync(net.minecraft.server.level.ServerPlayer player, net.minecraft.server.level.ServerLevel level, int ticks) {
            this.player = player;
            this.level = level;
            this.ticksRemaining = ticks;
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        com.custommusic.JukeboxManager.setStartupPause(true);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Pause playing music before the world is saved during shutdown.
        com.custommusic.JukeboxManager.pauseAllMusic(event.getServer());
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer sp) {
            com.custommusic.JukeboxManager.onItemTossed(sp, event.getEntity(), event.getEntity().getItem());
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity itemEntity) {
            LOGGER.info("JukeboxEventHandler onEntityJoinLevel ItemEntity item={}", itemEntity.getItem().getItem().getDescriptionId());
            com.custommusic.JukeboxManager.onItemEntitySpawn(itemEntity.level(), itemEntity);
        }
    }

}
