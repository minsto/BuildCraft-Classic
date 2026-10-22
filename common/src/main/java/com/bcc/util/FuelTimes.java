/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Burn times used by the Stirling engine. The 8.0 jar ships no custom sounds or fuel table. */
public final class FuelTimes {
    private FuelTimes() {}

    public static int burnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return 20000;
        }
        if (stack.is(Items.COAL_BLOCK)) {
            return 16000;
        }
        if (stack.is(Items.BLAZE_ROD)) {
            return 2400;
        }
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
            return 1600;
        }
        if (stack.is(ItemTags.LOGS) || stack.is(ItemTags.PLANKS)) {
            return 300;
        }
        if (stack.is(Items.STICK)) {
            return 100;
        }
        return 0;
    }

    public static boolean isCombustionFuel(ItemStack stack) {
        return stack.is(Items.LAVA_BUCKET);
    }
}
