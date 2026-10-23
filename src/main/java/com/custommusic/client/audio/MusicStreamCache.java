package com.custommusic.client.audio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
public class MusicStreamCache {
    private record CachedAudio(ByteBuffer data, AudioFormat format) {}

    private static final Map<Identifier, CachedAudio> cache = new ConcurrentHashMap<>();
    private static final Map<Identifier, CompletableFuture<CachedAudio>> loading = new ConcurrentHashMap<>();

    /**
     * Starts a background decode of the given sound if one is not already running
     * or cached. Call this as soon as a PLAY command is received so the cache is
     * ready by the scheduled start time.
     */
    public static void ensureLoaded(Identifier sound) {
        loading.computeIfAbsent(sound, k -> load(k));
    }

    /**
     * Returns a future that completes with a fresh seekable AudioStream for the given sound.
     *
     * The full audio is decoded into memory first. This guarantees that every client
     * (host and joiners) can seek to the exact current position, avoiding the drift
     * that the non-seekable streaming fallback can introduce.
     */
    public static CompletableFuture<AudioStream> getStream(Identifier sound, long startTimestampMillis, int offsetTicks, SoundBufferLibrary soundBuffers, Sound soundInfo, boolean looping) {
        CachedAudio cached = cache.get(sound);
        if (cached != null) {
            ByteBuffer duplicate = cached.data.asReadOnlyBuffer().position(0);
            try {
                return CompletableFuture.completedFuture(new OffsetAudioStream(new ByteBufferAudioStream(duplicate, cached.format), startTimestampMillis, offsetTicks));
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }

        // Wait for the full decode so the stream is seekable. Non-seekable streaming
        // is not used because it cannot keep up with the wall-clock target used for
        // multiplayer synchronization.
        return loading.computeIfAbsent(sound, k -> load(k)).thenApply(done -> {
            ByteBuffer duplicate = done.data.asReadOnlyBuffer().position(0);
            try {
                return new OffsetAudioStream(new ByteBufferAudioStream(duplicate, done.format), startTimestampMillis, offsetTicks);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        });
    }

    private static CompletableFuture<CachedAudio> load(Identifier sound) {
        CachedAudio cached = cache.get(sound);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        CompletableFuture<CachedAudio> future = CompletableFuture.supplyAsync(() -> {
            try (
                InputStream is = Minecraft.getInstance().getResourceManager().open(sound);
                JOrbisAudioStream decoder = new JOrbisAudioStream(is)
            ) {
                ByteBuffer data = decoder.readAll();
                AudioFormat format = decoder.getFormat();
                CachedAudio audio = new CachedAudio(data, format);
                cache.put(sound, audio);
                return audio;
            } catch (IOException e) {
                throw new java.io.UncheckedIOException("Failed to decode music " + sound, e);
            }
        }, Util.nonCriticalIoPool());

        future.whenComplete((audio, ex) -> loading.remove(sound));
        return future;
    }

    public static void clear() {
        cache.clear();
        loading.clear();
    }
}
