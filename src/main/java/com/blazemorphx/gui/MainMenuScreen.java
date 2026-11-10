package com.blazemorphx.gui;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.account.AccountManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

public class MainMenuScreen extends BlazeScreen {
	public MainMenuScreen() {
		super("Blaze Morphx", null);
	}

	@Override
	protected void init() {
		int w = 200;
		int x = this.width / 2 - w / 2;
		int y = this.height / 4 + 48;
		int gap = 24;
		addDrawableChild(button(x, y, w, 20, "Singleplayer", () -> client.setScreen(new SelectWorldScreen(this))));
		addDrawableChild(button(x, y + gap, w, 20, "Multiplayer", () -> client.setScreen(new MultiplayerScreen(this))));
		addDrawableChild(button(x, y + gap * 2, w, 20, "Accounts...", () -> client.setScreen(new AccountManagerScreen(this))));
		addDrawableChild(button(x, y + gap * 3, w, 20, "Customization", () -> client.setScreen(new CustomizationScreen(this))));
		addDrawableChild(button(x, y + gap * 4, w, 20, "Optimization", () -> client.setScreen(new OptimizationScreen(this))));
		addDrawableChild(button(x, y + gap * 5, 98, 20, "Settings", () -> client.setScreen(new SettingsScreen(this))));
		addDrawableChild(button(x + 102, y + gap * 5, 98, 20, "Vanilla Options", () -> client.setScreen(new OptionsScreen(this, client.options))));
		addDrawableChild(button(x, y + gap * 6, w, 20, "Quit Game", () -> client.scheduleStop()));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(textRenderer, Text.literal(BlazeMorphx.NAME), width / 2, height / 4 + 24, 0xFFFFFF);
		var account = AccountManager.get().selected();
		String footer = account == null ? "No account selected" : account.username + " · " + account.badge();
		context.drawCenteredTextWithShadow(textRenderer, footer, width / 2, height - 40, 0xFFA0A0A0);
		context.drawTextWithShadow(textRenderer, BlazeMorphx.VERSION + " · 1.21.11", 8, height - 12, 0xFF808080);
	}

	@Override
	public void close() {
	}

	@Override
	public void removed() {
		super.removed();
	}
}
