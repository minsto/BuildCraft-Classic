/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.fabric;

import com.bcc.client.BCClient;

import net.fabricmc.api.ClientModInitializer;

public final class BuildCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BCClient.init();
    }
}
