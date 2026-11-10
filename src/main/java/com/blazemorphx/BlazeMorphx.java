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
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlazeMorphx implements ClientModInitializer {
	public static final String MOD_ID = "blazemorphx";
	public static final String NAME = "Blaze Morphx";
	public static final String VERSION = "1.0.1";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	private static BlazeMorphx instance;

	@Override
	public void onInitializeClient() {
		instance = this;
		LOGGER.info("Igniting {} {} for Minecraft 1.21.11 (Fabric Loader {})", NAME, VERSION, loaderVersion());

		safe("config", ConfigManager::load);
		safe("themes", ThemeManager::init);
		safe("fonts", FontManager::init);
		safe("accounts", () -> AccountManager.get().load());
		safe("backgrounds", () -> BackgroundManager.get().init());
		safe("optimization", () -> OptimizationManager.get().init());
		safe("compatibility", ModCompatibilityManager::detect);
		safe("keybinds", KeyBindings::register);
		safe("clicks", ClickTracker::init);
		safe("fps", () -> FPSMonitor.get().init());
		safe("memory", () -> MemoryMonitor.get().init());
		safe("hud", BlazeMorphx::registerHud);
		safe("welcome", () -> NotificationRenderer.get().push("Welcome to Blaze Morphx", "Glass UI · Accounts · Optimization"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			safe("tick-keys", () -> KeyBindings.tick(client));
			safe("tick-clicks", ClickTracker::tick);
			safe("tick-fps", () -> FPSMonitor.get().tick(client));
			safe("tick-memory", () -> MemoryMonitor.get().tick());
			safe("tick-hud", () -> HUDManager.get().tick(client));
			safe("tick-notify", () -> NotificationRenderer.get().tick());
			safe("tick-video", () -> BackgroundManager.get().tickPlayback(client));
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			safe("shutdown-video", () -> BackgroundManager.get().shutdown());
			safe("shutdown-config", ConfigManager::save);
			safe("shutdown-accounts", () -> AccountManager.get().save());
			LOGGER.info("Blaze Morphx shutdown complete");
		});
	}

	private static void registerHud() {
		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (context, tickCounter) -> {
			try {
				if (CrosshairManager.get().shouldReplaceVanilla()) {
					CrosshairManager.get().render(context);
				} else {
					original.render(context, tickCounter);
				}
			} catch (Throwable t) {
				LOGGER.debug("Crosshair overlay failed", t);
			}
		});
		HudElementRegistry.addLast(Identifier.of(MOD_ID, "overlay"), (context, tickCounter) -> {
			try {
				HUDManager.get().render(context, tickCounter);
				NotificationRenderer.get().render(context);
			} catch (Throwable t) {
				LOGGER.debug("HUD overlay failed", t);
			}
		});
	}

	public static BlazeMorphx getInstance() {
		return instance;
	}

	public static boolean shouldReplaceTitle(net.minecraft.client.gui.screen.Screen screen) {
		try {
			return screen != null
				&& screen.getClass() == TitleScreen.class
				&& ConfigManager.get().customMainMenu
				&& !(screen instanceof MainMenuScreen);
		} catch (Throwable t) {
			return false;
		}
	}

	public static MinecraftClient client() {
		return MinecraftClient.getInstance();
	}

	public static void safe(String step, Runnable action) {
		try {
			action.run();
		} catch (Throwable t) {
			LOGGER.error("Blaze Morphx step '{}' failed; continuing so the game can still launch", step, t);
		}
	}

	private static String loaderVersion() {
		try {
			return FabricLoader.getInstance()
				.getModContainer("fabricloader")
				.map(ModContainer::getMetadata)
				.map(meta -> meta.getVersion().getFriendlyString())
				.orElse("unknown");
		} catch (Throwable t) {
			return "unknown";
		}
	}
}
