package de.djhub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * AutoMiner - portiert aus Nicos eigener Fabric-Mod "AutoMiner v1".
 *
 * - laeuft gerade aus und haelt Linksklick zum Minen
 * - graebt sich treppenartig auf Y -54 runter
 * - weicht Lava und Kies seitlich aus, sonst stehen bleiben
 * - Pendel-Schutz: nach mehreren erfolglosen Versuchen bleibt er stehen
 * - Kamera-Lock (Pitch) ueber Mixin, Auto-Zentrieren auf 90-Grad-Schritte
 */
public final class AutoMiner {

	/** Wird vom EntityMixin gelesen. */
	public static volatile boolean pitchLockActive = false;

	private static final int TARGET_Y = -54;
	private static final float DESCEND_PITCH = 45.0F;

	private static final int SCAN_RANGE = 4;
	private static final int MAX_ESCAPE = 5;
	private static final double DODGE_DISTANCE = 0.85D;
	private static final int DODGE_MAX_TICKS = 25;

	private static final int BLOCKED_LIMIT = 8;
	private static final double POS_EPSILON = 0.03D;

	private static final int FOCUS_DELAY = 40;
	private static final float MOUSE_EPSILON = 0.05F;
	private static final float FOCUS_SMOOTHING = 0.25F;
	private static final float FOCUS_MAX_STEP = 3.0F;
	private static final float SNAP_EPSILON = 0.1F;

	private static boolean wasRunning;
	private static boolean wasDescending;

	private static Direction lastFacingDir;
	private static double bestForward;
	private static boolean haveBestForward;
	private static int escapeAttempts;

	private static Direction lastDodgeSide;
	private static boolean dodging;
	private static Direction dodgeDir;
	private static boolean dodgeSneak;
	private static double dodgeStartX;
	private static double dodgeStartZ;
	private static int dodgeTicks;

	private static boolean haveLastPos;
	private static double lastX;
	private static double lastZ;
	private static int blockedTicks;

	private static boolean haveLastYaw;
	private static float lastYawSeen;
	private static int mouseIdleTicks;

	private AutoMiner() {
	}

	// ---------------------------------------------------------------- Tick

	public static void tick(Minecraft minecraft) {
		if (minecraft == null || minecraft.options == null) {
			return;
		}

		// Tasten
		if (Hotkeys.MINER.consumePress()) {
			Features.MINER.toggle();
		}

		if (Hotkeys.MINER_LOCK.consumePress() && Features.MINER.isEnabled()) {
			Features.MINER_PITCH_LOCK.toggle();
			DJConfig.save();
			DJChat.status(minecraft, "Kamera-Lock", Features.MINER_PITCH_LOCK.isEnabled());
		}

		boolean running = Features.MINER.isEnabled();

		// Zustandswechsel (Taste oder Menue)
		if (running != wasRunning) {
			wasRunning = running;

			if (running) {
				if (Features.MINER_PITCH_LOCK.isEnabled() && minecraft.player != null) {
					minecraft.player.setXRot(0.0F);
				}
			} else {
				releaseKeys(minecraft);
				dodging = false;
			}

			resetFocus();
			resetMovement();
			escapeAttempts = 0;
			haveBestForward = false;
			DJChat.status(minecraft, "AutoMiner", running);
		}

		pitchLockActive = running && Features.MINER_PITCH_LOCK.isEnabled();

		if (!running) {
			return;
		}

		LocalPlayer player = minecraft.player;
		ClientLevel level = minecraft.level;

		if (player == null || level == null) {
			return;
		}

		if (minecraft.screen != null) {
			releaseKeys(minecraft);
			dodging = false;
			resetFocus();
			resetMovement();
			return;
		}

		// Notfall-Absicherung, falls das Mixin nicht greift
		if (pitchLockActive && !wasDescending && Math.abs(player.getXRot()) > 0.4F) {
			player.setXRot(0.0F);
		}

		updateForwardProgress(player);

		if (dodging) {
			continueDodge(minecraft, player, level);
			return;
		}

		boolean lava = lavaAhead(player, level);
		boolean gravel = !lava && gravelInCrosshair(minecraft, level);

		if (lava) {
			avoidObstacle(minecraft, player, level, true);
			return;
		}

		if (gravel) {
			avoidObstacle(minecraft, player, level, false);
			return;
		}

		// Zielhoehe
		boolean descending = Features.MINER_DESCEND.isEnabled()
				&& (int) Math.floor(player.getY()) > TARGET_Y;

		if (descending) {
			player.setXRot(DESCEND_PITCH);
		} else if (wasDescending) {
			player.setXRot(0.0F);
		}

		wasDescending = descending;

		// vor + minen
		minecraft.options.keyUp.setDown(true);
		minecraft.options.keyAttack.setDown(true);
		minecraft.options.keyLeft.setDown(false);
		minecraft.options.keyRight.setDown(false);
		minecraft.options.keyShift.setDown(false);

		if (!descending && Features.MINER_CENTER.isEnabled()) {
			handleFocus(player);
		} else {
			resetFocus();
		}

		// Blockiert? (nicht zaehlen, solange er einen Block abbaut)
		boolean breaking = minecraft.hitResult != null
				&& minecraft.hitResult.getType() == HitResult.Type.BLOCK
				&& minecraft.options.keyAttack.isDown();
		boolean moved = updateMovement(player);

		if (moved) {
			blockedTicks = 0;
		} else if (!breaking) {
			blockedTicks++;

			if (blockedTicks >= BLOCKED_LIMIT) {
				avoidObstacle(minecraft, player, level, false);
			}
		} else {
			blockedTicks = 0;
		}
	}

