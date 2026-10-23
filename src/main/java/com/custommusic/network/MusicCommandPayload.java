package com.custommusic.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MusicCommandPayload(
    MusicCommandPayload.Command command,
    String musicId,
    Identifier sound,
    BlockPos pos,
    int offsetTicks,
    long startTimestamp,
    float volume,
    float pitch,
    boolean relative
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MusicCommandPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("custommusic", "music_command"));

    public static final StreamCodec<FriendlyByteBuf, MusicCommandPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeEnum(payload.command());
            buf.writeUtf(payload.musicId());
            buf.writeIdentifier(payload.sound());
            buf.writeBlockPos(payload.pos());
            buf.writeVarInt(payload.offsetTicks());
            buf.writeLong(payload.startTimestamp());
            buf.writeFloat(payload.volume());
            buf.writeFloat(payload.pitch());
            buf.writeBoolean(payload.relative());
        },
        (buf) -> new MusicCommandPayload(
            buf.readEnum(Command.class),
            buf.readUtf(),
            buf.readIdentifier(),
            buf.readBlockPos(),
            buf.readVarInt(),
            buf.readLong(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readBoolean()
        )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Command {
        PLAY, STOP, SET_VOLUME
    }
}
