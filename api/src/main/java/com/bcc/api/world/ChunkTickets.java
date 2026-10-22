/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.TicketType;

/**
 * Chunk tickets for machines. Vanilla freezes {@code TicketType} before mods load,
 * so this reuses {@link TicketType#PORTAL}: it is keyed by {@link BlockPos} and expires after 300 ticks.
 * Sessions refresh it while they are open and remove it when they close, so a quarry only keeps
 * the chunks inside its own radius.
 */
public final class ChunkTickets {
    private ChunkTickets() {}

    public static void acquire(ServerLevel level, ChunkPos center, int radius, BlockPos key) {
        ServerChunkCache chunks = level.getChunkSource();
        chunks.addRegionTicket(TicketType.PORTAL, center, radius, key);
    }

    public static void release(ServerLevel level, ChunkPos center, int radius, BlockPos key) {
        level.getChunkSource().removeRegionTicket(TicketType.PORTAL, center, radius, key);
    }
}
