package com.blazemorphx.optimization;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayDeque;
import java.util.Deque;

public final class FPSMonitor {
	private static final FPSMonitor INSTANCE = new FPSMonitor();
	private final Deque<Integer> history = new ArrayDeque<>();
	private int current;
	private int min = Integer.MAX_VALUE;
	private int max;
	private long samples;
	private long total;
	private Integer beforeAverage;
	private Integer afterAverage;
	private long lastFrameNs = System.nanoTime();
	private double frameMs;

	private FPSMonitor() {
	}

	public static FPSMonitor get() {
		return INSTANCE;
	}

	public void init() {
		history.clear();
	}

	public void tick(MinecraftClient client) {
		current = client.getCurrentFps();
		long now = System.nanoTime();
		frameMs = (now - lastFrameNs) / 1_000_000.0;
		lastFrameNs = now;
		if (current < min) {
			min = current;
		}
		if (current > max) {
			max = current;
		}
		samples++;
		total += current;
		history.addLast(current);
		while (history.size() > 180) {
			history.removeFirst();
		}
	}

	public void markBefore() {
		beforeAverage = average();
	}

	public void markAfter() {
		afterAverage = average();
	}

	public int current() {
		return current;
	}

	public int average() {
		return samples == 0 ? 0 : (int) (total / samples);
	}

	public int min() {
		return min == Integer.MAX_VALUE ? 0 : min;
	}

	public int max() {
		return max;
	}

	public double frameMs() {
		return frameMs;
	}

	public Integer beforeAverage() {
		return beforeAverage;
	}

	public Integer afterAverage() {
		return afterAverage;
	}

	public int[] graph() {
		int[] data = new int[history.size()];
		int i = 0;
		for (int value : history) {
			data[i++] = value;
		}
		return data;
	}

	public void resetStats() {
		min = Integer.MAX_VALUE;
		max = 0;
		samples = 0;
		total = 0;
		history.clear();
	}
}
