package com.blazemorphx.gui;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.account.AccountManager;
import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.customization.BackgroundManager;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.render.Animations;
import com.blazemorphx.render.BlazeLogo;
import com.blazemorphx.render.GlassRenderer;
import com.blazemorphx.render.NotificationRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class BlazeScreen extends Screen {
	protected final Screen parent;
	private float intro;

	protected BlazeScreen(String title, Screen parent) {
		super(Text.literal(title));
		this.parent = parent;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		BackgroundManager.get().render(context, width, height, deltaTicks);
		intro = Animations.approach(intro, 1f, deltaTicks);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		drawChrome(context);
		NotificationRenderer.get().render(context);
	}

	protected void drawChrome(DrawContext context) {
		GlassRenderer.panel(context, 16, 10, width - 32, 36, ThemeManager.panelStrong(), ThemeManager.glassBorder(), 0.5f);
		if (ConfigManager.get().showLogo) {
			BlazeLogo.draw(context, 40, 14, 28, Animations.pulse(System.currentTimeMillis(), 0.35f));
		}
		FontManager.get().draw(context, BlazeMorphx.NAME.toUpperCase(), 62, 22, ThemeManager.accent());
		FontManager.get().draw(context, getTitle().getString(), width / 2 - FontManager.get().width(getTitle().getString()) / 2, 22, ThemeManager.text());
		var account = AccountManager.get().selected();
		String session = account == null ? "No account" : account.username + "  " + account.badge();
		FontManager.get().draw(context, session, width - 28 - FontManager.get().width(session), 22, ThemeManager.muted());
		FontManager.get().draw(context, "v" + BlazeMorphx.VERSION + " · 1.21.11", 20, height - 16, ThemeManager.muted());
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
		return 56;
	}
}
