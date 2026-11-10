package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.gui.components.CustomButton;
import com.blazemorphx.gui.components.CustomDropdown;
import com.blazemorphx.gui.components.CustomSlider;
import com.blazemorphx.gui.components.CustomToggle;
import com.blazemorphx.render.GlassRenderer;
import com.blazemorphx.util.FileDialogs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;
import java.util.Set;

public class CustomizationScreen extends BlazeScreen {
	private String tab = "Overview";

	public CustomizationScreen(Screen parent) {
		super("Customization", parent);
	}

	@Override
	protected void init() {
		int x = 24;
		int y = contentTop();
		for (String name : List.of("Overview", "Font", "Background", "Crosshair", "HUD", "Theme")) {
			String id = name;
			addDrawableChild(new CustomButton(x, y, 90, 20, name, () -> {
				this.tab = id;
				if ("Background".equals(id)) {
					client.setScreen(new BackgroundEditorScreen(this));
				} else if ("Crosshair".equals(id)) {
					client.setScreen(new CrosshairEditorScreen(this));
				} else if ("HUD".equals(id)) {
					client.setScreen(new HUDCustomizationScreen(this));
				} else {
					clearAndInit();
				}
			}));
			x += 96;
		}
		ModConfig config = ConfigManager.get();
		int left = 24;
		int top = contentTop() + 36;
		if ("Font".equals(tab)) {
			addDrawableChild(new CustomDropdown(left, top, 180, 20, "Built-in font", FontManager.BUILTIN, () -> config.fontName, value -> {
				config.fontName = value;
				config.customFontPath = "";
				ConfigManager.save();
				FontManager.get().reload();
			}));
			addDrawableChild(new CustomSlider(left, top + 40, 240, 24, "Font size", 8, 18, () -> config.fontSize, v -> {
				config.fontSize = (float) v;
				ConfigManager.save();
				FontManager.get().reload();
			}, "%.0f"));
			addDrawableChild(new CustomToggle(left, top + 78, 240, 22, "Bold", () -> config.fontBold, v -> {
				config.fontBold = v;
				ConfigManager.save();
				FontManager.get().reload();
			}));
			addDrawableChild(new CustomToggle(left, top + 104, 240, 22, "Italic", () -> config.fontItalic, v -> {
				config.fontItalic = v;
				ConfigManager.save();
				FontManager.get().reload();
			}));
			addDrawableChild(new CustomToggle(left, top + 130, 240, 22, "Apply to Blaze UI", () -> config.applyFontToBlazeUi, v -> {
				config.applyFontToBlazeUi = v;
				ConfigManager.save();
			}));
			addDrawableChild(new CustomButton(left, top + 162, 140, 22, "Import TTF/OTF", () -> FileDialogs.open("Import font", Set.of("ttf", "otf"), path -> {
				if (path != null && FontManager.get().importFont(path)) {
					clearAndInit();
				}
			})));
			addDrawableChild(new CustomButton(left + 150, top + 162, 90, 22, "Reset", () -> {
				FontManager.get().reset();
				clearAndInit();
			}));
		} else if ("Theme".equals(tab) || "Overview".equals(tab)) {
			addDrawableChild(new CustomDropdown(left, top, 200, 20, "Theme", List.of(
				ThemeManager.ThemeId.BLAZE_ORANGE.displayName,
				ThemeManager.ThemeId.BLOOD_RED.displayName,
				ThemeManager.ThemeId.PURPLE_FLAME.displayName,
				ThemeManager.ThemeId.ICE_BLUE.displayName,
				ThemeManager.ThemeId.PURE_BLACK.displayName
			), () -> ThemeManager.current().displayName, value -> {
				for (ThemeManager.ThemeId theme : ThemeManager.ThemeId.values()) {
					if (theme.displayName.equals(value)) {
						ThemeManager.apply(theme);
					}
				}
				clearAndInit();
			}));
			addDrawableChild(new CustomSlider(left, top + 40, 240, 24, "UI scale", 0.8, 1.4, () -> config.uiScale, v -> {
				config.uiScale = (float) v;
				ConfigManager.save();
			}, "%.2f"));
			addDrawableChild(new CustomSlider(left, top + 72, 240, 24, "Transparency", 0.4, 1.0, () -> config.uiTransparency, v -> {
				config.uiTransparency = (float) v;
				ConfigManager.save();
			}, "%.2f"));
			addDrawableChild(new CustomSlider(left, top + 104, 240, 24, "Rounded corners", 2, 12, () -> config.roundedCorners, v -> {
				config.roundedCorners = (float) v;
				ConfigManager.save();
			}, "%.0f"));
			addDrawableChild(new CustomToggle(left, top + 140, 240, 22, "UI animations", () -> config.menuAnimations, v -> {
				config.menuAnimations = v;
				ConfigManager.save();
			}));
			addDrawableChild(new CustomToggle(left, top + 166, 240, 22, "Logo visibility", () -> config.showLogo, v -> {
				config.showLogo = v;
				ConfigManager.save();
			}));
			addDrawableChild(new CustomToggle(left, top + 192, 240, 22, "Sound effects", () -> config.soundEffects, v -> {
				config.soundEffects = v;
				ConfigManager.save();
			}));
			addDrawableChild(new CustomDropdown(left + 260, top, 160, 20, "Button style", List.of("glass", "solid", "outline"), () -> config.buttonStyle, v -> {
				config.buttonStyle = v;
				ConfigManager.save();
			}));
			addDrawableChild(new CustomDropdown(left + 260, top + 40, 160, 20, "Menu layout", List.of("centered", "left", "wide"), () -> config.menuLayout, v -> {
				config.menuLayout = v;
				ConfigManager.save();
			}));
		}
		addDrawableChild(new CustomButton(24, height - 36, 90, 22, "Back", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		GlassRenderer.panel(context, width - 280, contentTop() + 36, 256, 120);
		FontManager.get().draw(context, "Preview · " + FontManager.get().previewName(), width - 266, contentTop() + 50, ThemeManager.accent());
		FontManager.get().draw(context, "The quick brown fox", width - 266, contentTop() + 70, ThemeManager.text());
		FontManager.get().draw(context, "BLAZE MORPHX 012345", width - 266, contentTop() + 86, ThemeManager.muted());
		FontManager.get().draw(context, "Minecraft UI fonts stay vanilla so Unicode/language packs keep working.", 24, height - 54, ThemeManager.muted());
	}
}
