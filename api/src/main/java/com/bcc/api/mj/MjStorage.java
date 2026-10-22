/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.api.mj;

/**
 * A buffer of Minecraft Joules. Amounts are microjoules ({@link Mj#MJ}).
 * Addons should exchange power through this interface instead of writing their own tick loops.
 */
public interface MjStorage {
    long getStored();

    long getMax();

    /** Insert up to {@code max} microjoules. Returns the amount accepted. */
    long insert(long max, boolean simulate);

    /** Remove up to {@code max} microjoules. Returns the amount removed. */
    long extract(long max, boolean simulate);
}
