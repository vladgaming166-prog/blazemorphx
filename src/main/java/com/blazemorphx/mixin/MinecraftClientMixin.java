package com.blazemorphx.mixin;

import com.blazemorphx.BlazeMorphx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true, require = 0)
	private Screen blaze$replaceTitleScreen(Screen screen) {
		try {
			return BlazeMorphx.replaceTitleIfNeeded(screen);
		} catch (Throwable t) {
			return screen;
		}
	}
}
