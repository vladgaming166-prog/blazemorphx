package com.blazemorphx.customization;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.util.ColorUtil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Isolated so {@link VideoBackgroundRenderer} can load this class reflectively.
 * JavaCV native libraries must never be initialized from a class used during Fabric startup.
 */
public final class JavaCvDecoder {
	private JavaCvDecoder() {
	}

	public static boolean decode(
		Path file,
		AtomicBoolean running,
		AtomicBoolean paused,
		AtomicReference<int[]> pendingFrame,
		AtomicInteger widthOut,
		AtomicInteger heightOut
	) {
		FFmpegFrameGrabber grabber = null;
		try {
			System.setProperty("org.bytedeco.javacpp.maxbytes", "0");
			System.setProperty("org.bytedeco.javacpp.maxphysicalbytes", "0");
			ModConfig config = ConfigManager.get();
			grabber = new FFmpegFrameGrabber(file.toFile());
			grabber.setOption("threads", "1");
			grabber.setPixelFormat(org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_RGB24);
			grabber.setImageWidth(Math.max(320, config.videoDecodeWidth));
			grabber.start();
			int width = grabber.getImageWidth();
			int height = grabber.getImageHeight();
			if (width <= 0 || height <= 0) {
				return false;
			}
			widthOut.set(width);
			heightOut.set(height);
			double fps = grabber.getFrameRate() > 1 ? grabber.getFrameRate() : 24;
			fps = Math.min(fps, Math.max(8, config.videoFpsLimit));
			long frameNs = (long) (1_000_000_000.0 / fps);
			while (running.get()) {
				if (paused.get()) {
					Thread.sleep(50);
					continue;
				}
				long start = System.nanoTime();
				Frame frame = grabber.grabImage();
				if (frame == null || frame.image == null) {
					grabber.setTimestamp(0);
					continue;
				}
				pendingFrame.set(convert((ByteBuffer) frame.image[0], frame.imageWidth, frame.imageHeight, frame.imageStride, config));
				widthOut.set(frame.imageWidth);
				heightOut.set(frame.imageHeight);
				long wait = frameNs - (System.nanoTime() - start);
				if (wait > 0) {
					Thread.sleep(wait / 1_000_000L, (int) (wait % 1_000_000L));
				}
			}
			return true;
		} catch (Throwable t) {
			BlazeMorphx.LOGGER.info("JavaCV decoder unavailable: {}", t.toString());
			return false;
		} finally {
			if (grabber != null) {
				try {
					grabber.stop();
				} catch (Exception ignored) {
				}
				try {
					grabber.close();
				} catch (Exception ignored) {
				}
			}
		}
	}

	private static int[] convert(ByteBuffer buffer, int width, int height, int stride, ModConfig config) {
		int[] out = new int[width * height];
		int i = 0;
		for (int y = 0; y < height; y++) {
			int row = y * Math.max(stride, width * 3);
			for (int x = 0; x < width; x++) {
				int idx = row + x * 3;
				int r = buffer.get(idx) & 255;
				int g = buffer.get(idx + 1) & 255;
				int b = buffer.get(idx + 2) & 255;
				int color = ColorUtil.argb(255, r, g, b);
				color = ColorUtil.multiplyBrightness(color, config.backgroundBrightness);
				color = ColorUtil.saturate(color, config.backgroundSaturation);
				out[i++] = color;
			}
		}
		return out;
	}
}
