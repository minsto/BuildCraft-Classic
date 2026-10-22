/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;

public final class FluidBuffer {
    public static final ResourceLocation EMPTY = ResourceLocation.fromNamespaceAndPath("minecraft", "empty");
    public static final ResourceLocation WATER = ResourceLocation.fromNamespaceAndPath("minecraft", "water");
    public static final ResourceLocation LAVA = ResourceLocation.fromNamespaceAndPath("minecraft", "lava");

    private final int capacity;
    private ResourceLocation fluid = EMPTY;
    private int amount;

    public FluidBuffer(int capacity) {
        this.capacity = capacity;
    }

    public ResourceLocation fluid() { return fluid; }
    public int amount() { return amount; }
    public int capacity() { return capacity; }

    public int insert(ResourceLocation id, int mb, boolean simulate) {
        if (mb <= 0 || id == null || EMPTY.equals(id)) {
            return 0;
        }
        if (amount > 0 && !fluid.equals(id)) {
            return 0;
        }
        int moved = Math.min(mb, capacity - amount);
        if (!simulate && moved > 0) {
            fluid = id;
            amount += moved;
        }
        return moved;
    }

    public int extract(int mb, boolean simulate) {
        int moved = Math.min(mb, amount);
        if (!simulate && moved > 0) {
            amount -= moved;
            if (amount <= 0) {
                amount = 0;
                fluid = EMPTY;
            }
        }
        return moved;
    }

    public void save(CompoundTag tag) {
        tag.putString("Fluid", fluid.toString());
        tag.putInt("FluidAmount", amount);
    }

    public void load(CompoundTag tag) {
        fluid = tag.contains("Fluid") ? ResourceLocation.parse(tag.getString("Fluid")) : EMPTY;
        amount = tag.getInt("FluidAmount");
        if (amount <= 0) {
            fluid = EMPTY;
            amount = 0;
        }
    }

    public static ResourceLocation fromVanilla(net.minecraft.world.level.material.Fluid fluid) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return WATER;
        }
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return LAVA;
        }
        return EMPTY;
    }
}
