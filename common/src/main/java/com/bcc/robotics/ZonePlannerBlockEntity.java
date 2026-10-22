/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.robotics;

import java.util.List;
import java.util.UUID;

import com.bcc.blockentity.MachineBlockEntity;
import com.bcc.marker.MarkerIndex;
import com.bcc.menu.BCMenu;
import com.bcc.registry.BCBlockEntities;
import com.bcc.registry.BCEntities;
import com.bcc.registry.BCMenus;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.MenuProvider;

public class ZonePlannerBlockEntity extends MachineBlockEntity implements MenuProvider {
    private UUID robotId;
    private int cooldown;

    public ZonePlannerBlockEntity(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ZONE_PLANNER, pos, state, 9, 0L);
    }

    public void tick() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        AABB box = MarkerIndex.boxNear(level, worldPosition, 64);
        if (box == null) {
            return;
        }
        Entity existing = robotId == null ? null : server.getEntity(robotId);
        if (!(existing instanceof BuildCraftRobot robot) || !robot.isAlive()) {
            BuildCraftRobot spawned = BCEntities.ROBOT.create(server);
            if (spawned != null) {
                spawned.moveTo(worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, 0, 0);
                spawned.setHome(worldPosition, box);
                spawned.setPlanner(worldPosition);
                server.addFreshEntity(spawned);
                robotId = spawned.getUUID();
            }
            cooldown = 40;
        }
    }

    public void giveFromRobot(ItemEntity item) {
        give(item.getItem().copy());
        item.discard();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (robotId != null) {
            tag.putUUID("Robot", robotId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        robotId = tag.hasUUID("Robot") ? tag.getUUID("Robot") : null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftrobotics.zone_planner");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ZoneMenu(id, inventory, this);
    }

    public static List<ItemEntity> itemsIn(ServerLevel level, AABB box) {
        return level.getEntitiesOfClass(ItemEntity.class, box, item -> item.isAlive() && !item.getItem().isEmpty());
    }

    public static final class ZoneMenu extends BCMenu {
        public ZoneMenu(int id, Inventory inventory, ZonePlannerBlockEntity be) {
            super(BCMenus.ZONE, id, inventory, be, be, 9, be.getBlockPos(), be.getBlockState().getBlock());
            for (int i = 0; i < 9; i++) {
                int slot = i;
                addMachineSlot(new net.minecraft.world.inventory.Slot(be, slot, 8 + i * 18, 35) {
                    @Override
                    public boolean mayPlace(net.minecraft.world.item.ItemStack stack) { return false; }
                });
            }
            addPlayerSlots(inventory);
        }

        public ZoneMenu(int id, Inventory inventory) {
            super(BCMenus.ZONE, id, inventory, new net.minecraft.world.SimpleContainer(9), new net.minecraft.world.inventory.SimpleContainerData(2), 9, null, null);
            for (int i = 0; i < 9; i++) {
                addMachineSlot(new net.minecraft.world.inventory.Slot(machine, i, 8 + i * 18, 35));
            }
            addPlayerSlots(inventory);
        }
    }
}
