package com.blazemorphx.gui;

import com.blazemorphx.account.AccountManager;
import com.blazemorphx.account.MicrosoftAuth;
import com.blazemorphx.account.OfflineAccount;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.URI;

public class AddAccountScreen extends BlazeScreen {
	private TextFieldWidget offlineName;
	private String status = "Microsoft login opens a browser. The mod never asks for your password.";
	private String deviceUrl = "";
	private String deviceCode = "";
	private boolean authenticating;

	public AddAccountScreen(Screen parent) {
		super("Add Account", parent);
	}

	@Override
	protected void init() {
		int cx = width / 2;
		addDrawableChild(button(cx - 205, contentTop() + 8, 200, 20, "Microsoft Premium", this::startMicrosoft));
		addDrawableChild(button(cx + 5, contentTop() + 8, 200, 20, "Create Offline Profile", this::addOffline));
		offlineName = new TextFieldWidget(textRenderer, cx + 5, contentTop() + 36, 200, 20, Text.literal("Offline username"));
		offlineName.setMaxLength(16);
		addSelectableChild(offlineName);
		addDrawableChild(offlineName);
		addDrawableChild(button(cx - 100, height - 28, 200, 20, "Done", this::close));
	}

	private void startMicrosoft() {
		if (authenticating) {
			return;
		}
		authenticating = true;
		status = "Waiting for Microsoft device login...";
		MicrosoftAuth.loginDeviceCode(code -> {
			deviceUrl = code.getDirectVerificationUri() != null ? code.getDirectVerificationUri() : code.getVerificationUri();
			deviceCode = code.getUserCode();
			status = "Open the Microsoft page and enter " + deviceCode;
			try {
				Util.getOperatingSystem().open(URI.create(deviceUrl));
			} catch (Exception ignored) {
			}
		}).whenComplete((account, error) -> client.execute(() -> {
			authenticating = false;
			if (error != null) {
				status = error.getCause() == null ? error.getMessage() : error.getCause().getMessage();
				return;
			}
			AccountManager.get().add(account);
			status = "Added premium account " + account.username + ". Click Use on the account list to switch.";
			client.setScreen(new AccountManagerScreen(parent));
		}));
	}

	private void addOffline() {
		String name = offlineName.getText().trim();
		if (!OfflineAccount.isValidUsername(name)) {
			status = "Username must be 3-16 letters, numbers or underscore.";
			return;
		}
		AccountManager.get().add(OfflineAccount.create(name));
		client.setScreen(new AccountManagerScreen(parent));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, contentTop() + 68, 0xFFFFFF);
		if (!deviceCode.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer, Text.literal("Code: " + deviceCode), width / 2, contentTop() + 84, 0xFFFFFF00);
			context.drawCenteredTextWithShadow(textRenderer, Text.literal(deviceUrl), width / 2, contentTop() + 100, 0xFFA0A0A0);
		}
		context.drawCenteredTextWithShadow(textRenderer, Text.literal("Offline profiles do not own Minecraft and cannot join online-mode servers."), width / 2, contentTop() + 128, 0xFFA0A0A0);
	}

}
