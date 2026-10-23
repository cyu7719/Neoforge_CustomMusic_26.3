package com.custommusic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.custommusic.blockentity.JukeboxAlbumBlockEntity;
import com.custommusic.component.PortableJukeboxData;
import com.custommusic.item.PortableJukeboxAlbumItem;
import com.custommusic.item.PortableJukeboxContainer;
import com.custommusic.init.ModDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class JukeboxManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    // Tracks the currently playing portable jukebox per player.
    // Stores the original stack reference and an unpaused data snapshot so the item
    // can still be found after the player moves it into a container (when ItemStack copies are made).
    private static final Map<UUID, ActiveEntry> ACTIVE_PORTABLES = new HashMap<>();

    // True during the first minute after the server starts. Any JukeboxAlbum that was
    // left unpaused from a crash will be paused so it can be resumed from where it was.
    private static boolean startupPause = true;
    private static int startupTicks = 0;

    public static boolean isStartupPause() {
        return startupPause;
    }

    public static void setStartupPause(boolean value) {
        startupPause = value;
        startupTicks = 0;
    }

    private static final class ActiveEntry {
        final ItemStack stack;
        final PortableJukeboxData data;
        int missingTicks;

        ActiveEntry(ItemStack stack, PortableJukeboxData data) {
            this.stack = stack;
            this.data = data;
            this.missingTicks = 0;
        }
    }

    public static void setActivePortableJukebox(ServerPlayer player, ItemStack stack) {
        if (player == null || stack == null) return;
        var data = stack.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (data != null && data.isPlaying() && !data.isPaused()) {
            ActiveEntry existing = ACTIVE_PORTABLES.get(player.getUUID());
            if (existing == null || existing.stack != stack) {
                LOGGER.info("JukeboxManager setActive for {}: stack={}@{} count={}", player.getName().getString(), stack.getItem().getDescriptionId(), Integer.toHexString(System.identityHashCode(stack)), stack.getCount());
            }
            ACTIVE_PORTABLES.put(player.getUUID(), new ActiveEntry(stack, data));
        }
    }

    public static void clearActivePortableJukebox(ServerPlayer player) {
        if (player != null) ACTIVE_PORTABLES.remove(player.getUUID());
    }

    /**
     * Returns the playback session for the given stack. When the stack object has
     * been replaced (e.g. creative inventory syncs send client-provided stacks
     * whose data component can be stale), the tracked session's playback fields
     * are written back onto the stack's contents so a stale songStartGameTime
     * cannot trigger an early auto-advance. Returns null if the stack is not a
     * playing portable jukebox.
     */
    public static @org.jspecify.annotations.Nullable PortableJukeboxData sessionForStack(ServerPlayer player, ItemStack stack) {
        PortableJukeboxData data = stack.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (data == null || !data.isPlaying() || data.isPaused()) return null;
        ActiveEntry entry = ACTIVE_PORTABLES.get(player.getUUID());
        if (entry == null || entry.data == null || entry.stack == stack) return data;
        if (sameSession(entry.data, data)) return data;

        // Stale component on a replacement stack: restore the tracked session's
        // playback fields, keeping this stack's own disc contents.
        PortableJukeboxData healed = new PortableJukeboxData(
            data.items(),
            entry.data.maxPages(),
            entry.data.currentPage(),
            entry.data.isPlaying(),
            entry.data.isPaused(),
            entry.data.currentlyPlayingSlot(),
            entry.data.songStartGameTime(),
            entry.data.songLengthTicks(),
            entry.data.pausedTicksPosition(),
            entry.data.spinningStartTime(),
            entry.data.songStartRealTime(),
            entry.data.volume());
        stack.set(ModDataComponents.PORTABLE_JUKEBOX_DATA.get(), healed);
        LOGGER.info("JukeboxManager healed stale playback component for {}: slot={} startGameTime={}",
            player.getName().getString(), healed.currentlyPlayingSlot(), healed.songStartGameTime());
        return healed;
    }

    /** Whether two data snapshots describe the same playing session. */
    public static boolean sameSession(PortableJukeboxData a, PortableJukeboxData b) {
        if (a == null || b == null) return false;
        return a.currentlyPlayingSlot() == b.currentlyPlayingSlot()
            && a.songStartGameTime() == b.songStartGameTime()
            && a.songLengthTicks() == b.songLengthTicks()
            && a.spinningStartTime() == b.spinningStartTime();
    }

    public static void onItemTossed(ServerPlayer player, net.minecraft.world.entity.item.ItemEntity itemEntity, ItemStack stack) {
        if (player == null || itemEntity == null || stack == null) return;
        if (!(stack.getItem() instanceof PortableJukeboxAlbumItem)) return;
        PortableJukeboxData data = stack.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (data == null || !data.isPlaying() || data.isPaused()) return;
        PortableJukeboxContainer container = new PortableJukeboxContainer(stack, player.level());
        container.pauseMusic();
        itemEntity.setItem(stack);
        stopPortableSoundForPlayer(player);
        clearActivePortableJukebox(player);
    }

    public static void onItemEntitySpawn(Level level, net.minecraft.world.entity.item.ItemEntity itemEntity) {
        if (level.isClientSide()) return;
        ItemStack stack = itemEntity.getItem();
        if (!(stack.getItem() instanceof PortableJukeboxAlbumItem)) return;
        PortableJukeboxData data = stack.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (data == null || !data.isPlaying() || data.isPaused()) return;

        ServerPlayer player = null;
        if (itemEntity.getOwner() instanceof ServerPlayer sp) {
            player = sp;
        } else if (level.getServer() != null) {
            for (var e : ACTIVE_PORTABLES.entrySet()) {
                if (matchesActiveData(stack, e.getValue().data)) {
                    player = level.getServer().getPlayerList().getPlayer(e.getKey());
                    break;
                }
            }
        }
        if (player != null) {
            PortableJukeboxContainer container = new PortableJukeboxContainer(stack, level);
            container.pauseMusic();
            itemEntity.setItem(stack);
            stopPortableSoundForPlayer(player);
            clearActivePortableJukebox(player);
        }
    }

    public static void onServerTick(MinecraftServer server) {
        if (server == null) return;
        if (startupPause && startupTicks++ >= 1200) {
            startupPause = false;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ActiveEntry entry = ACTIVE_PORTABLES.get(player.getUUID());
            if (entry == null) continue;
            if (isInPlayerInventory(player, entry.stack)) continue;

            // The original stack reference is stale (ItemStack copies are made when moving to
            // containers/cursor). If the item is still in the player's inventory, keep it active.
            ItemStack moved = findInPlayerInventory(player, entry.data);
            if (moved != null) {
                setActivePortableJukebox(player, moved);
                continue;
            }

            AbstractContainerMenu menu = player.containerMenu;
            ItemStack carried = menu != null ? menu.getCarried() : ItemStack.EMPTY;
            PortableJukeboxData carriedData = carried.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
            LOGGER.info(
                "JukeboxManager lost track for {}: menu={} carried={}@{} count={} data={} playing={} paused={} active={}@{} count={}",
                player.getName().getString(),
                menu != null ? menu.getClass().getSimpleName() : "null",
                carried.getItem().getDescriptionId(),
                Integer.toHexString(System.identityHashCode(carried)),
                carried.getCount(),
                carriedData,
                carriedData != null ? carriedData.isPlaying() : "null",
                carriedData != null ? carriedData.isPaused() : "null",
                entry.stack.getItem().getDescriptionId(),
                Integer.toHexString(System.identityHashCode(entry.stack)),
                entry.stack.getCount()
            );
            if (carried.isEmpty()) {
                StringBuilder sb = new StringBuilder("inventory:");
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack inv = player.getInventory().getItem(i);
                    if (!inv.isEmpty()) {
                        sb.append(" [").append(i).append("]").append(inv.getItem().getDescriptionId()).append("@").append(Integer.toHexString(System.identityHashCode(inv)));
                    }
                }
                sb.append(" main=").append(player.getMainHandItem().getItem().getDescriptionId()).append("@").append(Integer.toHexString(System.identityHashCode(player.getMainHandItem())));
                sb.append(" off=").append(player.getOffhandItem().getItem().getDescriptionId()).append("@").append(Integer.toHexString(System.identityHashCode(player.getOffhandItem())));
                LOGGER.info(sb.toString());
            }

            // Give the container click a couple of ticks to move the item to the cursor;
            // in the meantime the cursor may be empty while the item is mid-transfer.
            if (carried.isEmpty() && entry.missingTicks < 2) {
                entry.missingTicks++;
                continue;
            }

            // The item may be in a container/anvil/crafting grid/backpack: pause it.
            // This also applies in creative, where container screens still use normal clicks.
            if (findAndPausePortable(player, entry.data)) {
                ACTIVE_PORTABLES.remove(player.getUUID());
                continue;
            }

            // In creative mode the cursor stack is client-side only: picking an item up in
            // the creative inventory sends SetCreativeModeSlot(slot, EMPTY), so the server
            // sees neither the slot item nor a carried item. If the item was not found
            // anywhere, keep the music playing until it reappears in a slot
            // (inventoryTick re-registers it then).
            if (carried.isEmpty() && player.isCreative()) {
                continue;
            }

            // The item was removed/destroyed, but its sound may still be playing; stop it.
            LOGGER.info("JukeboxManager stopping sound for {}", player.getName().getString());
            stopPortableSoundForPlayer(player);
            ACTIVE_PORTABLES.remove(player.getUUID());
        }
    }

    private static boolean isInPlayerInventory(ServerPlayer player, ItemStack active) {
        // Reference equality: the same ItemStack object must still be in the player's inventory.
        if (player.getMainHandItem() == active) return true;
        if (player.getOffhandItem() == active) return true;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i) == active) return true;
        }
        return false;
    }

    private static ItemStack findInPlayerInventory(ServerPlayer player, PortableJukeboxData activeData) {
        // Cursor/carried item.
        AbstractContainerMenu menu = player.containerMenu;
        ItemStack carried = menu != null ? menu.getCarried() : ItemStack.EMPTY;
        if (matchesActiveData(carried, activeData)) {
            return carried;
        }
        // Fallback: if the cursor is *any* playing portable jukebox, treat it as the active one.
        // This prevents the music from being stopped when the saved data snapshot is slightly stale.
        if (isPortableJukeboxPlaying(carried)) {
            LOGGER.info("JukeboxManager cursor fallback matched for {}", player.getName().getString());
            return carried;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (matchesActiveData(stack, activeData)) {
                return stack;
            }
            if (isPortableJukeboxPlaying(stack)) {
                LOGGER.info("JukeboxManager inventory fallback matched for {}", player.getName().getString());
                return stack;
            }
        }
        return null;
    }

    private static boolean isPortableJukeboxPlaying(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof PortableJukeboxAlbumItem)) return false;
        PortableJukeboxData data = stack.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        return data != null && data.isPlaying() && !data.isPaused();
    }

    /**
     * Searches for the portable jukebox described by the saved data snapshot in the player's
     * open menu, ender chest, nearby container block entities, backpack/container-item contents, and
     * dropped items. Returns true if it was found and paused.
     */
    private static boolean findAndPausePortable(ServerPlayer player, PortableJukeboxData activeData) {
        Level level = player.level();

        // Open menu slots (anvil, modded container GUIs, etc.) are checked first
        // because the player is actively interacting with them.
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null && pauseInMenuSlots(player, menu, activeData)) {
            return true;
        }

        // Ender chest is a per-player container.
        if (pauseInContainer(player, player.getEnderChestInventory(), activeData, true)) {
            return true;
        }

        // Container block entities in loaded chunks near the player.
        BlockPos playerPos = player.blockPosition();
        int chunkRange = 2; // 2 chunk radius around the player is plenty for a just-placed item
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;
        for (int cx = -chunkRange; cx <= chunkRange; cx++) {
            for (int cz = -chunkRange; cz <= chunkRange; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(playerChunkX + cx, playerChunkZ + cz);
                if (chunk == null) continue;
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof Container container) {
                        if (pauseInContainer(player, container, activeData, true)) {
                            return true;
                        }
                    }

                    BlockPos pos = blockEntity.getBlockPos();
                    BlockState state = blockEntity.getBlockState();
                    ResourceHandler<ItemResource> resourceHandler = Capabilities.Item.BLOCK.getCapability(level, pos, state, blockEntity, null);
                    if (resourceHandler != null && pauseInResourceHandler(resourceHandler, activeData, player)) {
                        blockEntity.setChanged();
                        return true;
                    }
                }
            }
        }

        // Backpacks / container items inside the player inventory (not the direct inventory slots).
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (pauseInContainerItem(stack, activeData, player)) {
                player.getInventory().setChanged();
                return true;
            }
        }

        // Dropped items in the world.
        AABB aabb = player.getBoundingBox().inflate(64);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, aabb)) {
            ItemStack stack = itemEntity.getItem();
            if (matchesActiveData(stack, activeData)) {
                new PortableJukeboxContainer(stack, level).pauseMusicForPlayer(player);
                itemEntity.setItem(stack);
                return true;
            }
            if (pauseInContainerItem(stack, activeData, player)) {
                itemEntity.setItem(stack);
                return true;
            }
        }

        return false;
    }

    private static boolean pauseInContainer(ServerPlayer player, Container container, PortableJukeboxData activeData, boolean pauseDirect) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (pauseDirect && matchesActiveData(stack, activeData)) {
                new PortableJukeboxContainer(stack, player.level()).pauseMusicForPlayer(player);
                container.setChanged();
                if (container instanceof BlockEntity be) {
                    be.setChanged();
                }
                return true;
            }
            if (pauseInContainerItem(stack, activeData, player)) {
                container.setChanged();
                if (container instanceof BlockEntity be) {
                    be.setChanged();
                }
                return true;
            }
        }
        return false;
    }

    private static boolean pauseInContainerItem(ItemStack stack, PortableJukeboxData activeData, ServerPlayer player) {
        if (stack.isEmpty()) return false;

        // Vanilla bundle / shulker-style contents.
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) {
            NonNullList<ItemStack> items = NonNullList.withSize(256, ItemStack.EMPTY);
            contents.copyInto(items);
            boolean found = false;
            for (int i = 0; i < items.size(); i++) {
                ItemStack inner = items.get(i);
                if (matchesActiveData(inner, activeData)) {
                    new PortableJukeboxContainer(inner, player.level()).pauseMusicForPlayer(player);
                    found = true;
                } else if (pauseInContainerItem(inner, activeData, player)) {
                    found = true;
                }
                if (found) {
                    // The inner stack was mutated; update the list so it is written back below.
                    items.set(i, inner);
                    break;
                }
            }
            if (found) {
                stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
            }
            return found;
        }

        // Modded backpacks / container items that expose an IItemHandler capability.
        ResourceHandler<ItemResource> resourceHandler = Capabilities.Item.ITEM.getCapability(stack, ItemAccess.forStack(stack));
        if (resourceHandler != null) {
            return pauseInResourceHandler(resourceHandler, activeData, player);
        }

        return false;
    }

    private static boolean pauseInMenuSlots(ServerPlayer player, AbstractContainerMenu menu, PortableJukeboxData activeData) {
        for (Slot slot : menu.slots) {
            // Skip player-inventory slots; those are handled by findInPlayerInventory.
            if (slot.container == player.getInventory()) {
                continue;
            }

            ItemStack stack = slot.getItem();
            if (matchesActiveData(stack, activeData)) {
                new PortableJukeboxContainer(stack, player.level()).pauseMusicForPlayer(player);
                slot.set(stack);
                return true;
            }
            if (pauseInContainerItem(stack, activeData, player)) {
                slot.set(stack);
                return true;
            }
        }
        return false;
    }

    private static boolean pauseInResourceHandler(ResourceHandler<ItemResource> handler, PortableJukeboxData activeData, ServerPlayer player) {
        for (int i = 0; i < handler.size(); i++) {
            ItemStack stack = ItemUtil.getStack(handler, i);
            if (matchesActiveData(stack, activeData)) {
                new PortableJukeboxContainer(stack, player.level()).pauseMusicForPlayer(player);
                return replaceStackInResourceHandler(handler, i, stack);
            }
            if (pauseInContainerItem(stack, activeData, player)) {
                // The nested container stack was mutated; write it back to this handler.
                return replaceStackInResourceHandler(handler, i, stack);
            }
        }
        return false;
    }

    private static boolean replaceStackInResourceHandler(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        ItemResource oldResource = handler.getResource(slot);
        int amount = handler.getAmountAsInt(slot);
        try (Transaction tx = Transaction.openRoot()) {
            handler.extract(slot, oldResource, amount, tx);
            ItemStack leftover = ItemUtil.insertItemReturnRemaining(handler, slot, stack, false, tx);
            if (leftover.isEmpty()) {
                tx.commit();
                return true;
            }
        }
        return false;
    }

    private static boolean matchesActiveData(ItemStack stack, PortableJukeboxData activeData) {
        if (stack.isEmpty() || !(stack.getItem() instanceof PortableJukeboxAlbumItem)) return false;
        PortableJukeboxData data = stack.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (data == null || !data.isPlaying() || data.isPaused()) return false;
        // Match the current play session, not the full saved contents/currentPage/maxPages.
        // This makes the item still findable after ItemStack copies or small state changes.
        return sameSession(data, activeData);
    }

    private static void stopPortableSoundForPlayer(ServerPlayer player) {
        // Reuse the same stop command the jukebox container sends.
        com.custommusic.network.MusicCommandPayload payload = new com.custommusic.network.MusicCommandPayload(
            com.custommusic.network.MusicCommandPayload.Command.STOP,
            "portable:" + player.getUUID().toString(),
            net.minecraft.resources.Identifier.withDefaultNamespace("empty"),
            player.blockPosition(),
            0,
            0L,
            0.0F,
            0.0F,
            true
        );
        com.custommusic.network.NetworkHandler.sendMusicCommand(player, payload);
    }
    public static void stopPortableJukeboxForPlayer(ServerPlayer player) {
        stopPortableJukeboxForPlayer(player, null);
    }

    public static void stopPortableJukeboxForPlayer(ServerPlayer player, @org.jspecify.annotations.Nullable ItemStack except) {
        if (player == null) return;
        boolean stopped = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (tryPausePortable(player, player.getInventory().getItem(i), except)) stopped = true;
        }
        if (tryPausePortable(player, player.getOffhandItem(), except)) stopped = true;
        AbstractContainerMenu menu = player.containerMenu;
        if (tryPausePortable(player, menu != null ? menu.getCarried() : ItemStack.EMPTY, except)) stopped = true;
    }

    private static boolean tryPausePortable(ServerPlayer player, ItemStack stack, @org.jspecify.annotations.Nullable ItemStack except) {
        if (stack == null || stack.isEmpty() || stack == except) return false;
        if (!(stack.getItem() instanceof PortableJukeboxAlbumItem)) return false;
        var data = stack.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (data == null || !data.isPlaying() || data.isPaused()) return false;
        PortableJukeboxContainer container = new PortableJukeboxContainer(stack, player.level());
        container.pauseMusic();
        container.saveToStack(stack);
        return true;
    }

    public static void stopNearbyJukeboxAlbumForPlayer(ServerPlayer player) {
        if (player == null) return;
        Level level = player.level();
        if (level.isClientSide()) return;

        BlockPos playerPos = player.blockPosition();
        int range = 64;
        int chunkRange = (range >> 4) + 1;
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;

        JukeboxAlbumBlockEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int cx = -chunkRange; cx <= chunkRange; cx++) {
            for (int cz = -chunkRange; cz <= chunkRange; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(playerChunkX + cx, playerChunkZ + cz);
                if (chunk == null) continue;
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    if (entry.getValue() instanceof JukeboxAlbumBlockEntity jukebox) {
                        if (jukebox.isPlaying() && !jukebox.isPaused()) {
                            double dist = player.distanceToSqr(jukebox.getBlockPos().getX() + 0.5, jukebox.getBlockPos().getY() + 0.5, jukebox.getBlockPos().getZ() + 0.5);
                            if (dist < range * range && dist < nearestDist) {
                                nearestDist = dist;
                                nearest = jukebox;
                            }
                        }
                    }
                }
            }
        }

        if (nearest != null) {
            nearest.pauseMusic();
        }
    }

    public static void pauseAllMusic(MinecraftServer server) {
        if (server == null) return;
        pauseAllJukeboxAlbums(server);
        pauseAllPortableJukeboxes(server);
    }

    private static void pauseAllJukeboxAlbums(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level == null) continue;
            try {
                level.getChunkSource().chunkMap.forEachReadyToSendChunk(chunk -> {
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be instanceof JukeboxAlbumBlockEntity jukebox) {
                            if (jukebox.isPlaying() && !jukebox.isPaused()) {
                                try {
                                    jukebox.pauseMusic();
                                } catch (Exception e) {
                                    LOGGER.error("[CustomMusic] Failed to pause jukebox album at {}", jukebox.getBlockPos(), e);
                                }
                            }
                        }
                    }
                });
            } catch (Exception e) {
                LOGGER.error("[CustomMusic] Failed to pause jukebox albums in level {}", level.dimension().identifier(), e);
            }
        }
    }

    private static void pauseAllPortableJukeboxes(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player == null) continue;
            try {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.getItem() instanceof PortableJukeboxAlbumItem) {
                        var data = stack.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
                        if (data != null && data.isPlaying() && !data.isPaused()) {
                            PortableJukeboxContainer container = new PortableJukeboxContainer(stack, player.level());
                            container.pauseMusic();
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[CustomMusic] Failed to pause portable jukebox for player {}", player.getName().getString(), e);
            }
        }
    }

    public static void onShutdown() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            pauseAllMusic(server);
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(JukeboxManager::onShutdown, "CustomMusic Shutdown Hook"));
    }
}
