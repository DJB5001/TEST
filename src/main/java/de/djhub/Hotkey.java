package de.djhub;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Eine frei belegbare Taste. Die Abfrage laeuft direkt ueber GLFW, damit die
 * Belegung im DJ-HUB-Menue geaendert werden kann.
 */
public class Hotkey {

	public static final int UNBOUND = -1;

	public final String id;
	public final String name;
	public final String description;
	public final int defaultKey;

	private int key;
	private boolean down;
	private boolean pending;

	public Hotkey(String id, String name, String description, int defaultKey) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.defaultKey = defaultKey;
		this.key = defaultKey;
	}

	public int getKey() {
		return this.key;
	}

	public void setKey(int key) {
		this.key = key;
		this.down = false;
		this.pending = false;
	}

	/**
	 * @param allowTrigger false, solange ein Bildschirm offen ist. Der gedrueckt/losgelassen
	 *                     Zustand wird trotzdem mitgefuehrt, damit beim Schliessen des Menues
	 *                     keine Taste doppelt ausloest.
	 */
	public void poll(Minecraft minecraft, boolean allowTrigger) {
		boolean now = this.key >= 0
				&& InputConstants.isKeyDown(minecraft.getWindow(), this.key);

		if (now && !this.down && allowTrigger) {
			this.pending = true;
		}

		this.down = now;
	}

	/** true genau einmal pro Tastendruck. */
	public boolean consumePress() {
		if (this.pending) {
			this.pending = false;
			return true;
		}

		return false;
	}

	/** true solange die Taste gehalten wird. */
	public boolean isDown() {
		return this.down;
	}

	public String getKeyLabel() {
		return label(this.key);
	}

	/**
	 * Eigene Namenstabelle statt der Minecraft-Uebersetzung - so bleibt die Anzeige
	 * unabhaengig von Mapping-Aenderungen.
	 */
	public static String label(int code) {
		if (code < 0) {
			return "---";
		}

		if (code >= GLFW.GLFW_KEY_A && code <= GLFW.GLFW_KEY_Z) {
			return String.valueOf((char) ('A' + (code - GLFW.GLFW_KEY_A)));
		}

		if (code >= GLFW.GLFW_KEY_0 && code <= GLFW.GLFW_KEY_9) {
			return String.valueOf((char) ('0' + (code - GLFW.GLFW_KEY_0)));
		}

		if (code >= GLFW.GLFW_KEY_F1 && code <= GLFW.GLFW_KEY_F25) {
			return "F" + (code - GLFW.GLFW_KEY_F1 + 1);
		}

		if (code >= GLFW.GLFW_KEY_KP_0 && code <= GLFW.GLFW_KEY_KP_9) {
			return "Num " + (code - GLFW.GLFW_KEY_KP_0);
		}

		switch (code) {
			case GLFW.GLFW_KEY_SPACE: return "Leertaste";
			case GLFW.GLFW_KEY_APOSTROPHE: return "'";
			case GLFW.GLFW_KEY_COMMA: return ",";
			case GLFW.GLFW_KEY_MINUS: return "-";
			case GLFW.GLFW_KEY_PERIOD: return ".";
			case GLFW.GLFW_KEY_SLASH: return "/";
			case GLFW.GLFW_KEY_SEMICOLON: return ";";
			case GLFW.GLFW_KEY_EQUAL: return "=";
			case GLFW.GLFW_KEY_LEFT_BRACKET: return "[";
			case GLFW.GLFW_KEY_RIGHT_BRACKET: return "]";
			case GLFW.GLFW_KEY_BACKSLASH: return "\\";
			case GLFW.GLFW_KEY_GRAVE_ACCENT: return "^";
			case GLFW.GLFW_KEY_ENTER: return "Enter";
			case GLFW.GLFW_KEY_TAB: return "Tab";
			case GLFW.GLFW_KEY_BACKSPACE: return "Rueck";
			case GLFW.GLFW_KEY_INSERT: return "Einfg";
			case GLFW.GLFW_KEY_DELETE: return "Entf";
			case GLFW.GLFW_KEY_RIGHT: return "Rechts";
			case GLFW.GLFW_KEY_LEFT: return "Links";
			case GLFW.GLFW_KEY_DOWN: return "Runter";
			case GLFW.GLFW_KEY_UP: return "Hoch";
			case GLFW.GLFW_KEY_PAGE_UP: return "Bild hoch";
			case GLFW.GLFW_KEY_PAGE_DOWN: return "Bild runter";
			case GLFW.GLFW_KEY_HOME: return "Pos1";
			case GLFW.GLFW_KEY_END: return "Ende";
			case GLFW.GLFW_KEY_CAPS_LOCK: return "Feststell";
			case GLFW.GLFW_KEY_LEFT_SHIFT: return "Shift links";
			case GLFW.GLFW_KEY_RIGHT_SHIFT: return "Shift rechts";
			case GLFW.GLFW_KEY_LEFT_CONTROL: return "Strg links";
			case GLFW.GLFW_KEY_RIGHT_CONTROL: return "Strg rechts";
			case GLFW.GLFW_KEY_LEFT_ALT: return "Alt";
			case GLFW.GLFW_KEY_RIGHT_ALT: return "Alt Gr";
			case GLFW.GLFW_KEY_KP_DECIMAL: return "Num ,";
			case GLFW.GLFW_KEY_KP_ADD: return "Num +";
			case GLFW.GLFW_KEY_KP_SUBTRACT: return "Num -";
			case GLFW.GLFW_KEY_KP_MULTIPLY: return "Num *";
			case GLFW.GLFW_KEY_KP_DIVIDE: return "Num /";
			case GLFW.GLFW_KEY_KP_ENTER: return "Num Enter";
			default: return "Taste " + code;
		}
	}
}
