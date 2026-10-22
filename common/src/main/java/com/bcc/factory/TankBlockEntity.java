/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.factory;

import com.bcc.api.mj.Mj;
import com.bcc.api.world.WorldOps;
import com.bcc.api.world.WorldTransforming;
import com.bcc.blockentity.MachineBlockEntity;
import com.bcc.fluid.FluidBuffer;
import com.bcc.fluid.HasFluid;
import com.bcc.menu.BCMenu;
import com.bcc.registry.BCBlockEntities;
import com.bcc.registry.BCMenus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.MenuProvider;

public class TankBlockEntity extends MachineBlockEntity implements HasFluid, MenuProvider {
    private final FluidBuffer fluid = new FluidBuffer(16_000);

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(BCBlockEntities.TANK, pos, state, 1, 0L);
    }

    @Override
    public FluidBuffer fluid() {
        return fluid;
    }

    public void tick() {
        tickSync();
        if (level == null || level.isClientSide) {
            return;
        }
        ItemStack stack = items.get(0);
        if (stack.is(Items.BUCKET) && fluid.amount() >= 1000) {
            ItemStack filled = fluid.fluid().equals(FluidBuffer.LAVA) ? new ItemStack(Items.LAVA_BUCKET) : new ItemStack(Items.WATER_BUCKET);
            if (fluid.fluid().equals(FluidBuffer.WATER) || fluid.fluid().equals(FluidBuffer.LAVA)) {
                fluid.extract(1000, false);
                items.set(0, filled);
                markSync();
            }
        } else if (stack.is(Items.WATER_BUCKET) && fluid.insert(FluidBuffer.WATER, 1000, true) == 1000) {
            fluid.insert(FluidBuffer.WATER, 1000, false);
            items.set(0, new ItemStack(Items.BUCKET));
            markSync();
        } else if (stack.is(Items.LAVA_BUCKET) && fluid.insert(FluidBuffer.LAVA, 1000, true) == 1000) {
            fluid.insert(FluidBuffer.LAVA, 1000, false);
            items.set(0, new ItemStack(Items.BUCKET));
            markSync();
        }
        pushFluid();
    }

    private void pushFluid() {
        if (fluid.amount() <= 0) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor instanceof HasFluid holder && !(neighbor instanceof TankBlockEntity)) {
                int moved = fluid.extract(250, true);
                moved = holder.fluid().insert(fluid.fluid(), moved, false);
                if (moved > 0) {
                    fluid.extract(moved, false);
                    setChanged();
                    return;
                }
            }
        }
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case 2 -> fluid.amount();
            case 3 -> fluid.fluid().equals(FluidBuffer.LAVA) ? 1 : 0;
            default -> super.get(index);
        };
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        fluid.save(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluid.load(tag);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftfactory.tank");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TankMenu(id, inventory, this);
    }

    public static final class TankMenu extends BCMenu {
        public TankMenu(int id, Inventory inventory, TankBlockEntity tank) {
            super(BCMenus.TANK, id, inventory, tank, tank, 1, tank.getBlockPos(), tank.getBlockState().getBlock());
            addMachineSlot(new Slot(tank, 0, 80, 35));
            addPlayerSlots(inventory);
        }

        public TankMenu(int id, Inventory inventory) {
            super(BCMenus.TANK, id, inventory, new net.minecraft.world.SimpleContainer(1), new net.minecraft.world.inventory.SimpleContainerData(4), 1, null, null);
            addMachineSlot(new Slot(machine, 0, 80, 35));
            addPlayerSlots(inventory);
        }

        public int fluidAmount() { return data(2); }
        public boolean lava() { return data(3) == 1; }
    }

    /** Pump pulls one fluid source per successful WorldTransforming apply. */
    public static final class PumpBlockEntity extends MachineBlockEntity implements HasFluid, MenuProvider {
        private final FluidBuffer fluid = new FluidBuffer(8_000);
        private WorldTransforming.Session session;

        public PumpBlockEntity(BlockPos pos, BlockState state) {
            super(BCBlockEntities.PUMP, pos, state, 0, 10_000L * Mj.MJ);
        }

        @Override
        public FluidBuffer fluid() {
            return fluid;
        }

        public void tick() {
            tickSync();
            if (level == null || level.isClientSide || !(level instanceof net.minecraft.server.level.ServerLevel server)) {
                return;
            }
            if (session == null) {
                session = WorldTransforming.open(server, worldPosition, 2);
            }
            long cost = 10L * Mj.MJ;
            if (session.pending() < 4 && mj.getStored() >= cost) {
                BlockPos target = findSource();
                if (target != null) {
                    session.enqueue(WorldOps.takeFluid(target, state -> {
                        fluid.insert(FluidBuffer.fromVanilla(state.getFluidState().getType()), 1000, false);
                        mj.extract(cost, false);
                    }));
                }
            }
            int applied = session.tick();
            if (applied > 0) {
                level.playSound(null, worldPosition, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.4f, 1f);
                markSync();
            }
            if (fluid.amount() > 0) {
                for (Direction direction : Direction.values()) {
                    BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
                    if (neighbor instanceof TankBlockEntity tank) {
                        int moved = fluid.extract(250, true);
                        moved = tank.fluid.insert(fluid.fluid(), moved, false);
                        if (moved > 0) {
                            fluid.extract(moved, false);
                        }
                    }
                }
            }
        }

        private BlockPos findSource() {
            for (Direction direction : Direction.values()) {
                BlockPos pos = worldPosition.relative(direction);
                var state = level.getBlockState(pos);
                if (state.getFluidState().isSource() && (state.getFluidState().is(Fluids.WATER) || state.getFluidState().is(Fluids.LAVA))) {
                    return pos.immutable();
                }
            }
            BlockPos below = worldPosition.below();
            var state = level.getBlockState(below);
            if (state.getFluidState().isSource()) {
                return below.immutable();
            }
            return null;
        }

        @Override
        public void setRemoved() {
            if (session != null) {
                session.close();
                session = null;
            }
            super.setRemoved();
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> storedMj();
                case 2 -> fluid.amount();
                case 3 -> fluid.fluid().equals(FluidBuffer.LAVA) ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public int getCount() { return 4; }

        @Override
        protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            fluid.save(tag);
        }

        @Override
        protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries);
            fluid.load(tag);
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("block.buildcraftfactory.pump");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new PumpMenu(id, inventory, this);
        }
    }

    public static final class PumpMenu extends BCMenu {
        public PumpMenu(int id, Inventory inventory, PumpBlockEntity pump) {
            super(BCMenus.PUMP, id, inventory, pump, pump, 0, pump.getBlockPos(), pump.getBlockState().getBlock());
            addPlayerSlots(inventory);
        }

        public PumpMenu(int id, Inventory inventory) {
            super(BCMenus.PUMP, id, inventory, new net.minecraft.world.SimpleContainer(0), new net.minecraft.world.inventory.SimpleContainerData(4), 0, null, null);
            addPlayerSlots(inventory);
        }

        public int energy() { return data(0); }
        public int fluidAmount() { return data(2); }
        public boolean lava() { return data(3) == 1; }
    }
}