	// ---------------------------------------------------------------- Hindernisse

	private static void avoidObstacle(Minecraft minecraft, LocalPlayer player, ClientLevel level, boolean sneak) {
		minecraft.options.keyUp.setDown(false);
		minecraft.options.keyAttack.setDown(false);
		resetFocus();

		if (!Features.MINER_DODGE.isEnabled() || escapeAttempts >= MAX_ESCAPE) {
			standStill(minecraft, sneak);
			return;
		}

		Direction facing = player.getDirection();
		Direction right = facing.getClockWise();
		Direction left = facing.getCounterClockWise();

		int rightD = clearLaneDistance(player, level, right, SCAN_RANGE);
		int leftD = clearLaneDistance(player, level, left, SCAN_RANGE);

		Direction choice;

		if (rightD > 0 && leftD > 0) {
			if (rightD < leftD) {
				choice = right;
			} else if (leftD < rightD) {
				choice = left;
			} else {
				choice = lastDodgeSide != null ? lastDodgeSide : right;
			}
		} else if (rightD > 0) {
			choice = right;
		} else if (leftD > 0) {
			choice = left;
		} else {
			choice = null;
		}

		if (choice != null) {
			startDodge(minecraft, player, choice, sneak);
		} else {
			standStill(minecraft, sneak);
		}
	}

	private static void standStill(Minecraft minecraft, boolean sneak) {
		minecraft.options.keyUp.setDown(false);
		minecraft.options.keyAttack.setDown(false);
		minecraft.options.keyLeft.setDown(false);
		minecraft.options.keyRight.setDown(false);
		minecraft.options.keyShift.setDown(sneak);
		dodging = false;
		resetMovement();
	}

	private static void startDodge(Minecraft minecraft, LocalPlayer player, Direction choice, boolean sneak) {
		dodgeDir = choice;
		dodgeSneak = sneak;
		dodgeStartX = player.getX();
		dodgeStartZ = player.getZ();
		dodgeTicks = 0;
		dodging = true;
		lastDodgeSide = choice;
		escapeAttempts++;
		minecraft.options.keyShift.setDown(sneak);
		pressStrafe(minecraft, player, choice);
		resetMovement();
	}

	private static void continueDodge(Minecraft minecraft, LocalPlayer player, ClientLevel level) {
		minecraft.options.keyUp.setDown(false);
		minecraft.options.keyAttack.setDown(false);
		minecraft.options.keyShift.setDown(dodgeSneak);
		pressStrafe(minecraft, player, dodgeDir);
		dodgeTicks++;

		double dx = player.getX() - dodgeStartX;
		double dz = player.getZ() - dodgeStartZ;
		double moved = Math.sqrt(dx * dx + dz * dz);

		boolean clearAhead = !lavaAhead(player, level)
				&& !gravelInCrosshair(minecraft, level)
				&& canStepForward(player, level);

		if (clearAhead || moved >= DODGE_DISTANCE || dodgeTicks >= DODGE_MAX_TICKS) {
			dodging = false;
			minecraft.options.keyLeft.setDown(false);
			minecraft.options.keyRight.setDown(false);
			minecraft.options.keyShift.setDown(false);
			resetMovement();
		}
	}

	private static void pressStrafe(Minecraft minecraft, LocalPlayer player, Direction dir) {
		Direction facing = player.getDirection();

		if (dir == facing.getClockWise()) {
			minecraft.options.keyRight.setDown(true);
			minecraft.options.keyLeft.setDown(false);
		} else {
			minecraft.options.keyLeft.setDown(true);
			minecraft.options.keyRight.setDown(false);
		}
	}

	private static boolean canStepForward(LocalPlayer player, ClientLevel level) {
		BlockPos foot = player.blockPosition().relative(player.getDirection());
		return level.getBlockState(foot).getCollisionShape(level, foot).isEmpty();
	}

