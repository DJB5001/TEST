package de.djhub;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class Hotkeys {

	public static final List<Hotkey> ALL = new ArrayList<>();

	public static final Hotkey MENU = add(new Hotkey("menu", "Menue oeffnen",
			"Oeffnet dieses Menue.", GLFW.GLFW_KEY_RIGHT_SHIFT));
	public static final Hotkey ZOOM = add(new Hotkey("zoom", "Zoom (halten)",
			"Solange gehalten wird herangezoomt.", GLFW.GLFW_KEY_C));
	public static final Hotkey MINER = add(new Hotkey("miner", "AutoMiner an/aus",
			"Schaltet den AutoMiner um.", GLFW.GLFW_KEY_PERIOD));
	public static final Hotkey MINER_LOCK = add(new Hotkey("miner_lock", "Kamera-Lock an/aus",
			"Schaltet den Kamera-Lock des AutoMiners um.", GLFW.GLFW_KEY_COMMA));
	public static final Hotkey HOLD_ATTACK = add(new Hotkey("hold_attack", "Auto Linksklick an/aus",
			"Haelt die linke Maustaste dauerhaft gedrueckt.", GLFW.GLFW_KEY_G));

	private Hotkeys() {
	}

	private static Hotkey add(Hotkey hotkey) {
		ALL.add(hotkey);
		return hotkey;
	}

	public static void init() {
	}

	public static void pollAll(Minecraft minecraft) {
		if (minecraft == null || minecraft.getWindow() == null) {
			return;
		}

		boolean allowTrigger = minecraft.screen == null;

		for (Hotkey hotkey : ALL) {
			hotkey.poll(minecraft, allowTrigger);
		}
	}

	/** Verhindert, dass zwei Aktionen auf derselben Taste liegen. */
	public static void clearDuplicates(Hotkey keep, int code) {
		if (code < 0) {
			return;
		}

		for (Hotkey hotkey : ALL) {
			if (hotkey != keep && hotkey.getKey() == code) {
				hotkey.setKey(Hotkey.UNBOUND);
			}
		}
	}
}
