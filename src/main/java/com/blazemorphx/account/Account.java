package com.blazemorphx.account;

import java.util.UUID;

public final class Account {
	public enum Type {
		PREMIUM,
		OFFLINE
	}

	public String id = UUID.randomUUID().toString();
	public String username = "Player";
	public String uuid = UUID.randomUUID().toString();
	public Type type = Type.OFFLINE;
	public String encryptedToken = "";
	public String encryptedRefresh = "";
	public String authJson = "";
	public long lastUsed;

	public boolean isPremium() {
		return type == Type.PREMIUM;
	}

	public String badge() {
		return isPremium() ? "PREMIUM" : "OFFLINE";
	}
}
