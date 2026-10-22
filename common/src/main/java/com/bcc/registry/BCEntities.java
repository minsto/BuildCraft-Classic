/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.registry;

import java.util.function.BiConsumer;

import com.bcc.BCConstants;
import com.bcc.robotics.BuildCraftRobot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class BCEntities {
    public static EntityType<BuildCraftRobot> ROBOT;

    private BCEntities() {}

    public static void register(BiConsumer<ResourceLocation, EntityType<?>> registrar) {
        ResourceLocation id = BCConstants.id(BCConstants.ROBOTICS, "robot");
        ROBOT = EntityType.Builder.of(BuildCraftRobot::new, MobCategory.MISC).sized(0.6f, 0.9f).clientTrackingRange(8).build(id.toString());
        registrar.accept(id, ROBOT);
    }
}
