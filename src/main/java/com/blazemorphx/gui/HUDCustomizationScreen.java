package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.customization.HUDManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class HUDCustomizationScreen extends BlazeScreen {
	public HUDCustomizationScreen(Screen parent) {
		super("HUD Editor", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		int x = 8;
		int y = contentTop();
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.customHud)
			.build(x, y, 150, 20, Text.literal("Custom HUD"), (btn, value) -> {
				config.customHud = value;
				ConfigManager.save();
			}));
		addDrawableChild(toggle(x, y + 22, "FPS", () -> config.hudFps, v -> config.hudFps = v));
		addDrawableChild(toggle(x, y + 44, "CPS", () -> config.hudCps, v -> config.hudCps = v));
		addDrawableChild(toggle(x, y + 66, "Coordinates", () -> config.hudCoords, v -> config.hudCoords = v));
		addDrawableChild(toggle(x, y + 88, "Keystrokes", () -> config.hudKeystrokes, v -> config.hudKeystrokes = v));
		addDrawableChild(toggle(x, y + 110, "Sprint", () -> config.hudSprint, v -> config.hudSprint = v));
		addDrawableChild(toggle(x, y + 132, "Ping", () -> config.hudPing, v -> config.hudPing = v));
		addDrawableChild(toggle(x, y + 154, "Armor", () -> config.hudArmor, v -> config.hudArmor = v));
		addDrawableChild(toggle(x, y + 176, "Potions", () -> config.hudPotions, v -> config.hudPotions = v));
		addDrawableChild(toggle(x, y + 198, "Direction", () -> config.hudDirection, v -> config.hudDirection = v));
		addDrawableChild(toggle(x, y + 220, "Stopwatch", () -> config.hudStopwatch, v -> config.hudStopwatch = v));
		addDrawableChild(button(x, y + 244, 150, 20, "Reset Stopwatch", () -> HUDManager.get().resetStopwatch()));
		addDrawableChild(button(width / 2 - 100, height - 28, 200, 20, "Done", this::close));
	}

	private CyclingButtonWidget<Boolean> toggle(int x, int y, String label, java.util.function.BooleanSupplier get, java.util.function.Consumer<Boolean> set) {
		return CyclingButtonWidget.onOffBuilder(get.getAsBoolean())
			.build(x, y, 150, 20, Text.literal(label), (btn, value) -> {
				set.accept(value);
				ConfigManager.save();
			});
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawTextWithShadow(textRenderer, "Drag HUD modules. Positions save automatically.", 170, contentTop() + 8, 0xFFA0A0A0);
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
	public boolean mouseReleased(Click click) {
		HUDManager.get().mouseReleased();
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		HUDManager.get().mouseDragged(click.x(), click.y(), width, height);
		return super.mouseDragged(click, offsetX, offsetY);
	}
}
