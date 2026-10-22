/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.machine;

import com.bcc.factory.TankBlockEntity;
import com.bcc.silicon.LaserBlockEntity;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WorkerBlock extends BaseEntityBlock {
    public enum Kind {
        TANK(SoundType.GLASS),
        PUMP(SoundType.METAL),
        MINING_WELL(SoundType.STONE),
        QUARRY(SoundType.METAL),
        FILLER(SoundType.STONE),
        BUILDER(SoundType.WOOD),
        LASER(SoundType.METAL),
        ASSEMBLY(SoundType.METAL),
        ZONE_PLANNER(SoundType.METAL);

        final SoundType sound;

        Kind(SoundType sound) {
            this.sound = sound;
        }
    }

    private final Kind kind;
    private final MapCodec<WorkerBlock> codec;

    public WorkerBlock(Kind kind, Properties properties) {
        this(kind, properties, true);
    }

    private WorkerBlock(Kind kind, Properties properties, boolean decorate) {
        super(decorate ? properties.strength(3.0f).sound(kind.sound) : properties);
        this.kind = kind;
        this.codec = BlockBehaviour.simpleCodec(props -> new WorkerBlock(kind, props, false));
    }

    @Override
    protected MapCodec<WorkerBlock> codec() {
        return codec;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case TANK -> new TankBlockEntity(pos, state);
            case PUMP -> new TankBlockEntity.PumpBlockEntity(pos, state);
            case MINING_WELL, QUARRY -> new DiggerBlockEntity(pos, state, kind == Kind.QUARRY);
            case FILLER, BUILDER -> new PlacerBlockEntity(pos, state, kind == Kind.BUILDER);
            case LASER -> new LaserBlockEntity(pos, state);
            case ASSEMBLY -> new LaserBlockEntity.AssemblyBlockEntity(pos, state);
            case ZONE_PLANNER -> new com.bcc.robotics.ZonePlannerBlockEntity(pos, state);
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, blockState, be) -> {
            if (be instanceof TankBlockEntity tank) tank.tick();
            else if (be instanceof TankBlockEntity.PumpBlockEntity pump) pump.tick();
            else if (be instanceof DiggerBlockEntity digger) digger.tick();
            else if (be instanceof PlacerBlockEntity placer) placer.tick();
            else if (be instanceof LaserBlockEntity laser) laser.tick();
            else if (be instanceof LaserBlockEntity.AssemblyBlockEntity assembly) assembly.tick();
            else if (be instanceof com.bcc.robotics.ZonePlannerBlockEntity zone) zone.tick();
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof net.minecraft.world.MenuProvider provider) {
            if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.openMenu(provider);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
