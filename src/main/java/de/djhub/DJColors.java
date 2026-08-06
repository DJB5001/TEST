package de.djhub;

import net.minecraft.client.gui.GuiGraphics;

public final class DJColors {

	public static final int BACKDROP = 0xCC05050B;

	public static final int PANEL = 0xFF15151F;
	public static final int SIDEBAR = 0xFF101019;
	public static final int HEADER = 0xFF1C1C2C;
	public static final int OUTLINE = 0xFF2E2E46;
	public static final int OUTLINE_SOFT = 0xFF232334;

	public static final int ROW = 0xFF1D1D2B;
	public static final int ROW_HOVER = 0xFF282839;
	public static final int ROW_ON = 0xFF242038;

	public static final int ACCENT = 0xFF7C5CFF;
	public static final int ACCENT_LIGHT = 0xFF9B7BFF;
	public static final int ACCENT_SOFT = 0xFF2B2349;
	public static final int CYAN = 0xFF22D3EE;

	public static final int ON = 0xFF34D399;
	public static final int OFF = 0xFF3A3A52;
	public static final int KNOB = 0xFFF4F4FA;

	public static final int TEXT = 0xFFEAEAF3;
	public static final int TEXT_DIM = 0xFF8B8BA9;
	public static final int TEXT_FAINT = 0xFF61617A;

	/** Farbtupfer pro Kategorie - Reihenfolge wie in Features.CATEGORIES. */
	public static final int[] CATEGORY_DOTS = {
			0xFF7C5CFF, 0xFF22D3EE, 0xFFF59E0B, 0xFF34D399,
			0xFF60A5FA, 0xFFF472B6, 0xFF94A3B8, 0xFFFBBF24
	};

	private DJColors() {
	}

	// ---------------------------------------------------------------- Formen

	public static void rect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		rect(graphics, x, y, width, height, color, Math.min(3, Math.min(width, height) / 4));
	}

	/** Rechteck mit abgerundeten Ecken. */
	public static void rect(GuiGraphics graphics, int x, int y, int width, int height, int color, int radius) {
		if (width <= 0 || height <= 0) {
			return;
		}

		int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));

		if (r == 0) {
			graphics.fill(x, y, x + width, y + height, color);
			return;
		}

		graphics.fill(x + r, y, x + width - r, y + r, color);
		graphics.fill(x, y + r, x + width, y + height - r, color);
		graphics.fill(x + r, y + height - r, x + width - r, y + height, color);

		if (r >= 2) {
			graphics.fill(x + 1, y + 1, x + r, y + r, color);
			graphics.fill(x + width - r, y + 1, x + width - 1, y + r, color);
			graphics.fill(x + 1, y + height - r, x + r, y + height - 1, color);
			graphics.fill(x + width - r, y + height - r, x + width - 1, y + height - 1, color);
		}
	}

	public static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		if (width <= 2 || height <= 2) {
			return;
		}

		graphics.fill(x + 3, y, x + width - 3, y + 1, color);
		graphics.fill(x + 3, y + height - 1, x + width - 3, y + height, color);
		graphics.fill(x, y + 3, x + 1, y + height - 3, color);
		graphics.fill(x + width - 1, y + 3, x + width, y + height - 3, color);
		graphics.fill(x + 1, y + 1, x + 3, y + 3, color);
		graphics.fill(x + width - 3, y + 1, x + width - 1, y + 3, color);
		graphics.fill(x + 1, y + height - 3, x + 3, y + height - 1, color);
		graphics.fill(x + width - 3, y + height - 3, x + width - 1, y + height - 1, color);
	}

	/** Waagerechter Farbverlauf, in Stufen gezeichnet. */
	public static void gradientX(GuiGraphics graphics, int x, int y, int width, int height, int from, int to) {
		int steps = Math.max(2, width / 3);

		for (int i = 0; i < steps; i++) {
			int x1 = x + (int) ((long) width * i / steps);
			int x2 = x + (int) ((long) width * (i + 1) / steps);

			if (x2 > x1) {
				graphics.fill(x1, y, x2, y + height, blend(from, to, (float) i / (steps - 1)));
			}
		}
	}

	/** Weicher Schatten hinter dem Panel. */
	public static void shadow(GuiGraphics graphics, int x, int y, int width, int height) {
		rect(graphics, x - 4, y - 3, width + 8, height + 8, 0x14000000, 5);
		rect(graphics, x - 3, y - 2, width + 6, height + 7, 0x1C000000, 5);
		rect(graphics, x - 2, y - 1, width + 4, height + 6, 0x24000000, 4);
	}

	/** Kleiner runder Farbpunkt. */
	public static void dot(GuiGraphics graphics, int x, int y, int size, int color) {
		rect(graphics, x, y, size, size, color, size / 2);
	}

	// ---------------------------------------------------------------- Farben

	public static int blend(int from, int to, float progress) {
		float p = Math.max(0.0F, Math.min(1.0F, progress));
		int a = mix((from >> 24) & 0xFF, (to >> 24) & 0xFF, p);
		int r = mix((from >> 16) & 0xFF, (to >> 16) & 0xFF, p);
		int g = mix((from >> 8) & 0xFF, (to >> 8) & 0xFF, p);
		int b = mix(from & 0xFF, to & 0xFF, p);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public static int alpha(int color, int alpha) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
	}

	private static int mix(int from, int to, float p) {
		return (int) (from + (to - from) * p);
	}
}
