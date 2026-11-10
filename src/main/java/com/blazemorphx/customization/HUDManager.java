package com.blazemorphx.customization;

import com.blazemorphx.config.ConfigManager;
import com.blazemorphx.config.ModConfig;
import com.blazemorphx.input.ClickTracker;
import com.blazemorphx.optimization.FPSMonitor;
import com.blazemorphx.render.GlassRenderer;
import com.blazemorphx.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.Locale;

public final class HUDManager {
	private static final HUDManager INSTANCE = new HUDManager();
	private long stopwatchStart = -1;
	private long stopwatchAccumulated;
	private boolean stopwatchRunning;
	private String dragging;

	private HUDManager() {
	}

	public static HUDManager get() {
		return INSTANCE;
	}

	public void tick(MinecraftClient client) {
	}

	public void toggleStopwatch() {
		if (stopwatchRunning) {
			stopwatchAccumulated += System.currentTimeMillis() - stopwatchStart;
			stopwatchRunning = false;
			stopwatchStart = -1;
		} else {
			stopwatchStart = System.currentTimeMillis();
			stopwatchRunning = true;
		}
	}

	public void resetStopwatch() {
		stopwatchStart = stopwatchRunning ? System.currentTimeMillis() : -1;
		stopwatchAccumulated = 0;
	}

