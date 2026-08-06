package de.djhub;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/**
 * Eine Zeile im Menue: Name links, Schiebeschalter rechts.
 */
public class ToggleWidget extends AbstractButton {

	public final Feature feature;

	private float switchAnim;
	private float hoverAnim;

	public ToggleWidget(int x, int y, int width, int height, Feature feature) {
		super(x, y, width, height, Component.literal(feature.name));
		this.feature = feature;
		this.switchAnim = feature.isEnabled() ? 1.0F : 0.0F;
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
		this.switchAnim += ((this.feature.isEnabled() ? 1.0F : 0.0F) - this.switchAnim) * speed;

		graphics.fill(x, y, x + w, y + h, DJColors.PANEL);

		int base = DJColors.blend(DJColors.ROW, DJColors.ROW_ON, this.switchAnim);
		DJColors.rect(graphics, x, y, w, h, DJColors.blend(base, DJColors.ROW_HOVER, this.hoverAnim * 0.7F));

		if (this.hoverAnim > 0.02F) {
			DJColors.outline(graphics, x, y, w, h,
					DJColors.alpha(DJColors.ACCENT, (int) (60 * this.hoverAnim)));
		}

		// farbiger Balken links, wenn an
		if (this.switchAnim > 0.02F) {
			int barHeight = (int) ((h - 8) * this.switchAnim) + 2;
			graphics.fill(x, y + (h - barHeight) / 2, x + 2, y + (h + barHeight) / 2, DJColors.ACCENT);
		}

		int textColor = DJColors.blend(DJColors.TEXT_DIM, DJColors.TEXT,
				Math.max(this.switchAnim, this.hoverAnim * 0.6F));
		graphics.drawString(minecraft.font, this.getMessage(), x + 11, y + (h - 8) / 2, textColor, false);

		// Schiebeschalter
		int trackW = 22;
		int trackH = 11;
		int trackX = x + w - trackW - 10;
		int trackY = y + (h - trackH) / 2;

		if (this.switchAnim > 0.05F) {
			DJColors.rect(graphics, trackX - 1, trackY - 1, trackW + 2, trackH + 2,
					DJColors.alpha(DJColors.ON, (int) (70 * this.switchAnim)), 6);
		}

		DJColors.rect(graphics, trackX, trackY, trackW, trackH,
				DJColors.blend(DJColors.OFF, DJColors.ON, this.switchAnim), 5);

		int knob = trackH - 4;
		int knobX = trackX + 2 + (int) ((trackW - trackH) * this.switchAnim);
		DJColors.rect(graphics, knobX, trackY + 2, knob, knob,
				DJColors.blend(0xFFC9C9DA, DJColors.KNOB, this.switchAnim), knob / 2);

		if (this.isHovered()) {
			graphics.setTooltipForNextFrame(minecraft.font,
					Component.literal(this.feature.description), mouseX, mouseY);
		}
	}

	@Override
	public void onPress(InputWithModifiers input) {
		this.feature.toggle();
		DJConfig.save();

		if (Features.CHAT_FEEDBACK.isEnabled()) {
			Minecraft minecraft = Minecraft.getInstance();

			if (minecraft.player != null) {
				minecraft.player.displayClientMessage(
						Component.literal("[DJ HUB] ").withStyle(ChatFormatting.LIGHT_PURPLE)
								.append(Component.literal(this.feature.name + " ").withStyle(ChatFormatting.WHITE))
								.append(this.feature.isEnabled()
										? Component.literal("AN").withStyle(ChatFormatting.GREEN)
										: Component.literal("AUS").withStyle(ChatFormatting.RED)),
						false);
			}
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}
}
