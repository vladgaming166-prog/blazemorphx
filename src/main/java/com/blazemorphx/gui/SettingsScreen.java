package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.customization.BackgroundManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class SettingsScreen extends BlazeScreen {
	public SettingsScreen(Screen parent) {
		super("Blaze Morphx Settings", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		int x = width / 2 - 100;
		int y = contentTop();
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.customMainMenu)
			.build(x, y, 200, 20, Text.literal("Custom Main Menu"), (btn, value) -> {
				config.customMainMenu = value;
				ConfigManager.save();
			}));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.animatedBackground)
			.build(x, y + 24, 200, 20, Text.literal("Animated Menu Background"), (btn, value) -> {
				config.animatedBackground = value;
				ConfigManager.save();
				BackgroundManager.get().restart();
			}));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.customCrosshair)
			.build(x, y + 48, 200, 20, Text.literal("Custom Crosshair"), (btn, value) -> {
				config.customCrosshair = value;
				ConfigManager.save();
			}));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.customHud)
			.build(x, y + 72, 200, 20, Text.literal("Custom HUD"), (btn, value) -> {
				config.customHud = value;
				ConfigManager.save();
			}));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.showLogo)
			.build(x, y + 96, 200, 20, Text.literal("Show Title Branding"), (btn, value) -> {
				config.showLogo = value;
				ConfigManager.save();
			}));
		addDrawableChild(button(x, y + 128, 200, 20, "Vanilla Options...", () -> client.setScreen(new OptionsScreen(this, client.options))));
		addDrawableChild(button(x, y + 152, 200, 20, "Reset Blaze Morphx Settings", () -> {
			ConfigManager.reset();
			BackgroundManager.get().restart();
			clearAndInit();
		}));
		addDrawableChild(button(x, height - 28, 200, 20, "Done", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("Right Shift opens this menu in-game."), width / 2, height - 44, 0xFFA0A0A0);
	}
}
