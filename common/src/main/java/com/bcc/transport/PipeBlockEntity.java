/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.transport;

import java.util.ArrayList;
import java.util.List;

import com.bcc.api.mj.Mj;
import com.bcc.api.mj.MjStorage;
import com.bcc.api.pipe.PipeFlow;
import com.bcc.blockentity.MachineBlockEntity;
import com.bcc.energy.EngineBlockEntity;
import com.bcc.fluid.FluidBuffer;
import com.bcc.fluid.HasFluid;
import com.bcc.menu.BCMenu;
import com.bcc.registry.BCBlockEntities;
import com.bcc.registry.BCMenus;
import com.bcc.util.ItemInsert;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.MenuProvider;

public class PipeBlockEntity extends MachineBlockEntity implements WorldlyContainer, HasFluid, MenuProvider {
    private static final int[] NO_SLOTS = new int[0];
    private final FluidBuffer fluid = new FluidBuffer(4000);
    private ItemStack carried = ItemStack.EMPTY;
    private Direction from = Direction.NORTH;
    private Direction to = Direction.SOUTH;
    private float progress;
    private boolean gate;

    public PipeBlockEntity(BlockPos pos, BlockState state) {
        super(BCBlockEntities.PIPE, pos, state, 9, 16L * Mj.MJ);
    }

    public PipeKind kind() {
        return ((PipeBlock) getBlockState().getBlock()).kind();
    }

    public PipeFlow flow() {
        return kind().flow();
    }

    public ItemStack carried() { return carried; }
    public float progress() { return progress; }
    public Direction from() { return from; }
    public Direction to() { return to; }
    public boolean hasGate() { return gate; }
    public boolean hasPayload() {
        return !carried.isEmpty() || fluid.amount() > 0 || getStored() > 0L;
    }

    public void setGate(boolean gate) {
        this.gate = gate;
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        markSync();
    }

    @Override
    public FluidBuffer fluid() {
        return fluid;
    }

    @Override
    public long insert(long max, boolean simulate) {
        if (flow() != PipeFlow.POWER) {
            return 0L;
        }
        return super.insert(max, simulate);
    }

    @Override
    public long extract(long max, boolean simulate) {
        if (flow() != PipeFlow.POWER) {
            return 0L;
        }
        return super.extract(max, simulate);
    }

    public void tick() {
        tickSync();
        if (level == null) {
            return;
        }
        if (level.isClientSide) {
            if (!carried.isEmpty()) {
                progress = Math.min(1f, progress + kind().speed());
            }
            return;
        }
        boolean changed = false;
        if (flow() == PipeFlow.ITEM) {
            changed = tickItems();
        } else if (flow() == PipeFlow.FLUID) {
            changed = tickFluids();
        } else {
            changed = tickPower();
        }
        if (changed || level.getGameTime() % 10L == 0L && hasPayload()) {
            markSync();
        }
    }

    private boolean tickItems() {
        boolean changed = false;
        if (kind().extracts() && carried.isEmpty() && level.getGameTime() % 8L == 0L) {
            for (Direction direction : Direction.values()) {
                if (!connected(direction)) {
                    continue;
                }
                Container container = ItemInsert.containerAt(level, worldPosition.relative(direction));
                if (container == null || container instanceof PipeBlockEntity) {
                    continue;
                }
                ItemStack extracted = ItemInsert.extractOne(container, direction.getOpposite());
                if (!extracted.isEmpty()) {
                    carried = extracted;
                    from = direction;
                    to = direction.getOpposite();
                    progress = 0f;
                    changed = true;
                    break;
                }
            }
        }
        if (!carried.isEmpty()) {
            progress += kind().speed();
            if (progress >= 1f) {
                changed = true;
                Direction next = choose(from);
                if (next == null) {
                    progress = 1f;
                } else if (!passFilter(carried)) {
                    progress = 1f;
                } else {
                    BlockPos destination = worldPosition.relative(next);
                    BlockEntity neighbor = level.getBlockEntity(destination);
                    if (neighbor instanceof PipeBlockEntity pipe && pipe.flow() == PipeFlow.ITEM && pipe.carried.isEmpty()) {
                        pipe.carried = carried;
                        pipe.from = next.getOpposite();
                        pipe.to = next;
                        pipe.progress = 0f;
                        carried = ItemStack.EMPTY;
                        progress = 0f;
                    } else {
                        Container container = ItemInsert.containerAt(level, destination);
                        if (container != null && !(container instanceof PipeBlockEntity)) {
                            ItemStack left = ItemInsert.insert(container, carried, next.getOpposite());
                            carried = left;
                            progress = left.isEmpty() ? 0f : 1f;
                        } else {
                            progress = 1f;
                        }
                    }
                    to = next;
                }
            }
        }
        return changed;
    }

