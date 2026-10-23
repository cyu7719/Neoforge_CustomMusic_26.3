package com.custommusic.network;

import com.custommusic.CustomMusicMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PortableVolumePayload(float volume) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PortableVolumePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "portable_volume"));

    public static final StreamCodec<FriendlyByteBuf, PortableVolumePayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeFloat(payload.volume());
        },
        (buf) -> new PortableVolumePayload(
            buf.readFloat()
        )
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
