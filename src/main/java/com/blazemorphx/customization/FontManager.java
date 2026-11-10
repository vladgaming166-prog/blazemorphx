package com.blazemorphx.customization;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FontManager {
	private static final FontManager INSTANCE = new FontManager();
	public static final List<String> BUILTIN = List.of("minecraft", "sans", "serif", "mono", "dialog");

	private Font awtFont;
	private final Map<String, CachedString> cache = new LinkedHashMap<>(64, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, CachedString> eldest) {
			return size() > 128;
		}
	};

	private FontManager() {
	}

	public static FontManager get() {
		return INSTANCE;
	}

	public static void init() {
		INSTANCE.reload();
	}

	public void reload() {
		cache.clear();
		ModConfig config = ConfigManager.get();
		try {
			if (config.customFontPath != null && !config.customFontPath.isBlank()) {
				Path path = Path.of(config.customFontPath);
				if (Files.isRegularFile(path) && isFontFile(path)) {
					try (InputStream in = Files.newInputStream(path)) {
						awtFont = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(style(config), config.fontSize);
						return;
					}
				}
			}
			String name = switch (config.fontName.toLowerCase(Locale.ROOT)) {
				case "serif" -> Font.SERIF;
				case "mono" -> Font.MONOSPACED;
				case "dialog" -> Font.DIALOG;
				case "sans" -> Font.SANS_SERIF;
				default -> null;
			};
			awtFont = name == null ? null : new Font(name, style(config), Math.max(8, Math.round(config.fontSize)));
		} catch (Exception e) {
			awtFont = null;
		}
	}

	public void draw(DrawContext context, String text, int x, int y, int color) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (text == null || text.isEmpty() || client.textRenderer == null) {
			return;
		}
		if (!ConfigManager.get().applyFontToBlazeUi || awtFont == null) {
			context.drawText(client.textRenderer, text, x, y, color, false);
			return;
		}
		context.drawText(client.textRenderer, text, x, y, color, false);
	}

	public void drawCentered(DrawContext context, String text, int cx, int y, int color) {
		int w = width(text);
		draw(context, text, cx - w / 2, y, color);
	}

	public int width(String text) {
		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer renderer = client.textRenderer;
		return renderer == null || text == null ? 0 : renderer.getWidth(text);
	}

	public int lineHeight() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.textRenderer == null ? 9 : client.textRenderer.fontHeight;
	}

	public boolean importFont(Path path) {
		if (path == null || !Files.isRegularFile(path) || !isFontFile(path)) {
			return false;
		}
		try (InputStream in = Files.newInputStream(path)) {
			Font.createFont(Font.TRUETYPE_FONT, in);
			ConfigManager.get().customFontPath = path.toAbsolutePath().toString();
			ConfigManager.get().fontName = path.getFileName().toString();
			ConfigManager.save();
			reload();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void reset() {
		ModConfig config = ConfigManager.get();
		config.fontName = "minecraft";
		config.customFontPath = "";
		config.fontSize = 9f;
		config.fontBold = false;
		config.fontItalic = false;
		ConfigManager.save();
		reload();
	}

	public String previewName() {
		return ConfigManager.get().fontName;
	}

	private static int style(ModConfig config) {
		int style = Font.PLAIN;
		if (config.fontBold) {
			style |= Font.BOLD;
		}
		if (config.fontItalic) {
			style |= Font.ITALIC;
		}
		return style;
	}

	private static boolean isFontFile(Path path) {
		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return name.endsWith(".ttf") || name.endsWith(".otf");
	}

	@SuppressWarnings("unused")
	private BufferedImage rasterize(String text, int color) {
		int style = style(ConfigManager.get());
		Font font = awtFont.deriveFont(style, ConfigManager.get().fontSize);
		BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dummy.createGraphics();
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		int w = Math.max(1, metrics.stringWidth(text) + 4);
		int h = Math.max(1, metrics.getHeight() + 2);
		g.dispose();
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(font);
		graphics.setColor(new java.awt.Color(color, true));
		graphics.drawString(text, 1, metrics.getAscent());
		graphics.dispose();
		return image;
	}

	private record CachedString(int width, int height) {
	}
}
