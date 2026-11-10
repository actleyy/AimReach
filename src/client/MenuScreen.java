package com.example.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {

    public MenuScreen() {
        super(Text.literal("Troll Menu"));
    }

    private static Text aimBtnLabel() {
        return Text.literal("AimBot: " + (ExampleModClient.aimEnabled ? "ON" : "OFF"));
    }

    private static Text aimLabel() {
        return Text.literal("Aim distance: " + ExampleModClient.aimRange + " blocks");
    }

    private static Text reachBtnLabel() {
        return Text.literal("Reach: " + (ExampleModClient.reachEnabled ? "ON" : "OFF"));
    }

    private static Text reachLabel() {
        return Text.literal("Reach distance: " + ExampleModClient.reach + " blocks");
    }

    @Override
    protected void init() {
        int w = 200, h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 60;

        // AimBot on/off
        this.addDrawableChild(ButtonWidget.builder(aimBtnLabel(), b -> {
            ExampleModClient.aimEnabled = !ExampleModClient.aimEnabled;
            b.setMessage(aimBtnLabel());
        }).dimensions(x, y, w, h).build());

        // Aim distance slider (1..20 blocks)
        this.addDrawableChild(new SliderWidget(x, y + 25, w, h, aimLabel(),
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
        this.addDrawableChild(ButtonWidget.builder(reachBtnLabel(), b -> {
            ExampleModClient.reachEnabled = !ExampleModClient.reachEnabled;
            b.setMessage(reachBtnLabel());
        }).dimensions(x, y + 60, w, h).build());

        // Reach distance slider (3..10 blocks, 0.5 steps)
        this.addDrawableChild(new SliderWidget(x, y + 85, w, h, reachLabel(),
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
