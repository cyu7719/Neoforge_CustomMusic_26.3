package com.custommusic.client.audio;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jspecify.annotations.Nullable;

public class OffsetAudioStream implements AudioStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(OffsetAudioStream.class);
    private final AudioStream source;
    private final AudioFormat format;
    private final long bytesPerSecond;
    private final int bytesPerSample;
    private final long startTimestampMillis;
    private final int offsetTicks;
    private final boolean seekable;
    private long targetBytes;
    private long totalBytesProcessed;
    private @Nullable ByteBuffer pendingBuffer;
    private boolean closed = false;
    private boolean firstRead = true;
    private int readCount;

    public OffsetAudioStream(AudioStream source, long startTimestampMillis, int offsetTicks) throws IOException {
        this.source = source;
        this.format = source.getFormat();
        this.bytesPerSecond = (long)(format.getSampleRate() * format.getChannels() * format.getSampleSizeInBits() / 8.0f);
        this.bytesPerSample = format.getChannels() * (format.getSampleSizeInBits() / 8);
        this.startTimestampMillis = startTimestampMillis;
        this.offsetTicks = offsetTicks;
        this.seekable = source instanceof SeekableAudioStream;
    }

    private void updateTarget() {
        float offsetSeconds = offsetTicks / 20.0f;
        if (startTimestampMillis > 0L) {
            offsetSeconds += Math.max(0.0f, (System.currentTimeMillis() - startTimestampMillis) / 1000.0f);
        }
        long target = (long)(offsetSeconds * bytesPerSecond);
        target = (target / bytesPerSample) * bytesPerSample;
        this.targetBytes = Math.max(0, target);
    }

    private void adjustToTarget() throws IOException {
        if (seekable) {
            SeekableAudioStream seekableSource = (SeekableAudioStream) source;
            seekableSource.seekTo(targetBytes);
            totalBytesProcessed = seekableSource.position();
            // A previously pending fallback buffer is no longer valid after a seek.
            pendingBuffer = null;
            return;
        }

        // Fallback for non-seekable streaming sources: read and discard until we reach
        // the target. This is slow but retained as a safety net.
        while (totalBytesProcessed < targetBytes) {
            long remaining = targetBytes - totalBytesProcessed;
            int request = (int) Math.min(remaining, 1_048_576);
            ByteBuffer buf = source.read(request);
            if (buf == null || !buf.hasRemaining()) {
                totalBytesProcessed = targetBytes;
                return;
            }
            int available = buf.remaining();
            if (available <= remaining) {
                totalBytesProcessed += available;
            } else {
                int skipWithinBuffer = (int) remaining;
                buf.position(buf.position() + skipWithinBuffer);
                pendingBuffer = buf.slice();
                totalBytesProcessed += skipWithinBuffer;
                return;
            }
            updateTarget();
        }
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public ByteBuffer read(int expectedSize) throws IOException {
        if (closed) return null;

        readCount++;
        if (firstRead) {
            firstRead = false;
            updateTarget();
            adjustToTarget();
            LOGGER.info("OffsetAudioStream firstRead startTimestamp={} offset={} targetSeconds={} targetBytes={} totalBytes={}",
                startTimestampMillis, offsetTicks, String.format("%.3f", targetBytes / (float) bytesPerSecond),
                targetBytes, totalBytesProcessed);
        }

        // If a previous fallback skip left a partial buffer, return it.
        if (pendingBuffer != null && pendingBuffer.hasRemaining()) {
            ByteBuffer result = pendingBuffer;
            pendingBuffer = null;
            totalBytesProcessed += result.remaining();
            return result;
        }

        // Ensure we are still caught up to the real-time target before returning live audio.
        updateTarget();
        if (totalBytesProcessed < targetBytes) {
            adjustToTarget();
            if (pendingBuffer != null && pendingBuffer.hasRemaining()) {
                ByteBuffer result = pendingBuffer;
                pendingBuffer = null;
                totalBytesProcessed += result.remaining();
                return result;
            }
        }

        ByteBuffer result = source.read(expectedSize);
        if (result != null) {
            totalBytesProcessed += result.remaining();
        }
        if (readCount % 100 == 0) {
            LOGGER.info("OffsetAudioStream read #{} startTimestamp={} offset={} targetSeconds={} totalBytes={}",
                readCount, startTimestampMillis, offsetTicks, String.format("%.3f", targetBytes / (float) bytesPerSecond), totalBytesProcessed);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        source.close();
    }
}
