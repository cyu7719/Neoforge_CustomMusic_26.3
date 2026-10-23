package com.custommusic.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Renders the current music disc on top of the portable jukebox item.
 * The disc is rendered in FIXED display context and spun around its Z axis while music is playing.
 */
public class PortableJukeboxDiscSpecialRenderer implements SpecialModelRenderer<PortableJukeboxDiscSpecialRenderer.DiscArgument> {
    public static final PortableJukeboxDiscSpecialRenderer INSTANCE = new PortableJukeboxDiscSpecialRenderer();

    @Override
    public void submit(DiscArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (argument == null || argument.disc.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, argument.yOffset, 0.5f);
        poseStack.scale(0.58f, 0.58f, 0.58f);
        poseStack.rotateDegrees(Axis.XP, 90f);
        poseStack.rotateDegrees(Axis.ZP, 180f);

        poseStack.rotateDegrees(Axis.ZP, argument.rotation);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            ItemModelResolver resolver = minecraft.getItemModelResolver();
            ItemStackRenderState discState = new ItemStackRenderState();
            resolver.updateForTopItem(discState, argument.disc, ItemDisplayContext.FIXED, argument.level, argument.owner, argument.seed);
            discState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, 0);
        }

        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // The base model provides the main bounds; this just gives a rough hint for the disc above.
        output.accept(new Vector3f(0.3f, 1.3f, 0.3f));
        output.accept(new Vector3f(-0.3f, 1.3f, 0.3f));
        output.accept(new Vector3f(0.3f, 1.3f, -0.3f));
        output.accept(new Vector3f(-0.3f, 1.3f, -0.3f));
    }

    @Override
    public DiscArgument extractArgument(ItemStack stack) {
        return null;
    }

    public record DiscArgument(ItemStack disc, float rotation, ClientLevel level, ItemOwner owner, int seed, float yOffset) {
    }
}
