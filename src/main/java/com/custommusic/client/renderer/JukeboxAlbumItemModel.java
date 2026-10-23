package com.custommusic.client.renderer;

import com.custommusic.item.CustomMusicDiscItem;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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
 * Item model for the Jukebox Album block item.
 * Renders the base block model and then places the current disc on top of it,
 * spinning while music is playing and static when paused or stopped.
 */
public class JukeboxAlbumItemModel implements ItemModel {
    private final ItemModel baseModel;
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;

    // Per-item pause offset so the disc freezes at its current angle when paused,
    // then resumes smoothly — mirroring JukeboxAlbumBlockEntityRenderer.
    private static final Map<Integer, Long> spinPauseOffset = new HashMap<>();
    private static final Map<Integer, Long> lastSpinUpdate = new HashMap<>();
    private static final Map<Integer, Long> lastSpinningStart = new HashMap<>();

    public JukeboxAlbumItemModel(ItemModel baseModel, ModelRenderProperties properties, Matrix4fc transformation) {
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

        // Render the base Jukebox Album block model.
        this.baseModel.update(output, item, resolver, displayContext, level, owner, seed);

        // Render the current disc on top.
        DiscInfo discInfo = findDiscToRender(item);
        if (discInfo.disc.isEmpty()) {
            return;
        }

        // Include the disc in the model identity so GUI item caching (atlas) distinguishes
        // an album with a disc from an empty one and re-renders when the disc changes.
        output.appendModelIdentityElement(discInfo.disc.getItem());

        boolean isActuallyPlaying = discInfo.isPlaying && !discInfo.isPaused
            && displayContext != ItemDisplayContext.FIXED;
        boolean isPausedState = discInfo.isPlaying && discInfo.isPaused
            && displayContext != ItemDisplayContext.FIXED;

        output.appendModelIdentityElement(isActuallyPlaying);
        output.appendModelIdentityElement(isPausedState);
        if (isActuallyPlaying) {
            output.setAnimated();
        }

        // Compute disc rotation with pause-offset logic (mirrors JukeboxAlbumBlockEntityRenderer).
        float rotation = 0f;
        long now = System.currentTimeMillis();
        long start = discInfo.spinningStartTime;

        if (discInfo.isPlaying) {
            Long previousStart = lastSpinningStart.put(seed, start);
            if (previousStart == null || previousStart != start) {
                spinPauseOffset.put(seed, 0L);
                lastSpinUpdate.put(seed, now);
            }

            long offset = spinPauseOffset.getOrDefault(seed, 0L);
            long lastUpdate = lastSpinUpdate.getOrDefault(seed, now);

            if (!isActuallyPlaying) {
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

        output.ensureCapacity(1);
        ItemStackRenderState.LayerRenderState discLayer = output.newLayer();
        discLayer.setExtents(ItemStackRenderState.LayerRenderState.NO_EXTENTS_SUPPLIER);
        discLayer.setLocalTransform(this.transformation);
        this.properties.applyToLayer(discLayer, displayContext);
        discLayer.setupSpecialModel(
            PortableJukeboxDiscSpecialRenderer.INSTANCE,
            new PortableJukeboxDiscSpecialRenderer.DiscArgument(
                discInfo.disc,
                rotation,
                level,
                owner,
                seed,
                1.01f
            )
        );
    }

    private DiscInfo findDiscToRender(ItemStack item) {
        var beData = item.get(DataComponents.BLOCK_ENTITY_DATA);
        if (beData == null) {
            return DiscInfo.EMPTY;
        }

        CompoundTag tag = beData.copyTagWithoutId();
        boolean isPlaying = tag.getBooleanOr("IsPlaying", false);
        boolean isPaused = tag.getBooleanOr("IsPaused", false);
        int currentlyPlayingSlot = tag.getIntOr("CurrentlyPlayingSlot", -1);
        int currentPage = tag.getIntOr("CurrentPage", 0);
        int maxPages = tag.getIntOr("MaxPages", 1);
        long spinningStartTime = tag.getLongOr("SpinningStartTime", 0L);

        CompoundTag inventory = tag.getCompoundOrEmpty("Inventory");
        if (inventory.isEmpty()) {
            return DiscInfo.EMPTY;
        }

        int totalSlots = 54 * Math.max(1, maxPages);
        NonNullList<ItemStack> items = NonNullList.withSize(totalSlots, ItemStack.EMPTY);
        for (int i = 0; i < totalSlots; i++) {
            Tag t = inventory.get("Item" + i);
            if (t != null) {
                Optional<ItemStack> parsed = ItemStack.CODEC.parse(NbtOps.INSTANCE, t).result();
                if (parsed.isPresent()) {
                    items.set(i, parsed.get());
                }
            }
        }

        if (isPlaying || isPaused) {
            int slot = currentlyPlayingSlot;
            if (slot >= 0 && slot < items.size() && !items.get(slot).isEmpty()) {
                return new DiscInfo(items.get(slot), isPlaying, isPaused, spinningStartTime);
            }
        }

        int startSlot = currentPage * 54;
        int endSlot = Math.min(startSlot + 54, items.size());
        for (int i = startSlot; i < endSlot; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty() && (stack.getItem() instanceof CustomMusicDiscItem || stack.has(DataComponents.JUKEBOX_PLAYABLE))) {
                return new DiscInfo(stack, isPlaying, isPaused, spinningStartTime);
            }
        }

        return DiscInfo.EMPTY;
    }

    private record DiscInfo(ItemStack disc, boolean isPlaying, boolean isPaused, long spinningStartTime) {
        static final DiscInfo EMPTY = new DiscInfo(ItemStack.EMPTY, false, false, 0L);
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
            return new JukeboxAlbumItemModel(baseModel, properties, new Matrix4f(transformation));
        }
    }
}
