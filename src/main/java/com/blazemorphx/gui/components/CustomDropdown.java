package com.blazemorphx.gui.components;

import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CustomDropdown extends ClickableWidget {
	private final List<String> options;
	private final Supplier<String> getter;
	private final Consumer<String> setter;
	private boolean open;

	public CustomDropdown(int x, int y, int w, int h, String label, List<String> options, Supplier<String> getter, Consumer<String> setter) {
		super(x, y, w, h, Text.literal(label));
		this.options = options;
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		FontManager.get().draw(context, getMessage().getString(), getX(), getY() - 10, ThemeManager.muted());
		GlassRenderer.button(context, getX(), getY(), getWidth(), 20, isHovered() ? 1f : 0f, open);
		FontManager.get().draw(context, getter.get(), getX() + 8, getY() + 6, ThemeManager.text());
		FontManager.get().draw(context, open ? "^" : "v", getX() + getWidth() - 12, getY() + 6, ThemeManager.accent());
		if (open) {
			int y = getY() + 22;
			GlassRenderer.panel(context, getX(), y, getWidth(), options.size() * 18 + 6);
			for (int i = 0; i < options.size(); i++) {
				int iy = y + 4 + i * 18;
				boolean hover = mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= iy && mouseY <= iy + 16;
				if (hover) {
					context.fill(getX() + 4, iy, getX() + getWidth() - 4, iy + 16, ThemeManager.accentGlow());
				}
				FontManager.get().draw(context, options.get(i), getX() + 8, iy + 4, ThemeManager.text());
			}
		}
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		if (!open) {
			open = true;
			return;
		}
		int rel = (int) click.y() - (getY() + 22);
		if (rel >= 0) {
			int index = rel / 18;
			if (index >= 0 && index < options.size()) {
				setter.accept(options.get(index));
			}
		}
		open = false;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		int extra = open ? options.size() * 18 + 8 : 0;
		return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getHeight() + extra;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
}
