/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * One block change. {@link #apply} must touch a single block and return quickly.
 * Quarry, filler, builder and mining well enqueue these instead of calling {@code setBlock} in a loop.
 */
public interface WorldOp {
    BlockPos pos();

    /** @return true when a block was actually changed */
    boolean apply(ServerLevel level);
}