	public void render(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null || client.currentScreen != null || client.options.hudHidden) {
			return;
		}
		if (!ConfigManager.get().customHud) {
			return;
		}
		int sw = client.getWindow().getScaledWidth();
		int sh = client.getWindow().getScaledHeight();
		drawTextWidget(context, "fps", sw, sh, "FPS " + FPSMonitor.get().current());
		drawTextWidget(context, "cps", sw, sh, "CPS " + String.format(Locale.ROOT, "%.1f", ClickTracker.cps()));
		BlockPos pos = client.player.getBlockPos();
		drawTextWidget(context, "coords", sw, sh, pos.getX() + " " + pos.getY() + " " + pos.getZ());
		drawTextWidget(context, "ping", sw, sh, "PING " + ping(client) + "ms");
		drawTextWidget(context, "sprint", sw, sh, client.player.isSprinting() ? "SPRINT" : "WALK");
		drawTextWidget(context, "direction", sw, sh, direction(client.player.getYaw()));
		drawTextWidget(context, "stopwatch", sw, sh, stopwatchText());
		drawKeystrokes(context, sw, sh, client);
		drawArmor(context, sw, sh, client);
		drawPotions(context, sw, sh, client);
	}

	public void renderEditorPreview(DrawContext context, int sw, int sh) {
		MinecraftClient client = MinecraftClient.getInstance();
		drawTextWidget(context, "fps", sw, sh, "FPS 240");
		drawTextWidget(context, "cps", sw, sh, "CPS 12.0");
		drawTextWidget(context, "coords", sw, sh, "0 64 0");
		drawTextWidget(context, "ping", sw, sh, "PING 24ms");
		drawTextWidget(context, "sprint", sw, sh, "SPRINT");
		drawTextWidget(context, "direction", sw, sh, "NORTH");
		drawTextWidget(context, "stopwatch", sw, sh, "00:12.4");
		drawKeystrokes(context, sw, sh, client);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int sw, int sh) {
		dragging = hit(mouseX, mouseY, sw, sh);
		return dragging != null;
	}

	public void mouseDragged(double mouseX, double mouseY, int sw, int sh) {
		if (dragging == null) {
			return;
		}
		ModConfig.HudWidgetConfig widget = ConfigManager.get().hudWidgets.get(dragging);
		if (widget == null) {
			return;
		}
		widget.x = ColorUtil.clamp01((float) (mouseX / sw));
		widget.y = ColorUtil.clamp01((float) (mouseY / sh));
	}

	public void mouseReleased() {
		if (dragging != null) {
			ConfigManager.save();
			dragging = null;
		}
	}

	private void drawTextWidget(DrawContext context, String id, int sw, int sh, String text) {
		ModConfig config = ConfigManager.get();
		ModConfig.HudWidgetConfig widget = config.hudWidgets.get(id);
		boolean enabled = switch (id) {
			case "fps" -> config.hudFps;
			case "cps" -> config.hudCps;
			case "coords" -> config.hudCoords;
			case "ping" -> config.hudPing;
			case "sprint" -> config.hudSprint;
			case "direction" -> config.hudDirection;
			case "stopwatch" -> config.hudStopwatch;
			default -> true;
		};
		if (widget == null || !widget.enabled || !enabled) {
			return;
		}
		int x = Math.round(widget.x * sw);
		int y = Math.round(widget.y * sh);
		int color = ColorUtil.withAlpha(widget.color, ColorUtil.clamp255((int) (widget.opacity * 255)));
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(x, y);
		context.getMatrices().scale(widget.scale, widget.scale);
		GlassRenderer.rounded(context, -3, -3, FontManager.get().width(text) + 8, 14, 3, ColorUtil.withAlpha(0xFF050505, 120));
		FontManager.get().draw(context, text, 0, 0, color);
		context.getMatrices().popMatrix();
	}

	private void drawKeystrokes(DrawContext context, int sw, int sh, MinecraftClient client) {
		ModConfig config = ConfigManager.get();
		if (!config.hudKeystrokes) {
			return;
		}
		ModConfig.HudWidgetConfig widget = config.hudWidgets.get("keystrokes");
		if (widget == null || !widget.enabled) {
			return;
		}
		int x = Math.round(widget.x * sw);
		int y = Math.round(widget.y * sh);
		key(context, x + 22, y, "W", client.options.forwardKey.isPressed(), widget);
		key(context, x, y + 22, "A", client.options.leftKey.isPressed(), widget);
		key(context, x + 22, y + 22, "S", client.options.backKey.isPressed(), widget);
		key(context, x + 44, y + 22, "D", client.options.rightKey.isPressed(), widget);
		key(context, x, y + 44, "LMB", ClickTracker.leftHeld(), widget, 32);
		key(context, x + 34, y + 44, "RMB", ClickTracker.rightHeld(), widget, 32);
		key(context, x, y + 66, "SPACE", client.options.jumpKey.isPressed(), widget, 66);
	}

	private void key(DrawContext context, int x, int y, String label, boolean pressed, ModConfig.HudWidgetConfig widget) {
		key(context, x, y, label, pressed, widget, 20);
	}

	private void key(DrawContext context, int x, int y, String label, boolean pressed, ModConfig.HudWidgetConfig widget, int w) {
		int fill = pressed ? ColorUtil.withAlpha(widget.color, 160) : ColorUtil.withAlpha(0xFF101010, 160);
		GlassRenderer.rounded(context, x, y, w, 18, 3, fill);
		FontManager.get().drawCentered(context, label, x + w / 2, y + 5, 0xFFFFFFFF);
	}

	private void drawArmor(DrawContext context, int sw, int sh, MinecraftClient client) {
		ModConfig config = ConfigManager.get();
		if (!config.hudArmor) {
			return;
		}
		ModConfig.HudWidgetConfig widget = config.hudWidgets.get("armor");
		if (widget == null || !widget.enabled || client.player == null) {
			return;
		}
		int x = Math.round(widget.x * sw);
		int y = Math.round(widget.y * sh);
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		for (int i = 0; i < slots.length; i++) {
			ItemStack stack = client.player.getEquippedStack(slots[i]);
			if (stack.isEmpty()) {
				continue;
			}
			context.drawItem(stack, x, y + i * 18);
			if (stack.isDamageable()) {
				int max = stack.getMaxDamage();
				int remain = max - stack.getDamage();
				FontManager.get().draw(context, remain + "/" + max, x + 18, y + i * 18 + 4, widget.color);
			}
		}
	}

	private void drawPotions(DrawContext context, int sw, int sh, MinecraftClient client) {
		ModConfig config = ConfigManager.get();
		if (!config.hudPotions || client.player == null) {
			return;
		}
		ModConfig.HudWidgetConfig widget = config.hudWidgets.get("potions");
		if (widget == null || !widget.enabled) {
			return;
		}
		int x = Math.round(widget.x * sw);
		int y = Math.round(widget.y * sh);
		Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
		int i = 0;
		for (StatusEffectInstance effect : effects) {
			String name = Registries.STATUS_EFFECT.getId(effect.getEffectType().value()).getPath();
			int seconds = effect.getDuration() / 20;
			FontManager.get().draw(context, name + " " + seconds + "s", x, y + i * 12, widget.color);
			i++;
		}
	}

	private String hit(double mouseX, double mouseY, int sw, int sh) {
		for (var entry : ConfigManager.get().hudWidgets.entrySet()) {
			ModConfig.HudWidgetConfig widget = entry.getValue();
			int x = Math.round(widget.x * sw);
			int y = Math.round(widget.y * sh);
			if (mouseX >= x - 4 && mouseX <= x + 90 && mouseY >= y - 4 && mouseY <= y + 20) {
				return entry.getKey();
			}
		}
		return null;
	}

	private static int ping(MinecraftClient client) {
		ClientPlayNetworkHandler handler = client.getNetworkHandler();
		if (handler == null || client.player == null) {
			return 0;
		}
		PlayerListEntry entry = handler.getPlayerListEntry(client.player.getUuid());
		return entry == null ? 0 : entry.getLatency();
	}

	private static String direction(float yaw) {
		int index = MathHelper.floor((MathHelper.wrapDegrees(yaw) + 180f + 22.5f) / 45f) & 7;
		return switch (index) {
			case 0 -> "SOUTH";
			case 1 -> "SOUTHWEST";
			case 2 -> "WEST";
			case 3 -> "NORTHWEST";
			case 4 -> "NORTH";
			case 5 -> "NORTHEAST";
			case 6 -> "EAST";
			default -> "SOUTHEAST";
		};
	}

	private String stopwatchText() {
		long time = stopwatchAccumulated + (stopwatchRunning ? System.currentTimeMillis() - stopwatchStart : 0);
		long ms = time % 1000;
		long s = (time / 1000) % 60;
		long m = time / 60000;
		return String.format(Locale.ROOT, "%02d:%02d.%01d", m, s, ms / 100);
	}
}
