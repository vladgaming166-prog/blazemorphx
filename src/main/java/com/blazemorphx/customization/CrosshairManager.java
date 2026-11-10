package com.blazemorphx.customization;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CrosshairManager {
	private static final CrosshairManager INSTANCE = new CrosshairManager();

	public static final Map<String, Preset> PRESETS = presets();

	private CrosshairManager() {
	}

	public static CrosshairManager get() {
		return INSTANCE;
	}

	public boolean shouldReplaceVanilla() {
		ModConfig config = ConfigManager.get();
		MinecraftClient client = MinecraftClient.getInstance();
		return config.customCrosshair && config.crosshairEnabled && client.world != null && client.currentScreen == null && !client.options.hudHidden;
	}

	public void render(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		ModConfig config = ConfigManager.get();
		int cx = context.getScaledWindowWidth() / 2;
		int cy = context.getScaledWindowHeight() / 2;
		float gap = config.crosshairGap;
		if (config.crosshairDynamic && client.player != null) {
			gap += client.player.getAttackCooldownProgress(0f) < 1f ? 4f : 0f;
			HitResult hit = client.crosshairTarget;
			if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity) {
				gap += 2f;
			}
		}
		int color = config.crosshairColor;
		int outline = 0xFF000000;
		float size = config.crosshairSize;
		float thick = Math.max(1f, config.crosshairThickness);
		float outlineT = config.crosshairOutline ? Math.max(1f, config.crosshairOutlineThickness) : 0f;
		drawBar(context, cx - size - gap, cy - thick / 2f, size, thick, color, outline, outlineT);
		if (!config.crosshairTShape) {
			drawBar(context, cx + gap, cy - thick / 2f, size, thick, color, outline, outlineT);
		}
		drawBar(context, cx - thick / 2f, cy - size - gap, thick, size, color, outline, outlineT);
		drawBar(context, cx - thick / 2f, cy + gap, thick, size, color, outline, outlineT);
		if (config.crosshairDot) {
			drawBar(context, cx - 1, cy - 1, 2, 2, color, outline, outlineT);
		}
	}

	public void applyPreset(String id) {
		Preset preset = PRESETS.getOrDefault(id, PRESETS.get("blaze"));
		ModConfig config = ConfigManager.get();
		config.crosshairPreset = preset.id;
		config.crosshairColor = preset.color;
		config.crosshairSize = preset.size;
		config.crosshairThickness = preset.thickness;
		config.crosshairGap = preset.gap;
		config.crosshairOutline = preset.outline;
		config.crosshairDot = preset.dot;
		config.crosshairTShape = preset.tShape;
		ConfigManager.save();
	}

	public void renderPreview(DrawContext context, int cx, int cy) {
		ModConfig config = ConfigManager.get();
		float size = config.crosshairSize;
		float thick = Math.max(1f, config.crosshairThickness);
		float gap = config.crosshairGap;
		drawBar(context, cx - size - gap, cy - thick / 2f, size, thick, config.crosshairColor, 0xFF000000, config.crosshairOutline ? 1 : 0);
		if (!config.crosshairTShape) {
			drawBar(context, cx + gap, cy - thick / 2f, size, thick, config.crosshairColor, 0xFF000000, config.crosshairOutline ? 1 : 0);
		}
		drawBar(context, cx - thick / 2f, cy - size - gap, thick, size, config.crosshairColor, 0xFF000000, config.crosshairOutline ? 1 : 0);
		drawBar(context, cx - thick / 2f, cy + gap, thick, size, config.crosshairColor, 0xFF000000, config.crosshairOutline ? 1 : 0);
		if (config.crosshairDot) {
			drawBar(context, cx - 1, cy - 1, 2, 2, config.crosshairColor, 0xFF000000, 1);
		}
	}

	private static void drawBar(DrawContext context, float x, float y, float w, float h, int color, int outline, float outlineT) {
		int ix = Math.round(x);
		int iy = Math.round(y);
		int iw = Math.max(1, Math.round(w));
		int ih = Math.max(1, Math.round(h));
		if (outlineT > 0) {
			int o = Math.round(outlineT);
			context.fill(ix - o, iy - o, ix + iw + o, iy + ih + o, outline);
		}
		context.fill(ix, iy, ix + iw, iy + ih, color);
	}

	private static Map<String, Preset> presets() {
		Map<String, Preset> map = new LinkedHashMap<>();
		map.put("blaze", new Preset("blaze", "Blaze", 0xFFFF6A00, 8, 2, 3, true, true, false));
		map.put("dot", new Preset("dot", "Dot", 0xFFFFFFFF, 2, 2, 0, true, true, false));
		map.put("classic", new Preset("classic", "Classic", 0xFFFFFFFF, 6, 2, 2, true, false, false));
		map.put("t", new Preset("t", "T-Shape", 0xFFFF4D4D, 8, 2, 2, true, false, true));
		map.put("gap", new Preset("gap", "Wide Gap", 0xFF3DC6FF, 7, 2, 6, true, true, false));
		return map;
	}

	public record Preset(String id, String name, int color, float size, float thickness, float gap, boolean outline, boolean dot, boolean tShape) {
	}
}
