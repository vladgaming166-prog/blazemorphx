package com.blazemorphx.util;

public final class ColorUtil {
	private ColorUtil() {
	}

	public static int argb(int a, int r, int g, int b) {
		return ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
	}

	public static int rgb(int r, int g, int b) {
		return argb(255, r, g, b);
	}

	public static int withAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | ((alpha & 255) << 24);
	}

	public static int lerp(int from, int to, float t) {
		t = clamp01(t);
		int a = mix(alpha(from), alpha(to), t);
		int r = mix(red(from), red(to), t);
		int g = mix(green(from), green(to), t);
		int b = mix(blue(from), blue(to), t);
		return argb(a, r, g, b);
	}

	public static int multiplyBrightness(int color, float brightness) {
		brightness = Math.max(0f, brightness);
		int r = clamp255((int) (red(color) * brightness));
		int g = clamp255((int) (green(color) * brightness));
		int b = clamp255((int) (blue(color) * brightness));
		return argb(alpha(color), r, g, b);
	}

	public static int saturate(int color, float saturation) {
		float r = red(color) / 255f;
		float g = green(color) / 255f;
		float b = blue(color) / 255f;
		float gray = r * 0.299f + g * 0.587f + b * 0.114f;
		r = gray + (r - gray) * saturation;
		g = gray + (g - gray) * saturation;
		b = gray + (b - gray) * saturation;
		return argb(alpha(color), clamp255((int) (r * 255f)), clamp255((int) (g * 255f)), clamp255((int) (b * 255f)));
	}

	public static int alpha(int color) {
		return (color >>> 24) & 255;
	}

	public static int red(int color) {
		return (color >>> 16) & 255;
	}

	public static int green(int color) {
		return (color >>> 8) & 255;
	}

	public static int blue(int color) {
		return color & 255;
	}

	public static float clamp01(float v) {
		return v < 0f ? 0f : Math.min(v, 1f);
	}

	public static int clamp255(int v) {
		return v < 0 ? 0 : Math.min(v, 255);
	}

	private static int mix(int a, int b, float t) {
		return Math.round(a + (b - a) * t);
	}
}
