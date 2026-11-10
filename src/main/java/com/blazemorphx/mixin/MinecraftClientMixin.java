package com.blazemorphx.mixin;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.gui.MainMenuScreen;
import com.blazemorphx.optimization.OptimizationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
	private Screen blaze$replaceTitleScreen(Screen screen) {
		if (BlazeMorphx.shouldReplaceTitle(screen)) {
			return new MainMenuScreen();
		}
		return screen;
	}

	@Inject(method = "getWindowTitle", at = @At("RETURN"), cancellable = true)
	private void blaze$windowTitle(CallbackInfoReturnable<String> cir) {
		if (ConfigManager.get().customMainMenu) {
			cir.setReturnValue("Blaze Morphx 1.0 · " + cir.getReturnValue());
		}
	}

	@Inject(method = "getCurrentFps", at = @At("HEAD"))
	private void blaze$resetParticleBudget() {
		OptimizationManager.get().resetParticleCounter();
	}
}
