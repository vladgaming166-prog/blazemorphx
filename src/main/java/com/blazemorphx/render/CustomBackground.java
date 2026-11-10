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
	private static final Identifier POSTER = Identifiers.of("textures/background/poster.png");
	private static final Identifier[] FRAMES = new Identifier[18];
	private static final int FRAME_W = 640;
	private static final int FRAME_H = 360;

	static {
		for (int i = 0; i < FRAMES.length; i++) {
			FRAMES[i] = Identifiers.of("textures/background/frame_%02d.png".formatted(i + 1));
		}
	}

	private NativeImageBackedTexture texture;
	private int width;
	private int height;

	public void load(Path file) {
		close();
		if (file == null || !Files.isRegularFile(file)) {
			return;
		}
		String name = file.getFileName().toString().toLowerCase();
		if (name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".mov") || name.endsWith(".mkv")) {
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
			blit(context, TEXTURE_ID, width, height, screenW, screenH);
			return;
		}
		renderFallback(context, screenW, screenH, delta);
	}

	public static void renderFallback(DrawContext context, int screenW, int screenH, float delta) {
		try {
			int index = (int) ((System.currentTimeMillis() / 160L) % FRAMES.length);
			blit(context, FRAMES[index], FRAME_W, FRAME_H, screenW, screenH);
			return;
		} catch (Throwable ignored) {
			try {
				blit(context, POSTER, 960, 540, screenW, screenH);
				return;
			} catch (Throwable ignoredAgain) {
			}
		}
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

	private static void blit(DrawContext context, Identifier texture, int tw, int th, int screenW, int screenH) {
		float scale = Math.max(screenW / (float) tw, screenH / (float) th);
		int dw = Math.max(1, Math.round(tw * scale));
		int dh = Math.max(1, Math.round(th * scale));
		int dx = (screenW - dw) / 2;
		int dy = (screenH - dh) / 2;
		int color = ColorUtil.multiplyBrightness(0xFFFFFFFF, ConfigManager.get().backgroundBrightness);
		context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, dx, dy, 0, 0, dw, dh, tw, th, color);
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
