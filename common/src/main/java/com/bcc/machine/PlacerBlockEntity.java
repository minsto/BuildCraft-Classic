/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.machine;

import com.bcc.api.mj.Mj;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.MenuProvider;

public class PlacerBlockEntity extends MachineBlockEntity implements MenuProvider {
    private final boolean builder;
    private WorldTransforming.Session session;
    private int x;
    private int y;
    private int z;
    private boolean started;

    public PlacerBlockEntity(BlockPos pos, BlockState state, boolean builder) {
        super(builder ? BCBlockEntities.BUILDER : BCBlockEntities.FILLER, pos, state, builder ? 9 : 1, 10_000L * Mj.MJ);
        this.builder = builder;
    }

    public void tick() {
        tickSync();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        AABB box = MarkerIndex.boxNear(level, worldPosition, 64);
        if (box == null) {
            return;
        }
        if (session == null) {
            int radius = Math.max(2, (int) Math.ceil(box.getXsize() / 16.0) + 1);
            session = WorldTransforming.open(server, worldPosition, radius);
        }
        long cost = 5L * Mj.MJ;
        int guard = 0;
        while (session.pending() < WorldTransforming.DEFAULT_BUDGET && mj.getStored() >= cost && guard++ < 256) {
            BlockPos next = nextPos(box);
            if (next == null) {
                break;
            }
            if (!level.getBlockState(next).isAir()) {
                continue;
            }
            int slot = takeSlot();
            if (slot < 0) {
                break;
            }
            ItemStack stack = items.get(slot);
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                continue;
            }
            BlockState place = blockItem.getBlock().defaultBlockState();
            BlockPos target = next.immutable();
            int usedSlot = slot;
            session.enqueue(new com.bcc.api.world.WorldOp() {
                @Override
                public BlockPos pos() { return target; }

                @Override
                public boolean apply(ServerLevel level) {
                    if (mj.getStored() < cost || items.get(usedSlot).isEmpty()) {
                        return false;
                    }
                    boolean placed = level.setBlock(target, place, net.minecraft.world.level.block.Block.UPDATE_ALL);
                    if (placed) {
                        items.get(usedSlot).shrink(1);
                        mj.extract(cost, false);
                        setChanged();
                    }
                    return placed;
                }
            });
        }
        session.tick();
    }

    private int takeSlot() {
        if (!builder) {
            return items.get(0).getItem() instanceof BlockItem ? 0 : -1;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    private BlockPos nextPos(AABB box) {
        int minX = (int) box.minX;
        int minY = (int) box.minY;
        int minZ = (int) box.minZ;
        int maxX = (int) box.maxX - 1;
        int maxY = (int) box.maxY - 1;
        int maxZ = (int) box.maxZ - 1;
        if (!started) {
            x = minX;
            y = minY;
            z = minZ;
            started = true;
        }
        if (y > maxY) {
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
            y++;
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
        return Component.translatable(builder ? "block.buildcraftbuilders.builder" : "block.buildcraftbuilders.filler");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PlacerMenu(id, inventory, this);
    }

    public static final class PlacerMenu extends BCMenu {
        public PlacerMenu(int id, Inventory inventory, PlacerBlockEntity be) {
            super(be.builder ? BCMenus.BUILDER : BCMenus.FILLER, id, inventory, be, be, be.getContainerSize(), be.getBlockPos(), be.getBlockState().getBlock());
            int count = be.getContainerSize();
            for (int i = 0; i < count; i++) {
                addMachineSlot(new Slot(be, i, 8 + (i % 9) * 18, 17 + (i / 9) * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.getItem() instanceof BlockItem;
                    }
                });
            }
            addPlayerSlots(inventory);
        }

        public PlacerMenu(int id, Inventory inventory, boolean builder) {
            super(builder ? BCMenus.BUILDER : BCMenus.FILLER, id, inventory, new net.minecraft.world.SimpleContainer(builder ? 9 : 1), new net.minecraft.world.inventory.SimpleContainerData(2), builder ? 9 : 1, null, null);
            int count = builder ? 9 : 1;
            for (int i = 0; i < count; i++) {
                addMachineSlot(new Slot(machine, i, 8 + (i % 9) * 18, 17));
            }
            addPlayerSlots(inventory);
        }

        public int energy() { return data(0); }
    }
}
