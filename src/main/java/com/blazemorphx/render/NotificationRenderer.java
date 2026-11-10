package com.blazemorphx.render;

import com.blazemorphx.customization.FontManager;
import com.blazemorphx.customization.ThemeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public final class NotificationRenderer {
	private static final NotificationRenderer INSTANCE = new NotificationRenderer();
	private final Deque<Toast> toasts = new ArrayDeque<>();

	private NotificationRenderer() {
	}

	public static NotificationRenderer get() {
		return INSTANCE;
	}

	public void push(String title, String body) {
		toasts.addLast(new Toast(title, body, ThemeManager.accent(), 160));
		while (toasts.size() > 5) {
			toasts.removeFirst();
		}
	}

	public void error(String body) {
		toasts.addLast(new Toast("Blaze Morphx", body, ThemeManager.danger(), 200));
	}

	public void tick() {
		toasts.removeIf(toast -> --toast.life <= 0);
	}

	public void render(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		int y = 12;
		for (Toast toast : toasts) {
			float fade = Math.min(1f, toast.life / 12f);
			int w = 220;
			int x = client.getWindow().getScaledWidth() - w - 12;
			GlassRenderer.panel(context, x, y, w, 36, ThemeManager.panelStrong(), toast.color, fade);
			FontManager.get().draw(context, toast.title, x + 10, y + 6, toast.color);
			FontManager.get().draw(context, toast.body, x + 10, y + 18, ThemeManager.muted());
			y += 42;
		}
	}

	private static final class Toast {
		private final String title;
		private final String body;
		private final int color;
		private int life;

		private Toast(String title, String body, int color, int life) {
			this.title = title;
			this.body = body;
			this.color = color;
			this.life = life;
		}
	}
}
