package com.blazemorphx.render;

import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.util.ColorUtil;
import com.blazemorphx.util.Identifiers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class BlazeLogo {
	public static final Identifier TEXTURE = Identifiers.of("textures/logo.png");

	private BlazeLogo() {
	}

	public static void draw(DrawContext context, int cx, int y, int size, float pulse) {
		int x = cx - size / 2;
		int glow = ColorUtil.withAlpha(ThemeManager.accent(), (int) (50 + 40 * pulse));
		GlassRenderer.rounded(context, x - 6, y - 6, size + 12, size + 12, 10, glow);
		context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, size, size, size, size);
	}

	public static void wordmark(DrawContext context, int cx, int y, String subtitle) {
		var client = MinecraftClient.getInstance();
		String title = "BLAZE MORPHX";
		int width = client.textRenderer.getWidth(title);
		context.drawText(client.textRenderer, title, cx - width / 2, y, ThemeManager.accent(), false);
		if (subtitle != null) {
			int sub = client.textRenderer.getWidth(subtitle);
			context.drawText(client.textRenderer, subtitle, cx - sub / 2, y + 12, ThemeManager.muted(), false);
		}
	}
}
