package com.blazemorphx.optimization;

import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.particle.ParticlesMode;

public final class PerformanceProfiles {
	public enum Kind {
		MAX_FPS,
		BALANCED,
		QUALITY,
		RESTORE
	}

	private PerformanceProfiles() {
	}

	public static void apply(Kind kind, GameOptions options) {
		switch (kind) {
			case MAX_FPS -> {
				options.applyGraphicsMode(GraphicsMode.FAST);
				options.getViewDistance().setValue(8);
				options.getSimulationDistance().setValue(6);
				options.getEntityDistanceScaling().setValue(0.5);
				options.getMaxFps().setValue(260);
				options.getEnableVsync().setValue(false);
				options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
				options.getParticles().setValue(ParticlesMode.MINIMAL);
				options.getAo().setValue(false);
				options.getEntityShadows().setValue(false);
				options.getBobView().setValue(false);
				options.getDamageTiltStrength().setValue(0.0);
				options.getDistortionEffectScale().setValue(0.0);
				options.getMipmapLevels().setValue(0);
				options.getBiomeBlendRadius().setValue(0);
				options.getWeatherRadius().setValue(3);
			}
			case BALANCED -> {
				options.applyGraphicsMode(GraphicsMode.FANCY);
				options.getViewDistance().setValue(12);
				options.getSimulationDistance().setValue(8);
				options.getEntityDistanceScaling().setValue(0.75);
				options.getMaxFps().setValue(120);
				options.getEnableVsync().setValue(false);
				options.getCloudRenderMode().setValue(CloudRenderMode.FAST);
				options.getParticles().setValue(ParticlesMode.DECREASED);
				options.getAo().setValue(true);
				options.getEntityShadows().setValue(false);
				options.getBobView().setValue(true);
				options.getDamageTiltStrength().setValue(0.5);
				options.getDistortionEffectScale().setValue(0.5);
				options.getMipmapLevels().setValue(2);
				options.getBiomeBlendRadius().setValue(2);
				options.getWeatherRadius().setValue(8);
			}
			case QUALITY -> {
				options.applyGraphicsMode(GraphicsMode.FABULOUS);
				options.getViewDistance().setValue(16);
				options.getSimulationDistance().setValue(12);
				options.getEntityDistanceScaling().setValue(1.0);
				options.getMaxFps().setValue(60);
				options.getEnableVsync().setValue(true);
				options.getCloudRenderMode().setValue(CloudRenderMode.FANCY);
				options.getParticles().setValue(ParticlesMode.ALL);
				options.getAo().setValue(true);
				options.getEntityShadows().setValue(true);
				options.getBobView().setValue(true);
				options.getDamageTiltStrength().setValue(1.0);
				options.getDistortionEffectScale().setValue(1.0);
				options.getMipmapLevels().setValue(4);
				options.getBiomeBlendRadius().setValue(5);
				options.getWeatherRadius().setValue(10);
			}
			case RESTORE -> {
			}
		}
		options.write();
	}
}
