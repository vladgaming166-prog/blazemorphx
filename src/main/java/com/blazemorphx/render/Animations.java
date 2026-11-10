package com.blazemorphx.render;

import com.blazemorphx.config.ConfigManager;

public final class Animations {
	private Animations() {
	}

	public static float approach(float current, float target, float deltaTicks) {
		if (!ConfigManager.get().menuAnimations) {
			return target;
		}
		float speed = 0.18f * Math.max(0.25f, deltaTicks);
		return current + (target - current) * Math.min(1f, speed);
	}

	public static float easeOutCubic(float t) {
		float inv = 1f - clamp(t);
		return 1f - inv * inv * inv;
	}

	public static float pulse(long millis, float speed) {
		return 0.5f + 0.5f * (float) Math.sin(millis / 1000.0 * speed * Math.PI * 2.0);
	}

	public static float clamp(float t) {
		return t < 0f ? 0f : Math.min(t, 1f);
	}
}
