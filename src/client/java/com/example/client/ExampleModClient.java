package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
    private static final float SMOOTHING = 0.2f;   // 0..1, chhota = zyada smooth
    private static final float MAX_STEP = 8.0f;    // max degrees per tick

    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("modid", "troll"));
        openKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.troll.open", GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                client.setScreen(new MenuScreen());
            }

            ClientPlayerEntity player = client.player;
            if (player == null || client.world == null) return;

            if (aimEnabled && client.currentScreen == null) {
                aim(client, player);
            }
            applyReach(client, player);
        });
    }

    private static void aim(MinecraftClient client, ClientPlayerEntity player) {
        Entity best = null;
        double bestDist = (double) aimRange * aimRange;

        for (Entity e : client.world.getEntities()) {
            if (!(e instanceof LivingEntity) || e instanceof ArmorStandEntity) continue;
            if (e == player || !e.isAlive() || e.isSpectator()) continue;
            double d = player.squaredDistanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        if (best == null) return;

        Vec3d diff = best.getEyePos().subtract(player.getEyePos());
        double flat = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetYaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(diff.y, flat)));

        float dYaw = MathHelper.wrapDegrees(targetYaw - player.getYaw());
        float dPitch = targetPitch - player.getPitch();

        if (Math.abs(dYaw) < 0.3f && Math.abs(dPitch) < 0.3f) return;

        float stepYaw = MathHelper.clamp(dYaw * SMOOTHING, -MAX_STEP, MAX_STEP);
        float stepPitch = MathHelper.clamp(dPitch * SMOOTHING, -MAX_STEP, MAX_STEP);

        player.setYaw(player.getYaw() + stepYaw);
        player.setPitch(MathHelper.clamp(player.getPitch() + stepPitch, -90.0f, 90.0f));
    }

    private static void applyReach(MinecraftClient client, ClientPlayerEntity player) {
        double entityRange = reachEnabled ? reach : DEFAULT_ENTITY_REACH;
        double blockRange = reachEnabled ? Math.max(reach, DEFAULT_BLOCK_REACH) : DEFAULT_BLOCK_REACH;

        setReach(player, entityRange, blockRange);

        // Singleplayer / LAN host: integrated server bhi reach check karta hai
        var server = client.getServer();
        if (server != null) {
            UUID id = player.getUuid();
            server.execute(() -> {
                ServerPlayerEntity sp = server.getPlayerManager().getPlayer(id);
                if (sp != null) setReach(sp, entityRange, blockRange);
            });
        }
    }

    private static void setReach(PlayerEntity p, double entityRange, double blockRange) {
        EntityAttributeInstance ei = p.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE);
        if (ei != null && ei.getBaseValue() != entityRange) ei.setBaseValue(entityRange);
        EntityAttributeInstance bi = p.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
        if (bi != null && bi.getBaseValue() != blockRange) bi.setBaseValue(blockRange);
    }
}
