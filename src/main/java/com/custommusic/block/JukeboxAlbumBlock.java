package com.custommusic.block;

import org.jetbrains.annotations.Nullable;

import com.custommusic.blockentity.JukeboxAlbumBlockEntity;
import com.custommusic.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import org.slf4j.LoggerFactory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;


public class JukeboxAlbumBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public JukeboxAlbumBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JukeboxAlbumBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof JukeboxAlbumBlockEntity jukeboxAlbum) {
                serverPlayer.openMenu(jukeboxAlbum);
                // Send current playback state to client so GUI shows correct button icon + highlight
                com.custommusic.network.NetworkHandler.sendStateUpdate(jukeboxAlbum, serverPlayer);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.JUKEBOX_ALBUM.get(),
                (lvl, pos, st, be) -> be.tick(lvl, pos, st));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide()) return;

        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof JukeboxAlbumBlockEntity jukeboxAlbum) {
            if (placer != null) {
                net.minecraft.core.Direction playerFacing = placer.getDirection();
                jukeboxAlbum.setPlayerFacingOnPlace(playerFacing);
            }

            TypedEntityData<BlockEntityType<?>> blockEntityData =
                stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);

            if (blockEntityData != null) {
                blockEntityData.loadInto(jukeboxAlbum, level.registryAccess());
                if (placer != null) {
                    net.minecraft.core.Direction playerFacing = placer.getDirection();
                    jukeboxAlbum.setPlayerFacingOnPlace(playerFacing);
                }
            }
        }
    }
    
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof JukeboxAlbumBlockEntity jukeboxAlbum) {
                // Pause music first so the saved state is paused and the sound is stopped.
                jukeboxAlbum.pauseMusic();

                if (player != null && player.isCreative() && jukeboxAlbum.isEmpty()) {
                    return super.playerWillDestroy(level, pos, state, player);
                }

                ItemStack drop = new ItemStack(state.getBlock());
                // Serialize BE data to TagValueOutput and set on item only when the jukebox has contents
                if (!jukeboxAlbum.isEmpty()) {
                    TagValueOutput output = TagValueOutput.createWithContext(
                        new ProblemReporter.ScopedCollector(be.problemPath(), LoggerFactory.getLogger(JukeboxAlbumBlock.class)),
                        level.registryAccess()
                    );
                    be.saveCustomOnly(output);
                    net.minecraft.world.item.BlockItem.setBlockEntityData(drop, ModBlockEntities.JUKEBOX_ALBUM.get(), output);
                }
                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        // Suppress the default loot table drop; we handle drops manually in playerWillDestroy
        // (drop only when the block has discs in it, with full BE data preserved).
        return Collections.emptyList();
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        // Disable restoration system to prevent crashes
        // The player data system is causing corrupted ItemStacks with BlockEntityTag
        System.out.println("Restoration tick disabled to prevent crashes");
    }
}
