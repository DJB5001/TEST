package de.djhub;

import java.util.ArrayList;
import java.util.List;

public final class Features {

	public static final String CAT_MOVE = "Auto Tasten";
	public static final String CAT_VIEW = "Ansicht";
	public static final String CAT_MINER = "AutoMiner";
	public static final String CAT_HUD = "HUD";
	public static final String CAT_TRADE = "Auto Trade";
	public static final String CAT_CHAT = "Auto Chat";
	public static final String CAT_MISC = "Sonstiges";
	public static final String CAT_KEYS = "Tasten";

	public static final String[] CATEGORIES = {CAT_MOVE, CAT_VIEW, CAT_MINER, CAT_TRADE, CAT_CHAT, CAT_HUD, CAT_MISC, CAT_KEYS};

	public static final List<Feature> ALL = new ArrayList<>();

	// ---------- Bewegung ----------
	public static final Feature AUTO_SPRINT = add("auto_sprint", "Auto Sprint",
			"Sprintet automatisch, sobald du vorwaerts laeufst.", CAT_MOVE, false, true);
	public static final Feature AUTO_WALK = add("auto_walk", "Auto Walk",
			"Haelt die Vorwaerts-Taste dauerhaft gedrueckt.", CAT_MOVE, false, true);
	public static final Feature AUTO_SNEAK = add("auto_sneak", "Auto Sneak",
			"Haelt die Schleichen-Taste dauerhaft gedrueckt.", CAT_MOVE, false, true);
	public static final Feature AUTO_JUMP = add("auto_jump", "Auto Jump",
			"Haelt die Sprung-Taste dauerhaft gedrueckt.", CAT_MOVE, false, true);
	public static final Feature HOLD_ATTACK = add("hold_attack", "Auto Linksklick",
			"Haelt die Linke Maustaste dauerhaft gedrueckt.", CAT_MOVE, false, false);

	// ---------- Ansicht ----------
	public static final Feature BRIGHT = add("bright", "Max Helligkeit",
			"Setzt die Helligkeit auf das Maximum.", CAT_VIEW, false, true);
	public static final Feature ZOOM = add("zoom", "Zoom",
			"Taste halten zum Heranzoomen (Tab 'Tasten').", CAT_VIEW, true, true);
	public static final Feature WIDE_FOV = add("wide_fov", "Weitwinkel",
			"Setzt das Sichtfeld auf 110 fuer mehr Uebersicht.", CAT_VIEW, false, true);
	public static final Feature NO_BREAK_PARTICLES = add("no_break_particles", "Keine Block-Partikel",
			"Blendet Blockstaub aus - beim Abbauen, Sprinten und Landen.", CAT_VIEW, false, true);

	// ---------- AutoMiner ----------
	public static final Feature MINER = add("miner", "AutoMiner",
			"Laeuft gerade aus und haelt Linksklick zum Minen.", CAT_MINER, false, false);
	public static final Feature MINER_PITCH_LOCK = add("miner_pitch_lock", "Kamera-Lock",
			"Haelt den Blick waagerecht, Umsehen nach links/rechts bleibt moeglich.", CAT_MINER, true, true);
	public static final Feature MINER_CENTER = add("miner_center", "Auto-Zentrieren",
			"Richtet dich bei ruhiger Maus automatisch auf 90-Grad-Schritte aus.", CAT_MINER, true, true);
	public static final Feature MINER_DESCEND = add("miner_descend", "Auf Y -54 graben",
			"Graebt sich treppenartig runter, bis Y -54 erreicht ist.", CAT_MINER, true, true);
	public static final Feature MINER_DODGE = add("miner_dodge", "Ausweichen",
			"Weicht Lava und Kies seitlich aus statt nur stehen zu bleiben.", CAT_MINER, true, true);

	// ---------- Auto Trade ----------
	public static final Feature AUTO_TRADE = add("auto_trade", "Auto Trade",
			"Handelt beim Oeffnen eines Villager-Menues automatisch das gewaehlte Item.", CAT_TRADE, false, true);
	public static final Feature AUTO_TRADE_REPEAT = add("auto_trade_repeat", "Wiederholen",
			"Handelt so lange, bis das Angebot ausverkauft ist oder du nicht mehr zahlen kannst.", CAT_TRADE, false, true);

	// ---------- Auto Chat ----------
	public static final Feature AUTO_CHAT = add("auto_chat", "Auto Chat",
			"Sendet die eingegebene Nachricht in festem Abstand in den Chat.", CAT_CHAT, false, true);

	// ---------- HUD ----------
	public static final Feature HUD_FPS = add("hud_fps", "FPS Anzeige",
			"Zeigt die Bilder pro Sekunde.", CAT_HUD, true, true);
	public static final Feature HUD_COORDS = add("hud_coords", "Koordinaten",
			"Zeigt deine X / Y / Z Position.", CAT_HUD, true, true);
	public static final Feature HUD_FACING = add("hud_facing", "Blickrichtung",
			"Zeigt in welche Himmelsrichtung du schaust.", CAT_HUD, false, true);
	public static final Feature HUD_SPEED = add("hud_speed", "Geschwindigkeit",
			"Zeigt deine Geschwindigkeit in Bloecken pro Sekunde.", CAT_HUD, false, true);
	public static final Feature HUD_CPS = add("hud_cps", "CPS Zaehler",
			"Zeigt deine Klicks pro Sekunde.", CAT_HUD, false, true);
	public static final Feature HUD_TIME = add("hud_time", "Uhrzeit",
			"Zeigt die echte Uhrzeit deines PCs.", CAT_HUD, false, true);

	// ---------- Sonstiges ----------
	public static final Feature ANTI_DROP = add("anti_drop", "Anti Drop",
			"Blockiert das Wegwerfen von Items, solange kein Inventar offen ist.", CAT_MISC, false, true);
	public static final Feature HUD_RIGHT = add("hud_right", "HUD rechts",
			"Zeigt das HUD oben rechts statt oben links.", CAT_MISC, false, true);
	public static final Feature HUD_BOX = add("hud_box", "HUD Hintergrund",
			"Dunkle Box hinter dem HUD-Text.", CAT_MISC, true, true);
	public static final Feature HUD_SHADOW = add("hud_shadow", "HUD Textschatten",
			"Zeichnet den HUD-Text mit Schatten.", CAT_MISC, true, true);
	public static final Feature CHAT_FEEDBACK = add("chat_feedback", "Chat-Meldungen",
			"Schreibt beim Umschalten eine Nachricht in den Chat.", CAT_MISC, true, true);
	public static final Feature MENU_SOUND = add("menu_sound", "Menue-Sound",
			"Klick-Geraeusch beim Bedienen des Menues.", CAT_MISC, true, true);

	private Features() {
	}

	private static Feature add(String id, String name, String description, String category,
			boolean def, boolean persistent) {
		Feature feature = new Feature(id, name, description, category, def, persistent);
		ALL.add(feature);
		return feature;
	}

	public static void init() {
	}

	public static List<Feature> byCategory(String category) {
		List<Feature> result = new ArrayList<>();

		for (Feature feature : ALL) {
			if (feature.category.equals(category)) {
				result.add(feature);
			}
		}

		return result;
	}

	public static int activeCount() {
		int count = 0;

		for (Feature feature : ALL) {
			if (feature.isEnabled()) {
				count++;
			}
		}

		return count;
	}
}
