package de.djhub.mixin;

import de.djhub.Features;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blendet die Partikel beim Abbauen von Bloecken aus.
 *
 * destroy(...) = die Wolke, wenn der Block zerspringt
 * crack(...)   = die Kruemel, die waehrend des Abbauens wegfliegen
 *
 * require/expect = 0: Falls Mojang die Methoden umbenennt, wird der Mixin
 * uebersprungen statt das Spiel abstuerzen zu lassen.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

	@Inject(method = "destroy", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
	private void djhub$noDestroyParticles(CallbackInfo ci) {
		if (Features.NO_BREAK_PARTICLES.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "crack", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
	private void djhub$noCrackParticles(CallbackInfo ci) {
		if (Features.NO_BREAK_PARTICLES.isEnabled()) {
			ci.cancel();
		}
	}
}
