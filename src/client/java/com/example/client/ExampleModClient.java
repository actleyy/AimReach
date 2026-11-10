package com.example.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class ExampleModClient implements ClientModInitializer {

    // AimBot
    public static boolean aimEnabled = false;
    public static int aimRange = 10;          // blocks (1..20)

    // Reach
    public static boolean reachEnabled = false;
    public static double reach = 6.0;         // blocks (3..10)

    private static final double DEFAULT_ENTITY_REACH = 3.0;
    private static final double DEFAULT_BLOCK_REACH = 4.5;
    private static final float SMOOTHING = 0.2f;   // chhota = zyada smooth
    private static final float MAX_STEP = 8.0f;    // max degrees per tick

    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("modid", "troll"));
        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.troll.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                client.setScreen(new MenuScreen());
            }

            LocalPlayer player = client.player;
            if (player == null || client.level == null) return;

            if (aimEnabled && client.screen == null) {
                aim(client, player);
            }
            applyReach(client, player);
        });
    }

    private static void aim(Minecraft client, LocalPlayer player) {
        Entity best = null;
        double bestDist = (double) aimRange * aimRange;

        for (Entity e : client.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity) || e instanceof ArmorStand) continue;
            if (e == player || !e.isAlive() || e.isSpectator()) continue;
            double d = player.distanceToSqr(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        if (best == null) return;

        Vec3 diff = best.getEyePosition().subtract(player.getEyePosition());
        double flat = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetYaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(diff.y, flat)));

        float dYaw = Mth.wrapDegrees(targetYaw - player.getYRot());
        float dPitch = targetPitch - player.getXRot();

        if (Math.abs(dYaw) < 0.3f && Math.abs(dPitch) < 0.3f) return;

        float stepYaw = Mth.clamp(dYaw * SMOOTHING, -MAX_STEP, MAX_STEP);
        float stepPitch = Mth.clamp(dPitch * SMOOTHING, -MAX_STEP, MAX_STEP);

        player.setYRot(player.getYRot() + stepYaw);
        player.setXRot(Mth.clamp(player.getXRot() + stepPitch, -90.0f, 90.0f));
    }

    private static void applyReach(Minecraft client, LocalPlayer player) {
        double entityRange = reachEnabled ? reach : DEFAULT_ENTITY_REACH;
        double blockRange = reachEnabled ? Math.max(reach, DEFAULT_BLOCK_REACH) : DEFAULT_BLOCK_REACH;

        setReach(player, entityRange, blockRange);

        // Singleplayer / LAN host: integrated server bhi reach check karta hai
        var server = client.getSingleplayerServer();
        if (server != null) {
            UUID id = player.getUUID();
            server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayer(id);
                if (sp != null) setReach(sp, entityRange, blockRange);
            });
        }
    }

    private static void setReach(Player p, double entityRange, double blockRange) {
        AttributeInstance ei = p.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (ei != null && ei.getBaseValue() != entityRange) ei.setBaseValue(entityRange);
        AttributeInstance bi = p.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (bi != null && bi.getBaseValue() != blockRange) bi.setBaseValue(blockRange);
    }
}
