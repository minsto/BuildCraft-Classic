/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.machine;

import com.bcc.api.mj.Mj;
import com.bcc.api.world.WorldOp;
import com.bcc.api.world.WorldOps;
import com.bcc.api.world.WorldTransforming;
import com.bcc.blockentity.MachineBlockEntity;
import com.bcc.marker.MarkerIndex;
import com.bcc.menu.BCMenu;
import com.bcc.registry.BCBlockEntities;
import com.bcc.registry.BCMenus;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.MenuProvider;

public class DiggerBlockEntity extends MachineBlockEntity implements MenuProvider {
    private final boolean quarry;
    private WorldTransforming.Session session;
    private int x;
    private int y;
    private int z;
    private boolean started;
    private int skips;

    public DiggerBlockEntity(BlockPos pos, BlockState state, boolean quarry) {
        super(quarry ? BCBlockEntities.QUARRY : BCBlockEntities.MINING_WELL, pos, state, 9, 10_000L * Mj.MJ);
        this.quarry = quarry;
    }

    public boolean quarry() {
        return quarry;
    }

    public void tick() {
        tickSync();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (session == null) {
            session = WorldTransforming.open(server, worldPosition, quarry ? 8 : 2);
        }
        long cost = (quarry ? 25L : 20L) * Mj.MJ;
        int guard = 0;
        while (session.pending() < WorldTransforming.DEFAULT_BUDGET && mj.getStored() >= cost && guard++ < 256) {
            BlockPos next = nextPos();
            if (next == null) {
                break;
            }
            BlockState state = level.getBlockState(next);
            if (state.isAir() || state.getDestroySpeed(level, next) < 0) {
                continue;
            }
            BlockPos target = next.immutable();
            session.enqueue(new WorldOp() {
                @Override
                public BlockPos pos() {
                    return target;
                }

                @Override
                public boolean apply(ServerLevel level) {
                    if (mj.getStored() < cost) {
                        return false;
                    }
                    boolean broke = WorldOps.breakBlock(target, DiggerBlockEntity.this::give).apply(level);
                    if (broke) {
                        mj.extract(cost, false);
                    }
                    return broke;
                }
            });
        }
        if (session.tick() > 0 && level.getGameTime() % 10L == 0L) {
            level.playSound(null, worldPosition, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.3f, 0.8f);
            markSync();
        }
    }

    private BlockPos nextPos() {
        if (!quarry) {
            if (!started) {
                y = worldPosition.getY() - 1;
                x = worldPosition.getX();
                z = worldPosition.getZ();
                started = true;
            }
            if (y <= level.getMinBuildHeight()) {
                return null;
            }
            BlockPos pos = new BlockPos(x, y, z);
            y--;
            return pos;
        }
        AABB box = MarkerIndex.boxNear(level, worldPosition, 64);
        if (box == null) {
            return null;
        }
        int minX = (int) box.minX;
        int minY = (int) box.minY;
        int minZ = (int) box.minZ;
        int maxX = (int) box.maxX - 1;
        int maxY = (int) box.maxY - 1;
        int maxZ = (int) box.maxZ - 1;
        if (!started) {
            x = minX;
            y = maxY;
            z = minZ;
            started = true;
        }
        if (y < minY) {
            return null;
        }
        BlockPos pos = new BlockPos(x, y, z);
        x++;
        if (x > maxX) {
            x = minX;
            z++;
        }
        if (z > maxZ) {
            z = minZ;
            y--;
        }
        if (pos.equals(worldPosition) || level.getBlockState(pos).is(com.bcc.registry.BCBlocks.MARKER)) {
            return nextPos();
        }
        return pos;
    }

    @Override
    public void setRemoved() {
        if (session != null) {
            session.close();
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("X", x);
        tag.putInt("Y", y);
        tag.putInt("Z", z);
        tag.putBoolean("Started", started);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        x = tag.getInt("X");
        y = tag.getInt("Y");
        z = tag.getInt("Z");
        started = tag.getBoolean("Started");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(quarry ? "block.buildcraftbuilders.quarry" : "block.buildcraftfactory.mining_well");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DiggerMenu(id, inventory, this);
    }

    public static final class DiggerMenu extends BCMenu {
        public DiggerMenu(int id, Inventory inventory, DiggerBlockEntity be) {
            super(be.quarry ? BCMenus.QUARRY : BCMenus.MINING_WELL, id, inventory, be, be, 9, be.getBlockPos(), be.getBlockState().getBlock());
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int slot = col + row * 3;
                    addMachineSlot(new Slot(be, slot, 62 + col * 18, 17 + row * 18) {
                        @Override
                        public boolean mayPlace(ItemStack stack) {
                            return false;
                        }
                    });
                }
            }
            addPlayerSlots(inventory);
        }

        public DiggerMenu(int id, Inventory inventory, boolean quarry) {
            super(quarry ? BCMenus.QUARRY : BCMenus.MINING_WELL, id, inventory, new net.minecraft.world.SimpleContainer(9), new net.minecraft.world.inventory.SimpleContainerData(2), 9, null, null);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    addMachineSlot(new Slot(machine, col + row * 3, 62 + col * 18, 17 + row * 18));
                }
            }
            addPlayerSlots(inventory);
        }

        public int energy() { return data(0); }
    }
}
