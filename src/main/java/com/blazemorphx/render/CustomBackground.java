package com.blazemorphx.render;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.util.ColorUtil;
import com.blazemorphx.util.Identifiers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CustomBackground {
	private static final Identifier TEXTURE_ID = Identifiers.of("dynamic/static_background");
	private NativeImageBackedTexture texture;
	private int width;
	private int height;

	public void load(Path file) {
		close();
		if (file == null || !Files.isRegularFile(file)) {
			return;
		}
		try (InputStream in = Files.newInputStream(file)) {
			NativeImage image = NativeImage.read(in);
			width = image.getWidth();
			height = image.getHeight();
			texture = new NativeImageBackedTexture(() -> "blaze_static_bg", image);
			MinecraftClient.getInstance().getTextureManager().registerTexture(TEXTURE_ID, texture);
		} catch (Exception ignored) {
			close();
		}
	}

	public void render(DrawContext context, int screenW, int screenH, float delta) {
		if (texture != null && width > 0 && height > 0) {
			float scale = Math.max(screenW / (float) width, screenH / (float) height);
			int dw = Math.round(width * scale);
			int dh = Math.round(height * scale);
			int dx = (screenW - dw) / 2;
			int dy = (screenH - dh) / 2;
			int color = ColorUtil.multiplyBrightness(0xFFFFFFFF, ConfigManager.get().backgroundBrightness);
			context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, dx, dy, 0, 0, dw, dh, width, height, color);
			return;
		}
		renderFallback(context, screenW, screenH, delta);
	}

	public static void renderFallback(DrawContext context, int screenW, int screenH, float delta) {
		long time = System.currentTimeMillis();
		context.fillGradient(0, 0, screenW, screenH, 0xFF05070C, 0xFF140801);
		for (int i = 0; i < 28; i++) {
			float t = (float) ((time / 40.0 + i * 37) % 1000) / 1000f;
			int bx = (int) ((Math.sin(i * 1.7 + time / 1400.0) * 0.5 + 0.5) * screenW);
			int by = screenH - (int) (t * (screenH + 40));
			int size = 3 + (i % 5);
			int color = ColorUtil.withAlpha(ThemeManager.accent(), 40 + (i % 5) * 12);
			context.fill(bx, by, bx + size, by + size, color);
		}
		int glow = ColorUtil.withAlpha(ThemeManager.accent(), 28);
		context.fill(screenW / 2 - 180, 40, screenW / 2 + 180, 160, glow);
	}

	public void close() {
		if (texture != null) {
			texture.close();
			texture = null;
		}
		width = 0;
		height = 0;
	}
}
