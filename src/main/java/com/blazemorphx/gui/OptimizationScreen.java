package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.optimization.FPSMonitor;
import com.blazemorphx.optimization.MemoryMonitor;
import com.blazemorphx.optimization.ModCompatibilityManager;
import com.blazemorphx.optimization.OptimizationManager;
import com.blazemorphx.optimization.PerformanceProfiles;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.text.Text;

public class OptimizationScreen extends BlazeScreen {
	public OptimizationScreen(Screen parent) {
		super("Optimization", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		GameOptions options = client.options;
		int x = width / 2 - 155;
		int y = contentTop();
		addDrawableChild(button(x, y, 100, 20, "Maximum FPS", () -> OptimizationManager.get().applyPreset(PerformanceProfiles.Kind.MAX_FPS)));
		addDrawableChild(button(x + 105, y, 100, 20, "Balanced", () -> OptimizationManager.get().applyPreset(PerformanceProfiles.Kind.BALANCED)));
		addDrawableChild(button(x + 210, y, 100, 20, "Visual Quality", () -> OptimizationManager.get().applyPreset(PerformanceProfiles.Kind.QUALITY)));
		y += 24;
		addDrawableChild(button(x, y, 310, 20, "Restore Previous Settings", () -> OptimizationManager.get().applyPreset(PerformanceProfiles.Kind.RESTORE)));
		y += 26;
		addDrawableChild(options.getViewDistance().createWidget(options, x, y, 150));
		addDrawableChild(options.getSimulationDistance().createWidget(options, x + 160, y, 150));
		y += 24;
		addDrawableChild(options.getMaxFps().createWidget(options, x, y, 150));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(options.getEnableVsync().getValue())
			.build(x + 160, y, 150, 20, Text.literal("VSync"), (btn, value) -> options.getEnableVsync().setValue(value)));
		y += 24;
		addDrawableChild(CyclingButtonWidget.builder((GraphicsMode mode) -> Text.literal(mode.name()), options.getPreset().getValue())
			.values(GraphicsMode.values())
			.build(x, y, 150, 20, Text.literal("Graphics"), (btn, value) -> options.applyGraphicsMode(value)));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(options.getAo().getValue())
			.build(x + 160, y, 150, 20, Text.literal("Smooth Lighting"), (btn, value) -> options.getAo().setValue(value)));
		y += 24;
		addDrawableChild(CyclingButtonWidget.builder((CloudRenderMode mode) -> Text.literal(mode.name()), options.getCloudRenderMode().getValue())
			.values(CloudRenderMode.values())
			.build(x, y, 150, 20, Text.literal("Clouds"), (btn, value) -> options.getCloudRenderMode().setValue(value)));
		addDrawableChild(CyclingButtonWidget.builder((ParticlesMode mode) -> Text.literal(mode.name()), options.getParticles().getValue())
			.values(ParticlesMode.values())
			.build(x + 160, y, 150, 20, Text.literal("Particles"), (btn, value) -> options.getParticles().setValue(value)));
		y += 24;
		addDrawableChild(CyclingButtonWidget.onOffBuilder(options.getEntityShadows().getValue())
			.build(x, y, 150, 20, Text.literal("Entity Shadows"), (btn, value) -> options.getEntityShadows().setValue(value)));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(options.getBobView().getValue())
			.build(x + 160, y, 150, 20, Text.literal("View Bobbing"), (btn, value) -> options.getBobView().setValue(value)));
		y += 24;
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.skipVignette)
			.build(x, y, 150, 20, Text.literal("Skip Vignette"), (btn, value) -> {
				config.skipVignette = value;
				ConfigManager.save();
			}));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.skipWeather)
			.build(x + 160, y, 150, 20, Text.literal("Skip Weather Mesh"), (btn, value) -> {
				config.skipWeather = value;
				ConfigManager.save();
			}));
		y += 24;
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.animatedBackground)
			.build(x, y, 150, 20, Text.literal("Menu Video"), (btn, value) -> {
				config.animatedBackground = value;
				ConfigManager.save();
			}));
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.customHud)
			.build(x + 160, y, 150, 20, Text.literal("Custom HUD"), (btn, value) -> {
				config.customHud = value;
				ConfigManager.save();
			}));
		addDrawableChild(button(width / 2 - 100, height - 28, 200, 20, "Done", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		FPSMonitor fps = FPSMonitor.get();
		MemoryMonitor mem = MemoryMonitor.get();
		int x = 8;
		int y = contentTop();
		context.drawTextWithShadow(textRenderer, "FPS " + fps.current() + "  avg " + fps.average() + "  min " + fps.min() + "  max " + fps.max(), x, height - 52, 0xFFFFFF);
		context.drawTextWithShadow(textRenderer, MemoryMonitor.mb(mem.used()) + " / " + MemoryMonitor.mb(mem.max()) + "   " + ModCompatibilityManager.summary().replace('\n', ' '), x, height - 40, 0xFFA0A0A0);
		if (fps.beforeAverage() != null && fps.afterAverage() != null) {
			context.drawTextWithShadow(textRenderer, "Measured before " + fps.beforeAverage() + " avg, after " + fps.afterAverage() + " avg", x, y - 12, 0xFFA0A0A0);
		}
	}

	@Override
	public void removed() {
		if (client != null) {
			client.options.write();
		}
		ConfigManager.save();
	}
}
