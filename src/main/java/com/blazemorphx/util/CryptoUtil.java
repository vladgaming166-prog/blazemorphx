package com.blazemorphx.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CryptoUtil {
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final String PREFIX = "BMX1:";

	private CryptoUtil() {
	}

	public static String encrypt(String plaintext, Path configDir) {
		if (plaintext == null || plaintext.isEmpty()) {
			return "";
		}
		try {
			byte[] iv = new byte[12];
			RANDOM.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key(configDir), new GCMParameterSpec(128, iv));
			byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
			byte[] packed = new byte[iv.length + cipherText.length];
			System.arraycopy(iv, 0, packed, 0, iv.length);
			System.arraycopy(cipherText, 0, packed, iv.length, cipherText.length);
			return PREFIX + Base64.getEncoder().encodeToString(packed);
		} catch (Exception e) {
			return PREFIX + Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8));
		}
	}

	public static String decrypt(String stored, Path configDir) {
		if (stored == null || stored.isEmpty()) {
			return "";
		}
		if (!stored.startsWith(PREFIX)) {
			return stored;
		}
		try {
			byte[] packed = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
			if (packed.length < 13) {
				return "";
			}
			byte[] iv = new byte[12];
			byte[] cipherText = new byte[packed.length - 12];
			System.arraycopy(packed, 0, iv, 0, 12);
			System.arraycopy(packed, 12, cipherText, 0, cipherText.length);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key(configDir), new GCMParameterSpec(128, iv));
			return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
		} catch (Exception e) {
			return "";
		}
	}

	private static SecretKey key(Path configDir) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update("blaze-morphx-token-v1".getBytes(StandardCharsets.UTF_8));
			digest.update(System.getProperty("user.name", "player").getBytes(StandardCharsets.UTF_8));
			digest.update(configDir.toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8));
			return new SecretKeySpec(digest.digest(), "AES");
		} catch (Exception e) {
			return new SecretKeySpec(new byte[16], "AES");
		}
	}
}
