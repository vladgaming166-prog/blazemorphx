package com.blazemorphx.config;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.render.NotificationRenderer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigProfile {
	private ConfigProfile() {
	}

	public static void saveCurrent(String name) {
		try {
			Path file = ConfigManager.directory().resolve("profiles").resolve(sanitize(name) + ".json");
			Files.writeString(file, ConfigManager.exportJson(), StandardCharsets.UTF_8);
			ModConfig config = ConfigManager.get();
			if (!config.profiles.contains(name)) {
				config.profiles.add(name);
			}
			config.activeProfile = name;
			ConfigManager.save();
			NotificationRenderer.get().push("Profile saved", name);
		} catch (Exception e) {
			BlazeMorphx.LOGGER.warn("Failed to save profile {}", name, e);
			NotificationRenderer.get().error("Could not save profile " + name);
		}
	}

	public static void load(String name) {
		try {
			Path file = ConfigManager.directory().resolve("profiles").resolve(sanitize(name) + ".json");
			if (!Files.exists(file)) {
				NotificationRenderer.get().error("Profile not found: " + name);
				return;
			}
			if (!ConfigManager.importJson(Files.readString(file, StandardCharsets.UTF_8))) {
				NotificationRenderer.get().error("Profile is invalid");
				return;
			}
			ConfigManager.get().activeProfile = name;
			ConfigManager.save();
			NotificationRenderer.get().push("Profile loaded", name);
		} catch (Exception e) {
			NotificationRenderer.get().error("Could not load profile " + name);
		}
	}

	private static String sanitize(String name) {
		return name.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
}
