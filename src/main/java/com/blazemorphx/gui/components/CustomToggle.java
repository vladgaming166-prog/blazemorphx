package com.blazemorphx.gui.components;

import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.render.Animations;
import com.blazemorphx.render.GlassRenderer;
import com.blazemorphx.util.ColorUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CustomToggle extends ClickableWidget {
	private final Supplier<Boolean> getter;
	private final Consumer<Boolean> setter;
	private float anim;

	public CustomToggle(int x, int y, int w, int h, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		super(x, y, w, h, Text.literal(label));
		this.getter = getter;
		this.setter = setter;
		this.anim = getter.get() ? 1f : 0f;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		boolean on = getter.get();
		anim = Animations.approach(anim, on ? 1f : 0f, deltaTicks);
		FontManager.get().draw(context, getMessage().getString(), getX(), getY() + 6, ThemeManager.text());
		int tx = getX() + getWidth() - 34;
		int ty = getY() + 4;
		int fill = ColorUtil.lerp(0xAA1A1A1E, ColorUtil.withAlpha(ThemeManager.accent(), 180), anim);
		GlassRenderer.rounded(context, tx, ty, 32, 16, 8, fill);
		int knob = tx + 2 + Math.round(16 * anim);
		GlassRenderer.rounded(context, knob, ty + 2, 12, 12, 6, 0xFFF5F5F5);
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		setter.accept(!getter.get());
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
}
