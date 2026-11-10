package com.blazemorphx.customization;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.util.ColorUtil;

public final class ThemeManager {
	public enum ThemeId {
		BLAZE_ORANGE("blaze_orange", "Blaze Orange", 0xFFFF6A00, 0xFFFF2E00, 0xFFFFC14A),
		BLOOD_RED("blood_red", "Blood Red", 0xFFFF2A2A, 0xFFB00020, 0xFFFF8A80),
		PURPLE_FLAME("purple_flame", "Purple Flame", 0xFFB14BFF, 0xFF6B21FF, 0xFFE0A6FF),
		ICE_BLUE("ice_blue", "Ice Blue", 0xFF3DC6FF, 0xFF1B6CFF, 0xFFB8ECFF),
		PURE_BLACK("pure_black", "Pure Black", 0xFFE6E6E6, 0xFF7A7A7A, 0xFFFFFFFF);

		public final String id;
		public final String displayName;
		public final int accent;
		public final int accentDark;
		public final int accentLight;

		ThemeId(String id, String displayName, int accent, int accentDark, int accentLight) {
			this.id = id;
			this.displayName = displayName;
			this.accent = accent;
			this.accentDark = accentDark;
			this.accentLight = accentLight;
		}

		public static ThemeId byId(String id) {
			for (ThemeId theme : values()) {
				if (theme.id.equalsIgnoreCase(id)) {
					return theme;
				}
			}
			return BLAZE_ORANGE;
		}
	}

	private static ThemeId current = ThemeId.BLAZE_ORANGE;

	private ThemeManager() {
	}

	public static void init() {
		apply(ThemeId.byId(ConfigManager.get().theme));
	}

	public static void apply(ThemeId theme) {
		current = theme;
		ConfigManager.get().theme = theme.id;
		ConfigManager.get().accentColor = theme.accent;
		ConfigManager.save();
	}

	public static ThemeId current() {
		return current;
	}

	public static int accent() {
		return ConfigManager.get().accentColor;
	}

	public static int accentGlow() {
		return ColorUtil.withAlpha(accent(), 90);
	}

	public static int panel() {
		int alpha = ColorUtil.clamp255((int) (ConfigManager.get().uiTransparency * 210));
		return ColorUtil.argb(alpha, 8, 8, 10);
	}

	public static int panelStrong() {
		int alpha = ColorUtil.clamp255((int) (ConfigManager.get().uiTransparency * 235));
		return ColorUtil.argb(alpha, 12, 12, 14);
	}

	public static int text() {
		return 0xFFF4F4F6;
	}

	public static int muted() {
		return 0xFFB7B7BE;
	}

	public static int danger() {
		return 0xFFFF4D4D;
	}

	public static int success() {
		return 0xFF5CFFA3;
	}

	public static int glassHighlight() {
		return 0x33FFFFFF;
	}

	public static int glassBorder() {
		return ColorUtil.withAlpha(accent(), 160);
	}
}
