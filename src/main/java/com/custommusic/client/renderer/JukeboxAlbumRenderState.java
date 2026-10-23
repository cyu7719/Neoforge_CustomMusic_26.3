package com.custommusic.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class JukeboxAlbumRenderState extends BlockEntityRenderState {
    public @Nullable ItemStackRenderState discToRender = null;
    public boolean shouldSpin = false;
    public float rotation = 0f;
    public Direction playerFacing = Direction.NORTH;
    public boolean isVanillaDisc = false;
}
