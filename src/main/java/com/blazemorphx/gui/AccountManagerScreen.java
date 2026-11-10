package com.blazemorphx.gui;

import com.blazemorphx.account.Account;
import com.blazemorphx.account.AccountManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AccountManagerScreen extends BlazeScreen {
	private final List<Account> view = new ArrayList<>();
	private int page;
	private String status = "Switching accounts does not restart Minecraft. Reconnect to a server after switching if you are in multiplayer.";

	public AccountManagerScreen(Screen parent) {
		super("Accounts", parent);
	}

	@Override
	protected void init() {
		view.clear();
		view.addAll(AccountManager.get().accounts());
		int perPage = Math.max(1, (height - 120) / 24);
		int from = Math.min(page * perPage, Math.max(0, view.size()));
		int y = contentTop() + 28;
		for (int i = from; i < view.size() && y < height - 70; i++) {
			Account account = view.get(i);
			boolean active = AccountManager.get().selected() != null && AccountManager.get().selected().id.equals(account.id);
			String label = (active ? "> " : "  ") + account.username + " [" + account.badge() + "]";
			addDrawableChild(button(width / 2 - 150, y, 220, 20, label, () -> switchTo(account)));
			if (!account.launcher) {
				addDrawableChild(button(width / 2 + 74, y, 76, 20, "Remove", () -> {
					AccountManager.get().remove(account);
					clearAndInit();
				}));
			}
			y += 24;
		}
		addDrawableChild(button(width / 2 - 150, height - 52, 100, 20, "Add Account", () -> client.setScreen(new AddAccountScreen(this))));
		if (from > 0) {
			addDrawableChild(button(width / 2 - 46, height - 52, 50, 20, "<", () -> {
				page = Math.max(0, page - 1);
				clearAndInit();
			}));
		}
		if (from + perPage < view.size()) {
			addDrawableChild(button(width / 2 + 8, height - 52, 50, 20, ">", () -> {
				page++;
				clearAndInit();
			}));
		}
		addDrawableChild(button(width / 2 + 50, height - 52, 100, 20, "Done", this::close));
	}

	private void switchTo(Account account) {
		if (AccountManager.get().isSwitching()) {
			status = "Already switching accounts...";
			return;
		}
		status = "Refreshing credentials for " + account.username + "...";
		AccountManager.get().selectAsync(account, result -> {
			status = result.message();
			clearAndInit();
		});
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		Account selected = AccountManager.get().selected();
		String current = selected == null ? "None" : selected.username + " [" + selected.badge() + "]";
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("Current: " + current), width / 2, contentTop(), 0xFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, contentTop() + 12, 0xFFA0A0A0);
	}
}
