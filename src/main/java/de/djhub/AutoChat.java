package de.djhub;

import net.minecraft.client.Minecraft;

/**
 * Schreibt in festen Abstaenden eine selbst eingegebene Nachricht in den Chat.
 * Faengt die Nachricht mit "/" an, wird sie als Befehl gesendet.
 */
public final class AutoChat {

	/** Auswaehlbare Abstaende in Minuten. */
	public static final int[] INTERVALS = {5, 10, 15, 20, 30, 60};

	private static final int MAX_LENGTH = 256;

	/** Nach dem (Wieder-)Betreten kurz warten, bevor gesendet wird. */
	private static final int SETTLE_TICKS = 60;
	/** Erst nach so langer Abwesenheit gilt es als neue Sitzung. */
	private static final long AWAY_RESET_MS = 60_000L;

	private static String message = "";
	private static int intervalMinutes = 20;
	private static long lastSent = System.currentTimeMillis();
	/** 0 = im Spiel, sonst Zeitpunkt, ab dem wir draussen sind. */
	private static long awaySince;
	private static int settle = SETTLE_TICKS;

	private AutoChat() {
	}

	// ---------------------------------------------------------------- Einstellungen

	public static String getMessage() {
		return message;
	}

	public static void setMessage(String value) {
		message = value == null ? "" : value;
	}

	public static int getIntervalMinutes() {
		return intervalMinutes;
	}

	public static void setIntervalMinutes(int minutes) {
		for (int value : INTERVALS) {
			if (value == minutes) {
				intervalMinutes = minutes;
				return;
			}
		}

		intervalMinutes = 20;
	}

	/** Schaltet zum naechsten Abstand weiter. */
	public static void cycleInterval() {
		int index = 0;

		for (int i = 0; i < INTERVALS.length; i++) {
			if (INTERVALS[i] == intervalMinutes) {
				index = i;
				break;
			}
		}

		intervalMinutes = INTERVALS[(index + 1) % INTERVALS.length];
		resetTimer();
	}

	public static void resetTimer() {
		lastSent = System.currentTimeMillis();
	}

	/** Millisekunden bis zur naechsten Nachricht. */
	public static long millisUntilNext() {
		long left = intervalMillis() - (System.currentTimeMillis() - lastSent);
		return left < 0L ? 0L : left;
	}

	public static String countdownText() {
		long seconds = millisUntilNext() / 1000L;
		return String.format("%d:%02d", seconds / 60L, seconds % 60L);
	}

	private static long intervalMillis() {
		return intervalMinutes * 60L * 1000L;
	}

	// ---------------------------------------------------------------- Tick

	public static void tick(Minecraft minecraft) {
		boolean inGame = minecraft != null && minecraft.player != null
				&& minecraft.player.connection != null;

		// ---------- Nicht im Spiel ----------
		if (!inGame) {
			// Wichtig: Ein Serverwechsel (z.B. Lobby -> Farmwelt) dauert nur ein paar
			// Sekunden. Der Timer laeuft dabei weiter, sonst faengt er jedes Mal von
			// vorne an. Erst nach einer Minute draussen gilt es als neue Sitzung.
			if (awaySince == 0L) {
				awaySince = System.currentTimeMillis();
			}

			settle = SETTLE_TICKS;

			if (System.currentTimeMillis() - awaySince > AWAY_RESET_MS) {
				resetTimer();
			}

			return;
		}

		awaySince = 0L;

		// Nach dem Ankommen kurz Ruhe, damit die Nachricht nicht im Ladebildschirm landet.
		if (settle > 0) {
			settle--;
			return;
		}

		if (!Features.AUTO_CHAT.isEnabled() || message.trim().isEmpty()) {
			resetTimer();
			return;
		}

		if (System.currentTimeMillis() - lastSent >= intervalMillis()) {
			resetTimer();
			send(minecraft);
		}
	}

	/** true, solange wir gerade nicht in einer Welt sind (z.B. Serverwechsel). */
	public static boolean isPaused() {
		return awaySince != 0L;
	}

	/**
	 * Sendet sofort. Gibt false zurueck, wenn nichts zu senden war.
	 */
	public static boolean sendNow(Minecraft minecraft) {
		resetTimer();

		if (minecraft == null || minecraft.player == null || minecraft.player.connection == null) {
			return false;
		}

		if (message.trim().isEmpty()) {
			return false;
		}

		send(minecraft);
		return true;
	}

	private static void send(Minecraft minecraft) {
		String text = message.trim();

		if (text.length() > MAX_LENGTH) {
			text = text.substring(0, MAX_LENGTH);
		}

		if (text.startsWith("/")) {
			minecraft.player.connection.sendCommand(text.substring(1));
		} else {
			minecraft.player.connection.sendChat(text);
		}
	}
}
