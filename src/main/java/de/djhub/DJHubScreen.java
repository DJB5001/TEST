package de.djhub;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class DJHubScreen extends Screen {

	private static final int PANEL_W = 410;
	private static final int PANEL_H = 262;

	private static final int SIDEBAR_W = 120;
	private static final int HEADER_H = 34;
	private static final int TAB_H = 20;
	private static final int TAB_STEP = 22;
	private static final int ROW_H = 22;
	private static final int ROW_STEP = 26;
	private static final int CONTENT_TOP = 62;

	/** Bleibt zwischen dem Oeffnen erhalten. */
	private static String selected = Features.CAT_MOVE;

	private final List<ToggleWidget> toggles = new ArrayList<>();
	private final List<KeybindWidget> keybinds = new ArrayList<>();
	private final List<AbstractWidget> chatWidgets = new ArrayList<>();
	private final List<AbstractWidget> tradeWidgets = new ArrayList<>();
	private final List<FlatButton> tabs = new ArrayList<>();

	private EditBox messageBox;
	private EditBox tradeBox;

	public DJHubScreen() {
		super(Component.literal("DJ HUB"));
	}

	@Override
	protected void init() {
		this.toggles.clear();
		this.keybinds.clear();
		this.chatWidgets.clear();
		this.tradeWidgets.clear();
		this.tabs.clear();

		final int left = (this.width - PANEL_W) / 2;
		final int top = (this.height - PANEL_H) / 2;

		// Hintergrund zuerst -> liegt unter allen Buttons
		Renderable background = (graphics, mouseX, mouseY, delta) -> this.drawPanel(graphics, left, top);
		this.addRenderableOnly(background);

		// ---------- Kategorien links ----------
		int tabY = top + 42;

		for (int i = 0; i < Features.CATEGORIES.length; i++) {
			final String target = Features.CATEGORIES[i];

			FlatButton tab = new FlatButton(left + 10, tabY, SIDEBAR_W - 20, TAB_H,
					Component.literal(target), () -> this.selectCategory(target), false);
			tab.dot(DJColors.CATEGORY_DOTS[i % DJColors.CATEGORY_DOTS.length]);
			tab.select(target.equals(selected));

			this.tabs.add(tab);
			this.addRenderableWidget(tab);
			tabY += TAB_STEP;
		}

		int contentX = left + SIDEBAR_W + 10;
		int contentW = PANEL_W - SIDEBAR_W - 24;

		// ---------- Schalter ----------
		for (String category : Features.CATEGORIES) {
			int rowY = top + CONTENT_TOP;

			for (Feature feature : Features.byCategory(category)) {
				ToggleWidget toggle = new ToggleWidget(contentX, rowY, contentW, ROW_H, feature);
				setVisible(toggle, category.equals(selected));

				this.toggles.add(toggle);
				this.addRenderableWidget(toggle);
				rowY += ROW_STEP;
			}
		}

		// ---------- Auto Trade ----------
		this.tradeBox = new EditBox(this.font, contentX, top + 130, contentW, 20,
				Component.literal("Name"));
		this.tradeBox.setMaxLength(64);
		this.tradeBox.setValue(AutoTrade.getNameFilter());
		this.tradeBox.setResponder(AutoTrade::setNameFilter);
		this.tradeWidgets.add(this.tradeBox);

		this.tradeWidgets.add(new FlatButton(contentX, top + 156, 156, 22,
				Component.literal("Aus Hand uebernehmen"), () -> {
			AutoTrade.takeFromHand(this.minecraft);
			DJConfig.save();
		}, false));

		this.tradeWidgets.add(new FlatButton(contentX + 164, top + 156, contentW - 164, 22,
				Component.literal("Loeschen"), () -> {
			AutoTrade.clearSelection();
			AutoTrade.setNameFilter("");

			if (this.tradeBox != null) {
				this.tradeBox.setValue("");
			}

			DJConfig.save();
		}, false));

		for (AbstractWidget widget : this.tradeWidgets) {
			setVisible(widget, Features.CAT_TRADE.equals(selected));
			this.addRenderableWidget(widget);
		}

		// ---------- Auto Chat ----------
		this.messageBox = new EditBox(this.font, contentX, top + 104, contentW, 20,
				Component.literal("Nachricht"));
		this.messageBox.setMaxLength(256);
		this.messageBox.setValue(AutoChat.getMessage());
		this.messageBox.setResponder(AutoChat::setMessage);
		this.chatWidgets.add(this.messageBox);

		FlatButton intervalButton = new FlatButton(contentX, top + 130, 132, 22,
				Component.literal("Abstand"), () -> {
			AutoChat.cycleInterval();
			DJConfig.save();
		}, false);
		intervalButton.label(() -> Component.literal("Abstand: " + AutoChat.getIntervalMinutes() + " Min"));
		this.chatWidgets.add(intervalButton);

		this.chatWidgets.add(new FlatButton(contentX + 140, top + 130, contentW - 140, 22,
				Component.literal("Jetzt senden"), () -> {
			DJConfig.save();

			if (AutoChat.sendNow(this.minecraft)) {
				this.onClose();
			}
		}, false));

		for (AbstractWidget widget : this.chatWidgets) {
			setVisible(widget, Features.CAT_CHAT.equals(selected));
			this.addRenderableWidget(widget);
		}

		// ---------- Tastenbelegungen ----------
		int keyY = top + CONTENT_TOP;

		for (Hotkey hotkey : Hotkeys.ALL) {
			KeybindWidget widget = new KeybindWidget(contentX, keyY, contentW, ROW_H, hotkey,
					this::stopListening);
			setVisible(widget, Features.CAT_KEYS.equals(selected));

			this.keybinds.add(widget);
			this.addRenderableWidget(widget);
			keyY += ROW_STEP;
		}

		// ---------- Footer ----------
		this.addRenderableWidget(new FlatButton(left + 12, top + PANEL_H - 32, 104, 20,
				Component.literal("Standard"), () -> {
			DJConfig.reset();

			if (this.messageBox != null) {
				this.messageBox.setValue(AutoChat.getMessage());
			}

			if (this.tradeBox != null) {
				this.tradeBox.setValue(AutoTrade.getNameFilter());
			}
		}, false));

		this.addRenderableWidget(new FlatButton(left + PANEL_W - 116, top + PANEL_H - 32, 104, 20,
				Component.literal("Fertig"), this::onClose, true));
	}

	private static void setVisible(AbstractWidget widget, boolean visible) {
		widget.visible = visible;
		widget.active = visible;
	}

	private void selectCategory(String category) {
		selected = category;
		this.stopListening();

		for (FlatButton tab : this.tabs) {
			tab.select(tab.getMessage().getString().equals(category));
		}

		for (ToggleWidget toggle : this.toggles) {
			setVisible(toggle, toggle.feature.category.equals(category));
		}

		for (AbstractWidget widget : this.tradeWidgets) {
			setVisible(widget, Features.CAT_TRADE.equals(category));
		}

		for (AbstractWidget widget : this.chatWidgets) {
			setVisible(widget, Features.CAT_CHAT.equals(category));
		}

		for (KeybindWidget widget : this.keybinds) {
			setVisible(widget, Features.CAT_KEYS.equals(category));
		}
	}

	private void stopListening() {
		for (KeybindWidget widget : this.keybinds) {
			widget.setListening(false);
		}
	}

	private KeybindWidget listening() {
		for (KeybindWidget widget : this.keybinds) {
			if (widget.isListening()) {
				return widget;
			}
		}

		return null;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		KeybindWidget target = this.listening();

		if (target != null) {
			int code = event.key();

			if (code == GLFW.GLFW_KEY_ESCAPE) {
				target.hotkey.setKey(Hotkey.UNBOUND);
			} else {
				target.hotkey.setKey(code);
				Hotkeys.clearDuplicates(target.hotkey, code);
			}

			this.stopListening();
			DJConfig.save();
			return true;
		}

		return super.keyPressed(event);
	}

	// ---------------------------------------------------------------- Zeichnen

	private void drawPanel(GuiGraphics graphics, int left, int top) {
		graphics.fill(0, 0, this.width, this.height, DJColors.BACKDROP);

		DJColors.shadow(graphics, left, top, PANEL_W, PANEL_H);
		DJColors.rect(graphics, left, top, PANEL_W, PANEL_H, DJColors.PANEL, 4);

		// Kopfzeile
		DJColors.rect(graphics, left, top, PANEL_W, HEADER_H, DJColors.HEADER, 4);
		graphics.fill(left, top + HEADER_H - 6, left + PANEL_W, top + HEADER_H, DJColors.HEADER);
		DJColors.gradientX(graphics, left + 1, top + HEADER_H, PANEL_W - 2, 2,
				DJColors.ACCENT, DJColors.CYAN);

		// Seitenleiste
		graphics.fill(left + 1, top + HEADER_H + 2, left + SIDEBAR_W, top + PANEL_H - 40, DJColors.SIDEBAR);
		graphics.fill(left + SIDEBAR_W, top + HEADER_H + 2, left + SIDEBAR_W + 1,
				top + PANEL_H - 40, DJColors.OUTLINE_SOFT);

		this.drawHeader(graphics, left, top);
		this.drawContentHeader(graphics, left, top);

		// Fusszeile
		graphics.fill(left + 10, top + PANEL_H - 40, left + PANEL_W - 10, top + PANEL_H - 39,
				DJColors.OUTLINE_SOFT);

		String hint = "/dj  -  " + Hotkeys.MENU.getKeyLabel();
		int hintCenter = left + (SIDEBAR_W + PANEL_W - 110) / 2;
		graphics.drawString(this.font, hint, hintCenter - this.font.width(hint) / 2,
				top + PANEL_H - 26, DJColors.TEXT_FAINT, false);

		DJColors.outline(graphics, left, top, PANEL_W, PANEL_H, DJColors.OUTLINE);

		if (Features.CAT_VIEW.equals(selected)) {
			this.drawViewInfo(graphics, left + SIDEBAR_W + 10, top);
		}

		if (Features.CAT_TRADE.equals(selected)) {
			this.drawTradeInfo(graphics, left + SIDEBAR_W + 10, top);
		}

		if (Features.CAT_CHAT.equals(selected)) {
			this.drawChatInfo(graphics, left + SIDEBAR_W + 10, top);
		}
	}

	private void drawHeader(GuiGraphics graphics, int left, int top) {
		int titleY = top + 13;

		// Akzentbalken als kleines Logo
		DJColors.rect(graphics, left + 14, top + 10, 3, 15, DJColors.ACCENT, 1);

		int textX = left + 23;
		graphics.drawString(this.font, "DJ", textX, titleY, DJColors.ACCENT_LIGHT, false);
		textX += this.font.width("DJ ");
		graphics.drawString(this.font, "HUB", textX, titleY, DJColors.TEXT, false);
		textX += this.font.width("HUB") + 7;
		graphics.drawString(this.font, "v" + DJHubClient.VERSION, textX, titleY + 1,
				DJColors.TEXT_FAINT, false);

		// Status-Plakette rechts
		String status;
		int color;

		if (Features.MINER.isEnabled()) {
			status = "AutoMiner laeuft";
			color = DJColors.ON;
		} else {
			status = Features.activeCount() + " aktiv";
			color = DJColors.CYAN;
		}

		int pillW = this.font.width(status) + 20;
		int pillX = left + PANEL_W - 14 - pillW;
		int pillY = top + 9;

		DJColors.rect(graphics, pillX, pillY, pillW, 16, DJColors.alpha(color, 0x28), 4);
		DJColors.dot(graphics, pillX + 7, pillY + 6, 4, color);
		graphics.drawString(this.font, status, pillX + 15, pillY + 4, color, false);
	}

	private void drawContentHeader(GuiGraphics graphics, int left, int top) {
		int contentX = left + SIDEBAR_W + 10;
		int rightEdge = left + PANEL_W - 14;

		graphics.drawString(this.font, selected, contentX, top + 45, DJColors.TEXT, false);

		int count = Features.CAT_KEYS.equals(selected)
				? Hotkeys.ALL.size()
				: Features.byCategory(selected).size();
		String sub = count + (count == 1 ? " Eintrag" : " Eintraege");
		graphics.drawString(this.font, sub, rightEdge - this.font.width(sub), top + 45,
				DJColors.TEXT_FAINT, false);

		graphics.fill(contentX, top + 56, rightEdge, top + 57, DJColors.OUTLINE_SOFT);
	}

	private void drawViewInfo(GuiGraphics graphics, int contentX, int top) {
		boolean seen = DJHubClient.particleMixinSeen;

		graphics.drawString(this.font,
				seen ? "Partikel-Filter: erkannt" : "Partikel-Filter: noch nicht erkannt",
				contentX, top + 176, seen ? DJColors.ON : DJColors.TEXT_FAINT, false);
		graphics.drawString(this.font, "Erkennung greift beim ersten Blockstaub.",
				contentX, top + 188, DJColors.TEXT_FAINT, false);
	}

	private void drawTradeInfo(GuiGraphics graphics, int contentX, int top) {
		graphics.drawString(this.font, "Ausgewaehlt: " + AutoTrade.selectionText(),
				contentX, top + 118, AutoTrade.hasSelection() ? DJColors.ON : DJColors.TEXT_DIM, false);
		graphics.drawString(this.font, "Item in die Hand nehmen und uebernehmen,",
				contentX, top + 184, DJColors.TEXT_FAINT, false);
		graphics.drawString(this.font, "oder oben einen Namensteil eintippen.",
				contentX, top + 196, DJColors.TEXT_FAINT, false);
	}

	private void drawChatInfo(GuiGraphics graphics, int contentX, int top) {
		graphics.drawString(this.font, "Nachricht (mit / am Anfang = Befehl):",
				contentX, top + 92, DJColors.TEXT_DIM, false);

		String info;
		int color;

		if (!Features.AUTO_CHAT.isEnabled()) {
			info = "Auto Chat ist aus - es wird nichts gesendet.";
			color = DJColors.TEXT_DIM;
		} else if (AutoChat.getMessage().trim().isEmpty()) {
			info = "Keine Nachricht eingegeben.";
			color = DJColors.TEXT_DIM;
		} else if (AutoChat.isPaused()) {
			info = "Pausiert (nicht im Spiel) - " + AutoChat.countdownText() + " offen";
			color = DJColors.TEXT_DIM;
		} else {
			info = "Naechste Nachricht in " + AutoChat.countdownText();
			color = DJColors.ON;
		}

		graphics.drawString(this.font, info, contentX, top + 160, color, false);
		graphics.drawString(this.font, "Serverwechsel unterbricht den Timer nicht.",
				contentX, top + 174, DJColors.TEXT_FAINT, false);
	}

	@Override
	public void onClose() {
		DJConfig.save();
		super.onClose();
	}
}
