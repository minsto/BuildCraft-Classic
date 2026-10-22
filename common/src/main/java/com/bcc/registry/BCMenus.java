/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.registry;

import java.util.function.BiConsumer;

import com.bcc.BCConstants;
import com.bcc.energy.EngineBlockEntity;
import com.bcc.factory.TankBlockEntity;
import com.bcc.machine.DiggerBlockEntity;
import com.bcc.machine.PlacerBlockEntity;
import com.bcc.robotics.ZonePlannerBlockEntity;
import com.bcc.silicon.LaserBlockEntity;
import com.bcc.transport.PipeBlockEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class BCMenus {
    public static final MenuType<EngineBlockEntity.EngineMenu> ENGINE = new MenuType<>((id, inv) -> new EngineBlockEntity.EngineMenu(id, inv), FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<PipeBlockEntity.DiamondPipeMenu> DIAMOND_PIPE = new MenuType<>((id, inv) -> new PipeBlockEntity.DiamondPipeMenu(id, inv), FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<TankBlockEntity.TankMenu> TANK = new MenuType<>((id, inv) -> new TankBlockEntity.TankMenu(id, inv), FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<TankBlockEntity.PumpMenu> PUMP = new MenuType<>((id, inv) -> new TankBlockEntity.PumpMenu(id, inv), FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<DiggerBlockEntity.DiggerMenu> QUARRY = new MenuType<>((id, inv) -> new DiggerBlockEntity.DiggerMenu(id, inv, true), FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<DiggerBlockEntity.DiggerMenu> MINING_WELL = new MenuType<>((id, inv) -> new DiggerBlockEntity.DiggerMenu(id, inv, false), FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<PlacerBlockEntity.PlacerMenu> FILLER = new MenuType<>((id, inv) -> new PlacerBlockEntity.PlacerMenu(id, inv, false), FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<PlacerBlockEntity.PlacerMenu> BUILDER = new MenuType<>((id, inv) -> new PlacerBlockEntity.PlacerMenu(id, inv, true), FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<LaserBlockEntity.AssemblyMenu> ASSEMBLY = new MenuType<>((id, inv) -> new LaserBlockEntity.AssemblyMenu(id, inv), FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<ZonePlannerBlockEntity.ZoneMenu> ZONE = new MenuType<>((id, inv) -> new ZonePlannerBlockEntity.ZoneMenu(id, inv), FeatureFlags.DEFAULT_FLAGS);

    private BCMenus() {}

    public static void register(BiConsumer<ResourceLocation, MenuType<?>> registrar) {
        registrar.accept(BCConstants.mod("engine"), ENGINE);
        registrar.accept(BCConstants.mod("diamond_pipe"), DIAMOND_PIPE);
        registrar.accept(BCConstants.mod("tank"), TANK);
        registrar.accept(BCConstants.mod("pump"), PUMP);
        registrar.accept(BCConstants.mod("quarry"), QUARRY);
        registrar.accept(BCConstants.mod("mining_well"), MINING_WELL);
        registrar.accept(BCConstants.mod("filler"), FILLER);
        registrar.accept(BCConstants.mod("builder"), BUILDER);
        registrar.accept(BCConstants.mod("assembly"), ASSEMBLY);
        registrar.accept(BCConstants.mod("zone"), ZONE);
    }
}
