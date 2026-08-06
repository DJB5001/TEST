package de.djhub;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Kurze Rueckmeldungen ueber der Hotbar.
 */
public final class DJChat {

	private DJChat() {
	}

	/** Zeigt "Name: AN" bzw. "Name: AUS" ueber der Hotbar. */
	public static void status(Minecraft minecraft, String label, boolean state) {
		if (minecraft == null || minecraft.player == null) {
			return;
		}

		minecraft.player.displayClientMessage(
				Component.literal(label + ": ").withStyle(ChatFormatting.LIGHT_PURPLE)
						.append(state
								? Component.literal("AN").withStyle(ChatFormatting.GREEN)
								: Component.literal("AUS").withStyle(ChatFormatting.RED)),
				true);
	}

	/** Neutrale Meldung ueber der Hotbar. */
	public static void info(Minecraft minecraft, String message) {
		if (minecraft == null || minecraft.player == null) {
			return;
		}

		minecraft.player.displayClientMessage(
				Component.literal(message).withStyle(ChatFormatting.AQUA), true);
	}

	public static void warn(Minecraft minecraft, String message) {
		if (minecraft == null || minecraft.player == null) {
			return;
		}

		minecraft.player.displayClientMessage(
				Component.literal(message).withStyle(ChatFormatting.RED), true);
	}
}