	private static int clearLaneDistance(LocalPlayer player, ClientLevel level, Direction side, int range) {
		BlockPos base = player.blockPosition();
		Direction facing = player.getDirection();

		for (int d = 1; d <= range; d++) {
			boolean pathOk = true;

			for (int s = 1; s <= d && pathOk; s++) {
				BlockPos step = base.relative(side, s);

				for (int dy = 0; dy <= 1; dy++) {
					BlockPos p = step.above(dy);

					if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
						pathOk = false;
						break;
					}
				}

				if (!pathOk) {
					break;
				}

				BlockPos floor = step.below();

				if (level.getBlockState(floor).getCollisionShape(level, floor).isEmpty()) {
					pathOk = false;
					break;
				}

				for (int dy = -1; dy <= 2; dy++) {
					if (level.getFluidState(step.above(dy)).is(FluidTags.LAVA)) {
						pathOk = false;
						break;
					}
				}
			}

			if (!pathOk) {
				continue;
			}

			BlockPos destFront = base.relative(side, d).relative(facing);
			boolean destClear = true;

			for (int dy = 0; dy <= 1; dy++) {
				BlockPos p = destFront.above(dy);

				if (level.getBlockState(p).getBlock() instanceof FallingBlock) {
					destClear = false;
					break;
				}

				if (level.getFluidState(p).is(FluidTags.LAVA)) {
					destClear = false;
					break;
				}
			}

			if (destClear) {
				return d;
			}
		}

		return 0;
	}

	// ---------------------------------------------------------------- Erkennung

	private static boolean lavaAhead(LocalPlayer player, ClientLevel level) {
		BlockPos base = player.blockPosition().relative(player.getDirection());

		for (int dy = -1; dy <= 1; dy++) {
			if (level.getFluidState(base.above(dy)).is(FluidTags.LAVA)) {
				return true;
			}
		}

		return false;
	}

	private static boolean gravelInCrosshair(Minecraft minecraft, ClientLevel level) {
		HitResult hit = minecraft.hitResult;

		if (hit != null && hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult) {
			BlockHitResult blockHit = (BlockHitResult) hit;
			BlockState state = level.getBlockState(blockHit.getBlockPos());
			return state.getBlock() instanceof FallingBlock;
		}

		return false;
	}

	private static void updateForwardProgress(LocalPlayer player) {
		Direction facing = player.getDirection();

		if (lastFacingDir == null || facing != lastFacingDir) {
			lastFacingDir = facing;
			haveBestForward = false;
			escapeAttempts = 0;
		}

		double forward = player.getX() * facing.getStepX() + player.getZ() * facing.getStepZ();

		if (!haveBestForward) {
			bestForward = forward;
			haveBestForward = true;
		} else if (forward > bestForward + 0.05D) {
			bestForward = forward;
			escapeAttempts = 0;
		}
	}

	private static boolean updateMovement(LocalPlayer player) {
		double x = player.getX();
		double z = player.getZ();

		if (!haveLastPos) {
			lastX = x;
			lastZ = z;
			haveLastPos = true;
			return true;
		}

		double dx = x - lastX;
		double dz = z - lastZ;
		boolean moved = (dx * dx + dz * dz) >= (POS_EPSILON * POS_EPSILON);
		lastX = x;
		lastZ = z;
		return moved;
	}

	private static void resetMovement() {
		haveLastPos = false;
		blockedTicks = 0;
	}

	// ---------------------------------------------------------------- Auto-Zentrieren

	private static void handleFocus(LocalPlayer player) {
		float yawNow = player.getYRot();

		if (!haveLastYaw) {
			lastYawSeen = yawNow;
			haveLastYaw = true;
			mouseIdleTicks = 0;
			return;
		}

		float drift = wrapDegrees(yawNow - lastYawSeen);

		if (Math.abs(drift) > MOUSE_EPSILON) {
			mouseIdleTicks = 0;
		} else {
			mouseIdleTicks++;

			if (mouseIdleTicks >= FOCUS_DELAY) {
				float target = Math.round(yawNow / 90.0F) * 90.0F;
				float diff = wrapDegrees(target - yawNow);

				if (Math.abs(diff) < SNAP_EPSILON) {
					player.setYRot(target);
				} else {
					float step = diff * FOCUS_SMOOTHING;

					if (Math.abs(step) > FOCUS_MAX_STEP) {
						step = Math.signum(diff) * FOCUS_MAX_STEP;
					}

					player.setYRot(yawNow + step);
				}
			}
		}

		lastYawSeen = player.getYRot();
	}

	private static void resetFocus() {
		haveLastYaw = false;
		mouseIdleTicks = 0;
	}

	/** Eigene Variante, damit kein Mapping-abhaengiger Helfer noetig ist. */
	private static float wrapDegrees(float degrees) {
		float result = degrees % 360.0F;

		if (result >= 180.0F) {
			result -= 360.0F;
		}

		if (result < -180.0F) {
			result += 360.0F;
		}

		return result;
	}

	// ---------------------------------------------------------------- Hilfen

	private static void releaseKeys(Minecraft minecraft) {
		minecraft.options.keyUp.setDown(false);
		minecraft.options.keyAttack.setDown(false);
		minecraft.options.keyLeft.setDown(false);
		minecraft.options.keyRight.setDown(false);
		minecraft.options.keyShift.setDown(false);
	}

}
