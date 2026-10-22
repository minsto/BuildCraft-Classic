/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.registry;

import java.util.function.BiConsumer;

import com.bcc.BCConstants;
import com.bcc.energy.EngineBlock;
import com.bcc.energy.EngineType;
import com.bcc.machine.WorkerBlock;
import com.bcc.marker.MarkerBlock;
import com.bcc.transport.PipeBlock;
import com.bcc.transport.PipeKind;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BCBlocks {
    public static Block ENGINE_STIRLING;
    public static Block ENGINE_COMBUSTION;
    public static Block MARKER;
    public static final Block[] PIPES = new Block[PipeKind.values().length];
    public static Block TANK;
    public static Block PUMP;
    public static Block MINING_WELL;
    public static Block QUARRY;
    public static Block FILLER;
    public static Block BUILDER;
    public static Block LASER;
    public static Block ASSEMBLY;
    public static Block ZONE_PLANNER;

    private BCBlocks() {}

    public static void register(BiConsumer<ResourceLocation, Block> registrar) {
        BlockBehaviour.Properties metal = BlockBehaviour.Properties.of();
        ENGINE_STIRLING = register(registrar, BCConstants.ENERGY, "engine_stirling", new EngineBlock(EngineType.STIRLING, metal));
        ENGINE_COMBUSTION = register(registrar, BCConstants.ENERGY, "engine_combustion", new EngineBlock(EngineType.COMBUSTION, BlockBehaviour.Properties.of()));
        MARKER = register(registrar, BCConstants.CORE, "marker_volume", new MarkerBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD)));
        for (PipeKind kind : PipeKind.values()) {
            PIPES[kind.ordinal()] = register(registrar, BCConstants.TRANSPORT, kind.id(), new PipeBlock(kind, BlockBehaviour.Properties.of()));
        }
        TANK = worker(registrar, BCConstants.FACTORY, "tank", WorkerBlock.Kind.TANK);
        PUMP = worker(registrar, BCConstants.FACTORY, "pump", WorkerBlock.Kind.PUMP);
        MINING_WELL = worker(registrar, BCConstants.FACTORY, "mining_well", WorkerBlock.Kind.MINING_WELL);
        QUARRY = worker(registrar, BCConstants.BUILDERS, "quarry", WorkerBlock.Kind.QUARRY);
        FILLER = worker(registrar, BCConstants.BUILDERS, "filler", WorkerBlock.Kind.FILLER);
        BUILDER = worker(registrar, BCConstants.BUILDERS, "builder", WorkerBlock.Kind.BUILDER);
        LASER = worker(registrar, BCConstants.SILICON, "laser", WorkerBlock.Kind.LASER);
        ASSEMBLY = worker(registrar, BCConstants.SILICON, "assembly_table", WorkerBlock.Kind.ASSEMBLY);
        ZONE_PLANNER = worker(registrar, BCConstants.ROBOTICS, "zone_planner", WorkerBlock.Kind.ZONE_PLANNER);
    }

    public static PipeBlock pipe(PipeKind kind) {
        return (PipeBlock) PIPES[kind.ordinal()];
    }

    private static Block worker(BiConsumer<ResourceLocation, Block> registrar, String namespace, String path, WorkerBlock.Kind kind) {
        return register(registrar, namespace, path, new WorkerBlock(kind, BlockBehaviour.Properties.of()));
    }

    private static Block register(BiConsumer<ResourceLocation, Block> registrar, String namespace, String path, Block block) {
        registrar.accept(BCConstants.id(namespace, path), block);
        return block;
    }
}
