package com.blazemorphx.gui;

import com.blazemorphx.account.Account;
import com.blazemorphx.account.AccountManager;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.gui.components.CustomButton;
import com.blazemorphx.render.GlassRenderer;
import com.blazemorphx.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;

public class AccountManagerScreen extends BlazeScreen {
	private final List<Account> view = new ArrayList<>();
	private int scroll;

	public AccountManagerScreen(Screen parent) {
		super("Accounts", parent);
	}

	@Override
	protected void init() {
		view.clear();
		view.addAll(AccountManager.get().accounts());
		int y = height - 36;
		addDrawableChild(new CustomButton(24, y, 120, 22, "Add Account", () -> client.setScreen(new AddAccountScreen(this))));
		addDrawableChild(new CustomButton(152, y, 90, 22, "Back", this::close));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		int y = contentTop();
		Account selected = AccountManager.get().selected();
		GlassRenderer.panel(context, 24, y, width - 48, 42);
		FontManager.get().draw(context, "Selected", 36, y + 8, ThemeManager.muted());
		FontManager.get().draw(context, selected == null ? "None" : selected.username + "  [" + selected.badge() + "]", 36, y + 22, ThemeManager.text());
		y += 56;
		int index = 0;
		for (Account account : view) {
			if (index++ < scroll) {
				continue;
			}
			if (y > height - 70) {
				break;
			}
			boolean hover = mouseX >= 24 && mouseX <= width - 24 && mouseY >= y && mouseY <= y + 46;
			GlassRenderer.panel(context, 24, y, width - 48, 46, hover ? ColorUtil.withAlpha(ThemeManager.accent(), 40) : ThemeManager.panel(), ThemeManager.glassBorder(), hover ? 0.7f : 0.2f);
			FontManager.get().draw(context, account.username, 40, y + 10, ThemeManager.text());
			FontManager.get().draw(context, account.uuid, 40, y + 24, ThemeManager.muted());
			int badgeColor = account.isPremium() ? ThemeManager.success() : ThemeManager.accent();
			GlassRenderer.badge(context, width - 210, y + 14, 70, 16, badgeColor);
			FontManager.get().drawCentered(context, account.badge(), width - 175, y + 18, badgeColor);
			boolean selHover = mouseX >= width - 130 && mouseX <= width - 70 && mouseY >= y + 12 && mouseY <= y + 32;
			boolean delHover = mouseX >= width - 64 && mouseX <= width - 32 && mouseY >= y + 12 && mouseY <= y + 32;
			FontManager.get().draw(context, "USE", width - 120, y + 18, selHover ? ThemeManager.accent() : ThemeManager.text());
			FontManager.get().draw(context, "DEL", width - 58, y + 18, delHover ? ThemeManager.danger() : ThemeManager.muted());
			y += 52;
		}
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
		int y = contentTop() + 56;
		int index = 0;
		for (Account account : view) {
			if (index++ < scroll) {
				continue;
			}
			if (y > height - 70) {
				break;
			}
			if (click.y() >= y && click.y() <= y + 46) {
				if (click.x() >= width - 64) {
					AccountManager.get().remove(account);
					clearAndInit();
					return true;
				}
				if (click.x() >= width - 130) {
					AccountManager.get().select(account, true);
					return true;
				}
			}
			y += 52;
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		scroll = Math.max(0, scroll - (int) Math.signum(vertical));
		return true;
	}
}
