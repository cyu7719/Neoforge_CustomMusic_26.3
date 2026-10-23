package com.custommusic.client.renderer;

import com.custommusic.blockentity.JukeboxAlbumBlockEntity;
import com.custommusic.item.CustomMusicDiscItem;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class JukeboxAlbumBlockEntityRenderer implements BlockEntityRenderer<JukeboxAlbumBlockEntity, JukeboxAlbumRenderState> {
    private final ItemModelResolver itemModelResolver;

    // Per-block accumulated pause offset so the disc freezes at its current angle
    // when the game is paused or the window loses focus, then resumes smoothly.
    private final Map<BlockPos, Long> spinPauseOffset = new HashMap<>();
    private final Map<BlockPos, Long> lastSpinUpdate = new HashMap<>();
    private final Map<BlockPos, Long> lastSpinningStart = new HashMap<>();

    public JukeboxAlbumBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public JukeboxAlbumRenderState createRenderState() {
        return new JukeboxAlbumRenderState();
    }

    @Override
    public void extractRenderState(JukeboxAlbumBlockEntity blockEntity, JukeboxAlbumRenderState state, float partialTick, Vec3 cameraPos, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);
        state.discToRender = null;
        state.isVanillaDisc = false;

        ItemStack toRender = ItemStack.EMPTY;
        if (blockEntity.isPlaying()) {
            int currentSlot = blockEntity.getCurrentlyPlayingSlot();
            if (currentSlot >= 0 && currentSlot < blockEntity.getContainerSize()) {
                ItemStack currentDisc = blockEntity.getItem(currentSlot);
                if (!currentDisc.isEmpty() && currentDisc.getItem() instanceof CustomMusicDiscItem) {
                    toRender = currentDisc;
                }
            }
        }

        if (toRender.isEmpty()) {
            int currentPage = blockEntity.getCurrentPage();
            int startSlot = currentPage * 54;
            int endSlot = Math.min(startSlot + 54, blockEntity.getContainerSize());
            for (int i = startSlot; i < endSlot; i++) {
                ItemStack stack = blockEntity.getItem(i);
                if (!stack.isEmpty() && (stack.getItem() instanceof CustomMusicDiscItem
                        || stack.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE))) {
                    toRender = stack;
                    break;
                }
            }
        }

        if (!toRender.isEmpty()) {
            ItemStackRenderState itemState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(itemState, toRender, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
            state.discToRender = itemState;
            state.isVanillaDisc = toRender.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE)
                    && !(toRender.getItem() instanceof CustomMusicDiscItem);
        }

        Minecraft mc = Minecraft.getInstance();
        boolean clientFrozen = mc.isPaused() || !mc.isWindowActive();
        boolean isPlaying = blockEntity.isPlaying();
        boolean isPaused = blockEntity.isPaused();
        state.shouldSpin = isPlaying && !isPaused && !clientFrozen;

        BlockPos pos = blockEntity.getBlockPos();
        long now = System.currentTimeMillis();
        long start = blockEntity.getSpinningStartTime();

        if (!isPlaying) {
            // Idle: no spin and clear cached freeze state for this block.
            state.rotation = 0f;
            spinPauseOffset.remove(pos);
            lastSpinUpdate.remove(pos);
            lastSpinningStart.remove(pos);
        } else {
            // Detect a new song (or a freshly placed block) and reset the offset.
            Long previousStart = lastSpinningStart.put(pos, start);
            if (previousStart == null || previousStart != start) {
                spinPauseOffset.put(pos, 0L);
                lastSpinUpdate.put(pos, now);
            }

            long offset = spinPauseOffset.getOrDefault(pos, 0L);
            long lastUpdate = lastSpinUpdate.getOrDefault(pos, now);

            if (!state.shouldSpin) {
                // Accumulate time spent not spinning (paused, game paused, or window not active).
                offset += now - lastUpdate;
                spinPauseOffset.put(pos, offset);
            }
            lastSpinUpdate.put(pos, now);

            long elapsed = Math.max(0, now - start - offset);
            state.rotation = (elapsed % 36000) / 25.0f;
        }
        
        state.playerFacing = blockEntity.getPlayerFacingOnPlace();
    }

    @Override
    public void submit(JukeboxAlbumRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.discToRender == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, 1.01f, 0.5f);
        poseStack.scale(0.58f, 0.58f, 0.58f);

        float rotationDegrees = switch (state.playerFacing) {
            case NORTH -> 0f;
            case SOUTH -> 180f;
            case EAST -> 270f;
            case WEST -> 90f;
            default -> 0f;
        };
        poseStack.rotateDegrees(com.mojang.math.Axis.YP, rotationDegrees);
        poseStack.rotateDegrees(com.mojang.math.Axis.XP, 90f);
        poseStack.rotateDegrees(com.mojang.math.Axis.ZP, 180f);

        poseStack.rotateDegrees(com.mojang.math.Axis.ZP, state.rotation);

        if (state.isVanillaDisc) {
            poseStack.translate(0.03f, 0, 0);
        }

        state.discToRender.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
