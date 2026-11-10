package com.blazemorphx.customization;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class VideoBackgroundRenderer {
	private static final Identifier TEXTURE_ID = Identifiers.of("dynamic/menu_video");
	private final AtomicBoolean running = new AtomicBoolean();
	private final AtomicBoolean paused = new AtomicBoolean();
	private final AtomicReference<int[]> pendingFrame = new AtomicReference<>();
	private final AtomicInteger widthOut = new AtomicInteger(640);
	private final AtomicInteger heightOut = new AtomicInteger(360);
	private Thread decoderThread;
	private NativeImageBackedTexture texture;
	private int texWidth = 640;
	private int texHeight = 360;
	private volatile boolean failed;
	private volatile String status = "idle";
	private Path activeFile;
	private long lastUploadNs;
	private Process ffmpegProcess;

	public synchronized void start(Path file) {
		stop();
		failed = false;
		activeFile = file;
		if (file == null || !Files.isRegularFile(file) || !hasFfmpeg()) {
			failed = true;
			status = "unavailable";
			return;
		}
		running.set(true);
		paused.set(false);
		decoderThread = new Thread(this::decodeLoop, "BlazeMorphx-Video");
		decoderThread.setDaemon(true);
		decoderThread.start();
	}

	public void pause() {
		paused.set(true);
	}

	public void resume() {
		paused.set(false);
	}

	public synchronized void stop() {
		running.set(false);
		destroyProcess();
		if (decoderThread != null) {
			decoderThread.interrupt();
			decoderThread = null;
		}
		pendingFrame.set(null);
	}

	public boolean isFailed() {
		return failed;
	}

	public String status() {
		return status;
	}

	public void uploadAndDraw(DrawContext context, int screenW, int screenH) {
		if (failed) {
			return;
		}
		texWidth = Math.max(1, widthOut.get());
		texHeight = Math.max(1, heightOut.get());
		try {
			int[] frame = pendingFrame.getAndSet(null);
			if (frame == null) {
				if (texture == null) {
					return;
				}
			} else {
				ensureTexture();
				if (texture != null && texture.getImage() != null
					&& texture.getImage().getWidth() == texWidth
					&& texture.getImage().getHeight() == texHeight
					&& frame.length >= texWidth * texHeight) {
					NativeImage image = texture.getImage();
					int i = 0;
					for (int y = 0; y < texHeight; y++) {
						for (int x = 0; x < texWidth; x++) {
							image.setColorArgb(x, y, frame[i++]);
						}
					}
					texture.upload();
					lastUploadNs = System.nanoTime();
				}
			}
			if (texture == null) {
				return;
			}
			ModConfig config = ConfigManager.get();
			int[] dest = destination(screenW, screenH, texWidth, texHeight, config.backgroundScale);
			int color = ColorUtil.multiplyBrightness(0xFFFFFFFF, config.backgroundBrightness);
			context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, dest[0], dest[1], 0, 0, dest[2], dest[3], texWidth, texHeight, color);
		} catch (Throwable t) {
			BlazeMorphx.LOGGER.warn("Video frame upload failed, using fallback");
			failed = true;
		}
	}

	private void decodeLoop() {
		status = "starting";
		if (tryFfmpegCli()) {
			return;
		}
		failed = true;
		status = "fallback";
		running.set(false);
	}

	private boolean tryFfmpegCli() {
		try {
			ModConfig config = ConfigManager.get();
			texWidth = Math.max(320, config.videoDecodeWidth);
			texHeight = Math.max(180, texWidth * 9 / 16);
			widthOut.set(texWidth);
			heightOut.set(texHeight);
			ProcessBuilder builder = new ProcessBuilder(
				"ffmpeg", "-hide_banner", "-loglevel", "error",
				"-stream_loop", "-1", "-an", "-i", activeFile.toAbsolutePath().toString(),
				"-vf", "scale=" + texWidth + ":" + texHeight + ":force_original_aspect_ratio=decrease,fps=" + Math.max(8, config.videoFpsLimit),
				"-f", "rawvideo", "-pix_fmt", "rgb24", "pipe:1"
			);
			builder.redirectError(ProcessBuilder.Redirect.DISCARD);
			ffmpegProcess = builder.start();
			status = "ffmpeg-cli";
			InputStream in = ffmpegProcess.getInputStream();
			byte[] raw = new byte[texWidth * texHeight * 3];
			while (running.get()) {
				if (paused.get()) {
					Thread.sleep(50);
					continue;
				}
				if (!readFully(in, raw)) {
					break;
				}
				pendingFrame.set(convertRgb(raw, texWidth, texHeight));
			}
			destroyProcess();
			return pendingFrame.get() != null || lastUploadNs > 0;
		} catch (Throwable t) {
			destroyProcess();
			return false;
		}
	}

	private int[] convertRgb(byte[] raw, int width, int height) {
		int[] out = new int[width * height];
		ModConfig config = ConfigManager.get();
		int i = 0;
		int p = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int r = raw[p++] & 255;
				int g = raw[p++] & 255;
				int b = raw[p++] & 255;
				int color = ColorUtil.argb(255, r, g, b);
				color = ColorUtil.multiplyBrightness(color, config.backgroundBrightness);
				color = ColorUtil.saturate(color, config.backgroundSaturation);
				out[i++] = color;
			}
		}
		return out;
	}

	private void ensureTexture() {
		if (texture != null && texture.getImage() != null
			&& texture.getImage().getWidth() == texWidth
			&& texture.getImage().getHeight() == texHeight) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.getTextureManager() == null || texWidth <= 0 || texHeight <= 0) {
			return;
		}
		if (texture != null) {
			texture.close();
		}
		texture = new NativeImageBackedTexture(() -> "blaze_video", texWidth, texHeight, false);
		client.getTextureManager().registerTexture(TEXTURE_ID, texture);
	}

	private static int[] destination(int screenW, int screenH, int tw, int th, String mode) {
		if ("stretch".equalsIgnoreCase(mode)) {
			return new int[]{0, 0, screenW, screenH};
		}
		float scale = "fit".equalsIgnoreCase(mode)
			? Math.min(screenW / (float) tw, screenH / (float) th)
			: Math.max(screenW / (float) tw, screenH / (float) th);
		int dw = Math.max(1, Math.round(tw * scale));
		int dh = Math.max(1, Math.round(th * scale));
		int dx = (screenW - dw) / 2;
		int dy = (screenH - dh) / 2;
		return new int[]{dx, dy, dw, dh};
	}

	private static boolean readFully(InputStream in, byte[] raw) throws Exception {
		int off = 0;
		while (off < raw.length) {
			int n = in.read(raw, off, raw.length - off);
			if (n < 0) {
				return false;
			}
			off += n;
		}
		return true;
	}

	private void destroyProcess() {
		if (ffmpegProcess != null) {
			ffmpegProcess.destroy();
			ffmpegProcess = null;
		}
	}

	public static boolean hasFfmpeg() {
		try {
			Process process = new ProcessBuilder("ffmpeg", "-hide_banner", "-version")
				.redirectError(ProcessBuilder.Redirect.DISCARD)
				.redirectOutput(ProcessBuilder.Redirect.DISCARD)
				.start();
			if (!process.waitFor(2, TimeUnit.SECONDS)) {
				process.destroyForcibly();
				return false;
			}
			return process.exitValue() == 0;
		} catch (Throwable t) {
			return false;
		}
	}

	public long lastUploadNs() {
		return lastUploadNs;
	}
}
