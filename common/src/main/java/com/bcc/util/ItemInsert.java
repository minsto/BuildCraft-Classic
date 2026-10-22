/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ItemInsert {
    private ItemInsert() {}

    public static ItemStack insert(Container container, ItemStack stack, Direction face) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remaining = stack.copy();
        int[] slots = slots(container, face);
        for (int slot : slots) {
            if (remaining.isEmpty()) {
                break;
            }
            if (!container.canPlaceItem(slot, remaining)) {
                continue;
            }
            if (container instanceof WorldlyContainer worldly && face != null && !worldly.canPlaceItemThroughFace(slot, remaining, face)) {
                continue;
            }
            ItemStack in = container.getItem(slot);
            if (in.isEmpty()) {
                int move = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                container.setItem(slot, remaining.split(move));
            } else if (ItemStack.isSameItemSameComponents(in, remaining)) {
                int move = Math.min(remaining.getCount(), in.getMaxStackSize() - in.getCount());
                if (move > 0) {
                    in.grow(move);
                    remaining.shrink(move);
                    container.setItem(slot, in);
                }
            }
        }
        return remaining;
    }

    public static ItemStack extractOne(Container container, Direction face) {
        for (int slot : slots(container, face)) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (container instanceof WorldlyContainer worldly && face != null && !worldly.canTakeItemThroughFace(slot, stack, face)) {
                continue;
            }
            return container.removeItem(slot, 1);
        }
        return ItemStack.EMPTY;
    }

    public static Container containerAt(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            return container;
        }
        return null;
    }

    public static NonNullList<ItemStack> list(int size) {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        return items;
    }

    private static int[] slots(Container container, Direction face) {
        if (container instanceof WorldlyContainer worldly && face != null) {
            return worldly.getSlotsForFace(face);
        }
        int[] slots = new int[container.getContainerSize()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }
}
