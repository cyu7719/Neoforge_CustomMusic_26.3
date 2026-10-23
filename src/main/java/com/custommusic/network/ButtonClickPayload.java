package com.custommusic.network;

import com.custommusic.CustomMusicMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ButtonClickPayload(BlockPos pos, ButtonType buttonType, int slot) implements CustomPacketPayload {
    public static final Type<ButtonClickPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "button_click"));

    public static final StreamCodec<FriendlyByteBuf, ButtonClickPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeBlockPos(payload.pos);
            buf.writeEnum(payload.buttonType);
            buf.writeInt(payload.slot);
        },
        buf -> new ButtonClickPayload(
            buf.readBlockPos(),
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
