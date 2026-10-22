/* Copyright (c) 2016-2018 SpaceToad and the BuildCraft team
 * Copyright (c) 2026 BuildCraft Classic contributors
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.bcc.client;

import com.bcc.energy.EngineBlockEntity;
import com.bcc.robotics.BuildCraftRobot;
import com.bcc.silicon.LaserBlockEntity;
import com.bcc.transport.PipeBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public final class Renderers {
    private Renderers() {}

    public static final class EngineRenderer implements BlockEntityRenderer<EngineBlockEntity> {
        private static final ResourceLocation TRUNK = ResourceLocation.fromNamespaceAndPath("buildcraftlib", "textures/blocks/engine/trunk_blue.png");

        public EngineRenderer(BlockEntityRendererProvider.Context context) {}

        @Override
        public void render(EngineBlockEntity engine, float partial, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
            if (!engine.running() && engine.renderProgress() <= 0f) {
                return;
            }
            float lift = (float) (Math.sin((engine.renderProgress() + partial * 0.05f) * Math.PI * 2) * 0.15 + 0.2);
            pose.pushPose();
            pose.translate(0.25, 0.25 + lift, 0.25);
            pose.scale(0.5f, 0.35f, 0.5f);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(TRUNK));
            Cube.draw(pose, consumer, light);
            pose.popPose();
        }
    }

    public static final class PipeItemRenderer implements BlockEntityRenderer<PipeBlockEntity> {
        public PipeItemRenderer(BlockEntityRendererProvider.Context context) {}

        @Override
        public void render(PipeBlockEntity pipe, float partial, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
            if (pipe.carried().isEmpty()) {
                return;
            }
            Direction to = pipe.to();
            float distance = Math.min(1f, pipe.progress() + partial * pipe.kind().speed());
            double x = 0.5 + to.getStepX() * (distance - 0.5) * 0.5;
            double y = 0.5 + to.getStepY() * (distance - 0.5) * 0.5;
            double z = 0.5 + to.getStepZ() * (distance - 0.5) * 0.5;
            pose.pushPose();
            pose.translate(x, y, z);
            pose.scale(0.4f, 0.4f, 0.4f);
            pose.mulPose(Axis.YP.rotationDegrees((pipe.progress() * 360f)));
            Minecraft.getInstance().getItemRenderer().renderStatic(pipe.carried(), ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, pose, buffer, pipe.getLevel(), 0);
            pose.popPose();
        }
    }

    public static final class LaserRenderer implements BlockEntityRenderer<LaserBlockEntity> {
        private static final ResourceLocation BEAM = ResourceLocation.fromNamespaceAndPath("buildcraftbuilders", "textures/blocks/quarry/laser.png");

        public LaserRenderer(BlockEntityRendererProvider.Context context) {}

        @Override
        public void render(LaserBlockEntity laser, float partial, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
            if (!laser.active() || laser.target().equals(net.minecraft.core.BlockPos.ZERO)) {
                return;
            }
            Vec3 start = Vec3.atCenterOf(laser.getBlockPos());
            Vec3 end = Vec3.atCenterOf(laser.target());
            Vec3 delta = end.subtract(start);
            pose.pushPose();
            pose.translate(0.5, 0.8, 0.5);
            VertexConsumer consumer = buffer.getBuffer(RenderType.beaconBeam(BEAM, true));
            float length = (float) delta.length();
            pose.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.atan2(delta.x, delta.z))));
            pose.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(Math.atan2(-delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)))));
            pose.scale(0.08f, 0.08f, length);
            Cube.draw(pose, consumer, Light);
            pose.popPose();
        }

        @Override
        public boolean shouldRenderOffScreen(LaserBlockEntity laser) {
            return laser.active();
        }

        private static final int Light = 0xF000F0;
    }

    public static final class RobotRenderer extends EntityRenderer<BuildCraftRobot> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("buildcraftbuilders", "textures/robot.png");

        public RobotRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(BuildCraftRobot robot, float yaw, float partial, PoseStack pose, MultiBufferSource buffer, int light) {
            pose.pushPose();
            pose.translate(0, 0.45, 0);
            pose.scale(0.6f, 0.6f, 0.6f);
            int packed = LevelRenderer.getLightColor(robot.level(), robot.blockPosition());
            Cube.draw(pose, buffer.getBuffer(RenderType.entityCutout(TEXTURE)), packed);
            pose.popPose();
            super.render(robot, yaw, partial, pose, buffer, light);
        }

        @Override
        public ResourceLocation getTextureLocation(BuildCraftRobot robot) {
            return TEXTURE;
        }
    }

    /** Unit cube centered so callers can translate and scale it. */
    static final class Cube {
        static void draw(PoseStack pose, VertexConsumer consumer, int light) {
            PoseStack.Pose entry = pose.last();
            face(consumer, entry, light, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1);
            face(consumer, entry, light, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0);
            face(consumer, entry, light, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0);
            face(consumer, entry, light, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1);
            face(consumer, entry, light, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1);
            face(consumer, entry, light, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0);
        }

        private static void face(VertexConsumer consumer, PoseStack.Pose pose, int light, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
            vertex(consumer, pose, light, x1, y1, z1, 0, 1);
            vertex(consumer, pose, light, x2, y2, z2, 0, 0);
            vertex(consumer, pose, light, x3, y3, z3, 1, 0);
            vertex(consumer, pose, light, x4, y4, z4, 1, 1);
        }

        private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int light, float x, float y, float z, float u, float v) {
            consumer.addVertex(pose, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }
}
