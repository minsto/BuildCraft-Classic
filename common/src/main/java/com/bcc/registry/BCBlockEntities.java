/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.registry;

import java.util.function.BiConsumer;

import com.bcc.BCConstants;
import com.bcc.energy.EngineBlockEntity;
import com.bcc.factory.TankBlockEntity;
import com.bcc.machine.DiggerBlockEntity;
import com.bcc.machine.PlacerBlockEntity;
import com.bcc.marker.MarkerBlock;
import com.bcc.robotics.ZonePlannerBlockEntity;
import com.bcc.silicon.LaserBlockEntity;
import com.bcc.transport.PipeBlockEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCBlockEntities {
    public static BlockEntityType<EngineBlockEntity> ENGINE;
    public static BlockEntityType<MarkerBlock.MarkerBlockEntity> MARKER;
    public static BlockEntityType<PipeBlockEntity> PIPE;
    public static BlockEntityType<TankBlockEntity> TANK;
    public static BlockEntityType<TankBlockEntity.PumpBlockEntity> PUMP;
    public static BlockEntityType<DiggerBlockEntity> MINING_WELL;
    public static BlockEntityType<DiggerBlockEntity> QUARRY;
    public static BlockEntityType<PlacerBlockEntity> FILLER;
    public static BlockEntityType<PlacerBlockEntity> BUILDER;
    public static BlockEntityType<LaserBlockEntity> LASER;
    public static BlockEntityType<LaserBlockEntity.AssemblyBlockEntity> ASSEMBLY;
    public static BlockEntityType<ZonePlannerBlockEntity> ZONE_PLANNER;

    private BCBlockEntities() {}

    @SuppressWarnings("DataFlowIssue")
    public static void register(BiConsumer<ResourceLocation, BlockEntityType<?>> registrar) {
        ENGINE = register(registrar, BCConstants.ENERGY, "engine", BlockEntityType.Builder.of(EngineBlockEntity::new, BCBlocks.ENGINE_STIRLING, BCBlocks.ENGINE_COMBUSTION).build(null));
        MARKER = register(registrar, BCConstants.CORE, "marker_volume", BlockEntityType.Builder.of(MarkerBlock.MarkerBlockEntity::new, BCBlocks.MARKER).build(null));
        PIPE = register(registrar, BCConstants.TRANSPORT, "pipe", BlockEntityType.Builder.of(PipeBlockEntity::new, BCBlocks.PIPES).build(null));
        TANK = register(registrar, BCConstants.FACTORY, "tank", BlockEntityType.Builder.of(TankBlockEntity::new, BCBlocks.TANK).build(null));
        PUMP = register(registrar, BCConstants.FACTORY, "pump", BlockEntityType.Builder.of(TankBlockEntity.PumpBlockEntity::new, BCBlocks.PUMP).build(null));
        MINING_WELL = register(registrar, BCConstants.FACTORY, "mining_well", BlockEntityType.Builder.of((pos, state) -> new DiggerBlockEntity(pos, state, false), BCBlocks.MINING_WELL).build(null));
        QUARRY = register(registrar, BCConstants.BUILDERS, "quarry", BlockEntityType.Builder.of((pos, state) -> new DiggerBlockEntity(pos, state, true), BCBlocks.QUARRY).build(null));
        FILLER = register(registrar, BCConstants.BUILDERS, "filler", BlockEntityType.Builder.of((pos, state) -> new PlacerBlockEntity(pos, state, false), BCBlocks.FILLER).build(null));
        BUILDER = register(registrar, BCConstants.BUILDERS, "builder", BlockEntityType.Builder.of((pos, state) -> new PlacerBlockEntity(pos, state, true), BCBlocks.BUILDER).build(null));
        LASER = register(registrar, BCConstants.SILICON, "laser", BlockEntityType.Builder.of(LaserBlockEntity::new, BCBlocks.LASER).build(null));
        ASSEMBLY = register(registrar, BCConstants.SILICON, "assembly_table", BlockEntityType.Builder.of(LaserBlockEntity.AssemblyBlockEntity::new, BCBlocks.ASSEMBLY).build(null));
        ZONE_PLANNER = register(registrar, BCConstants.ROBOTICS, "zone_planner", BlockEntityType.Builder.of(ZonePlannerBlockEntity::new, BCBlocks.ZONE_PLANNER).build(null));
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityType<T> register(BiConsumer<ResourceLocation, BlockEntityType<?>> registrar, String namespace, String path, BlockEntityType<T> type) {
        registrar.accept(BCConstants.id(namespace, path), type);
        return type;
    }
}
