package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.HUDManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.gui.components.CustomButton;
import com.blazemorphx.gui.components.CustomToggle;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class HUDCustomizationScreen extends BlazeScreen {
	public HUDCustomizationScreen(Screen parent) {
		super("HUD Editor", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		int x = 24;
		int y = contentTop();
		addDrawableChild(new CustomToggle(x, y, 180, 20, "FPS", () -> config.hudFps, v -> {
			config.hudFps = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 22, 180, 20, "CPS", () -> config.hudCps, v -> {
			config.hudCps = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 44, 180, 20, "Coordinates", () -> config.hudCoords, v -> {
			config.hudCoords = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 66, 180, 20, "Keystrokes", () -> config.hudKeystrokes, v -> {
			config.hudKeystrokes = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 88, 180, 20, "Sprint", () -> config.hudSprint, v -> {
			config.hudSprint = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 110, 180, 20, "Ping", () -> config.hudPing, v -> {
			config.hudPing = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 132, 180, 20, "Armor", () -> config.hudArmor, v -> {
			config.hudArmor = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 154, 180, 20, "Potions", () -> config.hudPotions, v -> {
			config.hudPotions = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 176, 180, 20, "Direction", () -> config.hudDirection, v -> {
			config.hudDirection = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 198, 180, 20, "Stopwatch", () -> config.hudStopwatch, v -> {
			config.hudStopwatch = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomButton(x, y + 230, 140, 22, "Reset stopwatch", () -> HUDManager.get().resetStopwatch()));
		addDrawableChild(new CustomButton(24, height - 36, 90, 22, "Back", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		FontManager.get().draw(context, "Drag HUD modules on the right. Positions save automatically.", 220, contentTop() + 8, ThemeManager.muted());
		HUDManager.get().renderEditorPreview(context, width, height);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (HUDManager.get().mouseClicked(click.x(), click.y(), width, height)) {
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		HUDManager.get().mouseDragged(click.x(), click.y(), width, height);
		return true;
	}

	@Override
	public boolean mouseReleased(Click click) {
		HUDManager.get().mouseReleased();
		return super.mouseReleased(click);
	}
}
