/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.robotics;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class BuildCraftRobot extends PathfinderMob {
    private BlockPos home = BlockPos.ZERO;
    private BlockPos planner = BlockPos.ZERO;
    private AABB zone = new AABB(0, 0, 0, 1, 1, 1);
    private int scanCooldown;
    private ItemEntity target;

    public BuildCraftRobot(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    public void setHome(BlockPos home, AABB zone) {
        this.home = home.immutable();
        this.zone = zone;
    }

    public void setPlanner(BlockPos planner) {
        this.planner = planner.immutable();
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        if (scanCooldown-- <= 0) {
            scanCooldown = 10;
            List<ItemEntity> items = ZonePlannerBlockEntity.itemsIn(server, zone);
            target = items.isEmpty() ? null : items.get(0);
            double best = Double.MAX_VALUE;
            for (ItemEntity item : items) {
                double distance = item.distanceToSqr(this);
                if (distance < best) {
                    best = distance;
                    target = item;
                }
            }
        }
        if (target != null && target.isAlive()) {
            getNavigation().moveTo(target, 1.0);
            if (distanceToSqr(target) < 2.0) {
                BlockEntity be = server.getBlockEntity(planner);
                if (be instanceof ZonePlannerBlockEntity zonePlanner) {
                    zonePlanner.giveFromRobot(target);
                } else {
                    target.discard();
                }
                target = null;
            }
        } else if (home != null && distanceToSqr(home.getX() + 0.5, home.getY() + 1, home.getZ() + 0.5) > 4) {
            getNavigation().moveTo(home.getX() + 0.5, home.getY() + 1, home.getZ() + 0.5, 1.0);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("Home", home.asLong());
        tag.putLong("Planner", planner.asLong());
        tag.putDouble("MinX", zone.minX);
        tag.putDouble("MinY", zone.minY);
        tag.putDouble("MinZ", zone.minZ);
        tag.putDouble("MaxX", zone.maxX);
        tag.putDouble("MaxY", zone.maxY);
        tag.putDouble("MaxZ", zone.maxZ);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        home = BlockPos.of(tag.getLong("Home"));
        planner = BlockPos.of(tag.getLong("Planner"));
        zone = new AABB(tag.getDouble("MinX"), tag.getDouble("MinY"), tag.getDouble("MinZ"), tag.getDouble("MaxX"), tag.getDouble("MaxY"), tag.getDouble("MaxZ"));
    }
}
