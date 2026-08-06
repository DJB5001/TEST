package de.djhub;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Wird jeden Client-Tick aufgerufen.
 */
public final class DJFeatures {

	private static boolean heldWalk;
	private static boolean heldSneak;
	private static boolean heldJump;
	private static boolean heldSprint;
	private static boolean heldAttack;

	private static boolean brightApplied;
	private static double savedGamma = 0.5D;

	private static boolean fovApplied;
	private static int savedFov = 70;

	private static long lastDropWarn;

	private DJFeatures() {
	}

	// ---------------------------------------------------------------- Ende des Ticks

	public static void tick(Minecraft minecraft) {
		if (minecraft == null || minecraft.options == null) {
			return;
		}

		// Auto Linksklick per Taste umschalten
		if (Hotkeys.HOLD_ATTACK.consumePress()) {
			Features.HOLD_ATTACK.toggle();
			DJChat.status(minecraft, "Auto Linksklick", Features.HOLD_ATTACK.isEnabled());
		}

		boolean inWorld = minecraft.player != null && minecraft.screen == null;

		// ---------- Tasten halten ----------
		heldWalk = hold(minecraft.options.keyUp,
				inWorld && Features.AUTO_WALK.isEnabled(), heldWalk);
		heldSneak = hold(minecraft.options.keyShift,
				inWorld && Features.AUTO_SNEAK.isEnabled(), heldSneak);
		heldJump = hold(minecraft.options.keyJump,
				inWorld && Features.AUTO_JUMP.isEnabled(), heldJump);
		heldSprint = hold(minecraft.options.keySprint,
				inWorld && Features.AUTO_SPRINT.isEnabled() && minecraft.options.keyUp.isDown(),
				heldSprint);
		heldAttack = hold(minecraft.options.keyAttack,
				inWorld && Features.HOLD_ATTACK.isEnabled(), heldAttack);

		// ---------- Helligkeit ----------
		boolean wantBright = Features.BRIGHT.isEnabled();

		if (wantBright && !brightApplied) {
			savedGamma = minecraft.options.gamma().get();
			minecraft.options.gamma().set(1.0D);
			brightApplied = true;
		} else if (!wantBright && brightApplied) {
			minecraft.options.gamma().set(savedGamma);
			brightApplied = false;
		}

		// ---------- Zoom / Weitwinkel ----------
		boolean wantZoom = Features.ZOOM.isEnabled()
				&& Hotkeys.ZOOM.isDown()
				&& minecraft.screen == null;
		boolean wantWide = Features.WIDE_FOV.isEnabled();

		if (wantZoom || wantWide) {
			if (!fovApplied) {
				savedFov = minecraft.options.fov().get();
				fovApplied = true;
			}

			minecraft.options.fov().set(wantZoom ? 30 : 110);
		} else if (fovApplied) {
			minecraft.options.fov().set(savedFov);
			fovApplied = false;
		}
	}

	// ---------------------------------------------------------------- Anfang des Ticks

	/**
	 * Anti Drop. Muss am ANFANG des Ticks laufen, weil Minecraft die Drop-Taste
	 * mitten im Tick auswertet. Die aufgelaufenen Tastendruecke werden vorher
	 * abgeraeumt, dadurch sieht Minecraft sie gar nicht erst.
	 *
	 * Bei offenem Inventar wird nichts blockiert - dort darf normal geworfen werden.
	 */
	public static void startTick(Minecraft minecraft) {
		if (minecraft == null || minecraft.options == null || minecraft.player == null) {
			return;
		}

		if (!Features.ANTI_DROP.isEnabled() || minecraft.screen != null) {
			return;
		}

		boolean blocked = false;

		while (minecraft.options.keyDrop.consumeClick()) {
			blocked = true;
		}

		minecraft.options.keyDrop.setDown(false);

		if (blocked) {
			long now = System.currentTimeMillis();

			if (now - lastDropWarn > 2000L) {
				lastDropWarn = now;
				DJChat.warn(minecraft, "Anti Drop: Wegwerfen blockiert");
			}
		}
	}

	// ---------------------------------------------------------------- Hilfen

	/** Haelt eine Taste gedrueckt und laesst sie sauber wieder los. */
	private static boolean hold(KeyMapping key, boolean wanted, boolean wasHeld) {
		if (wanted) {
			key.setDown(true);
			return true;
		}

		if (wasHeld) {
			key.setDown(false);
		}

		return false;
	}
}
