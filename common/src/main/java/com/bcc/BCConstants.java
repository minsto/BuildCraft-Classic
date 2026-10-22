/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc;

import net.minecraft.resources.ResourceLocation;

public final class BCConstants {
    public static final String MOD_ID = "buildcraftclassic";
    public static final String CORE = "buildcraftcore";
    public static final String ENERGY = "buildcraftenergy";
    public static final String TRANSPORT = "buildcrafttransport";
    public static final String FACTORY = "buildcraftfactory";
    public static final String BUILDERS = "buildcraftbuilders";
    public static final String SILICON = "buildcraftsilicon";
    public static final String ROBOTICS = "buildcraftrobotics";

    private BCConstants() {}

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation mod(String path) {
        return id(MOD_ID, path);
    }
}
