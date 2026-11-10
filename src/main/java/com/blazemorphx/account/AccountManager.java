package com.blazemorphx.account;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.config.ConfigManager;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AccountManager {
	private static final AccountManager INSTANCE = new AccountManager();
	private final List<Account> accounts = new ArrayList<>();
	private final SessionSwitcher switcher = new SessionSwitcher();
	private Account selected;
	private boolean switching;

	private AccountManager() {
	}

	public static AccountManager get() {
		return INSTANCE;
	}

	public SessionSwitcher switcher() {
		return switcher;
	}

	public boolean isSwitching() {
		return switching;
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
			BlazeMorphx.LOGGER.warn("Failed to load accounts", e);
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (client != null) {
			switcher.captureLauncher(client);
			seedCurrentSession(client);
			Session session = client.getSession();
			if (session != null) {
				selected = byUsername(session.getUsername()).orElse(accounts.isEmpty() ? null : null);
				if (selected == null) {
					selected = accounts.stream()
						.filter(account -> session.getUsername().equalsIgnoreCase(account.username))
						.findFirst()
						.orElse(accounts.isEmpty() ? null : accounts.stream().filter(a -> a.launcher).findFirst().orElse(null));
				}
			}
		}
		if (selected != null) {
			ConfigManager.get().lastAccountId = selected.id;
			ConfigManager.save();
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
			Path target = file();
			Path temp = target.resolveSibling("accounts.json.tmp");
			Files.writeString(temp, ConfigManager.gson().toJson(stored), StandardCharsets.UTF_8);
			try {
				Files.move(temp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
			} catch (Exception moveError) {
				Files.move(temp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			BlazeMorphx.LOGGER.warn("Failed to save accounts", e);
		}
	}

	public synchronized List<Account> accounts() {
		return List.copyOf(accounts);
	}

	public Account selected() {
		return selected;
	}

	public synchronized void add(Account account) {
		accounts.removeIf(existing -> !existing.launcher
			&& existing.username.equalsIgnoreCase(account.username)
			&& existing.type == account.type);
		accounts.add(account);
		save();
	}

	public synchronized void remove(Account account) {
		if (account.launcher) {
			return;
		}
		accounts.removeIf(existing -> existing.id.equals(account.id));
		if (selected != null && selected.id.equals(account.id)) {
			selected = accounts.stream().filter(a -> a.launcher).findFirst().orElse(accounts.isEmpty() ? null : accounts.getFirst());
		}
		save();
	}

	public void selectAsync(Account account, Consumer<SessionSwitcher.Result> callback) {
		if (account == null || switching) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null) {
			callback.accept(SessionSwitcher.Result.fail("Minecraft is not ready."));
			return;
		}
		switching = true;
		CompletableFuture.supplyAsync(() -> prepare(account)).whenComplete((prepared, error) -> client.execute(() -> {
			try {
				if (error != null) {
					callback.accept(SessionSwitcher.Result.fail(root(error)));
					return;
				}
				SessionSwitcher.Result result = account.launcher
					? switcher.restoreLauncher(client)
					: switcher.apply(client, prepared);
				if (result.success()) {
					selected = prepared;
					prepared.lastUsed = System.currentTimeMillis();
					ConfigManager.get().lastAccountId = prepared.id;
					ConfigManager.save();
					save();
				}
				callback.accept(result);
			} finally {
				switching = false;
			}
		}));
	}

	private Account prepare(Account account) {
		if (account.isPremium() && !account.launcher) {
			try {
				MicrosoftAuth.refreshAccessToken(account);
			} catch (Exception e) {
				throw new RuntimeException(root(e), e);
			}
		}
		return account;
	}

	public Optional<Account> byId(String id) {
		if (id == null || id.isBlank()) {
			return Optional.empty();
		}
		return accounts.stream().filter(account -> account.id.equals(id)).findFirst();
	}

	public Optional<Account> byUsername(String username) {
		if (username == null || username.isBlank()) {
			return Optional.empty();
		}
		return accounts.stream().filter(account -> account.username.equalsIgnoreCase(username)).findFirst();
	}

	private void seedCurrentSession(MinecraftClient client) {
		Session session = client.getSession();
		if (session == null) {
			return;
		}
		for (Account existing : accounts) {
			if (existing.username.equalsIgnoreCase(session.getUsername())) {
				existing.launcher = true;
				existing.encryptedToken = session.getAccessToken();
				existing.xuid = session.getXuid().orElse("");
				existing.clientId = session.getClientId().orElse("");
				UUID uuid = session.getUuidOrNull();
				if (uuid != null) {
					existing.uuid = uuid.toString();
				}
				existing.type = session.getAccessToken() != null && session.getAccessToken().length() > 8 ? Account.Type.PREMIUM : Account.Type.OFFLINE;
				return;
			}
		}
		Account account = new Account();
		account.username = session.getUsername();
		UUID uuid = session.getUuidOrNull();
		account.uuid = uuid == null ? UUID.randomUUID().toString() : uuid.toString();
		account.type = session.getAccessToken() != null && session.getAccessToken().length() > 8 ? Account.Type.PREMIUM : Account.Type.OFFLINE;
		account.encryptedToken = session.getAccessToken();
		account.xuid = session.getXuid().orElse("");
		account.clientId = session.getClientId().orElse("");
		account.launcher = true;
		accounts.add(0, account);
		save();
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
		copy.xuid = account.xuid;
		copy.clientId = account.clientId;
		copy.launcher = account.launcher;
		copy.lastUsed = account.lastUsed;
		return copy;
	}

	private static String root(Throwable error) {
		Throwable current = error;
		while (current.getCause() != null && current.getCause() != current) {
			current = current.getCause();
		}
		return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
	}

	private static Path file() {
		return ConfigManager.directory().resolve("accounts.json");
	}
}
