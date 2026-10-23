package com.custommusic.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MusicStatePayload(BlockPos pos, boolean isPlaying, boolean isPaused, int currentSlot, int currentPage, int ticksSinceStart, long spinningStartTime, float volume, long songStartGameTime) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MusicStatePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("custommusic:music_state"));

    public static final StreamCodec<FriendlyByteBuf, MusicStatePayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            BlockPos.STREAM_CODEC.encode(buf, payload.pos());
            buf.writeBoolean(payload.isPlaying());
            buf.writeBoolean(payload.isPaused());
            buf.writeVarInt(payload.currentSlot());
            buf.writeVarInt(payload.currentPage());
            buf.writeVarInt(payload.ticksSinceStart());
            buf.writeLong(payload.spinningStartTime());
            buf.writeFloat(payload.volume());
            buf.writeLong(payload.songStartGameTime());
        },
        (buf) -> new MusicStatePayload(
            BlockPos.STREAM_CODEC.decode(buf),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readLong(),
            buf.readFloat(),
            buf.readLong()
        )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
