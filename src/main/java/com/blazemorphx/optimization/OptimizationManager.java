package com.blazemorphx.optimization;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.render.NotificationRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.particle.ParticlesMode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OptimizationManager {
	private static final OptimizationManager INSTANCE = new OptimizationManager();
	private Snapshot original;
	private boolean particleCapActive;
	private int renderedParticles;

	private OptimizationManager() {
	}

	public static OptimizationManager get() {
		return INSTANCE;
	}

	public void init() {
		loadSnapshot();
	}

	public void applyPreset(PerformanceProfiles.Kind kind) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.options == null) {
			return;
		}
		FPSMonitor.get().markBefore();
		if (kind != PerformanceProfiles.Kind.RESTORE) {
			captureOriginal(client.options);
		}
		if (kind == PerformanceProfiles.Kind.RESTORE) {
			restore(client.options);
			NotificationRenderer.get().push("Optimization", "Vanilla settings restored");
		} else {
			PerformanceProfiles.apply(kind, client.options);
			ModConfig config = ConfigManager.get();
			config.reduceParticles = kind == PerformanceProfiles.Kind.MAX_FPS;
			config.skipVignette = kind == PerformanceProfiles.Kind.MAX_FPS;
			config.skipWeather = kind == PerformanceProfiles.Kind.MAX_FPS;
			config.skipEntityShadows = kind != PerformanceProfiles.Kind.QUALITY;
			config.reduceMenuAnimations = kind == PerformanceProfiles.Kind.MAX_FPS;
			ConfigManager.save();
			NotificationRenderer.get().push("Optimization applied", kind.name().replace('_', ' '));
		}
		FPSMonitor.get().markAfter();
	}

	public boolean shouldSkipParticle() {
		ModConfig config = ConfigManager.get();
		if (!config.reduceParticles) {
			return false;
		}
		renderedParticles++;
		if (renderedParticles > config.particleLimit) {
			return true;
		}
		return false;
	}

	public void resetParticleCounter() {
		renderedParticles = 0;
	}

	public boolean skipVignette() {
		return ConfigManager.get().skipVignette;
	}

	public boolean skipWeather() {
		return ConfigManager.get().skipWeather;
	}

	public void requestGc() {
		NotificationRenderer.get().push("Garbage collection", "Requested. This does not always improve FPS.");
		System.gc();
	}

	private void captureOriginal(GameOptions options) {
		if (original != null) {
			return;
		}
		original = Snapshot.from(options);
		saveSnapshot();
	}

	private void restore(GameOptions options) {
		if (original == null) {
			NotificationRenderer.get().error("No original settings were saved yet");
			return;
		}
		original.apply(options);
		options.write();
		ModConfig config = ConfigManager.get();
		config.reduceParticles = false;
		config.skipVignette = false;
		config.skipWeather = false;
		config.skipEntityShadows = false;
		ConfigManager.save();
	}

	private void saveSnapshot() {
		try {
			Files.writeString(snapshotFile(), ConfigManager.gson().toJson(original), StandardCharsets.UTF_8);
		} catch (Exception ignored) {
		}
	}

	private void loadSnapshot() {
		try {
			Path file = snapshotFile();
			if (Files.exists(file)) {
				original = ConfigManager.gson().fromJson(Files.readString(file), Snapshot.class);
			}
		} catch (Exception ignored) {
		}
	}

	private static Path snapshotFile() {
		return ConfigManager.directory().resolve("vanilla-options-backup.json");
	}

	public static final class Snapshot {
		public GraphicsMode graphicsMode;
		public int viewDistance;
		public int simulationDistance;
		public double entityDistance;
		public int maxFps;
		public boolean vsync;
		public CloudRenderMode clouds;
		public ParticlesMode particles;
		public boolean ao;
		public boolean entityShadows;
		public boolean bobView;
		public double damageTilt;
		public double distortion;
		public int mipmap;
		public int biomeBlend;
		public int weatherRadius;

		public static Snapshot from(GameOptions options) {
			Snapshot snapshot = new Snapshot();
			snapshot.graphicsMode = options.getPreset().getValue();
			snapshot.viewDistance = options.getViewDistance().getValue();
			snapshot.simulationDistance = options.getSimulationDistance().getValue();
			snapshot.entityDistance = options.getEntityDistanceScaling().getValue();
			snapshot.maxFps = options.getMaxFps().getValue();
			snapshot.vsync = options.getEnableVsync().getValue();
			snapshot.clouds = options.getCloudRenderMode().getValue();
			snapshot.particles = options.getParticles().getValue();
			snapshot.ao = options.getAo().getValue();
			snapshot.entityShadows = options.getEntityShadows().getValue();
			snapshot.bobView = options.getBobView().getValue();
			snapshot.damageTilt = options.getDamageTiltStrength().getValue();
			snapshot.distortion = options.getDistortionEffectScale().getValue();
			snapshot.mipmap = options.getMipmapLevels().getValue();
			snapshot.biomeBlend = options.getBiomeBlendRadius().getValue();
			snapshot.weatherRadius = options.getWeatherRadius().getValue();
			return snapshot;
		}

		public void apply(GameOptions options) {
			options.applyGraphicsMode(graphicsMode);
			options.getViewDistance().setValue(viewDistance);
			options.getSimulationDistance().setValue(simulationDistance);
			options.getEntityDistanceScaling().setValue(entityDistance);
			options.getMaxFps().setValue(maxFps);
			options.getEnableVsync().setValue(vsync);
			options.getCloudRenderMode().setValue(clouds);
			options.getParticles().setValue(particles);
			options.getAo().setValue(ao);
			options.getEntityShadows().setValue(entityShadows);
			options.getBobView().setValue(bobView);
			options.getDamageTiltStrength().setValue(damageTilt);
			options.getDistortionEffectScale().setValue(distortion);
			options.getMipmapLevels().setValue(mipmap);
			options.getBiomeBlendRadius().setValue(biomeBlend);
			options.getWeatherRadius().setValue(weatherRadius);
		}
	}

	public Map<String, String> liveOptions(GameOptions options) {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("Graphics", String.valueOf(options.getPreset().getValue()));
		map.put("Render distance", String.valueOf(options.getViewDistance().getValue()));
		map.put("Simulation", String.valueOf(options.getSimulationDistance().getValue()));
		map.put("Max FPS", String.valueOf(options.getMaxFps().getValue()));
		map.put("VSync", String.valueOf(options.getEnableVsync().getValue()));
		map.put("Clouds", String.valueOf(options.getCloudRenderMode().getValue()));
		map.put("Particles", String.valueOf(options.getParticles().getValue()));
		return map;
	}
}
