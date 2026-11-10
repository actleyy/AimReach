package com.example.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MenuScreen extends Screen {

    public MenuScreen() {
        super(Component.literal("Troll Menu"));
    }

    private static Component aimBtnLabel() {
        return Component.literal("AimBot: " + (ExampleModClient.aimEnabled ? "ON" : "OFF"));
    }

    private static Component aimLabel() {
        return Component.literal("Aim distance: " + ExampleModClient.aimRange + " blocks");
    }

    private static Component reachBtnLabel() {
        return Component.literal("Reach: " + (ExampleModClient.reachEnabled ? "ON" : "OFF"));
    }

    private static Component reachLabel() {
        return Component.literal("Reach distance: " + ExampleModClient.reach + " blocks");
    }

    @Override
    protected void init() {
        int w = 200, h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 60;

        // AimBot on/off
        this.addRenderableWidget(Button.builder(aimBtnLabel(), b -> {
            ExampleModClient.aimEnabled = !ExampleModClient.aimEnabled;
            b.setMessage(aimBtnLabel());
        }).bounds(x, y, w, h).build());

        // Aim distance slider (1..20 blocks)
        this.addRenderableWidget(new AbstractSliderButton(x, y + 25, w, h, aimLabel(),
                (ExampleModClient.aimRange - 1) / 19.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(aimLabel());
            }

            @Override
            protected void applyValue() {
                ExampleModClient.aimRange = (int) Math.round(1 + this.value * 19);
            }
        });

        // Reach on/off
        this.addRenderableWidget(Button.builder(reachBtnLabel(), b -> {
            ExampleModClient.reachEnabled = !ExampleModClient.reachEnabled;
            b.setMessage(reachBtnLabel());
        }).bounds(x, y + 60, w, h).build());

        // Reach distance slider (3..10 blocks, 0.5 steps)
        this.addRenderableWidget(new AbstractSliderButton(x, y + 85, w, h, reachLabel(),
                (ExampleModClient.reach - 3.0) / 7.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(reachLabel());
            }

            @Override
            protected void applyValue() {
                ExampleModClient.reach = Math.round((3.0 + this.value * 7.0) * 2.0) / 2.0;
            }
        });
    }
}
