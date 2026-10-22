/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Shared slot layout: machine slots first, then the player inventory. */
public abstract class BCMenu extends AbstractContainerMenu {
    protected final Container machine;
    protected final ContainerData data;
    private final int machineSlots;
    private final BlockPos pos;
    private final Block block;

    protected BCMenu(MenuType<?> type, int id, Container playerInventory, Container machine, ContainerData data, int machineSlots, BlockPos pos, Block block) {
        super(type, id);
        this.machine = machine;
        this.data = data;
        this.machineSlots = machineSlots;
        this.pos = pos;
        this.block = block;
        addDataSlots(data);
    }

    protected void addMachineSlot(Slot slot) {
        addSlot(slot);
    }

    /** Call after every machine slot has been added. Player slots are appended. */
    protected void addPlayerSlots(Container playerInventory) {
        if (playerInventory == null) {
            return;
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public int data(int index) {
        return data.get(index);
    }

    @Override
    public boolean stillValid(Player player) {
        if (pos == null || block == null) {
            return true;
        }
        Level level = player.level();
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(level, pos), player, block);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        original = stack.copy();
        int playerStart = machineSlots;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, playerStart, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, machineSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    public int machineSlots() {
        return machineSlots;
    }
}
