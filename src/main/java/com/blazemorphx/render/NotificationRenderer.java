package com.blazemorphx.render;

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
		toasts.addLast(new Toast(title, body, 0xFFFFFF, 160));
		while (toasts.size() > 5) {
			toasts.removeFirst();
		}
	}

	public void error(String body) {
		toasts.addLast(new Toast("Blaze Morphx", body, 0xFFFF5555, 220));
	}

	public void tick() {
		toasts.removeIf(toast -> --toast.life <= 0);
	}

	public void render(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.textRenderer == null) {
			return;
		}
		int y = 8;
		for (Toast toast : toasts) {
			int w = Math.min(280, Math.max(160, client.textRenderer.getWidth(toast.body) + 16));
			int x = client.getWindow().getScaledWidth() - w - 8;
			context.fill(x, y, x + w, y + 28, 0xC0101010);
			context.drawTextWithShadow(client.textRenderer, toast.title, x + 6, y + 4, toast.color);
			context.drawTextWithShadow(client.textRenderer, toast.body, x + 6, y + 14, 0xFFA0A0A0);
			y += 32;
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
