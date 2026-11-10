package com.blazemorphx.gui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public final class VanillaSlider extends SliderWidget {
	private final String label;
	private final double min;
	private final double max;
	private final DoubleSupplier getter;
	private final DoubleConsumer setter;
	private final String format;
	private boolean skipApply;

	public VanillaSlider(int x, int y, int w, int h, String label, double min, double max, DoubleSupplier getter, DoubleConsumer setter, String format) {
		super(x, y, w, h, Text.literal(label), 0);
		this.label = label;
		this.min = min;
		this.max = max;
		this.getter = getter;
		this.setter = setter;
		this.format = format;
		this.skipApply = true;
		this.value = clamp((getter.getAsDouble() - min) / (max - min));
		this.skipApply = false;
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		setMessage(Text.literal(label + ": " + String.format(java.util.Locale.ROOT, format, getter.getAsDouble())));
	}

	@Override
	protected void applyValue() {
		if (skipApply) {
			return;
		}
		setter.accept(min + clamp(this.value) * (max - min));
		updateMessage();
	}

	private static double clamp(double value) {
		return value < 0 ? 0 : Math.min(1, value);
	}
}
