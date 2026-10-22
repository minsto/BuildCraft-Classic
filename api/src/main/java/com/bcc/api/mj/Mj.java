/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.api.mj;

import java.text.DecimalFormat;

/**
 * Minecraft Joules, stored as microjoules. One MJ is {@code 1_000_000} microjoules,
 * the same unit BuildCraft 8 uses.
 */
public final class Mj {
    public static final long MJ = 1_000_000L;
    /** Historical BuildCraft ratio: 1 MJ = 10 RF, so 1 RF = MJ / 10. */
    public static final long MICROS_PER_RF = MJ / 10L;
    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.##");

    private Mj() {}

    public static String format(long microMj) {
        return FORMAT.format(microMj / (double) MJ);
    }

    public static long fromRf(long rf) {
        return rf * MICROS_PER_RF;
    }

    public static long toRf(long microMj) {
        return microMj / MICROS_PER_RF;
    }
}
