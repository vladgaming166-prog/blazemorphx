package com.blazemorphx.gui;

import com.blazemorphx.account.AccountManager;
import com.blazemorphx.account.MicrosoftAuth;
import com.blazemorphx.account.OfflineAccount;
import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import com.blazemorphx.gui.components.CustomButton;
import com.blazemorphx.gui.components.CustomTextField;
import com.blazemorphx.render.GlassRenderer;
import com.blazemorphx.render.NotificationRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;

import java.net.URI;

public class AddAccountScreen extends BlazeScreen {
	private CustomTextField offlineName;
	private String deviceStatus = "Microsoft login never asks for your password inside Blaze Morphx.";
	private String deviceUrl = "";
	private String deviceCode = "";
	private boolean authenticating;

	public AddAccountScreen(Screen parent) {
		super("Add Account", parent);
	}

	@Override
	protected void init() {
		int cx = width / 2;
		addDrawableChild(new CustomButton(cx - 220, contentTop() + 20, 200, 54, "ADD PREMIUM", this::startMicrosoft));
		addDrawableChild(new CustomButton(cx + 20, contentTop() + 20, 200, 54, "ADD CRACKED (OFFLINE)", this::addOffline));
		offlineName = new CustomTextField(cx + 20, contentTop() + 84, 200, 22, "", 16);
		offlineName.setMessage(net.minecraft.text.Text.literal("Offline username"));
		addDrawableChild(offlineName);
		addDrawableChild(new CustomButton(24, height - 36, 90, 22, "Back", this::close));
	}

	private void startMicrosoft() {
		if (authenticating) {
			return;
		}
		authenticating = true;
		deviceStatus = "Waiting for Microsoft device login...";
		MicrosoftAuth.loginDeviceCode(code -> {
			deviceUrl = code.getDirectVerificationUri() != null ? code.getDirectVerificationUri() : code.getVerificationUri();
			deviceCode = code.getUserCode();
			deviceStatus = "Open the link and enter " + deviceCode;
			try {
				Util.getOperatingSystem().open(URI.create(deviceUrl));
			} catch (Exception ignored) {
			}
		}).whenComplete((account, error) -> client.execute(() -> {
			authenticating = false;
			if (error != null) {
				deviceStatus = error.getCause() == null ? error.getMessage() : error.getCause().getMessage();
				NotificationRenderer.get().error("Premium login failed");
				return;
			}
			AccountManager.get().add(account);
			NotificationRenderer.get().push("Premium account added", account.username);
			client.setScreen(new AccountManagerScreen(parent));
		}));
	}

	private void addOffline() {
		String name = offlineName.value().trim();
		if (!OfflineAccount.isValidUsername(name)) {
			NotificationRenderer.get().error("Username must be 3-16 letters, numbers or _");
			return;
		}
		AccountManager.get().add(OfflineAccount.create(name));
		client.setScreen(new AccountManagerScreen(parent));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		int cx = width / 2;
		GlassRenderer.panel(context, cx - 220, contentTop() + 20, 200, 54);
		GlassRenderer.panel(context, cx + 20, contentTop() + 20, 200, 54);
		FontManager.get().drawCentered(context, "Microsoft OAuth · Device code", cx - 120, contentTop() + 88, ThemeManager.muted());
		GlassRenderer.panel(context, 24, contentTop() + 120, width - 48, 80);
		FontManager.get().draw(context, deviceStatus, 36, contentTop() + 134, ThemeManager.text());
		if (!deviceCode.isEmpty()) {
			FontManager.get().draw(context, "Code: " + deviceCode, 36, contentTop() + 150, ThemeManager.accent());
			FontManager.get().draw(context, deviceUrl, 36, contentTop() + 166, ThemeManager.muted());
		}
		FontManager.get().draw(context, "Offline accounts only work on cracked / offline-mode servers.", 36, contentTop() + 210, ThemeManager.muted());
	}
}
