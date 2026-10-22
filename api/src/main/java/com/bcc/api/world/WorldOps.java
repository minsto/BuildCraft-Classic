/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.api.world;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class WorldOps {
    private WorldOps() {}

    public static WorldOp breakBlock(BlockPos pos, Consumer<ItemStack> drops) {
        return new WorldOp() {
            @Override
            public BlockPos pos() {
                return pos;
            }

            @Override
            public boolean apply(ServerLevel level) {
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.getDestroySpeed(level, pos) < 0) {
                    return false;
                }
                List<ItemStack> items = Block.getDrops(state, level, pos, level.getBlockEntity(pos), null, ItemStack.EMPTY);
                level.destroyBlock(pos, false);
                for (ItemStack stack : items) {
                    if (!stack.isEmpty()) {
                        drops.accept(stack);
                    }
                }
                return true;
            }
        };
    }

    public static WorldOp placeBlock(BlockPos pos, BlockState state) {
        return new WorldOp() {
            @Override
            public BlockPos pos() {
                return pos;
            }

            @Override
            public boolean apply(ServerLevel level) {
                if (!level.getBlockState(pos).isAir()) {
                    return false;
                }
                return level.setBlock(pos, state, Block.UPDATE_ALL);
            }
        };
    }

    /** Removes a source fluid block and reports its bucket-sized amount (1000 mB) through {@code gained}. */
    public static WorldOp takeFluid(BlockPos pos, Consumer<BlockState> taken) {
        return new WorldOp() {
            @Override
            public BlockPos pos() {
                return pos;
            }

            @Override
            public boolean apply(ServerLevel level) {
                BlockState state = level.getBlockState(pos);
                if (state.getFluidState().isEmpty() || !state.getFluidState().isSource()) {
                    return false;
                }
                taken.accept(state);
                level.destroyBlock(pos, false);
                return true;
            }
        };
    }
}
