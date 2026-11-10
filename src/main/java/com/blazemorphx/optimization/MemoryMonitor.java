package com.blazemorphx.optimization;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.Deque;

public final class MemoryMonitor {
	private static final MemoryMonitor INSTANCE = new MemoryMonitor();
	private final Deque<Long> history = new ArrayDeque<>();
	private long used;
	private long max;
	private long allocated;

	private MemoryMonitor() {
	}

	public static MemoryMonitor get() {
		return INSTANCE;
	}

	public void init() {
	}

	public void tick() {
		Runtime runtime = Runtime.getRuntime();
		max = runtime.maxMemory();
		allocated = runtime.totalMemory();
		used = allocated - runtime.freeMemory();
		history.addLast(used);
		while (history.size() > 120) {
			history.removeFirst();
		}
	}

	public long used() {
		return used;
	}

	public long max() {
		return max;
	}

	public long allocated() {
		return allocated;
	}

	public int percent() {
		return max == 0 ? 0 : (int) (used * 100 / max);
	}

	public String javaVersion() {
		return System.getProperty("java.version", "unknown");
	}

	public String gcSummary() {
		long count = 0;
		long time = 0;
		for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
			count += Math.max(0, bean.getCollectionCount());
			time += Math.max(0, bean.getCollectionTime());
		}
		return count + " collections / " + time + " ms";
	}

	public long[] graph() {
		long[] data = new long[history.size()];
		int i = 0;
		for (long value : history) {
			data[i++] = value;
		}
		return data;
	}

	public String cpu() {
		String name = System.getenv("PROCESSOR_IDENTIFIER");
		if (name == null || name.isBlank()) {
			name = System.getProperty("os.arch", "unknown");
		}
		return Runtime.getRuntime().availableProcessors() + " threads · " + name;
	}

	public String gpu() {
		return System.getProperty("os.name", "unknown") + " / " + System.getProperty("os.arch", "gpu");
	}

	public static String mb(long bytes) {
		return String.format(java.util.Locale.ROOT, "%.1f MB", bytes / 1024.0 / 1024.0);
	}
}
