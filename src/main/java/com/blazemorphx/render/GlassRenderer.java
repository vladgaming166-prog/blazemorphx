package com.blazemorphx.render;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public final class GlassRenderer {
	private GlassRenderer() {
	}

	public static void panel(DrawContext context, int x, int y, int w, int h) {
		panel(context, x, y, w, h, ThemeManager.panel(), ThemeManager.glassBorder(), 1f);
	}

	public static void panel(DrawContext context, int x, int y, int w, int h, int fill, int border, float glow) {
		int radius = Math.max(2, Math.round(ConfigManager.get().roundedCorners));
		if (glow > 0.01f) {
			int glowColor = ColorUtil.withAlpha(ThemeManager.accent(), (int) (40 * glow));
			rounded(context, x - 2, y - 2, w + 4, h + 4, radius + 2, glowColor);
		}
		rounded(context, x, y, w, h, radius, fill);
		context.fill(x + radius, y, x + w - radius, y + 1, ThemeManager.glassHighlight());
		outline(context, x, y, w, h, radius, border);
	}

	public static void button(DrawContext context, int x, int y, int w, int h, float hover, boolean focused) {
		int fill = ColorUtil.lerp(ThemeManager.panelStrong(), ColorUtil.withAlpha(ThemeManager.accent(), 70), hover * 0.65f);
		int border = ColorUtil.lerp(ColorUtil.withAlpha(ThemeManager.accent(), 110), ThemeManager.accent(), hover);
		if (focused) {
			border = ThemeManager.accent();
		}
		panel(context, x, y, w, h, fill, border, 0.35f + hover * 0.8f);
		if (hover > 0f) {
			context.fill(x + 6, y + h - 2, x + w - 6, y + h - 1, ColorUtil.withAlpha(ThemeManager.accent(), (int) (180 * hover)));
		}
	}

	public static void rounded(DrawContext context, int x, int y, int w, int h, int radius, int color) {
		int r = Math.min(radius, Math.min(w, h) / 2);
		context.fill(x + r, y, x + w - r, y + h, color);
		context.fill(x, y + r, x + w, y + h - r, color);
		context.fill(x + 1, y + 1, x + r, y + r, color);
		context.fill(x + w - r, y + 1, x + w - 1, y + r, color);
		context.fill(x + 1, y + h - r, x + r, y + h - 1, color);
		context.fill(x + w - r, y + h - r, x + w - 1, y + h - 1, color);
	}

	public static void outline(DrawContext context, int x, int y, int w, int h, int radius, int color) {
		int r = Math.min(radius, Math.min(w, h) / 2);
		context.fill(x + r, y, x + w - r, y + 1, color);
		context.fill(x + r, y + h - 1, x + w - r, y + h, color);
		context.fill(x, y + r, x + 1, y + h - r, color);
		context.fill(x + w - 1, y + r, x + w, y + h - r, color);
	}

	public static void badge(DrawContext context, int x, int y, int w, int h, int color) {
		rounded(context, x, y, w, h, 3, ColorUtil.withAlpha(color, 50));
		outline(context, x, y, w, h, 3, ColorUtil.withAlpha(color, 200));
	}
}
