package com.blazemorphx.gui.components;

import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class CustomSlider extends ClickableWidget {
	private final double min;
	private final double max;
	private final DoubleSupplier getter;
	private final DoubleConsumer setter;
	private final String format;

	public CustomSlider(int x, int y, int w, int h, String label, double min, double max, DoubleSupplier getter, DoubleConsumer setter, String format) {
		super(x, y, w, h, Text.literal(label));
		this.min = min;
		this.max = max;
		this.getter = getter;
		this.setter = setter;
		this.format = format;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		double value = getter.getAsDouble();
		float t = (float) ((value - min) / (max - min));
		FontManager.get().draw(context, getMessage().getString(), getX(), getY(), ThemeManager.muted());
		String label = String.format(java.util.Locale.ROOT, format, value);
		FontManager.get().draw(context, label, getX() + getWidth() - FontManager.get().width(label), getY(), ThemeManager.text());
		int barY = getY() + 14;
		GlassRenderer.rounded(context, getX(), barY, getWidth(), 6, 3, 0xAA101014);
		GlassRenderer.rounded(context, getX(), barY, Math.max(6, Math.round(getWidth() * t)), 6, 3, ThemeManager.accent());
		int knob = getX() + Math.round((getWidth() - 8) * t);
		GlassRenderer.rounded(context, knob, barY - 3, 8, 12, 4, 0xFFFFFFFF);
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		update(click.x());
	}

	@Override
	protected void onDrag(Click click, double offsetX, double offsetY) {
		update(click.x());
	}

	private void update(double mouseX) {
		float t = (float) ((mouseX - getX()) / (double) getWidth());
		t = Math.max(0f, Math.min(1f, t));
		setter.accept(min + t * (max - min));
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
}
