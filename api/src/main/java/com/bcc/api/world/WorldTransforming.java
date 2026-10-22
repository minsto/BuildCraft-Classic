/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.api.world;

import java.util.ArrayDeque;
import java.util.Comparator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Server-side queue of block edits. Each machine owns a {@link Session}.
 * A session applies at most {@link #DEFAULT_BUDGET} blocks per tick, skips chunks that are not loaded,
 * and can hold a chunk ticket limited to its work radius. The client never applies these edits:
 * vanilla block updates are the only thing the renderer sees.
 */
public final class WorldTransforming {
    public static final int DEFAULT_BUDGET = 32;

    private WorldTransforming() {}

    public static Session open(ServerLevel level, BlockPos owner, int chunkRadius) {
        Session session = new Session(level, owner, Math.max(0, chunkRadius));
        session.acquireTicket();
        return session;
    }

    public static final class Session implements AutoCloseable {
        private final ServerLevel level;
        private final BlockPos owner;
        private final ChunkPos center;
        private final int chunkRadius;
        private final ArrayDeque<WorldOp> queue = new ArrayDeque<>();
        private int budget = DEFAULT_BUDGET;
        private boolean ticket;
        private int refresh;

        private Session(ServerLevel level, BlockPos owner, int chunkRadius) {
            this.level = level;
            this.owner = owner;
            this.center = new ChunkPos(owner);
            this.chunkRadius = chunkRadius;
        }

        public void setBudget(int budget) {
            this.budget = Math.max(1, budget);
        }

        public int budget() {
            return budget;
        }

        public int pending() {
            return queue.size();
        }

        public void enqueue(WorldOp op) {
            queue.addLast(op);
        }

        /**
         * Applies up to {@link #budget} operations whose chunk is loaded.
         * Unloaded operations stay queued and are rotated behind the loaded ones.
         */
        public int tick() {
            if (ticket && ++refresh >= 100) {
                refresh = 0;
                ChunkTickets.acquire(level, center, chunkRadius, owner);
            }
            if (queue.isEmpty()) {
                return 0;
            }
            int size = queue.size();
            int applied = 0;
            int seen = 0;
            while (applied < budget && seen < size && !queue.isEmpty()) {
                WorldOp op = queue.peekFirst();
                BlockPos pos = op.pos();
                seen++;
                if (!level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                    queue.addLast(queue.removeFirst());
                    continue;
                }
                queue.removeFirst();
                if (op.apply(level)) {
                    applied++;
                }
            }
            return applied;
        }

        public void sortByChunk() {
            var list = new java.util.ArrayList<>(queue);
            list.sort(Comparator.comparingLong((WorldOp op) -> new ChunkPos(op.pos()).toLong()));
            queue.clear();
            queue.addAll(list);
        }

        private void acquireTicket() {
            if (ticket) {
                return;
            }
            ChunkTickets.acquire(level, center, chunkRadius, owner);
            ticket = true;
        }

        @Override
        public void close() {
            if (!ticket) {
                return;
            }
            ChunkTickets.release(level, center, chunkRadius, owner);
            ticket = false;
            queue.clear();
        }
    }
}
