package com.custommusic.network;

import com.custommusic.CustomMusicMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PortableButtonClickPayload(ButtonType buttonType, int slot) implements CustomPacketPayload {
    public static final Type<PortableButtonClickPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "portable_button_click"));

    public static final StreamCodec<FriendlyByteBuf, PortableButtonClickPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeEnum(payload.buttonType);
            buf.writeInt(payload.slot);
        },
        buf -> new PortableButtonClickPayload(
            buf.readEnum(ButtonType.class),
            buf.readInt()
        )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum ButtonType {
        PLAY_PAUSE,
        NEXT_SONG,
        PREVIOUS_SONG,
        NEXT_PAGE,
        PREVIOUS_PAGE,
        PLAY_SLOT,
        STOP_MUSIC,
        SHIFT_CLICK_SLOT
    }
}
