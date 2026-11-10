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
				HttpClient httpClient = MinecraftAuth.createHttpClient("BlazeMorphx/1.1");
				JavaAuthManager authManager = JavaAuthManager.create(httpClient)
					.login(DeviceCodeMsaAuthService::new, onCode);
				var profile = authManager.getMinecraftProfile().getUpToDate();
				var token = authManager.getMinecraftToken().getUpToDate();
				if (profile == null || profile.getName() == null || token == null || token.getToken() == null) {
					throw new IllegalStateException("Microsoft login succeeded but Minecraft Java profile data was missing. This Microsoft account may not own Java Edition.");
				}
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
				BlazeMorphx.LOGGER.warn("Microsoft authentication failed: {}", e.toString());
				throw new RuntimeException(userSafe(e), e);
			}
		});
	}

	public static String refreshAccessToken(Account account) throws Exception {
		if (account.authJson == null || account.authJson.isBlank()) {
			if (account.encryptedToken != null && account.encryptedToken.length() > 10) {
				return account.encryptedToken;
			}
			throw new IllegalStateException("This Microsoft account has no refresh data. Sign in again with device login.");
		}
		HttpClient httpClient = MinecraftAuth.createHttpClient("BlazeMorphx/1.1");
		JsonObject json = com.google.gson.JsonParser.parseString(account.authJson).getAsJsonObject();
		JavaAuthManager authManager = JavaAuthManager.fromJson(httpClient, json);
		var token = authManager.getMinecraftToken().getUpToDate();
		if (token == null || token.getToken() == null || token.getToken().isBlank()) {
			throw new IllegalStateException("Minecraft services did not return a fresh access token.");
		}
		account.authJson = JavaAuthManager.toJson(authManager).toString();
		account.encryptedToken = token.getToken();
		var profile = authManager.getMinecraftProfile().getUpToDate();
		if (profile != null && profile.getName() != null) {
			account.username = profile.getName();
			if (profile.getId() != null) {
				account.uuid = profile.getId().toString();
			}
		}
		return token.getToken();
	}

	private static String userSafe(Exception e) {
		Throwable current = e;
		while (current.getCause() != null && current.getCause() != current) {
			current = current.getCause();
		}
		String message = current.getMessage() == null ? "Microsoft authentication failed" : current.getMessage();
		String lower = message.toLowerCase();
		if (lower.contains("token") || lower.contains("xbox") || lower.contains("xsts") || lower.contains("forbidden")) {
			return "Microsoft authentication failed. The Microsoft account may not own Minecraft Java Edition.";
		}
		return message;
	}
}
