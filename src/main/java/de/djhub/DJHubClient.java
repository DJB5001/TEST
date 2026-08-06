package de.djhub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class DJHubClient implements ClientModInitializer {

	public static final String MOD_ID = "djhub";
	public static final String VERSION = "1.5.1";

	/** Wird vom TerrainParticleMixin gesetzt, sobald er das erste Mal greift. */
	public static volatile boolean particleMixinSeen;

	private static boolean openRequested;

	@Override
	@SuppressWarnings("deprecation")
	public void onInitializeClient() {
		Features.init();
		Hotkeys.init();
		DJConfig.load();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
			dispatcher.register(command("dj"));
			dispatcher.register(command("DJ"));
			dispatcher.register(command("djhub"));
		});

		// Anti Drop muss VOR der Tastenauswertung von Minecraft laufen.
		ClientTickEvents.START_CLIENT_TICK.register(DJFeatures::startTick);

		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
			Hotkeys.pollAll(minecraft);

			if (Hotkeys.MENU.consumePress()) {
				openRequested = true;
			}

			// Erst oeffnen, wenn der Chat wirklich zu ist.
			if (openRequested && minecraft.screen == null) {
				openRequested = false;
				minecraft.setScreen(new DJHubScreen());
			}

			// Reihenfolge wichtig: der AutoMiner hat das letzte Wort ueber die Tasten.
			DJFeatures.tick(minecraft);
			AutoMiner.tick(minecraft);
			AutoChat.tick(minecraft);
			AutoTrade.tick(minecraft);
			DJHud.tick(minecraft);
		});

		HudRenderCallback.EVENT.register((graphics, tickCounter) -> DJHud.render(graphics));

		System.out.println("[DJ HUB] geladen - tippe /dj im Spiel.");
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> command(String name) {
		return ClientCommandManager.literal(name).executes(context -> {
			openRequested = true;
			return 1;
		});
	}
}
