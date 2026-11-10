package com.blazemorphx.gui.components;

import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class CustomCategoryTab extends ClickableWidget {
	private final Consumer<String> onSelect;
	private boolean selected;

	public CustomCategoryTab(int x, int y, int w, int h, String label, boolean selected, Consumer<String> onSelect) {
		super(x, y, w, h, Text.literal(label));
		this.selected = selected;
		this.onSelect = onSelect;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		GlassRenderer.button(context, getX(), getY(), getWidth(), getHeight(), selected || isHovered() ? 1f : 0.15f, selected);
		FontManager.get().drawCentered(context, getMessage().getString(), getX() + getWidth() / 2, getY() + 6, selected ? ThemeManager.accent() : ThemeManager.text());
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		onSelect.accept(getMessage().getString());
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
}
