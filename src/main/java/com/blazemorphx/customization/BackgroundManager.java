package com.blazemorphx.customization;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.gui.BlazeScreen;
import com.blazemorphx.render.CustomBackground;
import com.blazemorphx.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class BackgroundManager {
	private static final BackgroundManager INSTANCE = new BackgroundManager();
	private final VideoBackgroundRenderer video = new VideoBackgroundRenderer();
	private final CustomBackground fallback = new CustomBackground();
	private boolean prepared;
	private boolean started;

	private BackgroundManager() {
	}

	public static BackgroundManager get() {
		return INSTANCE;
	}

	public void init() {
		ensureDefaultExtracted();
		prepared = true;
	}

	public void restart() {
		if (!prepared) {
			return;
		}
		started = true;
		ModConfig config = ConfigManager.get();
		if (!config.animatedBackground) {
			video.stop();
			fallback.load(resolveCurrentFile());
			return;
		}
		Path file = resolveCurrentFile();
		if (file != null && isVideo(file)) {
			video.start(file);
			fallback.load(null);
		} else {
			video.stop();
			fallback.load(file);
		}
	}

	public void tickPlayback(MinecraftClient client) {
		if (client.world != null) {
			if (started) {
				video.stop();
				started = false;
			}
			return;
		}
		Screen screen = client.currentScreen;
		if (!(screen instanceof com.blazemorphx.gui.MainMenuScreen)) {
			if (started) {
				video.pause();
			}
			return;
		}
		if (!started) {
			restart();
		} else {
			video.resume();
		}
	}

	public void render(DrawContext context, int width, int height, float delta) {
		if (prepared && !started) {
			try {
				restart();
			} catch (Throwable t) {
				BlazeMorphx.LOGGER.warn("Video background start failed, using fallback", t);
				started = true;
			}
		}
		ModConfig config = ConfigManager.get();
		try {
			boolean drewVideo = false;
			if (config.animatedBackground && !video.isFailed()) {
				video.uploadAndDraw(context, width, height);
				drewVideo = video.lastUploadNs() > 0 && !video.isFailed();
			}
			if (!drewVideo) {
				fallback.render(context, width, height, delta);
			}
		} catch (Throwable t) {
			CustomBackground.renderFallback(context, width, height, delta);
		}
		int overlay = ColorUtil.withAlpha(0xFF050508, ColorUtil.clamp255((int) (config.overlayOpacity * 255)));
		context.fill(0, 0, width, height, overlay);
		if (config.backgroundBlur > 0.01f) {
			int blur = ColorUtil.withAlpha(0xFF000000, ColorUtil.clamp255((int) (config.backgroundBlur * 90)));
			context.fill(0, 0, width, height, blur);
		}
	}

	public boolean importBackground(Path path) {
		if (path == null || !Files.isRegularFile(path)) {
			return false;
		}
		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		if (!isVideo(path) && !isImage(path)) {
			return false;
		}
		try {
			Path dest = ConfigManager.directory().resolve("backgrounds").resolve(path.getFileName().toString());
			Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
			ModConfig config = ConfigManager.get();
			config.backgroundPath = dest.toAbsolutePath().toString();
			config.backgroundType = isVideo(path) ? "video" : "image";
			ConfigManager.save();
			restart();
			return true;
		} catch (Exception e) {
			BlazeMorphx.LOGGER.warn("Failed to import background {}", name, e);
			return false;
		}
	}

	public void resetToDefault() {
		ModConfig config = ConfigManager.get();
		config.backgroundPath = "default";
		config.backgroundType = "video";
		ConfigManager.save();
		restart();
	}

	public void clearCustom() {
		resetToDefault();
	}

	public VideoBackgroundRenderer video() {
		return video;
	}

	public void shutdown() {
		video.stop();
		fallback.close();
	}

	public Path resolveCurrentFile() {
		ModConfig config = ConfigManager.get();
		if (config.backgroundPath == null || config.backgroundPath.isBlank() || "default".equals(config.backgroundPath)) {
			return defaultFile();
		}
		Path custom = Path.of(config.backgroundPath);
		return Files.isRegularFile(custom) ? custom : defaultFile();
	}

	public Path defaultFile() {
		return ConfigManager.directory().resolve("cache").resolve("default_background.mp4");
	}

	private void ensureDefaultExtracted() {
		Path dest = defaultFile();
		if (Files.isRegularFile(dest) && fileSizeSafe(dest) > 1024) {
			return;
		}
		try (InputStream in = BlazeMorphx.class.getResourceAsStream("/assets/blazemorphx/video/default_background.mp4")) {
			if (in != null) {
				Files.createDirectories(dest.getParent());
				Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			BlazeMorphx.LOGGER.warn("Could not extract default background video", e);
		}
	}

	private static boolean isTitleLike(Screen screen) {
		return screen != null && screen.getClass().getName().contains("TitleScreen");
	}

	public static boolean isVideo(Path path) {
		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".mov") || name.endsWith(".mkv") || name.endsWith(".avi");
	}

	public static boolean isImage(Path path) {
		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp");
	}

	private static long fileSizeSafe(Path path) {
		try {
			return Files.size(path);
		} catch (Exception e) {
			return 0;
		}
	}
}
