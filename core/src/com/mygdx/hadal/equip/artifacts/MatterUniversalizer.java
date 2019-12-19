package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity.particleSyncType;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.Status;

public class MatterUniversalizer extends Artifact {

	private final static String name = "Matter Universalizer";
	private final static String descr = "Regenerates Fuel on kill.";
	private final static String descrLong = "";
	private final static int statusNum = 1;
	
	private final float amount = 30.f;
	private final float particleDura = 1.5f;
	
	public MatterUniversalizer() {
		super(name, descr, descrLong, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new Status(state, name, descr, b) {

			
			@Override
			public void onKill(BodyData vic) {
				new ParticleEntity(state, inflicted.getSchmuck(), Particle.PICKUP_ENERGY, 0.0f, particleDura, true, particleSyncType.TICKSYNC);
				((PlayerBodyData)inflicted).fuelGain(amount);
			}
		};
		return enchantment;
	}
}
