package com.custommusic.client.audio;

import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;

/**
 * An in-memory AudioStream backed by a fully decoded ByteBuffer.
 * This allows instantaneous seeking, which is required to keep custom music
 * synchronized across clients when players join or resume after a pause.
 */
public class ByteBufferAudioStream implements AudioStream, SeekableAudioStream {
    private final ByteBuffer data;
    private final AudioFormat format;

    public ByteBufferAudioStream(ByteBuffer data, AudioFormat format) {
        this.data = data;
        this.format = format;
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public ByteBuffer read(int expectedSize) {
        if (!data.hasRemaining()) {
            return null;
        }
        int len = Math.min(expectedSize, data.remaining());
        if (len <= 0) {
            return null;
        }
        ByteBuffer slice = data.slice(data.position(), len);
        data.position(data.position() + len);
        return slice;
    }

    @Override
    public void seekTo(long byteOffset) {
        long pos = Math.max(0, Math.min(byteOffset, data.limit()));
        data.position((int) pos);
    }

    @Override
    public long position() {
        return data.position();
    }

    @Override
    public long capacity() {
        return data.limit();
    }

    @Override
    public void close() {
        // Nothing to close; the underlying buffer is managed by MusicStreamCache.
    }
}
