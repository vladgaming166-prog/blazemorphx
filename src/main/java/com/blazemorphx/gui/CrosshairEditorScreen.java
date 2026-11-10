package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.customization.CrosshairManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class CrosshairEditorScreen extends BlazeScreen {
	public CrosshairEditorScreen(Screen parent) {
		super("Crosshair Editor", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		int x = width / 2 - 155;
		int y = contentTop();
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.customCrosshair)
			.build(x, y, 150, 20, Text.literal("Custom Crosshair"), (btn, value) -> {
				config.customCrosshair = value;
				ConfigManager.save();
			}));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.crosshairEnabled)
			.build(x + 160, y, 150, 20, Text.literal("In-game"), (btn, value) -> {
				config.crosshairEnabled = value;
				ConfigManager.save();
			}));
		y += 24;
		addDrawableChild(new VanillaSlider(x, y, 150, 20, "Size", 2, 20, () -> config.crosshairSize, v -> config.crosshairSize = (float) v, "%.0f"));
		addDrawableChild(new VanillaSlider(x + 160, y, 150, 20, "Thickness", 1, 6, () -> config.crosshairThickness, v -> config.crosshairThickness = (float) v, "%.0f"));
		y += 24;
		addDrawableChild(new VanillaSlider(x, y, 150, 20, "Gap", 0, 12, () -> config.crosshairGap, v -> config.crosshairGap = (float) v, "%.0f"));
		addDrawableChild(new VanillaSlider(x + 160, y, 150, 20, "Outline", 0, 4, () -> config.crosshairOutlineThickness, v -> {
			config.crosshairOutlineThickness = (float) v;
			config.crosshairOutline = v > 0.2;
		}, "%.0f"));
		y += 24;
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.crosshairDynamic)
			.build(x, y, 150, 20, Text.literal("Dynamic"), (btn, value) -> {
				config.crosshairDynamic = value;
				ConfigManager.save();
			}));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.crosshairDot)
			.build(x + 160, y, 150, 20, Text.literal("Center Dot"), (btn, value) -> {
				config.crosshairDot = value;
				ConfigManager.save();
			}));
		y += 24;
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.crosshairTShape)
			.build(x, y, 150, 20, Text.literal("T-Shape"), (btn, value) -> {
				config.crosshairTShape = value;
				ConfigManager.save();
			}));
		addDrawableChild(new VanillaSlider(x + 160, y, 150, 20, "Opacity", 20, 255, () -> config.crosshairOpacity, v -> config.crosshairOpacity = (int) Math.round(v), "%.0f"));
		y += 24;
		addDrawableChild(new VanillaSlider(x, y, 150, 20, "Red", 0, 255, () -> (config.crosshairColor >> 16) & 255, v -> config.crosshairColor = rgb(v, green(config.crosshairColor), blue(config.crosshairColor)), "%.0f"));
		addDrawableChild(new VanillaSlider(x + 160, y, 150, 20, "Green", 0, 255, () -> (config.crosshairColor >> 8) & 255, v -> config.crosshairColor = rgb(red(config.crosshairColor), v, blue(config.crosshairColor)), "%.0f"));
		y += 24;
		addDrawableChild(new VanillaSlider(x, y, 150, 20, "Blue", 0, 255, () -> config.crosshairColor & 255, v -> config.crosshairColor = rgb(red(config.crosshairColor), green(config.crosshairColor), v), "%.0f"));
		addDrawableChild(new VanillaSlider(x + 160, y, 150, 20, "Outline R", 0, 255, () -> (config.crosshairOutlineColor >> 16) & 255, v -> config.crosshairOutlineColor = rgb(v, green(config.crosshairOutlineColor), blue(config.crosshairOutlineColor)), "%.0f"));
		y += 28;
		int px = x;
		for (var preset : CrosshairManager.PRESETS.values()) {
			addDrawableChild(button(px, y, 74, 20, preset.name(), () -> {
				CrosshairManager.get().applyPreset(preset.id());
				clearAndInit();
			}));
			px += 78;
		}
		addDrawableChild(button(x, height - 28, 150, 20, "Reset Defaults", () -> {
			CrosshairManager.get().applyPreset("classic");
			clearAndInit();
		}));
		addDrawableChild(button(x + 160, height - 28, 150, 20, "Done", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		int px = width / 2 + 170;
		int py = contentTop() + 40;
		context.fill(px, py, px + 90, py + 90, 0xFF101010);
		context.drawCenteredTextWithShadow(textRenderer, "Preview", px + 45, py + 6, 0xFFA0A0A0);
		CrosshairManager.get().renderPreview(context, px + 45, py + 50);
	}

	@Override
	public void removed() {
		ConfigManager.save();
	}

	private static int red(int color) {
		return (color >> 16) & 255;
	}

	private static int green(int color) {
		return (color >> 8) & 255;
	}

	private static int blue(int color) {
		return color & 255;
	}

	private static int rgb(double r, double g, double b) {
		return 0xFF000000 | ((int) r << 16) | ((int) g << 8) | (int) b;
	}
}
