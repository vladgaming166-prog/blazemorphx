package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ConfigProfile;
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
import net.minecraft.client.gui.screen.option.OptionsScreen;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class SettingsScreen extends BlazeScreen {
	public SettingsScreen(Screen parent) {
		super("Blaze Morphx Settings", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		int x = 24;
		int y = contentTop();
		addDrawableChild(new CustomToggle(x, y, 260, 22, "Custom main menu", () -> config.customMainMenu, v -> {
			config.customMainMenu = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 24, 260, 22, "Animated background", () -> config.animatedBackground, v -> {
			config.animatedBackground = v;
			ConfigManager.save();
			BackgroundManager.get().restart();
		}));
		addDrawableChild(new CustomToggle(x, y + 48, 260, 22, "Menu animations", () -> config.menuAnimations, v -> {
			config.menuAnimations = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 72, 260, 22, "Menu music", () -> config.menuMusic, v -> {
			config.menuMusic = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomSlider(x, y + 104, 260, 24, "UI scale", 0.8, 1.4, () -> config.uiScale, v -> {
			config.uiScale = (float) v;
			ConfigManager.save();
		}, "%.2f"));
		addDrawableChild(new CustomDropdown(x, y + 148, 200, 20, "Theme", List.of(
			"Blaze Orange", "Blood Red", "Purple Flame", "Ice Blue", "Pure Black"
		), () -> ThemeManager.current().displayName, value -> {
			for (ThemeManager.ThemeId theme : ThemeManager.ThemeId.values()) {
				if (theme.displayName.equals(value)) {
					ThemeManager.apply(theme);
				}
			}
		}));
		addDrawableChild(new CustomDropdown(x, y + 188, 200, 20, "Language", List.of("en_us"), () -> config.language, v -> {
			config.language = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomSlider(x, y + 228, 260, 24, "Background FPS cap", 8, 30, () -> config.videoFpsLimit, v -> {
			config.videoFpsLimit = (int) Math.round(v);
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomButton(x + 280, y, 160, 22, "Vanilla settings", () -> client.setScreen(new OptionsScreen(this, client.options))));
		addDrawableChild(new CustomButton(x + 280, y + 28, 160, 22, "Export config", () -> FileDialogs.save("Export Blaze Morphx config", "blazemorphx.json", path -> {
			if (path == null) {
				return;
			}
			try {
				Files.writeString(path, ConfigManager.exportJson(), StandardCharsets.UTF_8);
				NotificationRenderer.get().push("Exported", path.getFileName().toString());
			} catch (Exception e) {
				NotificationRenderer.get().error("Export failed");
			}
		})));
		addDrawableChild(new CustomButton(x + 280, y + 56, 160, 22, "Import config", () -> FileDialogs.open("Import config", java.util.Set.of("json"), path -> {
			try {
				if (path != null && ConfigManager.importJson(Files.readString(path))) {
					NotificationRenderer.get().push("Imported", path.getFileName().toString());
					clearAndInit();
				}
			} catch (Exception e) {
				NotificationRenderer.get().error("Import failed");
			}
		})));
		addDrawableChild(new CustomButton(x + 280, y + 84, 160, 22, "Save profile", () -> ConfigProfile.saveCurrent(config.activeProfile)));
		addDrawableChild(new CustomButton(x + 280, y + 112, 160, 22, "Load profile", () -> ConfigProfile.load(config.activeProfile)));
		addDrawableChild(new CustomButton(x + 280, y + 140, 160, 22, "Reset all", () -> {
			ConfigManager.reset();
			ThemeManager.init();
			BackgroundManager.get().restart();
			clearAndInit();
			NotificationRenderer.get().push("Settings", "All Blaze Morphx settings reset");
		}));
		addDrawableChild(new CustomButton(24, height - 36, 90, 22, "Back", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		FontManager.get().draw(context, "Right Shift opens this menu in-game. Config is saved automatically.", 24, height - 54, ThemeManager.muted());
	}
}
