package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.strategies.hitbox.ContactUnitShock;
import com.mygdx.hadal.strategies.hitbox.CreateParticles;

public class BucketofBatteries extends Artifact {

	private final static int statusNum = 1;
	private final static int slotCost = 3;
	
	private final static float baseDamage = 8.0f;
	private final static int radius = 25;
	private final static int chainAmount = 3;
	
	private final static float procCd = 0.5f;
	
	public BucketofBatteries() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, final BodyData b) {
		enchantment[0] = new StatusComposite(state, b, 
				new Status(state, b) {

			private float procCdCount;
			
			@Override
			public void timePassing(float delta) {
				if (procCdCount < procCd) {
					procCdCount += delta;
				}
			}
			
			@Override
			public void onHitboxCreation(Hitbox hbox) {
				if (procCdCount >= procCd) {
					procCdCount -= procCd;
					
					hbox.addStrategy(new ContactUnitShock(state, hbox, inflicted, baseDamage, radius, chainAmount, inflicted.getSchmuck().getHitboxfilter()));
					hbox.addStrategy(new CreateParticles(state, hbox, inflicted, Particle.LIGHTNING, hbox.getLifeSpan(), 3.0f).setParticleSize(90));
				}
			}
		});
		
		return enchantment;
	}
}
