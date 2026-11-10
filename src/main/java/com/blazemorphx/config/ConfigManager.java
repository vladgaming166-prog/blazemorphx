package com.blazemorphx.config;

import com.blazemorphx.BlazeMorphx;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ModConfig config = new ModConfig();
	private static Path root;
	private static boolean loaded;

	private ConfigManager() {
	}

	public static void load() {
		root = FabricLoader.getInstance().getConfigDir().resolve("blazemorphx");
		try {
			Files.createDirectories(root);
			Files.createDirectories(root.resolve("fonts"));
			Files.createDirectories(root.resolve("backgrounds"));
			Files.createDirectories(root.resolve("cache"));
			Files.createDirectories(root.resolve("profiles"));
			Path file = file();
			if (Files.exists(file)) {
				config = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), ModConfig.class);
				if (config == null) {
					config = new ModConfig();
				}
				if (config.hudWidgets == null || config.hudWidgets.isEmpty()) {
					config = new ModConfig();
				}
			} else {
				save();
			}
			loaded = true;
		} catch (Exception e) {
			BlazeMorphx.LOGGER.warn("Failed to load Blaze Morphx config, using defaults", e);
			config = new ModConfig();
			loaded = true;
		}
	}

	public static void save() {
		try {
			if (root == null) {
				root = FabricLoader.getInstance().getConfigDir().resolve("blazemorphx");
				Files.createDirectories(root);
			}
			Files.writeString(file(), GSON.toJson(config), StandardCharsets.UTF_8);
		} catch (IOException e) {
			BlazeMorphx.LOGGER.warn("Failed to save Blaze Morphx config", e);
		}
	}

	public static ModConfig get() {
		if (!loaded) {
			load();
		}
		return config;
	}

	public static Path directory() {
		if (root == null) {
			load();
		}
		return root;
	}

	public static Path file() {
		return directory().resolve("config.json");
	}

	public static void reset() {
		config = new ModConfig();
		save();
	}

	public static void replace(ModConfig replacement) {
		config = replacement == null ? new ModConfig() : replacement;
		save();
	}

	public static String exportJson() {
		return GSON.toJson(config);
	}

	public static boolean importJson(String json) {
		try {
			ModConfig parsed = GSON.fromJson(json, ModConfig.class);
			if (parsed == null) {
				return false;
			}
			replace(parsed);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Gson gson() {
		return GSON;
	}
}
