package de.djhub.mixin;

import de.djhub.DJHubClient;
import de.djhub.Features;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blockstaub-Partikel ausblenden.
 *
 * Setzt direkt am Partikel an statt an der ParticleEngine: jeder Blockkruemel ist ein
 * TerrainParticle, egal ob er beim Abbauen, beim Sprinten oder per Server-Paket entsteht.
 * Die Konstruktor-Signaturen stammen 1:1 aus den 1.21.11-Quellen.
 *
 * Weil die Mixin-Klasse von SingleQuadParticle erbt, prueft der Compiler alpha und
 * remove() mit - ein falscher Name wuerde hier auffallen statt still zu scheitern.
 */
@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin extends SingleQuadParticle {

	private TerrainParticleMixin() {
		super(null, 0, 0, 0, null);
	}

	@Inject(
			method = {
					"<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
					"<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;)V"
			},
			at = @At("RETURN"),
			require = 0,
			expect = 0
	)
	private void djhub$hideBlockDust(CallbackInfo ci) {
		// Merker fuer die Anzeige im Menue - so ist erkennbar, ob der Mixin greift.
		DJHubClient.particleMixinSeen = true;

		if (Features.NO_BREAK_PARTICLES.isEnabled()) {
			this.alpha = 0.0F;
			this.remove();
		}
	}
}
