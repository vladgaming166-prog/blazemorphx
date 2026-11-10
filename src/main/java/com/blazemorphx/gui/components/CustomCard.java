package com.blazemorphx.gui.components;

import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

public final class CustomCard {
	private CustomCard() {
	}

	public static void draw(DrawContext context, int x, int y, int w, int h, String title, String body) {
		GlassRenderer.panel(context, x, y, w, h);
		FontManager.get().draw(context, title, x + 12, y + 10, ThemeManager.accent());
		if (body != null) {
			FontManager.get().draw(context, body, x + 12, y + 24, ThemeManager.muted());
		}
	}
}
