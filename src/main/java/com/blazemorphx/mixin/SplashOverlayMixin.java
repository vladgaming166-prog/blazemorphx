package com.blazemorphx.mixin;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.render.BlazeLogo;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void blaze$brand(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
		int w = context.getScaledWindowWidth();
		int h = context.getScaledWindowHeight();
		GlassRenderer.panel(context, w / 2 - 90, h - 48, 180, 28);
		BlazeLogo.wordmark(context, w / 2, h - 38, BlazeMorphx.NAME + " " + BlazeMorphx.VERSION);
		context.fill(0, 0, w, 2, ThemeManager.accent());
	}
}
