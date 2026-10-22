/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.client;

import com.bcc.energy.EngineBlockEntity;
import com.bcc.factory.TankBlockEntity;
import com.bcc.machine.DiggerBlockEntity;
import com.bcc.machine.PlacerBlockEntity;
import com.bcc.menu.BCMenu;
import com.bcc.registry.BCBlockEntities;
import com.bcc.registry.BCEntities;
import com.bcc.registry.BCMenus;
import com.bcc.robotics.BuildCraftRobot;
import com.bcc.silicon.LaserBlockEntity;
import com.bcc.transport.PipeBlockEntity;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class BCClient {
    private BCClient() {}

    public static void init() {
        MenuScreens.register(BCMenus.ENGINE, EngineScreen::new);
        MenuScreens.register(BCMenus.DIAMOND_PIPE, BCClient::diamondScreen);
        MenuScreens.register(BCMenus.TANK, TankScreen::new);
        MenuScreens.register(BCMenus.PUMP, PumpScreen::new);
        MenuScreens.register(BCMenus.QUARRY, DiggerScreen::new);
        MenuScreens.register(BCMenus.MINING_WELL, DiggerScreen::new);
        MenuScreens.register(BCMenus.FILLER, PlacerScreen::new);
        MenuScreens.register(BCMenus.BUILDER, PlacerScreen::new);
        MenuScreens.register(BCMenus.ASSEMBLY, AssemblyScreen::new);
        MenuScreens.register(BCMenus.ZONE, BCClient::zoneScreen);
        BlockEntityRenderers.register(BCBlockEntities.ENGINE, Renderers.EngineRenderer::new);
        BlockEntityRenderers.register(BCBlockEntities.PIPE, Renderers.PipeItemRenderer::new);
        BlockEntityRenderers.register(BCBlockEntities.LASER, Renderers.LaserRenderer::new);
        EntityRenderers.register(BCEntities.ROBOT, Renderers.RobotRenderer::new);
    }

    static boolean combustion(Component title) {
        return key(title).contains("combustion");
    }

    static String key(Component title) {
        if (title.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents contents) {
            return contents.getKey();
        }
        return "";
    }

    private static TextureScreen<PipeBlockEntity.DiamondPipeMenu> diamondScreen(PipeBlockEntity.DiamondPipeMenu menu, Inventory inventory, Component title) {
        return new TextureScreen<>(menu, inventory, title, id("buildcrafttransport", "textures/gui/filtered_buffer.png"));
    }

    private static TextureScreen<com.bcc.robotics.ZonePlannerBlockEntity.ZoneMenu> zoneScreen(com.bcc.robotics.ZonePlannerBlockEntity.ZoneMenu menu, Inventory inventory, Component title) {
        return new TextureScreen<>(menu, inventory, title, id("buildcraftrobotics", "textures/gui/zone_planner.png"));
    }

    static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static class TextureScreen<T extends BCMenu> extends AbstractContainerScreen<T> {
        private final ResourceLocation texture;

        public TextureScreen(T menu, Inventory inventory, Component title, ResourceLocation texture) {
            super(menu, inventory, title);
            this.texture = texture;
            this.imageWidth = 176;
            this.imageHeight = 166;
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
            graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
            super.render(graphics, mouseX, mouseY, partial);
            renderTooltip(graphics, mouseX, mouseY);
        }

        protected void energyBar(GuiGraphics graphics, int x, int y) {
            int filled = Math.min(52, menu.data(0) * 52 / 10000);
            graphics.fill(leftPos + x, topPos + y + (52 - filled), leftPos + x + 6, topPos + y + 52, 0xFFE36B2C);
        }
    }

    public static class EngineScreen extends TextureScreen<EngineBlockEntity.EngineMenu> {
        public EngineScreen(EngineBlockEntity.EngineMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, combustion(title)
                    ? id("buildcraftenergy", "textures/gui/combustion_engine_gui.png")
                    : id("buildcraftenergy", "textures/gui/steam_engine_gui.png"));
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
            super.renderBg(graphics, partial, mouseX, mouseY);
            int total = menu.burnTotal();
            if (total > 0 && menu.burn() > 0) {
                int height = menu.burn() * 14 / total;
                graphics.blit(id("minecraft", "textures/gui/container/furnace.png"), leftPos + 81, topPos + 25 + (14 - height), 176, 14 - height, 14, height, 256, 256);
            }
            energyBar(graphics, 160, 20);
        }
    }

    public static class TankScreen extends TextureScreen<TankBlockEntity.TankMenu> {
        public TankScreen(TankBlockEntity.TankMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, id("buildcraftfactory", "textures/gui/tank.png"));
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
            super.renderBg(graphics, partial, mouseX, mouseY);
            int filled = menu.fluidAmount() * 48 / 16000;
            int color = menu.lava() ? 0xFFFF6A00 : 0xFF3F76E4;
            graphics.fill(leftPos + 80, topPos + 20 + (48 - filled), leftPos + 96, topPos + 68, color);
        }
    }

    public static class PumpScreen extends TextureScreen<TankBlockEntity.PumpMenu> {
        public PumpScreen(TankBlockEntity.PumpMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, id("buildcraftfactory", "textures/gui/tank.png"));
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
            super.renderBg(graphics, partial, mouseX, mouseY);
            energyBar(graphics, 20, 20);
            int filled = menu.fluidAmount() * 48 / 8000;
            graphics.fill(leftPos + 80, topPos + 20 + (48 - filled), leftPos + 96, topPos + 68, menu.lava() ? 0xFFFF6A00 : 0xFF3F76E4);
        }
    }

    public static class DiggerScreen extends TextureScreen<DiggerBlockEntity.DiggerMenu> {
        public DiggerScreen(DiggerBlockEntity.DiggerMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, key(title).contains("quarry")
                    ? id("buildcraftbuilders", "textures/gui/quarry.png")
                    : id("buildcraftfactory", "textures/gui/tank.png"));
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
            super.renderBg(graphics, partial, mouseX, mouseY);
            energyBar(graphics, 20, 17);
        }
    }

    public static class PlacerScreen extends TextureScreen<PlacerBlockEntity.PlacerMenu> {
        public PlacerScreen(PlacerBlockEntity.PlacerMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, key(title).contains("builder")
                    ? id("buildcraftbuilders", "textures/gui/builder.png")
                    : id("buildcraftbuilders", "textures/gui/filler.png"));
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
            super.renderBg(graphics, partial, mouseX, mouseY);
            energyBar(graphics, 160, 17);
        }
    }

    public static class AssemblyScreen extends TextureScreen<LaserBlockEntity.AssemblyMenu> {
        public AssemblyScreen(LaserBlockEntity.AssemblyMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, id("buildcraftsilicon", "textures/gui/assembly_table.png"));
        }

        @Override
        protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
            super.renderBg(graphics, partial, mouseX, mouseY);
            energyBar(graphics, 20, 20);
            int width = Math.min(22, menu.progress() * 22 / 200);
            graphics.fill(leftPos + 90, topPos + 38, leftPos + 90 + width, topPos + 44, 0xFF5CFF6A);
        }
    }
}
