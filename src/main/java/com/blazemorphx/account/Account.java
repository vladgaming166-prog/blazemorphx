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
	public String xuid = "";
	public String clientId = "";
	public boolean launcher = false;
	public long lastUsed;

	public boolean isPremium() {
		return type == Type.PREMIUM;
	}

	public String badge() {
		if (launcher) {
			return isPremium() ? "LAUNCHER" : "LAUNCHER / OFFLINE";
		}
		return isPremium() ? "PREMIUM" : "OFFLINE";
	}
}
