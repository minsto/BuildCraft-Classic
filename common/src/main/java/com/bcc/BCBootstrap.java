/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc;

import java.util.function.BiConsumer;

import com.bcc.registry.BCBlockEntities;
import com.bcc.registry.BCBlocks;
import com.bcc.registry.BCEntities;
import com.bcc.registry.BCItems;
import com.bcc.registry.BCMenus;
import com.bcc.registry.BCTabs;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCBootstrap {
    private BCBootstrap() {}

    public static void registerAll() {
        register(
                (id, block) -> Registry.register(BuiltInRegistries.BLOCK, id, block),
                (id, item) -> Registry.register(BuiltInRegistries.ITEM, id, item),
                (id, type) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type),
                (id, type) -> Registry.register(BuiltInRegistries.MENU, id, type),
                (id, type) -> Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type),
                (id, tab) -> Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab)
        );
    }

    public static void register(
            BiConsumer<ResourceLocation, Block> blocks,
            BiConsumer<ResourceLocation, Item> items,
            BiConsumer<ResourceLocation, BlockEntityType<?>> blockEntities,
            BiConsumer<ResourceLocation, MenuType<?>> menus,
            BiConsumer<ResourceLocation, EntityType<?>> entities,
            BiConsumer<ResourceLocation, CreativeModeTab> tabs
    ) {
        BCBlocks.register(blocks);
        BCItems.register(items);
        BCBlockEntities.register(blockEntities);
        BCMenus.register(menus);
        BCEntities.register(entities);
        BCTabs.register(tabs);
    }
}
