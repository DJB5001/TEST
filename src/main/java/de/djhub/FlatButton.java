package de.djhub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Selbst gezeichneter Button. Der Vanilla-Hintergrund wird komplett uebermalt.
 */
public class FlatButton extends AbstractButton {

	private final Runnable action;
	private final boolean primary;

	private boolean selected;
	private int dotColor;
	private float hoverAnim;
	private float selectAnim;
	private Supplier<Component> dynamicLabel;

	public FlatButton(int x, int y, int width, int height, Component message, Runnable action, boolean primary) {
		super(x, y, width, height, message);
		this.action = action;
		this.primary = primary;
	}

	/** Beschriftung, die sich zur Laufzeit aendert. */
	public FlatButton label(Supplier<Component> supplier) {
		this.dynamicLabel = supplier;
		return this;
	}

	/** Farbtupfer links vom Text (fuer die Kategorien). */
	public FlatButton dot(int color) {
		this.dotColor = color;
		return this;
	}

	public FlatButton select(boolean selected) {
		this.selected = selected;
		return this;
	}

	@Override
	protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		Minecraft minecraft = Minecraft.getInstance();

		int x = this.getX();
		int y = this.getY();
		int w = this.width;
		int h = this.height;

		float speed = Math.min(1.0F, delta * 0.35F + 0.16F);
		this.hoverAnim += ((this.isHovered() ? 1.0F : 0.0F) - this.hoverAnim) * speed;
		this.selectAnim += ((this.selected ? 1.0F : 0.0F) - this.selectAnim) * speed;

		// deckt die Vanilla-Textur vollstaendig ab
		graphics.fill(x, y, x + w, y + h, this.primary ? DJColors.PANEL : DJColors.SIDEBAR);

		int background;
		int textColor;

		if (this.primary) {
			background = DJColors.blend(DJColors.ACCENT, DJColors.ACCENT_LIGHT, this.hoverAnim);
			textColor = 0xFFFFFFFF;
		} else {
			int base = DJColors.blend(DJColors.SIDEBAR, DJColors.ROW_HOVER, this.hoverAnim);
			background = DJColors.blend(base, DJColors.ACCENT_SOFT, this.selectAnim);
			textColor = DJColors.blend(DJColors.TEXT_DIM, DJColors.TEXT,
					Math.max(this.hoverAnim, this.selectAnim));
		}

		DJColors.rect(graphics, x, y, w, h, background);

		if (this.selectAnim > 0.02F) {
			int barHeight = (int) ((h - 8) * this.selectAnim) + 2;
			graphics.fill(x, y + (h - barHeight) / 2, x + 2, y + (h + barHeight) / 2, DJColors.ACCENT);
		}

		int textX = x + 9;

		if (this.dotColor != 0) {
			int shade = this.selected ? this.dotColor
					: DJColors.blend(DJColors.alpha(this.dotColor, 0x90), this.dotColor, this.hoverAnim);
			DJColors.dot(graphics, x + 9, y + (h - 4) / 2, 4, shade);
			textX = x + 18;
		}

		Component label = this.dynamicLabel != null ? this.dynamicLabel.get() : this.getMessage();
		int textY = y + (h - 8) / 2;

		if (this.primary) {
			textX = x + (w - minecraft.font.width(label)) / 2;
		}

		graphics.drawString(minecraft.font, label, textX, textY, textColor, false);
	}

	@Override
	public void onPress(InputWithModifiers input) {
		this.action.run();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}
