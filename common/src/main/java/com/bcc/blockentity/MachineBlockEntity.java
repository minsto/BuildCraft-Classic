/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.blockentity;

import com.bcc.api.mj.Mj;
import com.bcc.api.mj.MjStorage;
import com.bcc.api.mj.SimpleMjStorage;
import com.bcc.util.ItemInsert;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Inventory, MJ buffer, container-data for menus, and throttled block-entity sync. */
public abstract class MachineBlockEntity extends BlockEntity implements Container, ContainerData, MjStorage {
    protected final NonNullList<ItemStack> items;
    protected final SimpleMjStorage mj;
    private int syncCooldown;

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots, long mjCapacity) {
        super(type, pos, state);
        this.items = NonNullList.withSize(slots, ItemStack.EMPTY);
        this.mj = new SimpleMjStorage(mjCapacity);
    }

    public void give(ItemStack stack) {
        ItemStack left = ItemInsert.insert(this, stack, null);
        if (!left.isEmpty() && level != null) {
            net.minecraft.world.level.block.Block.popResource(level, worldPosition, left);
        }
    }

    protected void markSync() {
        setChanged();
        if (level instanceof ServerLevel server && syncCooldown <= 0) {
            server.getChunkSource().blockChanged(worldPosition);
            syncCooldown = 10;
        }
    }

    protected void tickSync() {
        if (syncCooldown > 0) {
            syncCooldown--;
        }
    }

    @Override
    public long getStored() {
        return mj.getStored();
    }

    @Override
    public long getMax() {
        return mj.getMax();
    }

    @Override
    public long insert(long max, boolean simulate) {
        long moved = mj.insert(max, simulate);
        if (moved > 0 && !simulate) {
            setChanged();
        }
        return moved;
    }

    @Override
    public long extract(long max, boolean simulate) {
        long moved = mj.extract(max, simulate);
        if (moved > 0 && !simulate) {
            setChanged();
        }
        return moved;
    }

    public int storedMj() {
        return (int) Math.min(Integer.MAX_VALUE, mj.getStored() / Mj.MJ);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case 0 -> storedMj();
            case 1 -> (int) (mj.getMax() / Mj.MJ);
            default -> 0;
        };
    }

    @Override
    public void set(int index, int value) {
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putLong("Mj", mj.getStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        mj.setStored(tag.getLong("Mj"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
