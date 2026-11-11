package com.santiagogd.holobuilder.client.gui;

import com.santiagogd.holobuilder.builder.BuildManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HoloMenuScreen extends Screen {

    public HoloMenuScreen() {
        super(Component.literal("HoloBuilder"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 180;
        int buttonHeight = 20;
        int spacing = 28;

        // Construir holograma
        this.addRenderableWidget(Button.builder(Component.literal("Construir holograma"), btn -> {
            BuildManager.buildSelectedPlacement(this.minecraft);
            this.onClose();
        }).bounds(centerX - buttonWidth / 2, centerY - spacing, buttonWidth, buttonHeight).build());

        // Deshacer holograma construido
        this.addRenderableWidget(Button.builder(Component.literal("Deshacer holograma construido"), btn -> {
            BuildManager.undoLastBuild(this.minecraft);
            this.onClose();
        }).bounds(centerX - buttonWidth / 2, centerY, buttonWidth, buttonHeight).build());

        // Cerrar menú
        this.addRenderableWidget(Button.builder(Component.literal("Cerrar menú"), btn -> {
            this.onClose();
        }).bounds(centerX - buttonWidth / 2, centerY + spacing, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
