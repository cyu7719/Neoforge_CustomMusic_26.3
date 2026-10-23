package com.custommusic.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record NowPlayingPayload(String songDescriptionJson) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<NowPlayingPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("custommusic:now_playing"));
    
    public static final StreamCodec<FriendlyByteBuf, NowPlayingPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeUtf(payload.songDescriptionJson()),
        (buf) -> new NowPlayingPayload(buf.readUtf())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
