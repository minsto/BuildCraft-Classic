/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.api.mj;

public final class SimpleMjStorage implements MjStorage {
    private long stored;
    private final long max;

    public SimpleMjStorage(long maxMicro) {
        this.max = maxMicro;
    }

    @Override
    public long getStored() {
        return stored;
    }

    @Override
    public long getMax() {
        return max;
    }

    public void setStored(long stored) {
        this.stored = Math.max(0L, Math.min(max, stored));
    }

    @Override
    public long insert(long maxInsert, boolean simulate) {
        if (maxInsert <= 0L) {
            return 0L;
        }
        long accepted = Math.min(maxInsert, max - stored);
        if (!simulate) {
            stored += accepted;
        }
        return accepted;
    }

    @Override
    public long extract(long maxExtract, boolean simulate) {
        if (maxExtract <= 0L) {
            return 0L;
        }
        long removed = Math.min(maxExtract, stored);
        if (!simulate) {
            stored -= removed;
        }
        return removed;
    }
}
