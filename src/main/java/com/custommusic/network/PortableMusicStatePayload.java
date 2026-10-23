package com.custommusic.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PortableMusicStatePayload(boolean isPlaying, boolean isPaused, int currentSlot, int currentPage, int ticksSinceStart, float volume, long songStartGameTime, int songLengthTicks, int pausedTicksPosition) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PortableMusicStatePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("custommusic:portable_music_state"));

    public static final StreamCodec<FriendlyByteBuf, PortableMusicStatePayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeBoolean(payload.isPlaying());
            buf.writeBoolean(payload.isPaused());
            buf.writeVarInt(payload.currentSlot());
            buf.writeVarInt(payload.currentPage());
            buf.writeVarInt(payload.ticksSinceStart());
            buf.writeFloat(payload.volume());
            buf.writeLong(payload.songStartGameTime());
            buf.writeVarInt(payload.songLengthTicks());
            buf.writeVarInt(payload.pausedTicksPosition());
        },
        (buf) -> new PortableMusicStatePayload(
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readFloat(),
            buf.readLong(),
            buf.readVarInt(),
            buf.readVarInt()
        )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
