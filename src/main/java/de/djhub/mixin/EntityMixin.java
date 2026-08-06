package de.djhub.mixin;

import de.djhub.AutoMiner;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Setzt die vertikale Blickaenderung auf 0, solange der Kamera-Lock aktiv ist.
 * Die horizontale Drehung bleibt unveraendert -> ruckelfreier Pitch-Lock.
 *
 * require/expect = 0: falls Mojang die Methode umbenennt, wird der Mixin einfach
 * uebersprungen statt das Spiel abstuerzen zu lassen. AutoMiner hat dafuer eine
 * Notfall-Absicherung im Tick.
 */
@Mixin(Entity.class)
public class EntityMixin {

	@ModifyVariable(
			method = "turn(DD)V",
			at = @At("HEAD"),
			ordinal = 1,
			argsOnly = true,
			require = 0,
			expect = 0
	)
	private double djhub$lockPitch(double xRotDelta) {
		return AutoMiner.pitchLockActive ? 0.0D : xRotDelta;
	}
}