    private boolean tickFluids() {
        boolean changed = false;
        if (kind().extracts()) {
            for (Direction direction : Direction.values()) {
                if (!connected(direction)) {
                    continue;
                }
                BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
                if (neighbor instanceof HasFluid holder && !(neighbor instanceof PipeBlockEntity)) {
                    FluidBuffer other = holder.fluid();
                    int moved = other.extract(250, true);
                    moved = fluid.insert(other.fluid(), moved, false);
                    if (moved > 0) {
                        other.extract(moved, false);
                        changed = true;
                        break;
                    }
                }
            }
        }
        if (fluid.amount() > 0) {
            HasFluid best = null;
            int bestAmount = fluid.amount();
            for (Direction direction : Direction.values()) {
                if (!connected(direction)) {
                    continue;
                }
                BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
                if (neighbor instanceof HasFluid holder && holder.fluid().amount() < bestAmount) {
                    if (holder.fluid().amount() == 0 || holder.fluid().fluid().equals(fluid.fluid())) {
                        best = holder;
                        bestAmount = holder.fluid().amount();
                    }
                }
            }
            if (best != null) {
                int moved = fluid.extract(250, true);
                moved = best.fluid().insert(fluid.fluid(), moved, false);
                if (moved > 0) {
                    fluid.extract(moved, false);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean tickPower() {
        boolean changed = false;
        if (kind().extracts()) {
            for (Direction direction : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
                if (neighbor instanceof EngineBlockEntity engine) {
                    long moved = engine.extract(16L * Mj.MJ, true);
                    moved = insert(moved, false);
                    if (moved > 0) {
                        engine.extract(moved, false);
                        changed = true;
                    }
                }
            }
        }
        if (getStored() > 0L) {
            MjStorage best = null;
            float bestRatio = ratio();
            for (Direction direction : Direction.values()) {
                if (!connected(direction)) {
                    continue;
                }
                BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
                if (neighbor instanceof EngineBlockEntity || !(neighbor instanceof MjStorage storage)) {
                    continue;
                }
                if (neighbor instanceof PipeBlockEntity pipe && pipe.flow() != PipeFlow.POWER) {
                    continue;
                }
                float other = storage.getMax() == 0 ? 1f : storage.getStored() / (float) storage.getMax();
                if (other < bestRatio) {
                    best = storage;
                    bestRatio = other;
                }
            }
            if (best != null) {
                long offer = Math.min(getStored(), 16L * Mj.MJ);
                long moved = best.insert(offer, false);
                if (moved > 0) {
                    extract(moved, false);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private float ratio() {
        return getMax() == 0 ? 1f : getStored() / (float) getMax();
    }

    private boolean connected(Direction direction) {
        return getBlockState().getValue(PipeBlock.side(direction));
    }

    private Direction choose(Direction incoming) {
        List<Direction> options = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (direction == incoming || !connected(direction)) {
                continue;
            }
            if (kind().iron() && direction != getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
                continue;
            }
            options.add(direction);
        }
        if (options.isEmpty()) {
            return null;
        }
        if (options.contains(to) && to != incoming) {
            return to;
        }
        return options.get(level.random.nextInt(options.size()));
    }

    private boolean passFilter(ItemStack stack) {
        if (!kind().diamond()) {
            return true;
        }
        boolean any = false;
        for (int i = 0; i < 9; i++) {
            ItemStack filter = items.get(i);
            if (filter.isEmpty()) {
                continue;
            }
            any = true;
            if (ItemStack.isSameItem(filter, stack)) {
                return true;
            }
        }
        return !any;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return kind().diamond();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Carried", carried.saveOptional(registries));
        tag.putString("From", from.getName());
        tag.putString("To", to.getName());
        tag.putFloat("Progress", progress);
        tag.putBoolean("Gate", gate);
        fluid.save(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        carried = ItemStack.parseOptional(registries, tag.getCompound("Carried"));
        from = Direction.byName(tag.getString("From"));
        to = Direction.byName(tag.getString("To"));
        if (from == null) from = Direction.NORTH;
        if (to == null) to = Direction.SOUTH;
        progress = tag.getFloat("Progress");
        gate = tag.getBoolean("Gate");
        fluid.load(tag);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DiamondPipeMenu(id, inventory, this);
    }

    public static final class DiamondPipeMenu extends BCMenu {
        public DiamondPipeMenu(int id, Inventory inventory, PipeBlockEntity pipe) {
            super(BCMenus.DIAMOND_PIPE, id, inventory, pipe, pipe, 9, pipe.getBlockPos(), pipe.getBlockState().getBlock());
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    addMachineSlot(new Slot(pipe, col + row * 3, 62 + col * 18, 17 + row * 18));
                }
            }
            addPlayerSlots(inventory);
        }

        public DiamondPipeMenu(int id, Inventory inventory) {
            super(BCMenus.DIAMOND_PIPE, id, inventory, new net.minecraft.world.SimpleContainer(9), new net.minecraft.world.inventory.SimpleContainerData(2), 9, null, null);
            Container machine = this.machine;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    addMachineSlot(new Slot(machine, col + row * 3, 62 + col * 18, 17 + row * 18));
                }
            }
            addPlayerSlots(inventory);
        }
    }
}
