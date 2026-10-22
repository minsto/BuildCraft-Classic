/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.registry;

import java.util.function.BiConsumer;

import com.bcc.BCConstants;
import com.bcc.item.GateItem;
import com.bcc.item.RobotItem;
import com.bcc.item.WrenchItem;
import com.bcc.transport.PipeKind;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class BCItems {
    public static Item WRENCH;
    public static Item GEAR_WOOD;
    public static Item GEAR_STONE;
    public static Item GEAR_IRON;
    public static Item GEAR_GOLD;
    public static Item GEAR_DIAMOND;
    public static Item GATE;
    public static Item CHIPSET_REDSTONE;
    public static Item CHIPSET_IRON;
    public static Item CHIPSET_GOLD;
    public static Item CHIPSET_DIAMOND;
    public static Item ROBOT;

    private BCItems() {}

    public static void register(BiConsumer<ResourceLocation, Item> registrar) {
        Item.Properties props = new Item.Properties();
        WRENCH = item(registrar, BCConstants.CORE, "wrench", new WrenchItem(props));
        GEAR_WOOD = item(registrar, BCConstants.CORE, "gear_wood", new Item(props));
        GEAR_STONE = item(registrar, BCConstants.CORE, "gear_stone", new Item(props));
        GEAR_IRON = item(registrar, BCConstants.CORE, "gear_iron", new Item(props));
        GEAR_GOLD = item(registrar, BCConstants.CORE, "gear_gold", new Item(props));
        GEAR_DIAMOND = item(registrar, BCConstants.CORE, "gear_diamond", new Item(props));
        GATE = item(registrar, BCConstants.SILICON, "gate", new GateItem(props));
        CHIPSET_REDSTONE = item(registrar, BCConstants.SILICON, "chipset_redstone", new Item(props));
        CHIPSET_IRON = item(registrar, BCConstants.SILICON, "chipset_iron", new Item(props));
        CHIPSET_GOLD = item(registrar, BCConstants.SILICON, "chipset_gold", new Item(props));
        CHIPSET_DIAMOND = item(registrar, BCConstants.SILICON, "chipset_diamond", new Item(props));
        ROBOT = item(registrar, BCConstants.ROBOTICS, "robot", new RobotItem(props));
        blockItem(registrar, BCConstants.ENERGY, "engine_stirling", BCBlocks.ENGINE_STIRLING);
        blockItem(registrar, BCConstants.ENERGY, "engine_combustion", BCBlocks.ENGINE_COMBUSTION);
        blockItem(registrar, BCConstants.CORE, "marker_volume", BCBlocks.MARKER);
        for (PipeKind kind : PipeKind.values()) {
            blockItem(registrar, BCConstants.TRANSPORT, kind.id(), BCBlocks.PIPES[kind.ordinal()]);
        }
        blockItem(registrar, BCConstants.FACTORY, "tank", BCBlocks.TANK);
        blockItem(registrar, BCConstants.FACTORY, "pump", BCBlocks.PUMP);
        blockItem(registrar, BCConstants.FACTORY, "mining_well", BCBlocks.MINING_WELL);
        blockItem(registrar, BCConstants.BUILDERS, "quarry", BCBlocks.QUARRY);
        blockItem(registrar, BCConstants.BUILDERS, "filler", BCBlocks.FILLER);
        blockItem(registrar, BCConstants.BUILDERS, "builder", BCBlocks.BUILDER);
        blockItem(registrar, BCConstants.SILICON, "laser", BCBlocks.LASER);
        blockItem(registrar, BCConstants.SILICON, "assembly_table", BCBlocks.ASSEMBLY);
        blockItem(registrar, BCConstants.ROBOTICS, "zone_planner", BCBlocks.ZONE_PLANNER);
    }

    private static Item item(BiConsumer<ResourceLocation, Item> registrar, String namespace, String path, Item item) {
        registrar.accept(BCConstants.id(namespace, path), item);
        return item;
    }

    private static void blockItem(BiConsumer<ResourceLocation, Item> registrar, String namespace, String path, Block block) {
        registrar.accept(BCConstants.id(namespace, path), new BlockItem(block, new Item.Properties()));
    }
}
