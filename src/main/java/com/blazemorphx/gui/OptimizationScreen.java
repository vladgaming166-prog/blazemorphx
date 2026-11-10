package com.blazemorphx.gui;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.gui.components.CustomButton;
import com.blazemorphx.gui.components.CustomSlider;
import com.blazemorphx.gui.components.CustomToggle;
import com.blazemorphx.optimization.FPSMonitor;
import com.blazemorphx.optimization.MemoryMonitor;
import com.blazemorphx.optimization.ModCompatibilityManager;
import com.blazemorphx.optimization.OptimizationManager;
import com.blazemorphx.optimization.PerformanceProfiles;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.particle.ParticlesMode;

public class OptimizationScreen extends BlazeScreen {
	public OptimizationScreen(Screen parent) {
		super("Optimization", parent);
	}

	@Override
	protected void init() {
		ModConfig config = ConfigManager.get();
		int x = 24;
		int y = contentTop();
		addDrawableChild(new CustomButton(x, y, 170, 22, "Maximum FPS", () -> OptimizationManager.get().applyPreset(PerformanceProfiles.Kind.MAX_FPS)));
		addDrawableChild(new CustomButton(x + 178, y, 170, 22, "Balanced", () -> OptimizationManager.get().applyPreset(PerformanceProfiles.Kind.BALANCED)));
		addDrawableChild(new CustomButton(x + 356, y, 170, 22, "Best Visuals", () -> OptimizationManager.get().applyPreset(PerformanceProfiles.Kind.QUALITY)));
		addDrawableChild(new CustomButton(x + 534, y, 150, 22, "Restore Default", () -> OptimizationManager.get().applyPreset(PerformanceProfiles.Kind.RESTORE)));
		y += 36;
		var options = client.options;
		addDrawableChild(new CustomToggle(x, y, 220, 20, "Clouds", () -> options.getCloudRenderMode().getValue() != CloudRenderMode.OFF, v -> options.getCloudRenderMode().setValue(v ? CloudRenderMode.FAST : CloudRenderMode.OFF)));
		addDrawableChild(new CustomToggle(x, y + 22, 220, 20, "Smooth lighting", () -> options.getAo().getValue(), v -> options.getAo().setValue(v)));
		addDrawableChild(new CustomToggle(x, y + 44, 220, 20, "Entity shadows", () -> options.getEntityShadows().getValue(), v -> {
			options.getEntityShadows().setValue(v);
			config.skipEntityShadows = !v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 66, 220, 20, "View bobbing", () -> options.getBobView().getValue(), v -> options.getBobView().setValue(v)));
		addDrawableChild(new CustomToggle(x, y + 88, 220, 20, "Skip vignette", () -> config.skipVignette, v -> {
			config.skipVignette = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 110, 220, 20, "Skip weather mesh", () -> config.skipWeather, v -> {
			config.skipWeather = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 132, 220, 20, "Cap particles", () -> config.reduceParticles, v -> {
			config.reduceParticles = v;
			ConfigManager.save();
		}));
		addDrawableChild(new CustomToggle(x, y + 154, 220, 20, "VSync", () -> options.getEnableVsync().getValue(), v -> options.getEnableVsync().setValue(v)));
		addDrawableChild(new CustomSlider(x + 240, y, 220, 24, "Render distance", 2, 32, () -> options.getViewDistance().getValue(), v -> options.getViewDistance().setValue((int) Math.round(v)), "%.0f"));
		addDrawableChild(new CustomSlider(x + 240, y + 32, 220, 24, "Simulation", 5, 32, () -> options.getSimulationDistance().getValue(), v -> options.getSimulationDistance().setValue((int) Math.round(v)), "%.0f"));
		addDrawableChild(new CustomSlider(x + 240, y + 64, 220, 24, "Entity distance", 0.5, 2.0, () -> options.getEntityDistanceScaling().getValue(), v -> options.getEntityDistanceScaling().setValue(v), "%.2f"));
		addDrawableChild(new CustomSlider(x + 240, y + 96, 220, 24, "Max FPS", 10, 260, () -> options.getMaxFps().getValue(), v -> options.getMaxFps().setValue((int) Math.round(v)), "%.0f"));
		addDrawableChild(new CustomSlider(x + 240, y + 128, 220, 24, "Mipmap", 0, 4, () -> options.getMipmapLevels().getValue(), v -> options.getMipmapLevels().setValue((int) Math.round(v)), "%.0f"));
		addDrawableChild(new CustomSlider(x + 240, y + 160, 220, 24, "Particle limit", 50, 2000, () -> config.particleLimit, v -> {
			config.particleLimit = (int) Math.round(v);
			ConfigManager.save();
		}, "%.0f"));
		addDrawableChild(new CustomSlider(x + 240, y + 192, 220, 24, "Biome blend", 0, 7, () -> options.getBiomeBlendRadius().getValue(), v -> options.getBiomeBlendRadius().setValue((int) Math.round(v)), "%.0f"));
		addDrawableChild(new CustomSlider(x + 240, y + 224, 220, 24, "Weather radius", 3, 10, () -> options.getWeatherRadius().getValue(), v -> options.getWeatherRadius().setValue((int) Math.round(v)), "%.0f"));
		addDrawableChild(new CustomButton(x, y + 186, 160, 22, "Graphics FAST", () -> options.applyGraphicsMode(GraphicsMode.FAST)));
		addDrawableChild(new CustomButton(x, y + 210, 160, 22, "Particles MIN", () -> options.getParticles().setValue(ParticlesMode.MINIMAL)));
		addDrawableChild(new CustomButton(x, height - 36, 140, 22, "GC (warning)", () -> OptimizationManager.get().requestGc()));
		addDrawableChild(new CustomButton(170, height - 36, 90, 22, "Back", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		FPSMonitor fps = FPSMonitor.get();
		MemoryMonitor mem = MemoryMonitor.get();
		int x = width - 250;
		int y = contentTop() + 8;
		GlassRenderer.panel(context, x, y, 226, 210);
		FontManager.get().draw(context, "FPS MONITOR", x + 12, y + 10, ThemeManager.accent());
		FontManager.get().draw(context, "Now " + fps.current() + "   avg " + fps.average(), x + 12, y + 26, ThemeManager.text());
		FontManager.get().draw(context, "min " + fps.min() + "   max " + fps.max(), x + 12, y + 40, ThemeManager.muted());
		FontManager.get().draw(context, String.format(java.util.Locale.ROOT, "frame %.2f ms", fps.frameMs()), x + 12, y + 54, ThemeManager.muted());
		if (fps.beforeAverage() != null) {
			FontManager.get().draw(context, "Before " + fps.beforeAverage() + " avg", x + 12, y + 68, ThemeManager.muted());
		}
		if (fps.afterAverage() != null) {
			FontManager.get().draw(context, "After " + fps.afterAverage() + " avg", x + 12, y + 82, ThemeManager.success());
		}
		drawGraph(context, x + 12, y + 100, 200, 50, fps.graph());
		GlassRenderer.panel(context, x, y + 220, 226, 110);
		FontManager.get().draw(context, "MEMORY", x + 12, y + 230, ThemeManager.accent());
		FontManager.get().draw(context, MemoryMonitor.mb(mem.used()) + " / " + MemoryMonitor.mb(mem.max()), x + 12, y + 246, ThemeManager.text());
		FontManager.get().draw(context, "Allocated " + MemoryMonitor.mb(mem.allocated()) + "  " + mem.percent() + "%", x + 12, y + 260, ThemeManager.muted());
		FontManager.get().draw(context, "Java " + mem.javaVersion(), x + 12, y + 274, ThemeManager.muted());
		FontManager.get().draw(context, mem.gcSummary(), x + 12, y + 288, ThemeManager.muted());
		FontManager.get().draw(context, ModCompatibilityManager.summary().replace('\n', ' '), 24, height - 52, ThemeManager.muted());
	}

	private static void drawGraph(DrawContext context, int x, int y, int w, int h, int[] data) {
		context.fill(x, y, x + w, y + h, 0x66000000);
		if (data.length < 2) {
			return;
		}
		int max = 1;
		for (int v : data) {
			max = Math.max(max, v);
		}
		int prevX = x;
		int prevY = y + h;
		for (int i = 0; i < data.length; i++) {
			int px = x + i * w / Math.max(1, data.length - 1);
			int py = y + h - (data[i] * h / max);
			context.fill(prevX, prevY, px + 1, py + 1, ThemeManager.accent());
			prevX = px;
			prevY = py;
		}
	}

	@Override
	public void removed() {
		if (client != null) {
			client.options.write();
		}
	}
}
