package com.blazemorphx.optimization;

import net.fabricmc.loader.api.FabricLoader;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModCompatibilityManager {
	private static final Map<String, Boolean> MODS = new LinkedHashMap<>();

	private ModCompatibilityManager() {
	}

	public static void detect() {
		MODS.put("sodium", loaded("sodium"));
		MODS.put("lithium", loaded("lithium"));
		MODS.put("immediatelyfast", loaded("immediatelyfast"));
		MODS.put("entityculling", loaded("entityculling"));
		MODS.put("iris", loaded("iris"));
		MODS.put("indium", loaded("indium"));
		MODS.put("ferritecore", loaded("ferritecore"));
	}

	public static boolean loaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}

	public static Map<String, Boolean> snapshot() {
		if (MODS.isEmpty()) {
			detect();
		}
		return Map.copyOf(MODS);
	}

	public static String summary() {
		StringBuilder builder = new StringBuilder();
		snapshot().forEach((id, present) -> builder.append(id).append(": ").append(present ? "detected" : "not installed").append('\n'));
		return builder.toString().trim();
	}
}
