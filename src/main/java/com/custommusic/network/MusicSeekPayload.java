package com.custommusic.network;

import com.custommusic.CustomMusicMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Client -> server request to seek the current song to a position (in ticks).
// When portable is true the seek targets the player's active portable jukebox
// and pos is ignored; otherwise it targets the jukebox album block at pos.
public record MusicSeekPayload(boolean portable, BlockPos pos, int positionTicks) implements CustomPacketPayload {
    public static final Type<MusicSeekPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_seek"));

    public static final StreamCodec<FriendlyByteBuf, MusicSeekPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeBoolean(payload.portable);
            buf.writeBlockPos(payload.pos);
            buf.writeVarInt(payload.positionTicks);
        },
        buf -> new MusicSeekPayload(
            buf.readBoolean(),
            buf.readBlockPos(),
            buf.readVarInt()
        )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
