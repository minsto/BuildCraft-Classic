/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.wrench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface Wrenchable {
    InteractionResult wrench(Level level, BlockPos pos, BlockState state, Direction face, Player player);
}
