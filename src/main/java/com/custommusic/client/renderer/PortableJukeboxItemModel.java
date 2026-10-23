package com.custommusic.client.renderer;

import com.custommusic.component.PortableJukeboxData;
import com.custommusic.init.ModDataComponents;
import com.custommusic.item.CustomMusicDiscItem;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Item model for the portable jukebox album.
 * Renders the base jukebox model and then places the current disc on top of it,
 * spinning while music is playing and static when paused or stopped.
 */
public class PortableJukeboxItemModel implements ItemModel {
    private final ItemModel baseModel;
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;

    // Per-item pause offset so the disc freezes at its current angle when paused,
    // then resumes smoothly — mirroring JukeboxAlbumBlockEntityRenderer.
    private static final Map<Integer, Long> spinPauseOffset = new HashMap<>();
    private static final Map<Integer, Long> lastSpinUpdate = new HashMap<>();
    private static final Map<Integer, Long> lastSpinningStart = new HashMap<>();

    public PortableJukeboxItemModel(ItemModel baseModel, ModelRenderProperties properties, Matrix4fc transformation) {
        this.baseModel = baseModel;
        this.properties = properties;
        this.transformation = transformation;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        if (item.isEmpty()) {
            return;
        }

        // Render the base portable jukebox model.
        this.baseModel.update(output, item, resolver, displayContext, level, owner, seed);

        // Render the current disc on top.
        ItemStack disc = findDiscToRender(item);
        if (disc.isEmpty()) {
            return;
        }

        PortableJukeboxData data = item.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        Minecraft mc = Minecraft.getInstance();
        boolean clientFrozen = mc.isPaused() || !mc.isWindowActive();
        boolean isActuallyPlaying = data != null && data.isPlaying() && !data.isPaused()
            && !clientFrozen && displayContext != ItemDisplayContext.FIXED;
        boolean isPausedState = data != null && data.isPlaying() && data.isPaused()
            && !clientFrozen && displayContext != ItemDisplayContext.FIXED;

        // Compute disc rotation with pause-offset logic (mirrors JukeboxAlbumBlockEntityRenderer).
        float rotation = 0f;
        long now = System.currentTimeMillis();
        long start = data != null ? data.spinningStartTime() : 0L;

        if (data != null && data.isPlaying()) {
            Long previousStart = lastSpinningStart.put(seed, start);
            if (previousStart == null || previousStart != start) {
                spinPauseOffset.put(seed, 0L);
                lastSpinUpdate.put(seed, now);
            }

            long offset = spinPauseOffset.getOrDefault(seed, 0L);
            long lastUpdate = lastSpinUpdate.getOrDefault(seed, now);

            if (!isActuallyPlaying) {
                // Paused or frozen: accumulate non-spinning time.
                offset += now - lastUpdate;
                spinPauseOffset.put(seed, offset);
            }
            lastSpinUpdate.put(seed, now);

            long elapsed = Math.max(0, now - start - offset);
            rotation = (elapsed % 36000) / 25.0f;
        } else {
            spinPauseOffset.remove(seed);
            lastSpinUpdate.remove(seed);
            lastSpinningStart.remove(seed);
        }

        // Include the disc in the model identity so GUI item caching (atlas) distinguishes
        // a jukebox with a disc from an empty one and re-renders when the disc changes.
        output.appendModelIdentityElement(disc.getItem());
        // Distinguish playing / paused / idle so the cache invalidates on state changes.
        output.appendModelIdentityElement(isActuallyPlaying);
        output.appendModelIdentityElement(isPausedState);
        if (isActuallyPlaying) {
            output.setAnimated();
        }

        output.ensureCapacity(1);
        ItemStackRenderState.LayerRenderState discLayer = output.newLayer();
        discLayer.setExtents(ItemStackRenderState.LayerRenderState.NO_EXTENTS_SUPPLIER);
        discLayer.setLocalTransform(this.transformation);
        this.properties.applyToLayer(discLayer, displayContext);
        discLayer.setupSpecialModel(
            PortableJukeboxDiscSpecialRenderer.INSTANCE,
            new PortableJukeboxDiscSpecialRenderer.DiscArgument(
                disc,
                rotation,
                level,
                owner,
                seed,
                0.51f
            )
        );
    }

    private ItemStack findDiscToRender(ItemStack item) {
        PortableJukeboxData data = item.get(ModDataComponents.PORTABLE_JUKEBOX_DATA.get());
        if (data == null) {
            return ItemStack.EMPTY;
        }

        NonNullList<ItemStack> items = data.getItemsAsList(54 * Math.max(1, data.maxPages()));

        if (data.isPlaying() || data.isPaused()) {
            int slot = data.currentlyPlayingSlot();
            if (slot >= 0 && slot < items.size() && !items.get(slot).isEmpty()) {
                return items.get(slot);
            }
        }

        int currentPage = data.currentPage();
        int startSlot = currentPage * 54;
        int endSlot = Math.min(startSlot + 54, items.size());
        for (int i = startSlot; i < endSlot; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty() && (stack.getItem() instanceof CustomMusicDiscItem || stack.has(DataComponents.JUKEBOX_PLAYABLE))) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public record Unbaked(Identifier base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base)
            ).apply(instance, Unbaked::new)
        );

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.base);
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            ItemModel baseModel = new CuboidItemModelWrapper.Unbaked(this.base, Optional.empty(), List.of()).bake(context, transformation);
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel resolvedModel = baker.getModel(this.base);
            TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
            return new PortableJukeboxItemModel(baseModel, properties, new Matrix4f(transformation));
        }
    }
}
