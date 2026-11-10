package com.blazemorphx.account;

import com.blazemorphx.BlazeMorphx;
import com.google.gson.JsonObject;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.java.JavaAuthManager;
import net.raphimc.minecraftauth.msa.model.MsaDeviceCode;
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class MicrosoftAuth {
	private MicrosoftAuth() {
	}

	public static CompletableFuture<Account> loginDeviceCode(Consumer<MsaDeviceCode> onCode) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				HttpClient httpClient = MinecraftAuth.createHttpClient("BlazeMorphx/1.0");
				JavaAuthManager authManager = JavaAuthManager.create(httpClient)
					.login(DeviceCodeMsaAuthService::new, onCode);
				var profile = authManager.getMinecraftProfile().getUpToDate();
				var token = authManager.getMinecraftToken().getUpToDate();
				Account account = new Account();
				account.username = profile.getName();
				account.uuid = profile.getId() == null ? UUID.randomUUID().toString() : profile.getId().toString();
				account.type = Account.Type.PREMIUM;
				account.lastUsed = System.currentTimeMillis();
				JsonObject json = JavaAuthManager.toJson(authManager);
				account.authJson = json.toString();
				account.encryptedToken = token.getToken();
				return account;
			} catch (Exception e) {
				BlazeMorphx.LOGGER.warn("Microsoft authentication failed");
				throw new RuntimeException(userSafe(e), e);
			}
		});
	}

	public static String refreshAccessToken(Account account) {
		if (account.authJson == null || account.authJson.isBlank()) {
			return account.encryptedToken;
		}
		try {
			HttpClient httpClient = MinecraftAuth.createHttpClient("BlazeMorphx/1.0");
			JsonObject json = com.google.gson.JsonParser.parseString(account.authJson).getAsJsonObject();
			JavaAuthManager authManager = JavaAuthManager.fromJson(httpClient, json);
			var token = authManager.getMinecraftToken().getUpToDate();
			account.authJson = JavaAuthManager.toJson(authManager).toString();
			account.encryptedToken = token.getToken();
			var profile = authManager.getMinecraftProfile().getUpToDate();
			account.username = profile.getName();
			return token.getToken();
		} catch (Exception e) {
			BlazeMorphx.LOGGER.warn("Token refresh failed for {}", account.username);
			return account.encryptedToken;
		}
	}

	private static String userSafe(Exception e) {
		String message = e.getMessage() == null ? "Authentication failed" : e.getMessage();
		if (message.toLowerCase().contains("token")) {
			return "Microsoft authentication failed. The Microsoft account may not own Minecraft Java.";
		}
		return message;
	}
}
