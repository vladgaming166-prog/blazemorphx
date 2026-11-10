package com.blazemorphx.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class CustomizationScreen extends BlazeScreen {
	public CustomizationScreen(Screen parent) {
		super("Customization", parent);
	}

	@Override
	protected void init() {
		int x = width / 2 - 100;
		int y = height / 4 + 48;
		addDrawableChild(button(x, y, 200, 20, "Crosshair Editor...", () -> client.setScreen(new CrosshairEditorScreen(this))));
		addDrawableChild(button(x, y + 24, 200, 20, "HUD Editor...", () -> client.setScreen(new HUDCustomizationScreen(this))));
		addDrawableChild(button(x, y + 48, 200, 20, "Menu Background...", () -> client.setScreen(new BackgroundEditorScreen(this))));
		addDrawableChild(button(x, y + 96, 200, 20, "Done", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("Vanilla Minecraft widgets. Settings save automatically."), width / 2, height / 4 + 32, 0xFFA0A0A0);
	}
}
