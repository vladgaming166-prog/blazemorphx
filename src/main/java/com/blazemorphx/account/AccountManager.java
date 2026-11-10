package com.blazemorphx.account;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.mixin.MinecraftClientAccessor;
import com.blazemorphx.render.NotificationRenderer;
import com.blazemorphx.util.CryptoUtil;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class AccountManager {
	private static final AccountManager INSTANCE = new AccountManager();
	private final List<Account> accounts = new ArrayList<>();
	private Account selected;

	private AccountManager() {
	}

	public static AccountManager get() {
		return INSTANCE;
	}

	public synchronized void load() {
		accounts.clear();
		Path file = file();
		try {
			if (Files.exists(file)) {
				String json = Files.readString(file, StandardCharsets.UTF_8);
				List<Account> loaded = ConfigManager.gson().fromJson(json, new TypeToken<List<Account>>() {
				}.getType());
				if (loaded != null) {
					for (Account account : loaded) {
						account.encryptedToken = CryptoUtil.decrypt(account.encryptedToken, ConfigManager.directory());
						account.encryptedRefresh = CryptoUtil.decrypt(account.encryptedRefresh, ConfigManager.directory());
						account.authJson = CryptoUtil.decrypt(account.authJson, ConfigManager.directory());
						accounts.add(account);
					}
				}
			}
		} catch (Exception e) {
			BlazeMorphx.LOGGER.warn("Failed to load accounts");
		}
		try {
			seedCurrentSession();
			String last = ConfigManager.get().lastAccountId;
			selected = byId(last).orElse(accounts.isEmpty() ? null : accounts.getFirst());
			if (selected != null) {
				select(selected, false);
			}
		} catch (Throwable t) {
			BlazeMorphx.LOGGER.warn("Could not seed the current Minecraft session", t);
		}
	}

	public synchronized void save() {
		try {
			List<Account> stored = new ArrayList<>();
			for (Account account : accounts) {
				Account copy = copyOf(account);
				copy.encryptedToken = CryptoUtil.encrypt(account.encryptedToken, ConfigManager.directory());
				copy.encryptedRefresh = CryptoUtil.encrypt(account.encryptedRefresh, ConfigManager.directory());
				copy.authJson = CryptoUtil.encrypt(account.authJson, ConfigManager.directory());
				stored.add(copy);
			}
			Files.writeString(file(), ConfigManager.gson().toJson(stored), StandardCharsets.UTF_8);
		} catch (Exception e) {
			BlazeMorphx.LOGGER.warn("Failed to save accounts");
		}
	}

	public synchronized List<Account> accounts() {
		return List.copyOf(accounts);
	}

	public Account selected() {
		return selected;
	}

	public synchronized void add(Account account) {
		accounts.removeIf(existing -> existing.username.equalsIgnoreCase(account.username) && existing.type == account.type);
		accounts.add(account);
		select(account, true);
		save();
	}

	public synchronized void remove(Account account) {
		accounts.removeIf(existing -> existing.id.equals(account.id));
		if (selected != null && selected.id.equals(account.id)) {
			selected = accounts.isEmpty() ? null : accounts.getFirst();
			if (selected != null) {
				select(selected, true);
			}
		}
		save();
	}

	public synchronized void select(Account account, boolean notify) {
		if (account == null) {
			return;
		}
		selected = account;
		account.lastUsed = System.currentTimeMillis();
		ConfigManager.get().lastAccountId = account.id;
		ConfigManager.save();
		applySession(account);
		save();
		if (notify) {
			NotificationRenderer.get().push("Account selected", account.username + " · " + account.badge());
		}
	}

	public Optional<Account> byId(String id) {
		if (id == null || id.isBlank()) {
			return Optional.empty();
		}
		return accounts.stream().filter(account -> account.id.equals(id)).findFirst();
	}

	private void seedCurrentSession() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null) {
			return;
		}
		Session session = client.getSession();
		if (session == null) {
			return;
		}
		boolean exists = accounts.stream().anyMatch(account -> account.username.equalsIgnoreCase(session.getUsername()));
		if (exists) {
			return;
		}
		Account account = new Account();
		account.username = session.getUsername();
		UUID uuid = session.getUuidOrNull();
		account.uuid = uuid == null ? UUID.randomUUID().toString() : uuid.toString();
		account.type = session.getAccessToken() != null && session.getAccessToken().length() > 8 ? Account.Type.PREMIUM : Account.Type.OFFLINE;
		account.encryptedToken = session.getAccessToken();
		accounts.add(account);
		save();
	}

	private void applySession(Account account) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null) {
			return;
		}
		try {
			String token = account.isPremium() ? MicrosoftAuth.refreshAccessToken(account) : "0";
			if (token == null || token.isBlank()) {
				token = "0";
			}
			UUID uuid = UUID.fromString(account.uuid);
			Session session = new Session(account.username, uuid, token, Optional.empty(), Optional.empty());
			((MinecraftClientAccessor) client).blaze$setSession(session);
		} catch (Exception e) {
			NotificationRenderer.get().error("Could not apply account session");
			BlazeMorphx.LOGGER.warn("Session apply failed for {}", account.username);
		}
	}

	private static Account copyOf(Account account) {
		Account copy = new Account();
		copy.id = account.id;
		copy.username = account.username;
		copy.uuid = account.uuid;
		copy.type = account.type;
		copy.encryptedToken = account.encryptedToken;
		copy.encryptedRefresh = account.encryptedRefresh;
		copy.authJson = account.authJson;
		copy.lastUsed = account.lastUsed;
		return copy;
	}

	private static Path file() {
		return ConfigManager.directory().resolve("accounts.json");
	}
}
