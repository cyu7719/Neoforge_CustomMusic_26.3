package com.custommusic.network;

import com.custommusic.CustomMusicMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JukeboxVolumePayload(BlockPos pos, float volume) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JukeboxVolumePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "jukebox_volume"));

    public static final StreamCodec<FriendlyByteBuf, JukeboxVolumePayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeBlockPos(payload.pos());
            buf.writeFloat(payload.volume());
        },
        (buf) -> new JukeboxVolumePayload(
            buf.readBlockPos(),
            buf.readFloat()
        )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
