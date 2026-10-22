/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.forge;

import com.bcc.BCConstants;
import com.bcc.registry.BCBlockEntities;
import com.bcc.registry.BCBlocks;
import com.bcc.registry.BCEntities;
import com.bcc.registry.BCItems;
import com.bcc.registry.BCMenus;
import com.bcc.registry.BCTabs;
import com.bcc.robotics.BuildCraftRobot;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod(BCConstants.MOD_ID)
public final class BuildCraftForge {
    public BuildCraftForge(IEventBus bus) {
        bus.addListener(this::register);
        bus.addListener(BuildCraftForgeHooks::attributes);
    }

    private void register(RegisterEvent event) {
        ResourceKey<?> key = event.getRegistryKey();
        if (key.equals(Registries.BLOCK)) {
            event.register(Registries.BLOCK, helper -> BCBlocks.register(helper::register));
        } else if (key.equals(Registries.ITEM)) {
            event.register(Registries.ITEM, helper -> BCItems.register(helper::register));
        } else if (key.equals(Registries.BLOCK_ENTITY_TYPE)) {
            event.register(Registries.BLOCK_ENTITY_TYPE, helper -> BCBlockEntities.register(helper::register));
        } else if (key.equals(Registries.MENU)) {
            event.register(Registries.MENU, helper -> BCMenus.register(helper::register));
        } else if (key.equals(Registries.ENTITY_TYPE)) {
            event.register(Registries.ENTITY_TYPE, helper -> BCEntities.register(helper::register));
        } else if (key.equals(Registries.CREATIVE_MODE_TAB)) {
            event.register(Registries.CREATIVE_MODE_TAB, helper -> BCTabs.register(helper::register));
        }
    }
}
