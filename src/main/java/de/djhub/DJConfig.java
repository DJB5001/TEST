package de.djhub;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Speichert Schalter und Tastenbelegungen in config/djhub.properties.
 */
public final class DJConfig {

	private DJConfig() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("djhub.properties");
	}

	public static void load() {
		Path path = file();

		if (!Files.exists(path)) {
			save();
			return;
		}

		Properties properties = new Properties();

		try (InputStream in = Files.newInputStream(path)) {
			properties.load(in);
		} catch (IOException e) {
			System.err.println("[DJ HUB] Config konnte nicht geladen werden: " + e.getMessage());
			return;
		}

		for (Feature feature : Features.ALL) {
			if (!feature.persistent) {
				continue;
			}

			String value = properties.getProperty(feature.id);

			if (value != null) {
				feature.setEnabled(Boolean.parseBoolean(value.trim()));
			}
		}

		AutoTrade.setItemId(properties.getProperty("trade_item_id", AutoTrade.getItemId()));
		AutoTrade.setItemName(properties.getProperty("trade_item_name", AutoTrade.getItemName()));
		AutoTrade.setNameFilter(properties.getProperty("trade_name_filter", AutoTrade.getNameFilter()));

		String chatMessage = properties.getProperty("chat_message");

		if (chatMessage != null) {
			AutoChat.setMessage(chatMessage);
		}

		String chatInterval = properties.getProperty("chat_interval");

		if (chatInterval != null) {
			try {
				AutoChat.setIntervalMinutes(Integer.parseInt(chatInterval.trim()));
			} catch (NumberFormatException ignored) {
				// kaputter Eintrag -> Standard behalten
			}
		}

		for (Hotkey hotkey : Hotkeys.ALL) {
			String value = properties.getProperty("key." + hotkey.id);

			if (value != null) {
				try {
					hotkey.setKey(Integer.parseInt(value.trim()));
				} catch (NumberFormatException ignored) {
					// kaputter Eintrag -> Standard behalten
				}
			}
		}
	}

	public static void save() {
		Properties properties = new Properties();

		for (Feature feature : Features.ALL) {
			if (feature.persistent) {
				properties.setProperty(feature.id, Boolean.toString(feature.isEnabled()));
			}
		}

		for (Hotkey hotkey : Hotkeys.ALL) {
			properties.setProperty("key." + hotkey.id, Integer.toString(hotkey.getKey()));
		}

		properties.setProperty("trade_item_id", AutoTrade.getItemId());
		properties.setProperty("trade_item_name", AutoTrade.getItemName());
		properties.setProperty("trade_name_filter", AutoTrade.getNameFilter());
		properties.setProperty("chat_message", AutoChat.getMessage());
		properties.setProperty("chat_interval", Integer.toString(AutoChat.getIntervalMinutes()));

		Path path = file();

		try {
			if (path.getParent() != null) {
				Files.createDirectories(path.getParent());
			}

			try (OutputStream out = Files.newOutputStream(path)) {
				properties.store(out, "DJ HUB Einstellungen");
			}
		} catch (IOException e) {
			System.err.println("[DJ HUB] Config konnte nicht gespeichert werden: " + e.getMessage());
		}
	}

	public static void reset() {
		for (Feature feature : Features.ALL) {
			feature.setEnabled(feature.defaultValue);
		}

		for (Hotkey hotkey : Hotkeys.ALL) {
			hotkey.setKey(hotkey.defaultKey);
		}

		AutoTrade.clearSelection();
		AutoTrade.setNameFilter("");
		AutoChat.setMessage("");
		AutoChat.setIntervalMinutes(20);
		save();
	}
}
