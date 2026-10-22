/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.fabric;

import com.bcc.BCBootstrap;
import com.bcc.registry.BCEntities;
import com.bcc.robotics.BuildCraftRobot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public final class BuildCraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BCBootstrap.registerAll();
        FabricDefaultAttributeRegistry.register(BCEntities.ROBOT, BuildCraftRobot.createAttributes().build());
    }
}
