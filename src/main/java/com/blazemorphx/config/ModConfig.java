package com.blazemorphx.config;

import com.blazemorphx.customization.ThemeManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModConfig {
	public boolean customMainMenu = true;
	public boolean animatedBackground = true;
	public boolean menuAnimations = true;
	public boolean menuMusic = false;
	public boolean soundEffects = true;
	public boolean showLogo = true;
	public boolean customCrosshair = true;
	public boolean customHud = true;
	public boolean applyFontToBlazeUi = true;

	public float uiScale = 1.0f;
	public float uiTransparency = 0.82f;
	public float roundedCorners = 6f;
	public String theme = ThemeManager.ThemeId.BLAZE_ORANGE.id;
	public int accentColor = 0xFFFF6A00;
	public String buttonStyle = "glass";
	public String menuLayout = "centered";
	public String language = "en_us";

	public String fontName = "minecraft";
	public float fontSize = 9f;
	public boolean fontBold = false;
	public boolean fontItalic = false;
	public String customFontPath = "";

	public String backgroundPath = "default";
	public String backgroundType = "video";
	public String backgroundScale = "fill";
	public float backgroundBrightness = 0.78f;
	public float backgroundBlur = 0.15f;
	public float backgroundSaturation = 1.05f;
	public float overlayOpacity = 0.42f;
	public int videoFpsLimit = 24;
	public int videoDecodeWidth = 960;
	public boolean pauseVideoWhenHidden = true;

	public boolean crosshairEnabled = true;
	public int crosshairColor = 0xFFFF6A00;
	public float crosshairSize = 8f;
	public float crosshairThickness = 2f;
	public float crosshairGap = 3f;
	public boolean crosshairOutline = true;
	public float crosshairOutlineThickness = 1f;
	public boolean crosshairDynamic = true;
	public boolean crosshairDot = true;
	public boolean crosshairTShape = false;
	public String crosshairPreset = "blaze";

	public boolean hudFps = true;
	public boolean hudCps = true;
	public boolean hudCoords = true;
	public boolean hudKeystrokes = true;
	public boolean hudSprint = true;
	public boolean hudPing = true;
	public boolean hudArmor = true;
	public boolean hudPotions = true;
	public boolean hudDirection = true;
	public boolean hudStopwatch = false;

	public Map<String, HudWidgetConfig> hudWidgets = defaultHudWidgets();

	public boolean reduceParticles = false;
	public int particleLimit = 400;
	public boolean skipVignette = false;
	public boolean skipWeather = false;
	public boolean skipEntityShadows = false;
	public boolean reduceMenuAnimations = false;
	public boolean backgroundFpsLimitEnabled = true;
	public int backgroundFpsLimit = 30;
	public boolean reduceUiRendering = false;

	public String lastAccountId = "";
	public String activeProfile = "default";
	public List<String> profiles = new ArrayList<>(List.of("default"));

	public static final class HudWidgetConfig {
		public boolean enabled = true;
		public float x = 0.02f;
		public float y = 0.05f;
		public float scale = 1.0f;
		public float opacity = 1.0f;
		public int color = 0xFFFFFFFF;

		public HudWidgetConfig() {
		}

		public HudWidgetConfig(boolean enabled, float x, float y, float scale, int color) {
			this.enabled = enabled;
			this.x = x;
			this.y = y;
			this.scale = scale;
			this.color = color;
		}
	}

	private static Map<String, HudWidgetConfig> defaultHudWidgets() {
		Map<String, HudWidgetConfig> map = new LinkedHashMap<>();
		map.put("fps", new HudWidgetConfig(true, 0.012f, 0.018f, 1f, 0xFFFF9A3C));
		map.put("cps", new HudWidgetConfig(true, 0.012f, 0.048f, 1f, 0xFFFF6A00));
		map.put("coords", new HudWidgetConfig(true, 0.012f, 0.078f, 1f, 0xFFE8E8E8));
		map.put("ping", new HudWidgetConfig(true, 0.012f, 0.108f, 1f, 0xFF7CFFB2));
		map.put("sprint", new HudWidgetConfig(true, 0.012f, 0.138f, 1f, 0xFFFFC14A));
		map.put("direction", new HudWidgetConfig(true, 0.012f, 0.168f, 1f, 0xFF9AD7FF));
		map.put("stopwatch", new HudWidgetConfig(false, 0.012f, 0.198f, 1f, 0xFFFFE08A));
		map.put("keystrokes", new HudWidgetConfig(true, 0.012f, 0.72f, 1f, 0xFFFFFFFF));
		map.put("armor", new HudWidgetConfig(true, 0.86f, 0.62f, 1f, 0xFFFFFFFF));
		map.put("potions", new HudWidgetConfig(true, 0.78f, 0.18f, 1f, 0xFFFFFFFF));
		return map;
	}
}
