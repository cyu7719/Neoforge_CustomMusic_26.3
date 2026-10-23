package com.custommusic.client.audio;

import net.minecraft.client.sounds.AudioStream;

/**
 * Marker for AudioStream implementations that can jump to an arbitrary byte offset.
 */
public interface SeekableAudioStream extends AudioStream {
    void seekTo(long byteOffset) throws java.io.IOException;

    long position();

    long capacity();
}
