package com.blazemorphx.gui.components;

import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CustomTextField extends ClickableWidget {
	private String value;
	private final int maxLength;
	private int cursor;

	public CustomTextField(int x, int y, int w, int h, String value, int maxLength) {
		super(x, y, w, h, Text.literal(value));
		this.value = value == null ? "" : value;
		this.maxLength = maxLength;
		this.cursor = this.value.length();
	}

	public String value() {
		return value;
	}

	public void setValue(String value) {
		this.value = value == null ? "" : value;
		this.cursor = this.value.length();
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		GlassRenderer.panel(context, getX(), getY(), getWidth(), getHeight(), 0xCC0C0C10, isFocused() ? ThemeManager.accent() : ThemeManager.glassBorder(), isFocused() ? 0.6f : 0.15f);
		String shown = value.isEmpty() && !isFocused() ? getMessage().getString() : value;
		int color = value.isEmpty() && !isFocused() ? ThemeManager.muted() : ThemeManager.text();
		FontManager.get().draw(context, shown, getX() + 6, getY() + (getHeight() - 8) / 2, color);
		if (isFocused() && (System.currentTimeMillis() / 400) % 2 == 0) {
			int cx = getX() + 6 + FontManager.get().width(value.substring(0, Math.min(cursor, value.length())));
			context.fill(cx, getY() + 5, cx + 1, getY() + getHeight() - 5, ThemeManager.accent());
		}
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		setFocused(true);
	}

	@Override
	public boolean charTyped(CharInput input) {
		if (!isFocused()) {
			return false;
		}
		if (!input.isValidChar() || value.length() >= maxLength) {
			return false;
		}
		String typed = input.asString();
		value = value.substring(0, cursor) + typed + value.substring(cursor);
		cursor += typed.length();
		return true;
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (!isFocused()) {
			return false;
		}
		if (input.isEscape()) {
			setFocused(false);
			return true;
		}
		if (input.key() == GLFW.GLFW_KEY_BACKSPACE && cursor > 0) {
			value = value.substring(0, cursor - 1) + value.substring(cursor);
			cursor--;
			return true;
		}
		if (input.key() == GLFW.GLFW_KEY_LEFT) {
			cursor = Math.max(0, cursor - 1);
			return true;
		}
		if (input.key() == GLFW.GLFW_KEY_RIGHT) {
			cursor = Math.min(value.length(), cursor + 1);
			return true;
		}
		return false;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
}
