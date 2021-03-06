package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.strategies.hitbox.ContactWallDie;
import com.mygdx.hadal.strategies.hitbox.ContactWallSound;
import com.mygdx.hadal.strategies.hitbox.RemoveStrategy;
import com.mygdx.hadal.utils.Stats;

public class CuriousSauce extends Artifact {

	private static final int statusNum = 1;
	private static final int slotCost = 2;
	
	private static final float procCd = 0.25f;
	
	public CuriousSauce() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, final BodyData b) {
		enchantment[0] = new StatusComposite(state, b, 
				new StatChangeStatus(state, Stats.RANGED_PROJ_RESTITUTION, 1.0f, b),
				new Status(state, b) {
			
			private float procCdCount = procCd;

			@Override
			public void timePassing(float delta) {
				if (procCdCount < procCd) {
					procCdCount += delta;
				}
			}
			
			@Override
			public void onHitboxCreation(Hitbox hbox) {
				if (hbox.isEffectsMovement()) {
					hbox.addStrategy(new RemoveStrategy(state, hbox, b, ContactWallDie.class));
					hbox.setSensor(false);

					if (procCdCount >= procCd) {
						procCdCount = 0;
						hbox.addStrategy(new ContactWallSound(state, hbox, b, SoundEffect.SPRING, 0.1f));
					}
				}
			}
		});
		
		return enchantment;
	}
}
