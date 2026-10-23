package com.custommusic.network;

import com.custommusic.CustomMusicMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JukeboxPickPayload(int slotIndex) implements CustomPacketPayload {
    public static final Type<JukeboxPickPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "jukebox_pick"));

    public static final StreamCodec<FriendlyByteBuf, JukeboxPickPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeVarInt(payload.slotIndex),
        buf -> new JukeboxPickPayload(buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
