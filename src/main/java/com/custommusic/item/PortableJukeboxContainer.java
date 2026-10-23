package com.custommusic.item;

import com.custommusic.network.MusicCommandPayload;
import com.custommusic.network.PortableMusicStatePayload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class PortableJukeboxContainer implements Container {
    private final ItemStack stack;
    private final Level level;
    private NonNullList<ItemStack> items;
    private int maxPages = 1;
    private int currentPage = 0;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private int currentlyPlayingSlot = -1;
    private long songStartGameTime = 0L;
    private long spinningStartTime = 0L;
    private long songStartRealTime = 0L; // Wall-clock time the current song started at position 0
    private int ticksSinceStart = 0; // client-side display only, not persisted
    private int songLengthTicks = 0;
    private int pausedTicksPosition = 0;
    private float volume = 0.5F;
    private int pendingSlot = -1;
    private long pendingPlayGameTime = 0L;
    // No future scheduling delay. The startTimestamp is the current server time and
    // OffsetAudioStream seeks to the current song position on the first read.
    private static final int SYNC_DELAY_MS = 0;

    public PortableJukeboxContainer(ItemStack stack) {
        this.stack = stack;
        this.level = null; // Will be set when menu is opened
        loadFromStack();
    }
    
    public PortableJukeboxContainer(ItemStack stack, Level level) {
        this.stack = stack;
        this.level = level;
        loadFromStack();
    }
    
    public ItemStack getStack() {
        return stack;
    }
    
    private void loadFromStack() {
        // Load from custom data component (like backpack mods do)
        var data = stack.get(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        
        if (data != null) {
            // Load all data from the custom component
            this.maxPages = data.maxPages();
            int totalSlots = 54 * maxPages;
            this.items = data.getItemsAsList(totalSlots);
            this.currentPage = data.currentPage();
            this.isPlaying = data.isPlaying();
            this.isPaused = data.isPaused();
            this.currentlyPlayingSlot = data.currentlyPlayingSlot();
            this.songStartGameTime = data.songStartGameTime();
            this.spinningStartTime = data.spinningStartTime();
            this.songStartRealTime = data.songStartRealTime();
            this.ticksSinceStart = 0; // recomputed from game time when needed
            this.songLengthTicks = data.songLengthTicks();
            this.pausedTicksPosition = data.pausedTicksPosition();
            this.volume = data.volume();

            // Restore the real-time playhead only when none was stored: deriving
            // it from the game-time playhead here would make getRealElapsedTicks()
            // track game time, defeating the dual-clock end-of-song check whenever
            // the server is catching up ticks while this container is created.
            if (this.songStartRealTime <= 0) {
                int elapsed = getElapsedTicks();
                this.songStartRealTime = System.currentTimeMillis() - elapsed * 50L;
                if (this.isPaused) {
                    this.spinningStartTime = this.songStartRealTime;
                }
            }
        } else {
            // Create default data
            maxPages = 1;
            int totalSlots = 54 * maxPages;
            items = NonNullList.withSize(totalSlots, ItemStack.EMPTY);
            currentPage = 0;
            isPlaying = false;
            isPaused = false;
            currentlyPlayingSlot = -1;
            songStartGameTime = 0L;
            spinningStartTime = 0L;
            ticksSinceStart = 0;
            songLengthTicks = 0;
            pausedTicksPosition = 0;
            songStartRealTime = 0L;
            volume = 0.5F;
        }
    }
    
    @Override
    public void setChanged() {
        saveToStack();
        // Keep JukeboxManager's active data in sync so the item can still be found
        // after it is moved to the cursor or a different inventory slot.
        if (level != null && !level.isClientSide() && isPlaying && !isPaused) {
            ServerPlayer sp = findPlayerWithStack();
            if (sp != null) {
                com.custommusic.JukeboxManager.setActivePortableJukebox(sp, stack);
            }
        }
        // Send state update to client if we're on the server
        if (level != null && !level.isClientSide()) {
            sendStateUpdate();
        }
    }
    
    /**
     * Finds the ServerPlayer who owns this exact stack reference, checking the
     * full inventory (not just main hand / off hand). Uses reference equality
     * since we need the exact same ItemStack object from inventoryTick.
     */
    private ServerPlayer findPlayerWithStack() {
        if (level == null || level.isClientSide()) return null;
        for (net.minecraft.world.entity.player.Player p : level.players()) {
            if (!(p instanceof ServerPlayer sp)) continue;
            if (p.getMainHandItem() == stack || p.getOffhandItem() == stack) return sp;
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
                if (p.getInventory().getItem(i) == stack) return sp;
            }
        }
        return null;
    }

    private void sendStateUpdate() {
        int currentTicks = getTicksSinceStart();
        ServerPlayer sp = findPlayerWithStack();
        if (sp != null) {
            PacketDistributor.sendToPlayer(sp, new PortableMusicStatePayload(
                isPlaying, isPaused, currentlyPlayingSlot, currentPage, currentTicks, volume,
                songStartGameTime, songLengthTicks, pausedTicksPosition
            ));
        }
    }
    
    // Client-side state update method
    public void updateClientState(boolean isPlaying, boolean isPaused, int currentSlot, int currentPage, int ticksSinceStart, float volume, long songStartGameTime, int songLengthTicks, int pausedTicksPosition) {
        this.isPlaying = isPlaying;
        this.isPaused = isPaused;
        this.currentlyPlayingSlot = currentSlot;
        this.currentPage = currentPage;
        this.ticksSinceStart = ticksSinceStart;
        this.volume = volume;
        this.songStartGameTime = songStartGameTime;
        this.songLengthTicks = songLengthTicks;
        this.pausedTicksPosition = pausedTicksPosition;
    }
    
    public void saveToStack() {
        if (level == null) {
            return;
        }
        
        com.custommusic.component.PortableJukeboxData data = new com.custommusic.component.PortableJukeboxData(
            net.minecraft.world.item.component.ItemContainerContents.fromItems(items),
            maxPages,
            currentPage,
            isPlaying,
            isPaused,
            currentlyPlayingSlot,
            songStartGameTime,
            songLengthTicks,
            pausedTicksPosition,
            spinningStartTime,
            songStartRealTime,
            volume
        );
        
        stack.set(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get(), data);
    }
    
    public void saveToStack(ItemStack targetStack) {
        if (level == null) {
            return;
        }
        
        com.custommusic.component.PortableJukeboxData data = new com.custommusic.component.PortableJukeboxData(
            net.minecraft.world.item.component.ItemContainerContents.fromItems(items),
            maxPages,
            currentPage,
            isPlaying,
            isPaused,
            currentlyPlayingSlot,
            songStartGameTime,
            songLengthTicks,
            pausedTicksPosition,
            spinningStartTime,
            songStartRealTime,
            volume
        );
        
        targetStack.set(com.custommusic.init.ModDataComponents.PORTABLE_JUKEBOX_DATA.get(), data);
    }
    
    @Override
    public int getContainerSize() {
        return items.size();
    }
    
    @Override
    public boolean isEmpty() {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
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
        ItemStack result = net.minecraft.world.ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            // Check if the removed item was the currently playing disc
            if (isPlaying && slot == currentlyPlayingSlot) {
                stopMusic();
            } else {
                setChanged();
            }
        }
        return result;
    }
    
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return net.minecraft.world.ContainerHelper.takeItem(items, slot);
    }
    
    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true; // Portable jukebox is always valid for the holder
    }
    
    @Override
    public void clearContent() {
        items.clear();
        saveToStack();
    }
    
    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.getItem() instanceof com.custommusic.item.CustomMusicDiscItem || 
               stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE);
    }
    
    // Jukebox-specific methods
    public int getMaxPages() {
        return maxPages;
    }
    
    public void addPage() {
        maxPages++;
        NonNullList<ItemStack> newItems = NonNullList.withSize(54 * maxPages, ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            newItems.set(i, items.get(i));
        }
        items = newItems;
        setChanged();
    }
    
    public int getCurrentPage() {
        return currentPage;
    }

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(float volume) {
        this.volume = net.minecraft.util.Mth.clamp(volume, 0.0F, 1.0F);
        setChanged();

        if (level != null && !level.isClientSide() && isPlaying && !isPaused) {
            ServerPlayer sp = findPlayerWithStack();
            if (sp != null) {
                sendVolumeToPlayer(sp);
            }
        }
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

    public long getSongStartGameTime() {
        return songStartGameTime;
    }

    public int getSongLengthTicks() {
        return songLengthTicks;
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;
        
        if (pendingSlot != -1 && level.getGameTime() >= pendingPlayGameTime) {
            int slot = pendingSlot;
            pendingSlot = -1;
            pendingPlayGameTime = 0L;
            playSlot(slot);
            return;
        }
        
        // Check if song has finished. Both the game-time and the wall-clock
        // playheads must be past the end; checking both prevents premature
        // advancement when the server catches up ticks after a lag spike or the
        // wall clock is ahead of the actual audible position.
        if (isPlaying && !isPaused && songLengthTicks > 0) {
            if (getElapsedTicks() >= songLengthTicks && getRealElapsedTicks() >= songLengthTicks) {
                autoNextSong();
            }
        }
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
    
    public void playMusic() {
        // Find the first disc on current page
        int startSlot = currentPage * 54;
        for (int i = 0; i < 54; i++) {
            int slot = startSlot + i;
            if (slot < items.size()) {
                ItemStack itemStack = items.get(slot);
                if (!itemStack.isEmpty() && canPlaceItem(slot, itemStack)) {
                    playSlot(slot, false);
                    return;
                }
            }
        }
    }
    
    public void playSlot(int slot) {
        playSlot(slot, false);
    }

    public void playSlot(int slot, boolean resume) {
        if (slot < 0 || slot >= items.size()) return;

        ItemStack itemStack = items.get(slot);
        if (itemStack.isEmpty()) return;

        // Save resume position before stopMusic resets it
        int resumeFrom = resume ? Math.max(0, pausedTicksPosition) : 0;
        stopMusic();
        pendingSlot = -1;
        pendingPlayGameTime = 0L;

        isPlaying = true;
        isPaused = false;
        currentlyPlayingSlot = slot;
        pausedTicksPosition = 0;

        // Determine length first so we can clamp the resume position
        if (itemStack.getItem() instanceof com.custommusic.item.CustomMusicDiscItem customDisc) {
            songLengthTicks = customDisc.getLengthInTicks();
        } else if (itemStack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
            var holderOpt = net.minecraft.world.item.JukeboxSong.fromStack(itemStack);
            var song = (holderOpt.isPresent() && holderOpt.get().isBound()) ? holderOpt.get().value() : null;
            if (song != null) {
                songLengthTicks = (int)(song.lengthInSeconds() * 20);
            }
        }

        // Clamp resume position to the actual audio length (prevents stale data from skipping to EOF)
        if (resume && songLengthTicks > 0) {
            resumeFrom = Math.max(0, Math.min(resumeFrom, songLengthTicks - 1));
        } else if (resume) {
            resumeFrom = 0;
        }
        ticksSinceStart = resume ? resumeFrom : 0;
        songStartGameTime = (level != null) ? level.getGameTime() - ticksSinceStart : 0L;
        long scheduledStart = System.currentTimeMillis();
        songStartRealTime = scheduledStart - ticksSinceStart * 50L;
        spinningStartTime = scheduledStart - ticksSinceStart * 50L;

        // Send the sound
        if (itemStack.getItem() instanceof com.custommusic.item.CustomMusicDiscItem customDisc) {
            if (level != null && !level.isClientSide()) {
                ServerPlayer sp = findPlayerWithStack();
                if (sp != null) {
                    sendSoundToPlayer(sp, customDisc.getSound());
                }
            }
        } else if (itemStack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
            var holderOpt = net.minecraft.world.item.JukeboxSong.fromStack(itemStack);
            var song = (holderOpt.isPresent() && holderOpt.get().isBound()) ? holderOpt.get().value() : null;
            if (song != null) {
                if (level != null && !level.isClientSide()) {
                    ServerPlayer sp = findPlayerWithStack();
                    if (sp != null) {
                        sendSoundToPlayer(sp, song.soundEvent());
                    }
                }
            }
        }

        if (level != null && !level.isClientSide()) {
            ServerPlayer sp = findPlayerWithStack();
            if (sp != null) {
                sendNowPlayingMessage(sp, slot);
                // Active entry is updated by setChanged after the data is saved.
            }
        }

        setChanged();
    }

    private String getMusicId(ServerPlayer sp) {
        return "portable:" + sp.getUUID().toString();
    }

    private void sendSoundToPlayer(ServerPlayer sp, net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound) {
        if (sp == null) return;
        // Anchor sync to server game time (ticks). The client's game time is kept in
        // sync by vanilla, so it can measure packet transit without wall-clock skew.
        long sendGameTime = sp.level().getGameTime();
        int offset = songStartRealTime > 0
            ? (int) ((System.currentTimeMillis() - songStartRealTime) / 50L)
            : 0;
        if (songLengthTicks > 0) {
            offset = Math.max(0, Math.min(offset, songLengthTicks - 1));
        }
        com.custommusic.network.MusicCommandPayload payload = new com.custommusic.network.MusicCommandPayload(
            com.custommusic.network.MusicCommandPayload.Command.PLAY,
            getMusicId(sp),
            sound.unwrapKey().orElseThrow().identifier(),
            sp.blockPosition(),
            offset,
            sendGameTime,
            this.volume,
            1.0F,
            true
        );
        com.custommusic.network.NetworkHandler.sendMusicCommand(sp, payload);
    }

    private void stopSoundForPlayer(ServerPlayer sp) {
        if (sp == null) return;
        com.custommusic.network.MusicCommandPayload payload = new com.custommusic.network.MusicCommandPayload(
            com.custommusic.network.MusicCommandPayload.Command.STOP,
            getMusicId(sp),
            Identifier.withDefaultNamespace("empty"),
            sp.blockPosition(),
            0,
            0L,
            0.0F,
            0.0F,
            true
        );
        com.custommusic.network.NetworkHandler.sendMusicCommand(sp, payload);
    }

    private void sendVolumeToPlayer(ServerPlayer sp) {
        if (sp == null) return;
        com.custommusic.network.MusicCommandPayload payload = new com.custommusic.network.MusicCommandPayload(
            com.custommusic.network.MusicCommandPayload.Command.SET_VOLUME,
            getMusicId(sp),
            Identifier.withDefaultNamespace("empty"),
            sp.blockPosition(),
            0,
            0L,
            this.volume,
            1.0F,
            true
        );
        com.custommusic.network.NetworkHandler.sendMusicCommand(sp, payload);
    }

    public void resyncSoundForPlayer(ServerPlayer player) {
        if (level == null || level.isClientSide() || !isPlaying || isPaused || currentlyPlayingSlot < 0) return;

        ItemStack itemStack = items.get(currentlyPlayingSlot);
        if (itemStack.isEmpty()) return;

        if (itemStack.getItem() instanceof com.custommusic.item.CustomMusicDiscItem customDisc) {
            sendSoundToPlayer(player, customDisc.getSound());
            sendNowPlayingMessage(player, currentlyPlayingSlot);
        } else if (itemStack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
            var holderOpt = net.minecraft.world.item.JukeboxSong.fromStack(itemStack);
            var song = (holderOpt.isPresent() && holderOpt.get().isBound()) ? holderOpt.get().value() : null;
            if (song != null) {
                sendSoundToPlayer(player, song.soundEvent());
                sendNowPlayingMessage(player, currentlyPlayingSlot);
            }
        }
    }

    private void sendNowPlayingMessage(ServerPlayer sp, int slot) {
        if (sp == null) return;
        ItemStack itemStack = items.get(slot);
        if (itemStack.isEmpty()) return;

        if (itemStack.getItem() instanceof com.custommusic.item.CustomMusicDiscItem customDisc) {
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.registryAccess().lookup(net.minecraft.core.registries.Registries.JUKEBOX_SONG)
                    .flatMap(registry -> registry.get(customDisc.getJukeboxSongKey()))
                    .ifPresent(song -> {
                        String songDesc = song.value().description().getString();
                        com.custommusic.network.NetworkHandler.sendNowPlaying(net.minecraft.network.chat.Component.literal(songDesc), sp);
                    });
            }
        } else if (itemStack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
            var holderOpt = net.minecraft.world.item.JukeboxSong.fromStack(itemStack);
            var song = (holderOpt.isPresent() && holderOpt.get().isBound()) ? holderOpt.get().value() : null;
            if (song != null) {
                String songDesc = song.description().getString();
                com.custommusic.network.NetworkHandler.sendNowPlaying(net.minecraft.network.chat.Component.literal(songDesc), sp);
            }
        }
    }
    
    public void pauseMusic() {
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
        pausedTicksPosition = position;
        ticksSinceStart = position;
        songStartRealTime = System.currentTimeMillis() - pausedTicksPosition * 50L;
        
        // Stop the sound when pausing
        if (level != null && !level.isClientSide()) {
            ServerPlayer sp = findPlayerWithStack();
            if (sp != null) {
                stopSoundForPlayer(sp);
                com.custommusic.JukeboxManager.clearActivePortableJukebox(sp);
            }
        }

        setChanged();
    }

    public void pauseMusicForPlayer(ServerPlayer sp) {
        pauseMusic();
        if (sp != null) stopSoundForPlayer(sp);
    }

    // Seeks the current song to the given tick position. Replays the sound from
    // that offset when playing; when paused it just moves the stored position.
    public void seekTo(int positionTicks) {
        if (!isPlaying || currentlyPlayingSlot < 0) return;
        int clamped = Math.max(0, positionTicks);
        if (songLengthTicks > 0) {
            clamped = Math.min(clamped, songLengthTicks - 1);
        }
        if (isPaused) {
            pausedTicksPosition = clamped;
            ticksSinceStart = clamped;
            songStartRealTime = System.currentTimeMillis() - clamped * 50L;
            spinningStartTime = songStartRealTime;
            setChanged();
        } else {
            pausedTicksPosition = clamped;
            playSlot(currentlyPlayingSlot, true);
        }
    }

    public void resumeMusic() {
        if (!isPlaying || !isPaused) return;
        
        isPaused = false;
        
        // Use playSlot with resume flag to handle sound, then send "Now playing" message
        if (currentlyPlayingSlot >= 0) {
            playSlot(currentlyPlayingSlot, true);
        }
        
        setChanged();
    }
    
    public void stopMusic() {
        pendingSlot = -1;
        pendingPlayGameTime = 0L;
        // Stop any playing music
        if (level != null && !level.isClientSide()) {
            ServerPlayer sp = findPlayerWithStack();
            if (sp != null) {
                stopSoundForPlayer(sp);
                com.custommusic.JukeboxManager.clearActivePortableJukebox(sp);
            }
        }

        isPlaying = false;
        isPaused = false;
        currentlyPlayingSlot = -1;
        ticksSinceStart = 0;
        pausedTicksPosition = 0;
        songLengthTicks = 0;
        songStartRealTime = 0L;
        setChanged();
    }
    
    public void nextSong() {
        if (!isPlaying) return; // no song is playing, do nothing
        int nextSlot = findNextSongSlot(currentlyPlayingSlot);
        if (nextSlot != -1) {
            int newPage = nextSlot / 54;
            if (newPage != currentPage) {
                currentPage = newPage;
            }
            pendingSlot = nextSlot;
            pendingPlayGameTime = (level != null) ? level.getGameTime() + 2 : 0L;
            return;
        }

        // No songs found at all - stop playing
        stopMusic();
    }

    // Advances immediately to the next disc (used when the current song ends).
    // Unlike nextSong() this does not go through the pending-slot delay, which is
    // only consumed while the GUI is open, so it is safe to call from
    // inventoryTick when the menu is closed.
    public void autoNextSong() {
        if (!isPlaying) return;
        int nextSlot = findNextSongSlot(currentlyPlayingSlot);
        if (nextSlot != -1) {
            currentPage = nextSlot / 54;
            playSlot(nextSlot);
        } else {
            stopMusic();
        }
    }

    private int findNextSongSlot(int fromSlot) {
        int totalSlots = items.size();

        // Start searching from the slot after currently playing
        for (int i = fromSlot + 1; i < totalSlots; i++) {
            if (!items.get(i).isEmpty() && canPlaceItem(i, items.get(i))) {
                return i;
            }
        }

        // No song found after current slot, wrap around to beginning
        for (int i = 0; i <= fromSlot; i++) {
            if (!items.get(i).isEmpty() && canPlaceItem(i, items.get(i))) {
                return i;
            }
        }
        return -1;
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
        if (isPlaying && currentlyPlayingSlot != -1 && getTicksSinceStart() >= 60) {
            // Restart the current song
            pendingSlot = currentlyPlayingSlot;
            pendingPlayGameTime = level.getGameTime() + 2;
            return;
        }

        // Otherwise (less than 3 seconds), go to previous song
        int prevSlot = findPreviousSongCrossPage(currentlyPlayingSlot);
        if (prevSlot != -1) {
            currentPage = prevSlot / 54;
            pendingSlot = prevSlot;
            pendingPlayGameTime = level.getGameTime() + 2;
        } else {
            stopMusic();
        }
    }
    
    private int findPreviousSongCrossPage(int currentSlot) {
        // If we're at the first slot (0), there's no previous song
        if (currentSlot <= 0) {
            return -1; // no previous song available
        }
        
        int slot = currentSlot - 1;
        while (slot >= 0) {
            ItemStack stack = items.get(slot);
            if (stack.getItem() instanceof com.custommusic.item.CustomMusicDiscItem || stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
                return slot;
            }
            slot--;
        }
        return -1; // no songs found
    }
    
    private int findFirstSongOnPage(int page) {
        int startSlot = page * 54;
        int endSlot = Math.min(startSlot + 54, items.size());
        
        for (int i = startSlot; i < endSlot; i++) {
            ItemStack stack = items.get(i);
            if (stack.getItem() instanceof com.custommusic.item.CustomMusicDiscItem || stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)) {
                return i;
            }
        }
        return -1;
    }
    
    public BlockPos getBlockPos() {
        // Return a dummy position for portable jukebox
        return BlockPos.ZERO;
    }
    
    public Level getLevel() {
        return level;
    }
}
