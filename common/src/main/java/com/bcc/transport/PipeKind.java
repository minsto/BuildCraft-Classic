/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.transport;

import com.bcc.api.pipe.PipeFlow;

public enum PipeKind {
    WOOD_ITEM(PipeFlow.ITEM, 0.05f, true, false),
    COBBLE_ITEM(PipeFlow.ITEM, 0.05f, false, false),
    STONE_ITEM(PipeFlow.ITEM, 0.05f, false, true),
    IRON_ITEM(PipeFlow.ITEM, 0.05f, false, false),
    GOLD_ITEM(PipeFlow.ITEM, 0.20f, false, false),
    DIAMOND_ITEM(PipeFlow.ITEM, 0.05f, false, false),
    WOOD_FLUID(PipeFlow.FLUID, 0f, true, false),
    COBBLE_FLUID(PipeFlow.FLUID, 0f, false, false),
    WOOD_POWER(PipeFlow.POWER, 0f, true, false),
    COBBLE_POWER(PipeFlow.POWER, 0f, false, false);

    private final PipeFlow flow;
    private final float speed;
    private final boolean extract;
    private final boolean stone;

    PipeKind(PipeFlow flow, float speed, boolean extract, boolean stone) {
        this.flow = flow;
        this.speed = speed;
        this.extract = extract;
        this.stone = stone;
    }

    public PipeFlow flow() { return flow; }
    public float speed() { return speed; }
    public boolean extracts() { return extract; }
    public boolean stone() { return stone; }
    public boolean iron() { return this == IRON_ITEM; }
    public boolean diamond() { return this == DIAMOND_ITEM; }

    public String texture() {
        return switch (this) {
            case WOOD_ITEM -> "wood_item_clear";
            case COBBLE_ITEM -> "cobblestone_item";
            case STONE_ITEM -> "stone_item";
            case IRON_ITEM -> "iron_item_clear";
            case GOLD_ITEM -> "gold_item";
            case DIAMOND_ITEM -> "diamond_item";
            case WOOD_FLUID -> "wood_fluid_clear";
            case COBBLE_FLUID -> "cobblestone_fluid";
            case WOOD_POWER -> "wood_power_clear";
            case COBBLE_POWER -> "cobblestone_power";
        };
    }

    public String id() {
        return switch (this) {
            case WOOD_ITEM -> "pipe_wood_item";
            case COBBLE_ITEM -> "pipe_cobble_item";
            case STONE_ITEM -> "pipe_stone_item";
            case IRON_ITEM -> "pipe_iron_item";
            case GOLD_ITEM -> "pipe_gold_item";
            case DIAMOND_ITEM -> "pipe_diamond_item";
            case WOOD_FLUID -> "pipe_wood_fluid";
            case COBBLE_FLUID -> "pipe_cobble_fluid";
            case WOOD_POWER -> "pipe_wood_power";
            case COBBLE_POWER -> "pipe_cobble_power";
        };
    }
}
