package de.djhub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Locale;

/**
 * Handelt beim Oeffnen eines Villager-Menues automatisch das ausgewaehlte Item.
 *
 * Ablauf pro Handel:
 *   1. passendes Angebot suchen
 *   2. Angebot anklicken (handleInventoryButtonClick - genau das macht Vanilla auch)
 *   3. Ergebnis-Slot per Shift-Klick ins Inventar holen
 *
 * Kommt nichts im Ergebnis-Slot an (nicht bezahlbar), wird sauber gestoppt.
 */
public final class AutoTrade {

	/** Kurz warten, bevor ueberhaupt etwas passiert. */
	private static final int OPEN_DELAY = 5;
	/** So lange auf die Angebotsliste vom Server warten (6 Sekunden). */
	private static final int OFFER_TIMEOUT = 120;
	/** So lange auf den gefuellten Ergebnis-Slot warten (4 Sekunden). */
	private static final int RESULT_TIMEOUT = 80;
	/** Pause zwischen zwei Handeln. */
	private static final int AFTER_TRADE_DELAY = 4;
	private static final int MAX_TRADES = 64;
	private static final int RESULT_SLOT = 2;

	private static String itemId = "";
	private static String itemName = "";
	private static String nameFilter = "";

	private static AbstractContainerMenu handledMenu;
	private static int cooldown;
	private static int waited;
	private static int trades;
	private static boolean finished;
	private static boolean awaitingResult;

	private AutoTrade() {
	}

	// ---------------------------------------------------------------- Auswahl

	public static String getItemId() {
		return itemId;
	}

	public static void setItemId(String value) {
		itemId = value == null ? "" : value;
	}

	public static String getItemName() {
		return itemName;
	}

	public static void setItemName(String value) {
		itemName = value == null ? "" : value;
	}

	public static String getNameFilter() {
		return nameFilter;
	}

	public static void setNameFilter(String value) {
		nameFilter = value == null ? "" : value;
	}

	/** Uebernimmt das Item aus der Haupthand. */
	public static void takeFromHand(Minecraft minecraft) {
		if (minecraft == null || minecraft.player == null) {
			return;
		}

		ItemStack stack = minecraft.player.getMainHandItem();

		if (stack.isEmpty()) {
			DJChat.warn(minecraft, "Nimm das gewuenschte Item in die Hand");
			return;
		}

		itemId = idOf(stack);
		itemName = stack.getHoverName().getString();

		if (itemId.isEmpty()) {
			DJChat.warn(minecraft, "Dieses Item laesst sich nicht auswaehlen");
		}
	}

	public static void clearSelection() {
		itemId = "";
		itemName = "";
	}

	/** Text fuer die Anzeige im Menue. */
	public static String selectionText() {
		if (!itemId.isEmpty()) {
			return itemName.isEmpty() ? itemId : itemName;
		}

		if (!nameFilter.trim().isEmpty()) {
			return "Name enthaelt \"" + nameFilter.trim() + "\"";
		}

		return "nichts ausgewaehlt";
	}

	public static boolean hasSelection() {
		return !itemId.isEmpty() || !nameFilter.trim().isEmpty();
	}

	// ---------------------------------------------------------------- Tick

	public static void tick(Minecraft minecraft) {
		if (minecraft == null || minecraft.player == null || minecraft.gameMode == null
				|| !Features.AUTO_TRADE.isEnabled() || !hasSelection()) {
			reset();
			return;
		}

		if (!(minecraft.screen instanceof MenuAccess)) {
			reset();
			return;
		}

		AbstractContainerMenu menu = ((MenuAccess<?>) minecraft.screen).getMenu();

		if (!(menu instanceof MerchantMenu)) {
			reset();
			return;
		}

		MerchantMenu merchant = (MerchantMenu) menu;

		// Neues Villager-Menue erkannt
		if (menu != handledMenu) {
			handledMenu = menu;
			cooldown = OPEN_DELAY;
			waited = 0;
			trades = 0;
			finished = false;
			awaitingResult = false;
		}

		if (finished) {
			return;
		}

		if (cooldown > 0) {
			cooldown--;
			return;
		}

		waited++;

		try {
			step(minecraft, merchant);
		} catch (Throwable t) {
			finished = true;
			Features.AUTO_TRADE.setEnabled(false);
			DJConfig.save();
			DJChat.warn(minecraft, "Auto Trade abgeschaltet - Fehler beim Handeln");
			System.err.println("[DJ HUB] Auto Trade: " + t);
		}
	}

	private static void step(Minecraft minecraft, MerchantMenu merchant) {
		// ---------- Auf das Ergebnis warten ----------
		if (awaitingResult) {
			if (merchant.getSlot(RESULT_SLOT).hasItem()) {
				minecraft.gameMode.handleInventoryMouseClick(merchant.containerId, RESULT_SLOT, 0,
						ClickType.QUICK_MOVE, minecraft.player);
				trades++;
				awaitingResult = false;
				waited = 0;

				if (!Features.AUTO_TRADE_REPEAT.isEnabled() || trades >= MAX_TRADES) {
					stop(minecraft, null);
				} else {
					cooldown = AFTER_TRADE_DELAY;
				}

				return;
			}

			// Nichts gekommen -> entweder zu wenig Ping-Geduld oder nicht bezahlbar
			if (waited > RESULT_TIMEOUT) {
				stop(minecraft, "nicht bezahlbar");
			}

			return;
		}

		// ---------- Auf die Angebotsliste warten ----------
		if (merchant.getOffers().isEmpty()) {
			if (waited > OFFER_TIMEOUT) {
				stop(minecraft, "keine Angebote empfangen");
			}

			return;
		}

		int index = findOffer(merchant);

		if (index < 0) {
			stop(minecraft, "kein passendes Angebot");
			return;
		}

		minecraft.gameMode.handleInventoryButtonClick(merchant.containerId, index);
		awaitingResult = true;
		waited = 0;
	}

	private static int findOffer(MerchantMenu merchant) {
		for (int i = 0; i < merchant.getOffers().size(); i++) {
			MerchantOffer offer = merchant.getOffers().get(i);

			if (offer == null || offer.isOutOfStock()) {
				continue;
			}

			if (matches(offer.getResult())) {
				return i;
			}
		}

		return -1;
	}

	private static boolean matches(ItemStack result) {
		if (result == null || result.isEmpty()) {
			return false;
		}

		if (!itemId.isEmpty()) {
			return itemId.equals(idOf(result));
		}

		String filter = nameFilter.trim().toLowerCase(Locale.ROOT);

		if (filter.isEmpty()) {
			return false;
		}

		// Der Namensfilter greift sowohl auf den angezeigten Namen als auch auf die Item-ID.
		return result.getHoverName().getString().toLowerCase(Locale.ROOT).contains(filter)
				|| idOf(result).toLowerCase(Locale.ROOT).contains(filter);
	}

	/** Item-Kennung, z.B. "minecraft:emerald". */
	private static String idOf(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return "";
		}

		Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return key == null ? "" : key.toString();
	}

	private static void stop(Minecraft minecraft, String reason) {
		finished = true;

		if (trades > 0) {
			DJChat.info(minecraft, "Auto Trade: " + trades + "x gehandelt");
		} else if (reason != null) {
			DJChat.warn(minecraft, "Auto Trade: " + reason);
		}
	}

	private static void reset() {
		handledMenu = null;
		cooldown = 0;
		waited = 0;
		trades = 0;
		finished = false;
		awaitingResult = false;
	}
}
