package com.blazemorphx.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public final class FileDialogs {
	private FileDialogs() {
	}

	public static void open(String title, Set<String> extensions, Consumer<Path> callback) {
		Util.getIoWorkerExecutor().execute(() -> {
			try {
				FileDialog dialog = new FileDialog((Frame) null, title, FileDialog.LOAD);
				dialog.setMultipleMode(false);
				if (extensions != null && !extensions.isEmpty()) {
					dialog.setFilenameFilter((dir, name) -> {
						String lower = name.toLowerCase(Locale.ROOT);
						for (String ext : extensions) {
							if (lower.endsWith("." + ext.toLowerCase(Locale.ROOT))) {
								return true;
							}
						}
						return false;
					});
				}
				dialog.setVisible(true);
				String file = dialog.getFile();
				String dir = dialog.getDirectory();
				dialog.dispose();
				if (file != null && dir != null) {
					Path path = new File(dir, file).toPath();
					MinecraftClient.getInstance().execute(() -> callback.accept(path));
				}
			} catch (Exception ignored) {
				MinecraftClient.getInstance().execute(() -> callback.accept(null));
			}
		});
	}

	public static void save(String title, String defaultName, Consumer<Path> callback) {
		Util.getIoWorkerExecutor().execute(() -> {
			try {
				FileDialog dialog = new FileDialog((Frame) null, title, FileDialog.SAVE);
				dialog.setFile(defaultName);
				dialog.setVisible(true);
				String file = dialog.getFile();
				String dir = dialog.getDirectory();
				dialog.dispose();
				if (file != null && dir != null) {
					Path path = new File(dir, file).toPath();
					MinecraftClient.getInstance().execute(() -> callback.accept(path));
				}
			} catch (Exception ignored) {
				MinecraftClient.getInstance().execute(() -> callback.accept(null));
			}
		});
	}
}
