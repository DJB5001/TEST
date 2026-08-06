package de.djhub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/**
 * Zeigt eine Tastenbelegung an. Klick -> naechster Tastendruck wird uebernommen.
 */
public class KeybindWidget extends AbstractButton {

	public final Hotkey hotkey;

	private final Runnable onStartListening;

	private boolean listening;
	private float hoverAnim;
	private float listenAnim;

	public KeybindWidget(int x, int y, int width, int height, Hotkey hotkey, Runnable onStartListening) {
		super(x, y, width, height, Component.literal(hotkey.name));
		this.hotkey = hotkey;
		this.onStartListening = onStartListening;
	}

	public boolean isListening() {
		return this.listening;
	}

	public void setListening(boolean listening) {
		this.listening = listening;
	}

	@Override
	protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		Minecraft minecraft = Minecraft.getInstance();

		int x = this.getX();
		int y = this.getY();
		int w = this.width;
		int h = this.height;

		float speed = Math.min(1.0F, delta * 0.3F + 0.13F);
		this.hoverAnim += ((this.isHovered() ? 1.0F : 0.0F) - this.hoverAnim) * speed;
		this.listenAnim += ((this.listening ? 1.0F : 0.0F) - this.listenAnim) * speed;

		graphics.fill(x, y, x + w, y + h, DJColors.PANEL);
		DJColors.rect(graphics, x, y, w, h,
				DJColors.blend(DJColors.ROW, DJColors.ROW_HOVER, this.hoverAnim * 0.8F));

		if (this.listenAnim > 0.02F) {
			DJColors.outline(graphics, x, y, w, h,
					DJColors.alpha(DJColors.ACCENT, (int) (200 * this.listenAnim)));
		}

		graphics.drawString(minecraft.font, this.getMessage(), x + 11, y + (h - 8) / 2,
				DJColors.TEXT, false);

		String label = this.listening ? "Taste druecken" : this.hotkey.getKeyLabel();
		int boxW = Math.max(58, minecraft.font.width(label) + 14);
		int boxH = 15;
		int boxX = x + w - boxW - 8;
		int boxY = y + (h - boxH) / 2;

		int boxColor = DJColors.blend(DJColors.OFF, DJColors.ACCENT, this.listenAnim);
		DJColors.rect(graphics, boxX, boxY, boxW, boxH, boxColor);

		int labelColor = this.listening ? 0xFFFFFFFF
				: (this.hotkey.getKey() < 0 ? DJColors.TEXT_FAINT : DJColors.TEXT);
		graphics.drawString(minecraft.font, label,
				boxX + (boxW - minecraft.font.width(label)) / 2, boxY + 4, labelColor, false);

		if (this.isHovered()) {
			String tip = this.listening
					? "Taste druecken - ESC loescht die Belegung"
					: this.hotkey.description;
			graphics.setTooltipForNextFrame(minecraft.font, Component.literal(tip), mouseX, mouseY);
		}
	}

	@Override
	public void onPress(InputWithModifiers input) {
		this.onStartListening.run();
		this.listening = true;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}
