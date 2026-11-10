package com.blazemorphx.gui.components;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.render.Animations;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class CustomButton extends ClickableWidget {
	private final Runnable onPress;
	private float hover;
	private int accentOverride = Integer.MIN_VALUE;

	public CustomButton(int x, int y, int w, int h, String label, Runnable onPress) {
		super(x, y, w, h, Text.literal(label));
		this.onPress = onPress;
	}

	public CustomButton accent(int color) {
		this.accentOverride = color;
		return this;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		hover = Animations.approach(hover, isHovered() || isFocused() ? 1f : 0f, deltaTicks);
		GlassRenderer.button(context, getX(), getY(), getWidth(), getHeight(), hover, isFocused());
		int color = accentOverride == Integer.MIN_VALUE ? ThemeManager.text() : accentOverride;
		FontManager.get().drawCentered(context, getMessage().getString(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		if (!active) {
			return;
		}
		if (ConfigManager.get().soundEffects) {
			playDownSound(MinecraftClient.getInstance().getSoundManager());
		}
		onPress.run();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
}
