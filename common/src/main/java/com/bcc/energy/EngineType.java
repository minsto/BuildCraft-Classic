/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.energy;

import com.bcc.api.mj.Mj;

public enum EngineType {
    STIRLING(Mj.MJ, 10_000L * Mj.MJ, false),
    COMBUSTION(4L * Mj.MJ, 10_000L * Mj.MJ, true);

    private final long perTick;
    private final long capacity;
    private final boolean needsCoolant;

    EngineType(long perTick, long capacity, boolean needsCoolant) {
        this.perTick = perTick;
        this.capacity = capacity;
        this.needsCoolant = needsCoolant;
    }

    public long perTick() {
        return perTick;
    }

    public long capacity() {
        return capacity;
    }

    public boolean needsCoolant() {
        return needsCoolant;
    }
}
