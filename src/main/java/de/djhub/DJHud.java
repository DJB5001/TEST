package de.djhub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Info-Overlay oben am Bildschirmrand.
 */
public final class DJHud {

	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final String[] DIRECTIONS = {"Sued", "West", "Nord", "Ost"};

	private static final int COLOR_LABEL = 0xFF8A8AA8;
	private static final int COLOR_VALUE = 0xFFFFFFFF;

	private static final Deque<Long> CLICKS = new ArrayDeque<>();
	private static boolean attackWasDown;

	private static double lastX;
	private static double lastZ;
	private static double blocksPerSecond;

	private static int frames;
	private static long lastFpsTime = System.currentTimeMillis();
	private static int fps;

	private DJHud() {
	}

	public static void tick(Minecraft minecraft) {
		if (minecraft == null || minecraft.player == null) {
			return;
		}

		boolean down = minecraft.options.keyAttack.isDown();

		if (down && !attackWasDown) {
			CLICKS.addLast(System.currentTimeMillis());
		}

		attackWasDown = down;

		long limit = System.currentTimeMillis() - 1000L;

		while (!CLICKS.isEmpty() && CLICKS.peekFirst() < limit) {
			CLICKS.pollFirst();
		}

		double dx = minecraft.player.getX() - lastX;
		double dz = minecraft.player.getZ() - lastZ;
		lastX = minecraft.player.getX();
		lastZ = minecraft.player.getZ();

		double current = Math.sqrt(dx * dx + dz * dz) * 20.0D;
		blocksPerSecond = blocksPerSecond * 0.8D + current * 0.2D;
	}

	public static void render(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();

		countFrame();

		if (minecraft != null) {
			Hotkeys.pollAll(minecraft);
		}

		if (minecraft == null || minecraft.player == null || minecraft.font == null) {
			return;
		}

		if (minecraft.options.hideGui || minecraft.screen != null) {
			return;
		}

		List<String[]> rows = new ArrayList<>();

		if (Features.HUD_FPS.isEnabled()) {
			rows.add(new String[]{"FPS", Integer.toString(fps)});
		}

		if (Features.HUD_COORDS.isEnabled()) {
			rows.add(new String[]{"XYZ", (int) Math.floor(minecraft.player.getX()) + " "
					+ (int) Math.floor(minecraft.player.getY()) + " "
					+ (int) Math.floor(minecraft.player.getZ())});
		}

		if (Features.HUD_FACING.isEnabled()) {
			int index = (int) Math.floor((minecraft.player.getYRot() * 4.0F / 360.0F) + 0.5D) & 3;
			rows.add(new String[]{"Blick", DIRECTIONS[index]});
		}

		if (Features.HUD_SPEED.isEnabled()) {
			rows.add(new String[]{"Speed", String.format("%.2f b/s", blocksPerSecond)});
		}

		if (Features.HUD_CPS.isEnabled()) {
			rows.add(new String[]{"CPS", Integer.toString(CLICKS.size())});
		}

		if (Features.HUD_TIME.isEnabled()) {
			rows.add(new String[]{"Zeit", LocalTime.now().format(CLOCK)});
		}

		if (rows.isEmpty()) {
			return;
		}

		boolean shadow = Features.HUD_SHADOW.isEnabled();
		boolean right = Features.HUD_RIGHT.isEnabled();

		int lineHeight = 11;
		int padding = 4;
		int widest = 0;

		for (String[] row : rows) {
			int width = minecraft.font.width(row[0] + "  " + row[1]);

			if (width > widest) {
				widest = width;
			}
		}

		int boxWidth = widest + padding * 2;
		int boxHeight = rows.size() * lineHeight + padding * 2 - 2;
		int boxX = right ? graphics.guiWidth() - boxWidth - 4 : 4;
		int boxY = 4;

		if (Features.HUD_BOX.isEnabled()) {
			graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xA0101018);
			graphics.fill(boxX, boxY, boxX + 2, boxY + boxHeight, DJColors.ACCENT);
		}

		int textY = boxY + padding;

		for (String[] row : rows) {
			int labelWidth = minecraft.font.width(row[0] + "  ");
			int startX = boxX + padding;

			if (right) {
				startX = boxX + boxWidth - padding - minecraft.font.width(row[0] + "  " + row[1]);
			}

			graphics.drawString(minecraft.font, row[0], startX, textY, COLOR_LABEL, shadow);
			graphics.drawString(minecraft.font, row[1], startX + labelWidth, textY, COLOR_VALUE, shadow);
			textY += lineHeight;
		}
	}

	private static void countFrame() {
		frames++;
		long now = System.currentTimeMillis();
		long elapsed = now - lastFpsTime;

		if (elapsed >= 500L) {
			fps = (int) (frames * 1000L / elapsed);
			frames = 0;
			lastFpsTime = now;
		}
	}
}
