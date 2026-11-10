package com.blazemorphx.gui.components;

import com.blazemorphx.render.NotificationRenderer;

public final class CustomNotification {
	private CustomNotification() {
	}

	public static void info(String title, String body) {
		NotificationRenderer.get().push(title, body);
	}

	public static void error(String body) {
		NotificationRenderer.get().error(body);
	}
}
