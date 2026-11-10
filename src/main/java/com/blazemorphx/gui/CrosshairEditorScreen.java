package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.customization.CrosshairManager;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.gui.components.CustomButton;
import com.blazemorphx.gui.components.CustomSlider;
import com.blazemorphx.gui.components.CustomToggle;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class CrosshairEditorScreen extends BlazeScreen {
	public CrosshairEditorScreen(Screen parent) {
		super("Crosshair Editor", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		int x = 24;
		int y = contentTop();
		addDrawableChild(new CustomToggle(x, y, 240, 22, "Custom crosshair", () -> config.customCrosshair, v -> {
			config.customCrosshair = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 24, 240, 22, "Enabled in-game", () -> config.crosshairEnabled, v -> {
			config.crosshairEnabled = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomSlider(x, y + 52, 240, 24, "Size", 2, 20, () -> config.crosshairSize, v -> {
			config.crosshairSize = (float) v;
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomSlider(x, y + 84, 240, 24, "Thickness", 1, 6, () -> config.crosshairThickness, v -> {
			config.crosshairThickness = (float) v;
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomSlider(x, y + 116, 240, 24, "Gap", 0, 12, () -> config.crosshairGap, v -> {
			config.crosshairGap = (float) v;
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomSlider(x, y + 148, 240, 24, "Outline", 0, 3, () -> config.crosshairOutlineThickness, v -> {
			config.crosshairOutlineThickness = (float) v;
			config.crosshairOutline = v > 0.2;
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomToggle(x, y + 180, 240, 22, "Dynamic", () -> config.crosshairDynamic, v -> {
			config.crosshairDynamic = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 204, 240, 22, "Center dot", () -> config.crosshairDot, v -> {
			config.crosshairDot = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 228, 240, 22, "T-shape", () -> config.crosshairTShape, v -> {
			config.crosshairTShape = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomSlider(x, y + 256, 240, 24, "Red", 0, 255, () -> (config.crosshairColor >> 16) & 255, v -> {
			config.crosshairColor = color(v, (config.crosshairColor >> 8) & 255, config.crosshairColor & 255);
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomSlider(x, y + 288, 240, 24, "Green", 0, 255, () -> (config.crosshairColor >> 8) & 255, v -> {
			config.crosshairColor = color((config.crosshairColor >> 16) & 255, v, config.crosshairColor & 255);
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomSlider(x, y + 320, 240, 24, "Blue", 0, 255, () -> config.crosshairColor & 255, v -> {
			config.crosshairColor = color((config.crosshairColor >> 16) & 255, (config.crosshairColor >> 8) & 255, v);
			ConfigManager.save();
		}, "%.0f"));
		int px = 290;
		for (var preset : CrosshairManager.PRESETS.values()) {
			addDrawableChild(new CustomButton(px, contentTop(), 90, 20, preset.name(), () -> CrosshairManager.get().applyPreset(preset.id())));
			px += 96;
		}
		addDrawableChild(new CustomButton(24, height - 36, 90, 22, "Back", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		int px = width - 180;
		int py = contentTop() + 80;
		GlassRenderer.panel(context, px, py, 150, 150);
		FontManager.get().drawCentered(context, "PREVIEW", px + 75, py + 10, ThemeManager.muted());
		CrosshairManager.get().renderPreview(context, px + 75, py + 80);
	}

	private static int color(double r, double g, double b) {
		return 0xFF000000 | ((int) r << 16) | ((int) g << 8) | (int) b;
	}
}
