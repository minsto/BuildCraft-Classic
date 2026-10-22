/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.silicon;

import com.bcc.api.mj.Mj;
import com.bcc.blockentity.MachineBlockEntity;
import com.bcc.menu.BCMenu;
import com.bcc.registry.BCBlockEntities;
import com.bcc.registry.BCItems;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.MenuProvider;

public class LaserBlockEntity extends MachineBlockEntity {
    private BlockPos target = BlockPos.ZERO;
    private boolean active;

    public LaserBlockEntity(BlockPos pos, BlockState state) {
        super(BCBlockEntities.LASER, pos, state, 0, 10_000L * Mj.MJ);
    }

    public BlockPos target() { return target; }
    public boolean active() { return active; }

    public void tick() {
        tickSync();
        if (level == null || level.isClientSide) {
            return;
        }
        BlockPos found = findAssembly();
        active = found != null && getStored() > 0L;
        target = found == null ? BlockPos.ZERO : found;
        if (found != null) {
            BlockEntity be = level.getBlockEntity(found);
            if (be instanceof AssemblyBlockEntity assembly) {
                long moved = Math.min(getStored(), 20L * Mj.MJ);
                long accepted = assembly.insert(moved, false);
                extract(accepted, false);
            }
            if (level.getGameTime() % 40L == 0L) {
                level.playSound(null, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.25f, 1.6f);
            }
        }
        if (level.getGameTime() % 20L == 0L) {
            markSync();
        }
    }

    private BlockPos findAssembly() {
        for (Direction direction : Direction.values()) {
            for (int distance = 1; distance <= 8; distance++) {
                BlockPos pos = worldPosition.relative(direction, distance);
                if (level.getBlockEntity(pos) instanceof AssemblyBlockEntity) {
                    return pos.immutable();
                }
                if (!level.getBlockState(pos).isAir()) {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("Target", target.asLong());
        tag.putBoolean("Active", active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        target = BlockPos.of(tag.getLong("Target"));
        active = tag.getBoolean("Active");
    }

    public static final class AssemblyBlockEntity extends MachineBlockEntity implements MenuProvider {
        private int progress;

        public AssemblyBlockEntity(BlockPos pos, BlockState state) {
            super(BCBlockEntities.ASSEMBLY, pos, state, 4, 10_000L * Mj.MJ);
        }

        public int progress() { return progress; }

        public void tick() {
            tickSync();
            if (level == null || level.isClientSide) {
                return;
            }
            Recipe recipe = match();
            if (recipe == null || mj.getStored() < Mj.MJ) {
                progress = 0;
                return;
            }
            progress++;
            mj.extract(Mj.MJ, false);
            if (progress >= recipe.costMj) {
                for (Item ingredient : recipe.inputs()) {
                    consume(ingredient);
                }
                ItemStack out = items.get(3);
                if (out.isEmpty()) {
                    items.set(3, new ItemStack(recipe.output()));
                } else {
                    out.grow(1);
                }
                progress = 0;
                setChanged();
            }
            if (level.getGameTime() % 10L == 0L) {
                markSync();
            }
        }

        private void consume(Item item) {
            for (int i = 0; i < 3; i++) {
                if (items.get(i).is(item)) {
                    items.get(i).shrink(1);
                    return;
                }
            }
        }

        private Recipe match() {
            if (!items.get(3).isEmpty() && items.get(3).getCount() >= items.get(3).getMaxStackSize()) {
                return null;
            }
            for (Recipe recipe : Recipe.values()) {
                if (!items.get(3).isEmpty() && !items.get(3).is(recipe.output())) {
                    continue;
                }
                boolean ok = true;
                for (Item ingredient : recipe.inputs()) {
                    if (!has(ingredient)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    return recipe;
                }
            }
            return null;
        }

        private boolean has(Item item) {
            for (int i = 0; i < 3; i++) {
                if (items.get(i).is(item)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int get(int index) {
            return index == 6 ? progress : super.get(index);
        }

        @Override
        public int getCount() { return 7; }

        @Override
        protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            tag.putInt("Progress", progress);
        }

        @Override
        protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries);
            progress = tag.getInt("Progress");
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("block.buildcraftsilicon.assembly_table");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new AssemblyMenu(id, inventory, this);
        }
    }

    public enum Recipe {
        REDSTONE(50) {
            @Override Item[] inputs() { return new Item[] {Items.REDSTONE}; }
            @Override Item output() { return BCItems.CHIPSET_REDSTONE; }
        },
        IRON(100) {
            @Override Item[] inputs() { return new Item[] {BCItems.CHIPSET_REDSTONE, Items.IRON_INGOT}; }
            @Override Item output() { return BCItems.CHIPSET_IRON; }
        },
        GOLD(150) {
            @Override Item[] inputs() { return new Item[] {BCItems.CHIPSET_IRON, Items.GOLD_INGOT}; }
            @Override Item output() { return BCItems.CHIPSET_GOLD; }
        },
        DIAMOND(200) {
            @Override Item[] inputs() { return new Item[] {BCItems.CHIPSET_GOLD, Items.DIAMOND}; }
            @Override Item output() { return BCItems.CHIPSET_DIAMOND; }
        };

        final int costMj;

        Recipe(int costMj) {
            this.costMj = costMj;
        }

        abstract Item[] inputs();
        abstract Item output();
    }

    public static final class AssemblyMenu extends BCMenu {
        public AssemblyMenu(int id, Inventory inventory, AssemblyBlockEntity be) {
            super(BCMenus.ASSEMBLY, id, inventory, be, be, 4, be.getBlockPos(), be.getBlockState().getBlock());
            addMachineSlot(new Slot(be, 0, 44, 35));
            addMachineSlot(new Slot(be, 1, 62, 35));
            addMachineSlot(new Slot(be, 2, 80, 35));
            addMachineSlot(new Slot(be, 3, 116, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) { return false; }
            });
            addPlayerSlots(inventory);
        }

        public AssemblyMenu(int id, Inventory inventory) {
            super(BCMenus.ASSEMBLY, id, inventory, new net.minecraft.world.SimpleContainer(4), new net.minecraft.world.inventory.SimpleContainerData(7), 4, null, null);
            addMachineSlot(new Slot(machine, 0, 44, 35));
            addMachineSlot(new Slot(machine, 1, 62, 35));
            addMachineSlot(new Slot(machine, 2, 80, 35));
            addMachineSlot(new Slot(machine, 3, 116, 35));
            addPlayerSlots(inventory);
        }

        public int energy() { return data(0); }
        public int progress() { return data(6); }
    }
}
