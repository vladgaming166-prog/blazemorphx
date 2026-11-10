package com.blazemorphx.input;

import com.blazemorphx.customization.HUDManager;
import com.blazemorphx.gui.HUDCustomizationScreen;
import com.blazemorphx.gui.MainMenuScreen;
import com.blazemorphx.gui.SettingsScreen;
import com.blazemorphx.util.Identifiers;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
	public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifiers.of("main"));
	public static KeyBinding menu;
	public static KeyBinding hudEditor;
	public static KeyBinding stopwatch;

	private KeyBindings() {
	}

	public static void register() {
		menu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.blazemorphx.menu",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_RIGHT_SHIFT,
			CATEGORY
		));
		hudEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.blazemorphx.hud_editor",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_RIGHT_CONTROL,
			CATEGORY
		));
		stopwatch = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.blazemorphx.stopwatch",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_F7,
			CATEGORY
		));
	}

	public static void tick(MinecraftClient client) {
		while (menu.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new SettingsScreen(null));
			} else if (!(client.currentScreen instanceof MainMenuScreen)) {
				client.setScreen(new SettingsScreen(client.currentScreen));
			}
		}
		while (hudEditor.wasPressed()) {
			client.setScreen(new HUDCustomizationScreen(client.currentScreen));
		}
		while (stopwatch.wasPressed()) {
			HUDManager.get().toggleStopwatch();
		}
	}
}
