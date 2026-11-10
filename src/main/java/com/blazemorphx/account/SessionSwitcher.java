package com.blazemorphx.account;

import com.blazemorphx.BlazeMorphx;
import com.blazemorphx.mixin.MinecraftClientAccessor;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;

import java.net.Proxy;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Replaces the live Minecraft session and every service that was created from it.
 * Writing only {@link Session} leaves {@link UserApiService} and {@link ProfileKeys}
 * bound to the launcher account, which is what produced the invalid-session error.
 */
public final class SessionSwitcher {
	public record Result(boolean success, String message, boolean reconnectRecommended) {
		public static Result ok(String message, boolean reconnectRecommended) {
			return new Result(true, message, reconnectRecommended);
		}

		public static Result fail(String message) {
			return new Result(false, message, false);
		}
	}

	private record Snapshot(
		Session session,
		UserApiService userApiService,
		CompletableFuture<UserApiService.UserProperties> userPropertiesFuture,
		ProfileKeys profileKeys,
		SocialInteractionsManager socialInteractionsManager,
		AbuseReportContext abuseReportContext,
		CompletableFuture<?> gameProfileFuture
	) {
	}

	private Snapshot launcher;
	private Snapshot lastGood;

	public void captureLauncher(MinecraftClient client) {
		if (launcher != null || client == null) {
			return;
		}
		launcher = capture(client);
		lastGood = launcher;
	}

	public Snapshot launcherSnapshot() {
		return launcher;
	}

	public boolean isLauncher(Account account) {
		if (launcher == null || account == null || launcher.session() == null) {
			return false;
		}
		UUID live = launcher.session().getUuidOrNull();
		return live != null && live.toString().equalsIgnoreCase(account.uuid);
	}

	public Result apply(MinecraftClient client, Account account) {
		if (client == null || account == null) {
			return Result.fail("No Minecraft client or account is available.");
		}
		captureLauncher(client);
		Snapshot previous = capture(client);
		try {
			Session session = createSession(account);
			UserApiService userApi = createUserApiService(account, session);
			ProfileKeys keys = account.isPremium()
				? ProfileKeys.create(userApi, session, client.runDirectory.toPath())
				: ProfileKeys.MISSING;
			SocialInteractionsManager social = new SocialInteractionsManager(client, userApi);
			AbuseReportContext abuse = AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), userApi);
			CompletableFuture<UserApiService.UserProperties> properties = CompletableFuture.supplyAsync(() -> {
				try {
					return userApi.fetchProperties();
				} catch (Exception e) {
					return UserApiService.OFFLINE_PROPERTIES;
				}
			});
			write(client, new Snapshot(session, userApi, properties, keys, social, abuse, CompletableFuture.completedFuture(null)));
			lastGood = capture(client);
			boolean inWorld = client.world != null;
			String extra = inWorld
				? " Disconnect and rejoin the server for the new account to be used in multiplayer."
				: " You can join servers without restarting Minecraft.";
			if (!account.isPremium()) {
				extra += " Offline profiles cannot join online-mode servers.";
			}
			return Result.ok("Active account is now " + account.username + "." + extra, inWorld);
		} catch (AuthenticationException e) {
			write(client, previous);
			BlazeMorphx.LOGGER.warn("Session switch rejected by Minecraft services: {}", e.toString());
			return Result.fail(authMessage(e));
		} catch (Exception e) {
			write(client, previous);
			BlazeMorphx.LOGGER.warn("Session switch failed", e);
			return Result.fail(rootMessage(e));
		}
	}

	public Result restoreLauncher(MinecraftClient client) {
		if (launcher == null) {
			return Result.fail("The original launcher session was not captured.");
		}
		write(client, launcher);
		lastGood = launcher;
		boolean inWorld = client.world != null;
		return Result.ok("Restored the launcher account (" + launcher.session().getUsername() + ")."
			+ (inWorld ? " Reconnect to apply it on a server." : ""), inWorld);
	}

	private static Session createSession(Account account) {
		UUID uuid = parseUuid(account.uuid);
		String token = account.isPremium() ? account.encryptedToken : "0";
		if (token == null || token.isBlank()) {
			token = "0";
		}
		Optional<String> xuid = optional(account.xuid);
		Optional<String> clientId = optional(account.clientId);
		return new Session(account.username, uuid, token, xuid, clientId);
	}

	private static UserApiService createUserApiService(Account account, Session session) throws AuthenticationException {
		if (!account.isPremium()) {
			return UserApiService.OFFLINE;
		}
		if (session.getAccessToken() == null || session.getAccessToken().length() < 10) {
			throw new AuthenticationException("The Microsoft access token is missing. Sign in again with device login.");
		}
		YggdrasilAuthenticationService yas = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
		return yas.createUserApiService(session.getAccessToken());
	}

	private static Snapshot capture(MinecraftClient client) {
		MinecraftClientAccessor access = (MinecraftClientAccessor) client;
		return new Snapshot(
			access.blaze$getSession(),
			access.blaze$getUserApiService(),
			access.blaze$getUserPropertiesFuture(),
			access.blaze$getProfileKeys(),
			access.blaze$getSocialInteractionsManager(),
			access.blaze$getAbuseReportContext(),
			access.blaze$getGameProfileFuture()
		);
	}

	private static void write(MinecraftClient client, Snapshot snapshot) {
		MinecraftClientAccessor access = (MinecraftClientAccessor) client;
		access.blaze$setSession(snapshot.session());
		access.blaze$setUserApiService(snapshot.userApiService());
		access.blaze$setUserPropertiesFuture(snapshot.userPropertiesFuture());
		access.blaze$setProfileKeys(snapshot.profileKeys());
		access.blaze$setSocialInteractionsManager(snapshot.socialInteractionsManager());
		access.blaze$setAbuseReportContext(snapshot.abuseReportContext());
		access.blaze$setGameProfileFuture(snapshot.gameProfileFuture());
	}

	private static UUID parseUuid(String value) {
		try {
			return UUID.fromString(value);
		} catch (Exception e) {
			throw new IllegalArgumentException("The account UUID is not valid: " + value);
		}
	}

	private static Optional<String> optional(String value) {
		return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
	}

	private static String authMessage(AuthenticationException e) {
		String raw = rootMessage(e);
		if (raw.toLowerCase().contains("token") || raw.toLowerCase().contains("credential") || raw.toLowerCase().contains("forbidden")) {
			return "Minecraft rejected this session. The access token is invalid or expired. Sign in again with Microsoft device login. The previous account was restored.";
		}
		return "Minecraft rejected this session: " + raw + " The previous account was restored.";
	}

	private static String rootMessage(Throwable error) {
		Throwable current = error;
		while (current.getCause() != null && current.getCause() != current) {
			current = current.getCause();
		}
		String message = current.getMessage();
		return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
	}
}
