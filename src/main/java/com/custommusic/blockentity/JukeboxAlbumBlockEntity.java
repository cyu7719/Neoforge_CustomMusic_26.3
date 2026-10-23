package com.custommusic.blockentity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.custommusic.init.ModBlockEntities;
import com.custommusic.item.CustomMusicDiscItem;
import com.custommusic.menu.JukeboxAlbumMenu;
import com.custommusic.network.MusicCommandPayload;
import com.custommusic.network.MusicStatePayload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;

public class JukeboxAlbumBlockEntity extends BlockEntity implements Container, MenuProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SLOTS_PER_PAGE = 54;
    private NonNullList<ItemStack> items;
    private int maxPages = 1;
    private int currentPage = 0;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private int currentlyPlayingSlot = -1;
    private int ticksSinceStart = 0;
    private int songLengthTicks = 0;
    private int pausedTicksPosition = 0; // Track position when paused
    private long songStartGameTime = 0L; // Game time when the current song started (or was resumed)
    private long lastTickGameTime = 0L; // Last server tick this block was processed
    private long songStartRealTime = 0L; // Wall-clock time the current song started at position 0
    private net.minecraft.core.Direction playerFacingOnPlace = net.minecraft.core.Direction.NORTH;
    private int clientParticleTicks = 0; // Client-side counter for particle timing
    private long spinningStartTime = 0; // Track when spinning started for smooth rotation
    private int pendingSlot = -1;
    private long pendingPlayGameTime = 0L;
    private boolean messageSentThisTick = false; // Prevent multiple messages in same tick
    private boolean autoPaused = false; // True only when paused because no players were in the dimension
    private final Set<UUID> playersHearingSound = new HashSet<>(); // Server-side players already sent the active sound
    private final Map<UUID, Float> playerVolumes = new HashMap<>();
    private float volume = 0.5F;
    // No future scheduling delay. The startTimestamp is the current server time and
    // OffsetAudioStream seeks to the current song position on the first read.
    private static final int SYNC_DELAY_MS = 0;

    public JukeboxAlbumBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JUKEBOX_ALBUM.get(), pos, state);
        this.items = NonNullList.withSize(SLOTS_PER_PAGE * maxPages, ItemStack.EMPTY);
        System.out.println("JukeboxAlbumBlockEntity created at " + pos);
    }
    
    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
            sendBlockUpdated();

            // Check if the removed item was the currently playing disc
            if (isPlaying && slot == currentlyPlayingSlot) {
                // Stop music completely when the playing disc is removed
                stopMusic();
            }
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
        sendBlockUpdated();

        // If the currently playing slot is emptied via a slot swap/quick move, stop the music.
        if (isPlaying && slot == currentlyPlayingSlot && stack.isEmpty()) {
            stopMusic();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.getItem() instanceof CustomMusicDiscItem || stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // Do not auto-drop contents on removal; the block item itself preserves them
        // via BLOCK_ENTITY_DATA when broken with playerWillDestroy.
        if (isPlaying) {
            pauseMusic();
        }
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void addPage() {
        maxPages++;
        NonNullList<ItemStack> newItems = NonNullList.withSize(SLOTS_PER_PAGE * maxPages, ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            newItems.set(i, items.get(i));
        }
        items = newItems;
        setChanged();
        sendBlockUpdated();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        if (page >= 0 && page < maxPages) {
            currentPage = page;
            setChanged();
        }
    }

    public void nextPage() {
        if (currentPage < maxPages - 1) {
            currentPage++;
            setChanged();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            setChanged();
        }
    }

    private int computeSyncedOffset(long startTimestamp) {
        if (songStartRealTime <= 0) {
            return 0;
        }
        long positionMillis = startTimestamp - songStartRealTime;
        int offset = (int) (positionMillis / 50L);
        if (songLengthTicks > 0) {
            return Math.max(0, Math.min(offset, songLengthTicks - 1));
        }
        return Math.max(0, offset);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public int getCurrentlyPlayingSlot() {
        return currentlyPlayingSlot;
    }

    // Raw elapsed time (never negative). Used for end-of-song checks.
    public int getElapsedTicks() {
        if (isPaused) {
            return Math.max(0, pausedTicksPosition);
        }
        if (level != null && isPlaying && songStartGameTime > 0) {
            return Math.max(0, (int)(level.getGameTime() - songStartGameTime));
        }
        return Math.max(0, ticksSinceStart);
    }

    // Display/network offset clamped to a valid in-range position.
    public int getTicksSinceStart() {
        int elapsed = getElapsedTicks();
        if (songLengthTicks <= 0) return elapsed;
        return Math.max(0, Math.min(elapsed, songLengthTicks - 1));
    }

    // Wall-clock elapsed time (never negative), consistent with what listeners
    // actually hear since client playback is anchored to real time.
    public int getRealElapsedTicks() {
        if (isPaused) {
            return Math.max(0, pausedTicksPosition);
        }
        if (songStartRealTime <= 0) {
            return getElapsedTicks();
        }
        return Math.max(0, (int) ((System.currentTimeMillis() - songStartRealTime) / 50L));
    }

    public long getSongStartGameTime() {
        return songStartGameTime;
    }

    public int getSongLengthTicks() {
        return songLengthTicks;
    }

    // Seeks the current song to the given tick position. Replays the sound from
    // that offset when playing; when paused it just moves the stored position.
    public void seekTo(int positionTicks) {
        if (level == null || level.isClientSide() || !isPlaying || currentlyPlayingSlot < 0) return;
        int clamped = Math.max(0, positionTicks);
        if (songLengthTicks > 0) {
            clamped = Math.min(clamped, songLengthTicks - 1);
        }
        if (isPaused) {
            pausedTicksPosition = clamped;
            ticksSinceStart = clamped;
            songStartRealTime = System.currentTimeMillis() - clamped * 50L;
            spinningStartTime = songStartRealTime;
            syncToClients();
            setChanged();
        } else {
            pausedTicksPosition = clamped;
            playSlot(currentlyPlayingSlot, true);
        }
    }

    public void playMusic() {
        if (level == null || level.isClientSide()) return;

        // Always find the first disc on current page (don't try to resume previous slot)
        // This ensures that after removing and reinserting a disc, it plays the first available disc
        int startSlot = currentPage * SLOTS_PER_PAGE;
        for (int i = 0; i < SLOTS_PER_PAGE; i++) {
            int slot = startSlot + i;
            if (slot < items.size()) {
                ItemStack stack = items.get(slot);
                if (!stack.isEmpty() && canPlaceItem(slot, stack)) {
                    playSlot(slot);
                    return;
                }
            }
        }
    }

    public void playSlot(int slot) {
        playSlot(slot, false);
    }
    
    public void playSlot(int slot, boolean resume) {
        if (level == null || level.isClientSide()) return;

        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) return;

        // Capture the resume position before stopMusic resets it
        int resumeFrom = resume ? Math.max(0, pausedTicksPosition) : 0;

        stopMusic();
        pendingSlot = -1;
        pendingPlayGameTime = 0L;
        messageSentThisTick = false; // Reset message flag at start of playSlot

        // A deliberate play means any lingering crash-recovery state is obsolete.
        com.custommusic.JukeboxManager.setStartupPause(false);

        isPlaying = true;
        isPaused = false;
        autoPaused = false;
        currentlyPlayingSlot = slot;
        pausedTicksPosition = 0; // Reset paused position for new song

        // Determine length first so we can clamp the resume position
        SoundInfo info = getSoundInfo(slot);
        if (info != null) {
            songLengthTicks = info.lengthInTicks();
        } else {
            songLengthTicks = 200 * 20;
        }

        // Clamp resume position to the actual audio length (prevents stale data from skipping to EOF)
        if (resume && songLengthTicks > 0) {
            resumeFrom = Math.max(0, Math.min(resumeFrom, songLengthTicks - 1));
        } else if (resume) {
            resumeFrom = 0;
        }
        ticksSinceStart = resume ? resumeFrom : 0;
        songStartGameTime = level.getGameTime() - ticksSinceStart;
        long scheduledStart = System.currentTimeMillis();
        songStartRealTime = scheduledStart - ticksSinceStart * 50L;
        spinningStartTime = scheduledStart - (resume ? ticksSinceStart * 50L : 0L);
        lastTickGameTime = level.getGameTime();

        if (info != null) {
            playSoundToAllNearby(info.sound());
        }

        sendNowPlayingMessage(slot);
        syncToClients();
        setChanged();
    }

    private record SoundInfo(Holder<SoundEvent> sound, int lengthInTicks) {}

    private @Nullable SoundInfo getSoundInfo(int slot) {
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) return null;

        if (stack.getItem() instanceof CustomMusicDiscItem customDisc) {
            return new SoundInfo(customDisc.getSound(), customDisc.getLengthInTicks());
        } else if (stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
            var holderOpt = JukeboxSong.fromStack(stack);
            if (holderOpt.isPresent() && holderOpt.get().isBound()) {
                var song = holderOpt.get().value();
                return new SoundInfo(song.soundEvent(), (int)(song.lengthInSeconds() * 20));
            }
        }
        return null;
    }

    private String getMusicId() {
        return "block:" + worldPosition.asLong();
    }

    private void playSoundToAllNearby(Holder<SoundEvent> sound) {
        sendMusicToAllNearby(sound, MusicCommandPayload.Command.PLAY);
    }

    private void sendMusicToAllNearby(Holder<SoundEvent> sound, MusicCommandPayload.Command command) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        float range = sound.value().getRange(0.5F);
        double rangeSqr = range * range;
        String musicId = getMusicId();
        // Anchor sync to server game time (ticks). The client's game time is kept in
        // sync by vanilla, so it can measure packet transit without wall-clock skew.
        long sendGameTime = serverLevel.getGameTime();
        int offset = computeSyncedOffset(System.currentTimeMillis());

        if (command == MusicCommandPayload.Command.PLAY) {
            playersHearingSound.clear();
        }
        ChunkPos chunkPos = ChunkPos.containing(worldPosition);
        java.util.List<ServerPlayer> players = serverLevel.getChunkSource().chunkMap.getPlayers(chunkPos, false);
        LOGGER.info("sendMusicToAllNearby at {} command={} songStartRealTime={} sendGameTime={} offset={}",
            worldPosition, command, songStartRealTime, sendGameTime, offset);
        for (ServerPlayer player : players) {
            double distSqr = player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            LOGGER.info("  player {} distSq={} inRange={}", player.getName().getString(), distSqr, distSqr < rangeSqr);
            if (distSqr < rangeSqr) {
                float playerVolume = getVolumeForPlayer(player);
                MusicCommandPayload payload = new MusicCommandPayload(
                    command,
                    musicId,
                    sound.unwrapKey().orElseThrow().identifier(),
                    worldPosition,
                    offset,
                    sendGameTime,
                    playerVolume,
                    1.0F,
                    false
                );
                com.custommusic.network.NetworkHandler.sendMusicCommand(player, payload);
                playersHearingSound.add(player.getUUID());
            }
        }
    }

    private void stopSoundForAllNearby() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        MusicCommandPayload payload = new MusicCommandPayload(
            MusicCommandPayload.Command.STOP,
            getMusicId(),
            Identifier.withDefaultNamespace("empty"),
            worldPosition,
            0,
            0L,
            0.0F,
            0.0F,
            false
        );

        ChunkPos chunkPos = ChunkPos.containing(worldPosition);
        for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
            if (player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 64 * 64) {
                com.custommusic.network.NetworkHandler.sendMusicCommand(player, payload);
            }
        }
    }
    
    private @Nullable String getSongDescription(int slot) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) return null;

        if (stack.getItem() instanceof CustomMusicDiscItem customDisc) {
            return serverLevel.registryAccess().lookup(net.minecraft.core.registries.Registries.JUKEBOX_SONG)
                .flatMap(registry -> registry.get(customDisc.getJukeboxSongKey()))
                .map(song -> song.value().description().getString())
                .orElse(null);
        } else if (stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
            var holderOpt = net.minecraft.world.item.JukeboxSong.fromStack(stack);
            var song = (holderOpt.isPresent() && holderOpt.get().isBound()) ? holderOpt.get().value() : null;
            if (song != null) {
                return song.description().getString();
            }
        }
        return null;
    }

    private void sendNowPlayingMessage(ServerPlayer player, String songDesc) {
        if (songDesc == null || player == null) return;
        com.custommusic.network.NetworkHandler.sendNowPlaying(net.minecraft.network.chat.Component.literal(songDesc), player);
    }

    private void sendNowPlayingMessage(int slot) {
        if (messageSentThisTick) return; // Prevent multiple messages in same tick
        messageSentThisTick = true;

        if (!(level instanceof ServerLevel serverLevel)) return;

        String songDesc = getSongDescription(slot);
        if (songDesc == null) return;

        for (net.minecraft.server.level.ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 64 * 64) {
                sendNowPlayingMessage(player, songDesc);
            }
        }
    }

    public void pauseMusic() {
        pauseMusic(false);
    }

    public void pauseMusic(boolean autoPaused) {
        pendingSlot = -1;
        pendingPlayGameTime = 0L;
        if (!isPlaying || isPaused) return;

        // Save position based on wall-clock so resume/join sync is accurate,
        // but never past the game-time playhead: wall-clock runs ahead during
        // server lag and would otherwise inflate the resume position enough to
        // skip the song early.
        int position;
        if (songStartRealTime > 0) {
            position = Math.min((int) ((System.currentTimeMillis() - songStartRealTime) / 50L), getElapsedTicks());
        } else {
            position = getElapsedTicks();
        }
        if (songLengthTicks > 0) {
            position = Math.max(0, Math.min(position, songLengthTicks - 1));
        }
        isPaused = true;
        this.autoPaused = autoPaused;
        pausedTicksPosition = position;
        ticksSinceStart = position;
        songStartRealTime = System.currentTimeMillis() - pausedTicksPosition * 50L;
        spinningStartTime = System.currentTimeMillis() - pausedTicksPosition * 50L;
        playersHearingSound.clear();

        // Stop the sound when pausing
        stopSoundForAllNearby();

        syncToClients();
        setChanged();
    }

    public void resumeMusic() {
        if (!isPlaying || !isPaused) return;

        if (currentlyPlayingSlot >= 0) {
            playSlot(currentlyPlayingSlot, true);
        }
    }

    public void stopMusic() {
        pendingSlot = -1;
        pendingPlayGameTime = 0L;
        if (level != null && !level.isClientSide() && isPlaying) {
            stopSoundForAllNearby();
        }

        isPlaying = false;
        isPaused = false;
        autoPaused = false;
        currentlyPlayingSlot = -1;
        ticksSinceStart = 0;
        pausedTicksPosition = 0; // Reset paused position
        songLengthTicks = 0;
        lastTickGameTime = 0;
        songStartRealTime = 0L;
        playersHearingSound.clear();
        syncToClients();
        setChanged();
    }
    
    // Enhanced auto-next method with cross-page support
    public void autoNextSong() {
        if (level == null || level.isClientSide()) return;

        int currentSlot = currentlyPlayingSlot;
        int nextSlot = findNextSong(currentSlot);
        if (nextSlot != -1) {
            int nextPage = nextSlot / SLOTS_PER_PAGE;
            if (nextPage != currentPage) {
                currentPage = nextPage;
            }
            playSlot(nextSlot);
        } else {
            // No next song on current page, try next page
            if (currentPage < maxPages - 1) {
                // Go to next page
                currentPage++;
                int firstSlot = findFirstSongOnPage(currentPage);
                if (firstSlot != -1) {
                    playSlot(firstSlot);
                } else {
                    // No songs on next page, loop back to first page
                    currentPage = 0;
                    int firstSlotOnFirstPage = findFirstSongOnPage(0);
                    if (firstSlotOnFirstPage != -1) {
                        playSlot(firstSlotOnFirstPage);
                    } else {
                        stopMusic();
                    }
                }
            } else {
                // Last page, loop back to first page
                currentPage = 0;
                int firstSlot = findFirstSongOnPage(0);
                if (firstSlot != -1) {
                    playSlot(firstSlot);
                } else {
                    stopMusic();
                }
            }
        }
    }

    private int findNextSongCrossPage(int currentSlot) {
        int totalSlots = items.size();
        if (totalSlots == 0) return -1;
        int startSlot = (currentSlot + 1) % totalSlots;
        for (int checked = 0; checked < totalSlots; checked++) {
            int slot = (startSlot + checked) % totalSlots;
            ItemStack stack = items.get(slot);
            if (stack.getItem() instanceof CustomMusicDiscItem || stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
                return slot;
            }
        }
        return -1; // no songs found
    }

    private int findPreviousSongCrossPage(int currentSlot) {
        // If we're at the first slot (0), there's no previous song
        if (currentSlot <= 0) {
            return -1; // no previous song available
        }
        
        int slot = currentSlot - 1;
        while (slot >= 0) {
            ItemStack stack = items.get(slot);
            if (stack.getItem() instanceof CustomMusicDiscItem || stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
                return slot;
            }
            slot--;
        }
        return -1; // no songs found
    }

    public void nextSong() {
        if (level == null || level.isClientSide()) return;
        if (!isPlaying) return; // no song is playing, do nothing

        int nextSlot = findNextSongCrossPage(currentlyPlayingSlot);
        if (nextSlot != -1) {
            currentPage = nextSlot / SLOTS_PER_PAGE;
            pendingSlot = nextSlot;
            pendingPlayGameTime = level.getGameTime() + 2;
        } else {
            stopMusic();
        }
    }

    public void previousSong() {
        if (level == null || level.isClientSide()) return;
        if (!isPlaying) return; // no song is playing, do nothing

        // Check if current song is the first actual disc on its page
        int firstDiscOnCurrentPage = findFirstSongOnPage(currentPage);
        boolean isFirstDiscOnCurrentPage = (isPlaying && currentlyPlayingSlot == firstDiscOnCurrentPage && firstDiscOnCurrentPage != -1);
        
        if (isFirstDiscOnCurrentPage) {
            // Check if there are any discs on previous pages
            boolean hasDiscsOnPreviousPages = false;
            for (int page = 0; page < currentPage; page++) {
                if (findFirstSongOnPage(page) != -1) {
                    hasDiscsOnPreviousPages = true;
                    break;
                }
            }
            
            if (!hasDiscsOnPreviousPages) {
                // No discs on previous pages, always restart
                pendingSlot = currentlyPlayingSlot;
                pendingPlayGameTime = level.getGameTime() + 2;
                return;
            }
            // Otherwise, fall through to 3-second logic below
        }

        // Check if song has been playing for more than 3 seconds (60 ticks) - applies to all other cases
        if (isPlaying && currentlyPlayingSlot != -1 && ticksSinceStart >= 60) {
            // Restart the current song
            pendingSlot = currentlyPlayingSlot;
            pendingPlayGameTime = level.getGameTime() + 2;
            return;
        }

        // Otherwise (less than 3 seconds), go to previous song
        int prevSlot = findPreviousSongCrossPage(currentlyPlayingSlot);
        if (prevSlot != -1) {
            currentPage = prevSlot / SLOTS_PER_PAGE;
            pendingSlot = prevSlot;
            pendingPlayGameTime = level.getGameTime() + 2;
        } else {
            stopMusic();
        }
    }
    
    private int findFirstSongOnPage(int page) {
        int startSlot = page * SLOTS_PER_PAGE;
        int endSlot = Math.min(startSlot + SLOTS_PER_PAGE, items.size());
        
        for (int i = startSlot; i < endSlot; i++) {
            ItemStack stack = items.get(i);
            if (stack.getItem() instanceof CustomMusicDiscItem || stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
                return i;
            }
        }
        return -1;
    }

    public void loadFromItem(ItemStack stack, HolderLookup.Provider registries) {
        // Skip loading from item stack for now - API has changed
    }

    
    private int findLastSongOnPage(int page) {
        int startSlot = page * SLOTS_PER_PAGE;
        int endSlot = Math.min(startSlot + SLOTS_PER_PAGE, items.size());
        
        // Search backwards to find the last song on the page
        for (int i = endSlot - 1; i >= startSlot; i--) {
            ItemStack stack = items.get(i);
            if (stack.getItem() instanceof CustomMusicDiscItem || stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
                return i;
            }
        }
        return -1;
    }

    private int findNextSong(int currentSlot) {
        int currentPageStart = currentPage * SLOTS_PER_PAGE;
        int currentPageEnd = Math.min(currentPageStart + SLOTS_PER_PAGE, items.size());
        
        // Search within current page only
        for (int i = currentSlot + 1; i < currentPageEnd; i++) {
            ItemStack checkStack = items.get(i);
            if (checkStack.getItem() instanceof CustomMusicDiscItem || checkStack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
                return i;
            }
        }
        return -1;
    }

    private int findPreviousSong(int currentSlot) {
        int currentPageStart = currentPage * SLOTS_PER_PAGE;
        
        // Search within current page only
        for (int i = currentSlot - 1; i >= currentPageStart; i--) {
            ItemStack checkStack = items.get(i);
            if (checkStack.getItem() instanceof CustomMusicDiscItem || checkStack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
                return i;
            }
        }
        return -1;
    }

    private int soundCheckTimer = 0;
    
    // Helper method to get cycling color like vanilla jukebox (HSV color cycling)
    private net.minecraft.ChatFormatting getCyclingColor() {
        // Vanilla uses Mth.hsvToArgb(t / 50.0F, 0.7F, 0.6F, alpha) for overlay message animation
        // For system messages, we can approximate this with ChatFormatting colors
        // The HSV cycle creates a rainbow effect, so we cycle through colors more frequently
        int colorIndex = (ticksSinceStart / 5) % 8; // Faster cycle (every 5 ticks = 0.25 seconds)
        net.minecraft.ChatFormatting[] colors = {
            net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.YELLOW,
            net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.AQUA, net.minecraft.ChatFormatting.BLUE,
            net.minecraft.ChatFormatting.LIGHT_PURPLE, net.minecraft.ChatFormatting.WHITE
        };
        return colors[colorIndex];
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            // Spawn beat-synchronized music note particles
            if (isPlaying && !isPaused) {
                spawnBeatNotes(level, pos);
            }
            return;
        }

        if (pendingSlot != -1 && level.getGameTime() >= pendingPlayGameTime) {
            int slot = pendingSlot;
            pendingSlot = -1;
            pendingPlayGameTime = 0L;
            playSlot(slot);
            return;
        }

        if (isPlaying && !isPaused) {
            int elapsed = getElapsedTicks();
            ticksSinceStart = getTicksSinceStart();
            // Both the game-time and the wall-clock playheads must be past the
            // end. Game time alone can run ahead of the real playback position
            // while the server catches up ticks after a lag spike, which would
            // otherwise skip to the next song before it finished playing.
            if (songLengthTicks > 0 && elapsed >= songLengthTicks && getRealElapsedTicks() >= songLengthTicks) {
                autoNextSong();
                return;
            }
        }

        resyncSoundForNearbyPlayers();
    }

    private void resyncSoundForNearbyPlayers() {
        if (!isPlaying || isPaused || currentlyPlayingSlot < 0) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        SoundInfo info = getSoundInfo(currentlyPlayingSlot);
        if (info == null) return;

        float range = info.sound().value().getRange(0.5F);
        double rangeSqr = range * range;
        String songDesc = getSongDescription(currentlyPlayingSlot);
        String musicId = getMusicId();
        long sendGameTime = serverLevel.getGameTime();
        int offset = computeSyncedOffset(System.currentTimeMillis());

        LOGGER.info("resyncSoundForNearbyPlayers at {} songStartRealTime={} sendGameTime={} offset={}",
            worldPosition, songStartRealTime, sendGameTime, offset);

        MusicCommandPayload stopPayload = new MusicCommandPayload(
            MusicCommandPayload.Command.STOP,
            musicId,
            Identifier.withDefaultNamespace("empty"),
            worldPosition,
            0,
            0L,
            0.0F,
            0.0F,
            false
        );

        Set<UUID> playersInRange = new HashSet<>();
        ChunkPos resyncChunkPos = ChunkPos.containing(worldPosition);
        for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(resyncChunkPos, false)) {
            if (player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < rangeSqr) {
                playersInRange.add(player.getUUID());
                // Skip newly joined players (tickCount < 40); they are handled by the deferred
                // sync in JukeboxEventHandler which waits for the client's game time to sync.
                if (player.tickCount < 40) {
                    continue;
                }
                if (playersHearingSound.add(player.getUUID())) {
                    float playerVolume = getVolumeForPlayer(player);
                    MusicCommandPayload playPayload = new MusicCommandPayload(
                        MusicCommandPayload.Command.PLAY,
                        musicId,
                        info.sound().unwrapKey().orElseThrow().identifier(),
                        worldPosition,
                        offset,
                        sendGameTime,
                        playerVolume,
                        1.0F,
                        false
                    );
                    com.custommusic.network.NetworkHandler.sendMusicCommand(player, playPayload);
                    if (songDesc != null) {
                        sendNowPlayingMessage(player, songDesc);
                    }
                }
            }
        }

        // Stop for players that left the range
        for (UUID uuid : new HashSet<>(playersHearingSound)) {
            if (!playersInRange.contains(uuid)) {
                playersHearingSound.remove(uuid);
                ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
                if (player != null) {
                    com.custommusic.network.NetworkHandler.sendMusicCommand(player, stopPayload);
                }
            }
        }
    }
    
    // Method to spawn music notes matching vanilla jukebox behavior
    private void spawnBeatNotes(Level level, BlockPos pos) {
        // Increment client-side particle counter
        clientParticleTicks++;
        
        // Spawn particles at the specified interval (change this value to adjust speed)
        // 100 = 5 seconds, 200 = 10 seconds, etc.
        if (clientParticleTicks % 20 == 0) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.2; // Same height as vanilla (1.2F)
            double z = pos.getZ() + 0.5;
            // Vanilla uses random color: getRandom().nextInt(4) / 24.0F
            float randomColor = level.getRandom().nextInt(4) / 24.0F;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.NOTE, x, y, z, randomColor, 0.0, 0.0);
        }
    }
    
    // Calculate beat interval based on song characteristics
    private int calculateBeatInterval() {
        // Base interval on song length to create different rhythms
        if (songLengthTicks > 0) {
            // Shorter songs = faster beats, longer songs = slower beats
            // Range from 8 ticks (0.4 seconds) to 20 ticks (1 second)
            int baseInterval = Math.max(8, Math.min(20, songLengthTicks / 200));
            
            // Add some variation based on current position in song
            int variation = (ticksSinceStart / 100) % 4;
            return baseInterval + variation;
        }
        return 10; // Default to 10 ticks (0.5 seconds)
    }

    public void dropContents(Level level, BlockPos pos) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }
    
    // Client-side state update method
    public void updateClientState(boolean isPlaying, boolean isPaused, int currentSlot, int currentPage, int ticksSinceStart, long spinningStartTime, float volume, long songStartGameTime) {
        this.isPlaying = isPlaying;
        this.isPaused = isPaused;
        this.currentlyPlayingSlot = currentSlot;
        this.currentPage = currentPage;
        this.ticksSinceStart = ticksSinceStart;
        this.spinningStartTime = spinningStartTime;
        this.volume = volume;
        this.songStartGameTime = songStartGameTime;
        // The paused display position lives in pausedTicksPosition; without this
        // the scrubber would show the full remaining time while paused.
        this.pausedTicksPosition = ticksSinceStart;
        setChanged();
    }

    public BlockPos getBlockPos() {
        return this.worldPosition;
    }
    
    public void setPlayerFacingOnPlace(net.minecraft.core.Direction direction) {
        this.playerFacingOnPlace = direction;
        setChanged();
    }
    
    public net.minecraft.core.Direction getPlayerFacingOnPlace() {
        return this.playerFacingOnPlace;
    }
    
    public long getSpinningStartTime() {
        return this.spinningStartTime;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getVolumeForPlayer(Player player) {
        if (player == null) return this.volume;
        return playerVolumes.getOrDefault(player.getUUID(), this.volume);
    }

    public void setVolume(float volume) {
        // Kept for NBT / base volume fallback; does not broadcast to players.
        this.volume = net.minecraft.util.Mth.clamp(volume, 0.0F, 1.0F);
        setChanged();
        sendBlockUpdated();
    }

    public void setPlayerVolume(UUID playerId, float volume) {
        if (playerId == null) return;
        volume = net.minecraft.util.Mth.clamp(volume, 0.0F, 1.0F);
        playerVolumes.put(playerId, volume);
        this.volume = volume;

        if (level != null && !level.isClientSide()) {
            setChanged();
            sendBlockUpdated();
            syncToClients();

            if (isPlaying && !isPaused && currentlyPlayingSlot >= 0) {
                SoundInfo info = getSoundInfo(currentlyPlayingSlot);
                if (info != null) {
                    ServerPlayer player = (level instanceof ServerLevel serverLevel) ? serverLevel.getServer().getPlayerList().getPlayer(playerId) : null;
                    if (player != null) {
                        sendVolumeToPlayer(player, info.sound(), volume);
                    }
                }
            }
        }
    }

    private void sendVolumeToPlayer(ServerPlayer player, Holder<SoundEvent> sound, float playerVolume) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        MusicCommandPayload payload = new MusicCommandPayload(
            MusicCommandPayload.Command.SET_VOLUME,
            getMusicId(),
            sound.unwrapKey().orElseThrow().identifier(),
            worldPosition,
            0,
            0L,
            playerVolume,
            1.0F,
            false
        );
        com.custommusic.network.NetworkHandler.sendMusicCommand(player, payload);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("MaxPages", maxPages);
        output.putInt("CurrentPage", currentPage);
        output.putBoolean("CustomMusicUpdate", true);
        output.putBoolean("IsPlaying", isPlaying);
        output.putBoolean("IsPaused", isPaused);
        output.putBoolean("AutoPaused", autoPaused);
        output.putInt("CurrentlyPlayingSlot", currentlyPlayingSlot);
        output.putInt("TicksSinceStart", ticksSinceStart);
        output.putInt("SongLengthTicks", songLengthTicks);
        output.putInt("PausedTicksPosition", pausedTicksPosition);
        output.putLong("SongStartGameTime", songStartGameTime);
        output.putInt("SoundCheckTimer", soundCheckTimer);
        output.putLong("SpinningStartTime", spinningStartTime);
        output.putFloat("Volume", volume);
        output.putString("PlayerFacingOnPlace", playerFacingOnPlace.name());
        ValueOutput inventory = output.child("Inventory");
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                // Rebind the JukeboxPlayable holder to the current level registry so it can be serialized.
                stack = rebindJukeboxPlayableForSerialization(stack);
                inventory.store("Item" + i, ItemStack.CODEC, stack);
            }
        }
    }

    private ItemStack rebindJukeboxPlayableForSerialization(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return stack;
        }
        JukeboxPlayable playable = stack.get(DataComponents.JUKEBOX_PLAYABLE);
        if (playable == null || !(playable.song() instanceof Holder.Reference<JukeboxSong> ref)) {
            return stack;
        }
        Optional<Registry<JukeboxSong>> lookupOpt = level.registryAccess().lookup(Registries.JUKEBOX_SONG);
        if (lookupOpt.isEmpty()) {
            return stack;
        }
        Optional<Holder.Reference<JukeboxSong>> resolvedOpt = lookupOpt.get().get(ref.key());
        if (resolvedOpt.isEmpty() || resolvedOpt.get() == playable.song()) {
            return stack;
        }
        ItemStack copy = stack.copy();
        copy.set(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(resolvedOpt.get()));
        return copy;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        maxPages = input.getIntOr("MaxPages", 1);
        if (maxPages < 1) maxPages = 1;
        items = NonNullList.withSize(SLOTS_PER_PAGE * maxPages, ItemStack.EMPTY);
        input.child("Inventory").ifPresent(inventory -> {
            for (int i = 0; i < items.size(); i++) {
                items.set(i, inventory.read("Item" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
            }
        });
        currentPage = input.getIntOr("CurrentPage", 0);
        if (input.getBooleanOr("CustomMusicUpdate", false)) {
            isPlaying = input.getBooleanOr("IsPlaying", false);
            isPaused = input.getBooleanOr("IsPaused", false);
            autoPaused = input.getBooleanOr("AutoPaused", false);
            currentlyPlayingSlot = input.getIntOr("CurrentlyPlayingSlot", -1);
            ticksSinceStart = input.getIntOr("TicksSinceStart", 0);
            songLengthTicks = input.getIntOr("SongLengthTicks", 0);
            pausedTicksPosition = input.getIntOr("PausedTicksPosition", 0);
            songStartGameTime = input.getLongOr("SongStartGameTime", 0L);
            soundCheckTimer = input.getIntOr("SoundCheckTimer", 0);
            spinningStartTime = input.getLongOr("SpinningStartTime", 0L);
            volume = input.getFloatOr("Volume", 0.5F);
        } else {
            isPlaying = false;
            isPaused = false;
            autoPaused = false;
            currentlyPlayingSlot = -1;
            ticksSinceStart = 0;
            songLengthTicks = 0;
            pausedTicksPosition = 0;
            soundCheckTimer = 0;
            spinningStartTime = 0L;
        }
        // Crash recovery: if the world shut down uncleanly while this jukebox was playing,
        // come back paused so the player can resume from where it left off.
        // This only makes sense on the server; on the client it is just stale NBT and should
        // not override the live play state (e.g. when a block update packet arrives).
        boolean startupPause = this.getLevel() == null || !this.getLevel().isClientSide();
        if (isPlaying && !isPaused && startupPause && com.custommusic.JukeboxManager.isStartupPause()) {
            isPaused = true;
            pausedTicksPosition = ticksSinceStart;
            songStartRealTime = System.currentTimeMillis() - pausedTicksPosition * 50L;
            spinningStartTime = songStartRealTime;
        } else if (isPlaying && !isPaused) {
            // Otherwise keep the real-time playhead running from the saved position.
            songStartRealTime = System.currentTimeMillis() - ticksSinceStart * 50L;
            spinningStartTime = songStartRealTime;
        } else if (isPaused) {
            songStartRealTime = System.currentTimeMillis() - pausedTicksPosition * 50L;
            spinningStartTime = songStartRealTime;
        }

        String facingName = input.getStringOr("PlayerFacingOnPlace", "NORTH");
        try {
            playerFacingOnPlace = net.minecraft.core.Direction.valueOf(facingName);
        } catch (Exception e) {
            playerFacingOnPlace = net.minecraft.core.Direction.NORTH;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = this.saveWithoutMetadata(registries);
        tag.putBoolean("CustomMusicUpdate", true);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sendBlockUpdated() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void syncToClients() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        ChunkPos chunkPos = ChunkPos.containing(worldPosition);
        for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
            MusicStatePayload payload = new MusicStatePayload(
                worldPosition, isPlaying, isPaused, currentlyPlayingSlot, currentPage, ticksSinceStart, spinningStartTime, getVolumeForPlayer(player), songStartGameTime
            );
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    public void syncToPlayer(ServerPlayer player) {
        if (!isPlaying || isPaused || currentlyPlayingSlot < 0 || !(level instanceof ServerLevel)) return;
        // If resyncSoundForNearbyPlayers already synced this player (after the 40-tick
        // window), don't send a duplicate PLAY.
        if (playersHearingSound.contains(player.getUUID())) return;

        SoundInfo info = getSoundInfo(currentlyPlayingSlot);
        if (info == null) return;

        float range = info.sound().value().getRange(0.5F);
        double rangeSqr = range * range;
        if (player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) >= rangeSqr) {
            return;
        }

        String songDesc = getSongDescription(currentlyPlayingSlot);
        long sendGameTime = level.getGameTime();
        int offset = computeSyncedOffset(System.currentTimeMillis());
        float playerVolume = getVolumeForPlayer(player);
        LOGGER.info("syncToPlayer for {} at {} songStartRealTime={} sendGameTime={} offset={}",
            player.getName().getString(), worldPosition, songStartRealTime, sendGameTime, offset);
        MusicCommandPayload payload = new MusicCommandPayload(
            MusicCommandPayload.Command.PLAY,
            getMusicId(),
            info.sound().unwrapKey().orElseThrow().identifier(),
            worldPosition,
            offset,
            sendGameTime,
            playerVolume,
            1.0F,
            false
        );

        com.custommusic.network.NetworkHandler.sendMusicCommand(player, payload);
        if (songDesc != null) {
            sendNowPlayingMessage(player, songDesc);
        }
        com.custommusic.network.NetworkHandler.sendStateUpdate(this, player);
        playersHearingSound.add(player.getUUID());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.custommusic.jukebox_album");
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf extraData) {
        extraData.writeBlockPos(worldPosition);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new JukeboxAlbumMenu(containerId, playerInventory, this);
    }
}
