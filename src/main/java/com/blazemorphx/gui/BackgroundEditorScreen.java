package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.customization.BackgroundManager;
import com.blazemorphx.util.FileDialogs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

import java.util.Set;

public class BackgroundEditorScreen extends BlazeScreen {
	private String status = "";

	public BackgroundEditorScreen(Screen parent) {
		super("Menu Background", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		int x = width / 2 - 100;
		int y = contentTop();
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.animatedBackground)
			.build(x, y, 200, 20, Text.literal("Animated Background"), (btn, value) -> {
				config.animatedBackground = value;
				ConfigManager.save();
				BackgroundManager.get().restart();
			}));
		addDrawableChild(CyclingButtonWidget.builder(Text::literal, config.backgroundScale)
			.values("fill", "fit", "stretch")
			.build(x, y + 24, 200, 20, Text.literal("Scale"), (btn, value) -> {
				config.backgroundScale = value;
				ConfigManager.save();
			}));
		addDrawableChild(new VanillaSlider(x, y + 48, 200, 20, "Brightness", 0.3, 1.4, () -> config.backgroundBrightness, v -> config.backgroundBrightness = (float) v, "%.2f"));
		addDrawableChild(new VanillaSlider(x, y + 72, 200, 20, "Overlay", 0.0, 0.8, () -> config.overlayOpacity, v -> config.overlayOpacity = (float) v, "%.2f"));
		addDrawableChild(new VanillaSlider(x, y + 96, 200, 20, "Video FPS cap", 8, 30, () -> config.videoFpsLimit, v -> config.videoFpsLimit = (int) Math.round(v), "%.0f"));
		addDrawableChild(button(x, y + 124, 200, 20, "Import File...", () -> FileDialogs.open("Import background", Set.of("mp4", "webm", "mov", "png", "jpg", "jpeg", "webp"), path -> {
			if (path != null && BackgroundManager.get().importBackground(path)) {
				status = "Imported " + path.getFileName();
			} else {
				status = "Could not import that file.";
			}
		})));
		addDrawableChild(button(x, y + 148, 200, 20, "Reset Default Video", () -> {
			BackgroundManager.get().resetToDefault();
			status = "Default underwater background restored.";
		}));
		addDrawableChild(button(x, height - 28, 200, 20, "Done", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		var file = BackgroundManager.get().resolveCurrentFile();
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("Current: " + (file == null ? "none" : file.getFileName())), width / 2, height / 2 + 40, 0xFFA0A0A0);
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("Decoder: " + BackgroundManager.get().video().status()), width / 2, height / 2 + 52, 0xFFA0A0A0);
		if (!status.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, height / 2 + 64, 0xFFFFFF);
		}
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("Video decoding stops when you leave the main menu."), width / 2, height - 44, 0xFFA0A0A0);
	}

	@Override
	public void removed() {
		ConfigManager.save();
	}
}
