package com.blazemorphx.mixin;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.gui.MainMenuScreen;
import com.blazemorphx.optimization.OptimizationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
	private Screen blaze$replaceTitleScreen(Screen screen) {
		try {
			if (BlazeMorphx.shouldReplaceTitle(screen)) {
				return new MainMenuScreen();
			}
		} catch (Throwable t) {
			BlazeMorphx.LOGGER.warn("Custom title screen could not be opened, using vanilla", t);
		}
		return screen;
	}

	@Inject(method = "getWindowTitle", at = @At("RETURN"), cancellable = true)
	private void blaze$windowTitle(CallbackInfoReturnable<String> cir) {
		try {
			if (ConfigManager.get().customMainMenu) {
				cir.setReturnValue("Blaze Morphx 1.0 · " + cir.getReturnValue());
			}
		} catch (Throwable ignored) {
		}
	}

	@Inject(method = "getCurrentFps", at = @At("HEAD"))
	private void blaze$resetParticleBudget() {
		try {
			OptimizationManager.get().resetParticleCounter();
		} catch (Throwable ignored) {
		}
	}
}
