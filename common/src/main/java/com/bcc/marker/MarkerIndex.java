/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.marker;

import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Loaded volume markers. Scanning the world for them would walk millions of blocks. */
public final class MarkerIndex {
    private static final WeakHashMap<Level, Set<BlockPos>> MARKERS = new WeakHashMap<>();

    private MarkerIndex() {}

    public static void add(Level level, BlockPos pos) {
        MARKERS.computeIfAbsent(level, key -> java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>())).add(pos.immutable());
    }

    public static void remove(Level level, BlockPos pos) {
        Set<BlockPos> set = MARKERS.get(level);
        if (set != null) {
            set.remove(pos);
        }
    }

    public static AABB boxNear(Level level, BlockPos origin, int range) {
        Set<BlockPos> set = MARKERS.get(level);
        if (set == null || set.size() < 2) {
            return null;
        }
        long maxDist = (long) range * range;
        BlockPos min = null;
        BlockPos max = null;
        int count = 0;
        for (BlockPos pos : set) {
            if (pos.distSqr(origin) > maxDist) {
                continue;
            }
            count++;
            if (min == null) {
                min = pos;
                max = pos;
            } else {
                min = new BlockPos(Math.min(min.getX(), pos.getX()), Math.min(min.getY(), pos.getY()), Math.min(min.getZ(), pos.getZ()));
                max = new BlockPos(Math.max(max.getX(), pos.getX()), Math.max(max.getY(), pos.getY()), Math.max(max.getZ(), pos.getZ()));
            }
        }
        if (count < 2 || min == null) {
            return null;
        }
        return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
    }
}
