/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.marker;

import com.bcc.registry.BCBlockEntities;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MarkerBlock extends BaseEntityBlock {
    private final MapCodec<MarkerBlock> codec;

    public MarkerBlock(Properties properties) {
        this(properties, true);
    }

    private MarkerBlock(Properties properties, boolean decorate) {
        super(decorate ? properties.strength(0.5f).sound(SoundType.WOOD).noOcclusion() : properties);
        this.codec = BlockBehaviour.simpleCodec(props -> new MarkerBlock(props, false));
    }

    @Override
    protected MapCodec<MarkerBlock> codec() {
        return codec;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarkerBlockEntity(pos, state);
    }

    public static final class MarkerBlockEntity extends BlockEntity {
        public MarkerBlockEntity(BlockPos pos, BlockState state) {
            super(BCBlockEntities.MARKER, pos, state);
        }

        @Override
        public void setLevel(Level level) {
            super.setLevel(level);
            MarkerIndex.add(level, worldPosition);
        }

        @Override
        public void setRemoved() {
            if (level != null) {
                MarkerIndex.remove(level, worldPosition);
            }
            super.setRemoved();
        }
    }
}
