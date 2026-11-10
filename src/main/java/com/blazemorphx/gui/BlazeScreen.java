package com.blazemorphx.gui;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.account.AccountManager;
import com.blazemorphx.customization.BackgroundManager;
import com.blazemorphx.render.NotificationRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public abstract class BlazeScreen extends Screen {
	protected final Screen parent;

	protected BlazeScreen(String title, Screen parent) {
		super(Text.literal(title));
		this.parent = parent;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (this instanceof MainMenuScreen) {
			BackgroundManager.get().render(context, width, height, deltaTicks);
		} else {
			super.renderBackground(context, mouseX, mouseY, deltaTicks);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(textRenderer, this.title, width / 2, 15, 0xFFFFFFFF);
		var account = AccountManager.get().selected();
		String status = account == null
			? BlazeMorphx.NAME + "  ·  no account selected"
			: BlazeMorphx.NAME + "  ·  " + account.username + " [" + account.badge() + "]";
		context.drawTextWithShadow(textRenderer, status, 8, height - 12, 0xFFA0A0A0);
		NotificationRenderer.get().render(context);
	}

	@Override
	public void close() {
		if (client != null) {
			client.setScreen(parent);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	protected int contentTop() {
		return 40;
	}

	protected ButtonWidget button(int x, int y, int w, int h, String label, Runnable action) {
		return ButtonWidget.builder(Text.literal(label), btn -> action.run()).dimensions(x, y, w, h).build();
	}

	protected ButtonWidget backButton() {
		return button(width / 2 - 100, height - 28, 200, 20, "Done", this::close);
	}
}
