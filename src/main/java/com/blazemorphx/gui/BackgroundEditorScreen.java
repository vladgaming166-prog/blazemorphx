package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.customization.BackgroundManager;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.gui.components.CustomButton;
import com.blazemorphx.gui.components.CustomDropdown;
import com.blazemorphx.gui.components.CustomSlider;
import com.blazemorphx.gui.components.CustomToggle;
import com.blazemorphx.render.NotificationRenderer;
import com.blazemorphx.util.FileDialogs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;
import java.util.Set;

public class BackgroundEditorScreen extends BlazeScreen {
	public BackgroundEditorScreen(Screen parent) {
		super("Background Editor", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		int x = 24;
		int y = contentTop();
		addDrawableChild(new CustomToggle(x, y, 260, 22, "Animated background", () -> config.animatedBackground, v -> {
			config.animatedBackground = v;
			ConfigManager.save();
			BackgroundManager.get().restart();
		}));
		addDrawableChild(new CustomDropdown(x, y + 36, 180, 20, "Scaling", List.of("fill", "fit", "stretch"), () -> config.backgroundScale, v -> {
			config.backgroundScale = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomSlider(x, y + 70, 260, 24, "Brightness", 0.3, 1.4, () -> config.backgroundBrightness, v -> {
			config.backgroundBrightness = (float) v;
			ConfigManager.save();
		}, "%.2f"));
		addDrawableChild(new CustomSlider(x, y + 102, 260, 24, "Blur overlay", 0.0, 1.0, () -> config.backgroundBlur, v -> {
			config.backgroundBlur = (float) v;
			ConfigManager.save();
		}, "%.2f"));
		addDrawableChild(new CustomSlider(x, y + 134, 260, 24, "Saturation", 0.0, 1.8, () -> config.backgroundSaturation, v -> {
			config.backgroundSaturation = (float) v;
			ConfigManager.save();
		}, "%.2f"));
		addDrawableChild(new CustomSlider(x, y + 166, 260, 24, "Overlay opacity", 0.0, 0.8, () -> config.overlayOpacity, v -> {
			config.overlayOpacity = (float) v;
			ConfigManager.save();
		}, "%.2f"));
		addDrawableChild(new CustomSlider(x, y + 198, 260, 24, "Video FPS cap", 8, 30, () -> config.videoFpsLimit, v -> {
			config.videoFpsLimit = (int) Math.round(v);
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomSlider(x, y + 230, 260, 24, "Decode width", 480, 1280, () -> config.videoDecodeWidth, v -> {
			config.videoDecodeWidth = (int) Math.round(v);
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomButton(x, y + 268, 160, 22, "Import file", () -> FileDialogs.open("Import background", Set.of("mp4", "webm", "mov", "png", "jpg", "jpeg", "webp"), path -> {
			if (BackgroundManager.get().importBackground(path)) {
				NotificationRenderer.get().push("Background imported", path.getFileName().toString());
			} else {
				NotificationRenderer.get().error("Unsupported or invalid file");
			}
		})));
		addDrawableChild(new CustomButton(x + 170, y + 268, 120, 22, "Reset default", () -> {
			BackgroundManager.get().resetToDefault();
			NotificationRenderer.get().push("Background", "Default underwater video restored");
		}));
		addDrawableChild(new CustomButton(24, height - 36, 90, 22, "Back", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		var file = BackgroundManager.get().resolveCurrentFile();
		FontManager.get().draw(context, "Current: " + (file == null ? "none" : file.getFileName()), 310, contentTop() + 8, ThemeManager.muted());
		FontManager.get().draw(context, "Decoder: " + BackgroundManager.get().video().status(), 310, contentTop() + 22, ThemeManager.text());
		FontManager.get().draw(context, "Library: default MP4 + imported videos/images", 310, contentTop() + 36, ThemeManager.muted());
	}
}
