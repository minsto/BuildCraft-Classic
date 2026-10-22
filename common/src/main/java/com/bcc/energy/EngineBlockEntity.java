/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.energy;

import com.bcc.api.mj.Mj;
import com.bcc.api.mj.MjStorage;
import com.bcc.blockentity.MachineBlockEntity;
import com.bcc.menu.BCMenu;
import com.bcc.registry.BCBlocks;
import com.bcc.registry.BCMenus;
import com.bcc.util.FuelTimes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.MenuProvider;

public class EngineBlockEntity extends MachineBlockEntity implements MenuProvider {
    private int burn;
    private int burnTotal;
    private int water;
    private int sentThisTick;
    private float renderProgress;
    private boolean running;

    public EngineBlockEntity(BlockPos pos, BlockState state) {
        super(com.bcc.registry.BCBlockEntities.ENGINE, pos, state, 2, typeOf(state).capacity());
    }

    private static EngineType typeOf(BlockState state) {
        if (state.getBlock() instanceof EngineBlock engine) {
            return engine.type();
        }
        return EngineType.STIRLING;
    }

    public EngineType type() {
        return typeOf(getBlockState());
    }

    public float renderProgress() {
        return renderProgress;
    }

    public boolean running() {
        return running;
    }

    public void tick() {
        tickSync();
        if (level == null) {
            return;
        }
        sentThisTick = 0;
        EngineType engineType = type();
        boolean wasRunning = running;
        if (!level.isClientSide) {
            if (engineType.needsCoolant()) {
                fillWater();
            }
            if (burn > 0) {
                burn--;
            } else if (canBurn(engineType)) {
                consumeFuel(engineType);
            }
            running = burn > 0 && (!engineType.needsCoolant() || water > 0);
            if (running) {
                if (engineType.needsCoolant()) {
                    water = Math.max(0, water - 1);
                }
                mj.insert(engineType.perTick(), false);
                pushPower();
                if (level.getGameTime() % 20L == 0L) {
                    level.playSound(null, worldPosition, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.4f, engineType == EngineType.COMBUSTION ? 0.7f : 1.0f);
                }
            }
            if (wasRunning != running || level.getGameTime() % 20L == 0L) {
                markSync();
            }
        } else if (running) {
            renderProgress = (renderProgress + 0.05f) % 1.0f;
        }
    }

    private boolean canBurn(EngineType engineType) {
        ItemStack fuel = items.get(0);
        if (engineType.needsCoolant()) {
            return water > 0 && FuelTimes.isCombustionFuel(fuel);
        }
        return FuelTimes.burnTime(fuel) > 0;
    }

    private void consumeFuel(EngineType engineType) {
        ItemStack fuel = items.get(0);
        burnTotal = engineType.needsCoolant() ? 20000 : FuelTimes.burnTime(fuel);
        burn = burnTotal;
        if (fuel.is(Items.LAVA_BUCKET)) {
            items.set(0, new ItemStack(Items.BUCKET));
        } else {
            fuel.shrink(1);
        }
        setChanged();
    }

    private void fillWater() {
        ItemStack stack = items.get(1);
        if (stack.is(Items.WATER_BUCKET) && water <= 3000) {
            water += 1000;
            items.set(1, new ItemStack(Items.BUCKET));
            setChanged();
        }
    }

    private void pushPower() {
        Direction facing = getBlockState().getValue(EngineBlock.FACING);
        BlockEntity front = level.getBlockEntity(worldPosition.relative(facing));
        if (front instanceof MjStorage storage && !(front instanceof EngineBlockEntity) && !(front instanceof com.bcc.transport.PipeBlockEntity)) {
            long moved = storage.insert(Math.min(mj.getStored(), type().perTick()), false);
            mj.extract(moved, false);
            sentThisTick += moved;
        }
    }

    @Override
    public long extract(long max, boolean simulate) {
        long allowed = Math.max(0L, type().perTick() - sentThisTick);
        long moved = mj.extract(Math.min(max, allowed), simulate);
        if (!simulate) {
            sentThisTick += moved;
        }
        return moved;
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case 0 -> burn;
            case 1 -> burnTotal;
            case 2 -> storedMj();
            case 3 -> water;
            case 4 -> running ? 1 : 0;
            default -> 0;
        };
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Burn", burn);
        tag.putInt("BurnTotal", burnTotal);
        tag.putInt("Water", water);
        tag.putBoolean("Running", running);
        tag.putFloat("Progress", renderProgress);
    }

    @Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        burn = tag.getInt("Burn");
        burnTotal = tag.getInt("BurnTotal");
        water = tag.getInt("Water");
        running = tag.getBoolean("Running");
        renderProgress = tag.getFloat("Progress");
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == 1) {
            return stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
        }
        return type().needsCoolant() ? FuelTimes.isCombustionFuel(stack) : FuelTimes.burnTime(stack) > 0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EngineMenu(id, inventory, this);
    }

    public static final class EngineMenu extends BCMenu {
        public EngineMenu(int id, Inventory inventory, EngineBlockEntity be) {
            this(id, inventory, be, be, be.getBlockPos(), be.getBlockState().getBlock());
        }

        public EngineMenu(int id, Inventory inventory) {
            this(id, inventory, new net.minecraft.world.SimpleContainer(2), new net.minecraft.world.inventory.SimpleContainerData(5), null, BCBlocks.ENGINE_STIRLING);
        }

        private EngineMenu(int id, Container player, Container machine, ContainerData data, BlockPos pos, net.minecraft.world.level.block.Block block) {
            super(BCMenus.ENGINE, id, player, machine, data, 2, pos, block);
            addMachineSlot(new Slot(machine, 0, 80, 41) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return FuelTimes.burnTime(stack) > 0 || FuelTimes.isCombustionFuel(stack);
                }
            });
            addMachineSlot(new Slot(machine, 1, 44, 41) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
                }
            });
            addPlayerSlots(player);
        }

        public int burn() { return data(0); }
        public int burnTotal() { return data(1); }
        public int energy() { return data(2); }
        public int water() { return data(3); }
        public boolean running() { return data(4) != 0; }
    }
}
