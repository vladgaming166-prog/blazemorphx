package com.blazemorphx.account;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public final class OfflineAccount {
	private static final Pattern NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

	private OfflineAccount() {
	}

	public static boolean isValidUsername(String username) {
		return username != null && NAME.matcher(username).matches();
	}

	public static UUID uuidFor(String username) {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
	}

	public static Account create(String username) {
		Account account = new Account();
		account.username = username;
		account.uuid = uuidFor(username).toString();
		account.type = Account.Type.OFFLINE;
		account.lastUsed = System.currentTimeMillis();
		return account;
	}
}
