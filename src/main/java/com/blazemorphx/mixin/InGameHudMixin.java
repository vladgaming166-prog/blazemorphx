package com.blazemorphx.mixin;

import com.blazemorphx.optimization.OptimizationManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
	private void blaze$skipVignette(DrawContext context, Entity entity, CallbackInfo ci) {
		if (OptimizationManager.get().skipVignette()) {
			ci.cancel();
		}
	}
}
