package com.blazemorphx;

import com.blazemorphx.account.AccountManager;
import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.customization.BackgroundManager;
import com.blazemorphx.customization.CrosshairManager;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.HUDManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.gui.MainMenuScreen;
import com.blazemorphx.input.ClickTracker;
import com.blazemorphx.input.KeyBindings;
import com.blazemorphx.optimization.FPSMonitor;
import com.blazemorphx.optimization.MemoryMonitor;
import com.blazemorphx.optimization.ModCompatibilityManager;
import com.blazemorphx.optimization.OptimizationManager;
import com.blazemorphx.render.NotificationRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlazeMorphx implements ClientModInitializer {
	public static final String MOD_ID = "blazemorphx";
	public static final String NAME = "Blaze Morphx";
	public static final String VERSION = "1.0.0";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	private static BlazeMorphx instance;

	@Override
	public void onInitializeClient() {
		instance = this;
		LOGGER.info("Igniting {} {} for Minecraft 1.21.11", NAME, VERSION);

		ConfigManager.load();
		ThemeManager.init();
		FontManager.init();
		AccountManager.get().load();
		BackgroundManager.get().init();
		OptimizationManager.get().init();
		ModCompatibilityManager.detect();
		KeyBindings.register();
		ClickTracker.init();
		FPSMonitor.get().init();
		MemoryMonitor.get().init();
		NotificationRenderer.get().push("Welcome to Blaze Morphx", "Glass UI · Accounts · Optimization");

		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (context, tickCounter) -> {
			if (CrosshairManager.get().shouldReplaceVanilla()) {
				CrosshairManager.get().render(context);
			} else {
				original.render(context, tickCounter);
			}
		});

		HudElementRegistry.addLast(Identifier.of(MOD_ID, "overlay"), (context, tickCounter) -> {
			HUDManager.get().render(context, tickCounter);
			NotificationRenderer.get().render(context);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			KeyBindings.tick(client);
			ClickTracker.tick();
			FPSMonitor.get().tick(client);
			MemoryMonitor.get().tick();
			HUDManager.get().tick(client);
			NotificationRenderer.get().tick();
			BackgroundManager.get().tickPlayback(client);
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			BackgroundManager.get().shutdown();
			ConfigManager.save();
			AccountManager.get().save();
			LOGGER.info("Blaze Morphx shutdown complete");
		});
	}

	public static BlazeMorphx getInstance() {
		return instance;
	}

	public static boolean shouldReplaceTitle(net.minecraft.client.gui.screen.Screen screen) {
		return screen != null
			&& screen.getClass() == TitleScreen.class
			&& ConfigManager.get().customMainMenu
			&& !(screen instanceof MainMenuScreen);
	}

	public static MinecraftClient client() {
		return MinecraftClient.getInstance();
	}
}
