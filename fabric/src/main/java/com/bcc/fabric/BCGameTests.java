/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.fabric;

import com.bcc.api.mj.Mj;
import com.bcc.api.world.WorldOps;
import com.bcc.api.world.WorldTransforming;
import com.bcc.energy.EngineBlockEntity;
import com.bcc.factory.TankBlockEntity;
import com.bcc.machine.DiggerBlockEntity;
import com.bcc.machine.PlacerBlockEntity;
import com.bcc.registry.BCBlocks;
import com.bcc.robotics.ZonePlannerBlockEntity;
import com.bcc.silicon.LaserBlockEntity;
import com.bcc.transport.PipeBlock;
import com.bcc.transport.PipeBlockEntity;
import com.bcc.transport.PipeKind;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class BCGameTests implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void worldBudget(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        try (WorldTransforming.Session session = WorldTransforming.open(helper.getLevel(), origin, 1)) {
            for (int i = 0; i < 40; i++) {
                session.enqueue(WorldOps.placeBlock(origin.offset(0, 2 + i, 0), Blocks.STONE.defaultBlockState()));
            }
            int first = session.tick();
            helper.assertValueEqual(first, WorldTransforming.DEFAULT_BUDGET, "blocks placed in one tick");
            int second = session.tick();
            helper.assertValueEqual(session.pending(), 0, "queue drained");
            helper.assertTrue(first + second <= 40, "placed more blocks than were queued");
            helper.assertTrue(second < first, "the second tick was not smaller than the budget");
            session.sortByChunk();
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void stirlingStoresMj(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 2, 0);
        helper.setBlock(pos, BCBlocks.ENGINE_STIRLING.defaultBlockState());
        EngineBlockEntity engine = helper.getBlockEntity(pos);
        engine.setItem(0, new net.minecraft.world.item.ItemStack(Items.COAL));
        engine.tick();
        helper.assertTrue(engine.getStored() >= Mj.MJ, "stirling did not store a MJ");
        CompoundTag saved = engine.getUpdateTag(helper.getLevel().registryAccess());
        EngineBlockEntity loaded = new EngineBlockEntity(helper.absolutePos(pos), engine.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertValueEqual(loaded.getStored(), engine.getStored(), "saved MJ");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void pipeConnections(GameTestHelper helper) {
        BlockPos wood = new BlockPos(0, 2, 0);
        BlockPos woodNeighbor = wood.east();
        helper.setBlock(wood, BCBlocks.pipe(PipeKind.WOOD_ITEM).defaultBlockState());
        helper.setBlock(woodNeighbor, BCBlocks.pipe(PipeKind.WOOD_ITEM).defaultBlockState());
        helper.assertBlockProperty(wood, PipeBlock.side(Direction.EAST), true);
        BlockPos stone = new BlockPos(0, 3, 0);
        helper.setBlock(stone, BCBlocks.pipe(PipeKind.STONE_ITEM).defaultBlockState());
        helper.setBlock(stone.east(), BCBlocks.pipe(PipeKind.COBBLE_ITEM).defaultBlockState());
        helper.assertBlockProperty(stone, PipeBlock.side(Direction.EAST), false);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void menuSlotCounts(GameTestHelper helper) {
        var inventory = helper.makeMockPlayer(GameType.SURVIVAL).getInventory();
        helper.assertValueEqual(new EngineBlockEntity.EngineMenu(1, inventory).slots.size(), 38, "engine slots");
        helper.assertValueEqual(new PipeBlockEntity.DiamondPipeMenu(2, inventory).slots.size(), 45, "diamond slots");
        helper.assertValueEqual(new TankBlockEntity.TankMenu(3, inventory).slots.size(), 37, "tank slots");
        helper.assertValueEqual(new TankBlockEntity.PumpMenu(4, inventory).slots.size(), 36, "pump slots");
        helper.assertValueEqual(new DiggerBlockEntity.DiggerMenu(5, inventory, true).slots.size(), 45, "quarry slots");
        helper.assertValueEqual(new DiggerBlockEntity.DiggerMenu(6, inventory, false).slots.size(), 45, "mining well slots");
        helper.assertValueEqual(new PlacerBlockEntity.PlacerMenu(7, inventory, false).slots.size(), 37, "filler slots");
        helper.assertValueEqual(new PlacerBlockEntity.PlacerMenu(8, inventory, true).slots.size(), 45, "builder slots");
        helper.assertValueEqual(new LaserBlockEntity.AssemblyMenu(9, inventory).slots.size(), 40, "assembly slots");
        helper.assertValueEqual(new ZonePlannerBlockEntity.ZoneMenu(10, inventory).slots.size(), 45, "zone slots");
        helper.succeed();
    }
}
