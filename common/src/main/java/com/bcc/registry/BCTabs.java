/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.registry;

import java.util.function.BiConsumer;

import com.bcc.BCConstants;
import com.bcc.transport.PipeKind;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class BCTabs {
    public static CreativeModeTab MAIN;

    private BCTabs() {}

    public static void register(BiConsumer<ResourceLocation, CreativeModeTab> registrar) {
        MAIN = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.buildcraftclassic"))
                .icon(() -> new ItemStack(BCItems.WRENCH))
                .displayItems((params, output) -> {
                    output.accept(BCItems.WRENCH);
                    output.accept(BCItems.GEAR_WOOD);
                    output.accept(BCItems.GEAR_STONE);
                    output.accept(BCItems.GEAR_IRON);
                    output.accept(BCItems.GEAR_GOLD);
                    output.accept(BCItems.GEAR_DIAMOND);
                    output.accept(BCBlocks.MARKER);
                    output.accept(BCBlocks.ENGINE_STIRLING);
                    output.accept(BCBlocks.ENGINE_COMBUSTION);
                    for (PipeKind kind : PipeKind.values()) {
                        output.accept(BCBlocks.PIPES[kind.ordinal()]);
                    }
                    output.accept(BCItems.GATE);
                    output.accept(BCItems.CHIPSET_REDSTONE);
                    output.accept(BCItems.CHIPSET_IRON);
                    output.accept(BCItems.CHIPSET_GOLD);
                    output.accept(BCItems.CHIPSET_DIAMOND);
                    output.accept(BCBlocks.TANK);
                    output.accept(BCBlocks.PUMP);
                    output.accept(BCBlocks.MINING_WELL);
                    output.accept(BCBlocks.QUARRY);
                    output.accept(BCBlocks.FILLER);
                    output.accept(BCBlocks.BUILDER);
                    output.accept(BCBlocks.LASER);
                    output.accept(BCBlocks.ASSEMBLY);
                    output.accept(BCBlocks.ZONE_PLANNER);
                    output.accept(BCItems.ROBOT);
                })
                .build();
        registrar.accept(BCConstants.mod("main"), MAIN);
    }
}
