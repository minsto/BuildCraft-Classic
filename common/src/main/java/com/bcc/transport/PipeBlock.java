/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.transport;

import com.bcc.api.mj.MjStorage;
import com.bcc.api.pipe.PipeFlow;
import com.bcc.energy.EngineBlockEntity;
import com.bcc.fluid.HasFluid;
import com.bcc.wrench.Wrenchable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PipeBlock extends BaseEntityBlock implements Wrenchable {
    private static final ThreadLocal<PipeKind> BUILDING = new ThreadLocal<>();
    private static final BooleanProperty[] SIDES = {
            BlockStateProperties.DOWN, BlockStateProperties.UP, BlockStateProperties.NORTH,
            BlockStateProperties.SOUTH, BlockStateProperties.WEST, BlockStateProperties.EAST
    };
    private static final VoxelShape[] SHAPES = buildShapes();
    private final PipeKind kind;
    private final MapCodec<PipeBlock> codec;

    public PipeBlock(PipeKind kind, Properties properties) {
        this(kind, properties, true);
    }

    private PipeBlock(PipeKind kind, Properties properties, boolean decorate) {
        super(prepare(kind, properties, decorate));
        this.kind = BUILDING.get();
        BUILDING.remove();
        this.codec = BlockBehaviour.simpleCodec(props -> new PipeBlock(kind, props, false));
        BlockState state = stateDefinition.any();
        for (BooleanProperty side : SIDES) {
            state = state.setValue(side, false);
        }
        if (this.kind.iron()) {
            state = state.setValue(BlockStateProperties.FACING, Direction.NORTH);
        }
        registerDefaultState(state);
    }

    private static Properties prepare(PipeKind kind, Properties properties, boolean decorate) {
        BUILDING.set(kind);
        if (!decorate) {
            return properties;
        }
        SoundType sound = kind == PipeKind.WOOD_ITEM || kind == PipeKind.WOOD_FLUID || kind == PipeKind.WOOD_POWER ? SoundType.WOOD : SoundType.STONE;
        return properties.strength(0.25f).sound(sound).noOcclusion();
    }

    @Override
    protected MapCodec<PipeBlock> codec() {
        return codec;
    }

    public PipeKind kind() {
        return kind;
    }

    public static BooleanProperty side(Direction direction) {
        return SIDES[direction.ordinal()];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIDES);
        PipeKind building = BUILDING.get();
        if (building != null && building.iron()) {
            builder.add(BlockStateProperties.FACING);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connected(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(side(direction), connects(level, pos, direction));
    }

    private BlockState connected(BlockState state, Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(side(direction), connects(level, pos, direction));
        }
        return state;
    }

    public boolean connects(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockPos neighbor = pos.relative(direction);
        BlockState state = level.getBlockState(neighbor);
        if (state.getBlock() instanceof PipeBlock other) {
            if (other.kind.flow() != kind.flow()) {
                return false;
            }
            if (kind.stone() && other.kind == PipeKind.COBBLE_ITEM) {
                return false;
            }
            if (kind == PipeKind.COBBLE_ITEM && other.kind.stone()) {
                return false;
            }
            return true;
        }
        BlockEntity be = level.getBlockEntity(neighbor);
        return switch (kind.flow()) {
            case ITEM -> be instanceof net.minecraft.world.Container && !(be instanceof PipeBlockEntity);
            case FLUID -> be instanceof HasFluid && !(be instanceof PipeBlockEntity);
            case POWER -> be instanceof MjStorage && !(be instanceof PipeBlockEntity && ((PipeBlockEntity) be).flow() != PipeFlow.POWER);
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            if (state.getValue(side(direction))) {
                mask |= 1 << direction.ordinal();
            }
        }
        return SHAPES[mask];
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, blockState, be) -> ((PipeBlockEntity) be).tick();
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.phys.BlockHitResult hit) {
        if (kind.diamond() && level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
            if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.openMenu(pipe);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult wrench(Level level, BlockPos pos, BlockState state, Direction face, Player player) {
        if (kind.iron()) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.cycle(BlockStateProperties.FACING), Block.UPDATE_ALL);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof PipeBlockEntity pipe && pipe.hasGate()) {
            if (!level.isClientSide) {
                pipe.setGate(false);
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new net.minecraft.world.item.ItemStack(com.bcc.registry.BCItems.GATE));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof PipeBlockEntity pipe && pipe.hasGate() && pipe.hasPayload()) {
            return 15;
        }
        return 0;
    }

    private static VoxelShape[] buildShapes() {
        VoxelShape core = Block.box(4, 4, 4, 12, 12, 12);
        VoxelShape[] arms = new VoxelShape[] {
                Block.box(4, 0, 4, 12, 4, 12),
                Block.box(4, 12, 4, 12, 16, 12),
                Block.box(4, 4, 0, 12, 12, 4),
                Block.box(4, 4, 12, 12, 12, 16),
                Block.box(0, 4, 4, 4, 12, 12),
                Block.box(12, 4, 4, 16, 12, 12)
        };
        VoxelShape[] shapes = new VoxelShape[64];
        for (int mask = 0; mask < 64; mask++) {
            VoxelShape shape = core;
            for (int i = 0; i < 6; i++) {
                if ((mask & (1 << i)) != 0) {
                    shape = Shapes.or(shape, arms[i]);
                }
            }
            shapes[mask] = shape.optimize();
        }
        return shapes;
    }
}
