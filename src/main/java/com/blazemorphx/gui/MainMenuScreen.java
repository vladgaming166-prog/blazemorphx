package com.blazemorphx.gui;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.account.AccountManager;
import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.gui.components.CustomButton;
import com.blazemorphx.render.Animations;
import com.blazemorphx.render.BlazeLogo;
import com.blazemorphx.render.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

public class MainMenuScreen extends BlazeScreen {
	public MainMenuScreen() {
		super("Main Menu", null);
	}

	@Override
	protected void init() {
		int w = 210;
		int x = this.width / 2 - w / 2;
		int y = this.height / 2 - 8;
		int gap = 24;
		addDrawableChild(new CustomButton(x, y, w, 22, "Singleplayer", () -> client.setScreen(new SelectWorldScreen(this))));
		addDrawableChild(new CustomButton(x, y + gap, w, 22, "Multiplayer", () -> client.setScreen(new MultiplayerScreen(this))));
		addDrawableChild(new CustomButton(x, y + gap * 2, w, 22, "Accounts...", () -> client.setScreen(new AccountManagerScreen(this))));
		addDrawableChild(new CustomButton(x, y + gap * 3, w, 22, "Customization", () -> client.setScreen(new CustomizationScreen(this))));
		addDrawableChild(new CustomButton(x, y + gap * 4, w, 22, "Optimization", () -> client.setScreen(new OptimizationScreen(this))));
		addDrawableChild(new CustomButton(x, y + gap * 5, w, 22, "Settings", () -> client.setScreen(new SettingsScreen(this))));
		addDrawableChild(new CustomButton(x, y + gap * 6, w, 22, "Quit Game", () -> client.scheduleStop()).accent(ThemeManager.danger()));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		int cx = width / 2;
		if (ConfigManager.get().showLogo) {
			BlazeLogo.draw(context, cx, height / 2 - 118, 54, Animations.pulse(System.currentTimeMillis(), 0.4f));
		}
		BlazeLogo.wordmark(context, cx, height / 2 - 58, "ULTIMATE PVP CLIENT");
		var account = AccountManager.get().selected();
		String footer = account == null ? "No account selected" : account.username + " · " + account.badge();
		GlassRenderer.badge(context, 18, height - 38, FontManager.get().width(footer) + 16, 16, account != null && account.isPremium() ? ThemeManager.success() : ThemeManager.accent());
		FontManager.get().draw(context, footer, 26, height - 34, ThemeManager.text());
		FontManager.get().draw(context, BlazeMorphx.NAME + " " + BlazeMorphx.VERSION, width - 24 - FontManager.get().width(BlazeMorphx.NAME + " " + BlazeMorphx.VERSION), height - 34, ThemeManager.muted());
	}

	@Override
	public void close() {
	}
}
