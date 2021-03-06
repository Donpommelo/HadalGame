package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.strategies.hitbox.HomingMouse;
import com.mygdx.hadal.utils.Stats;

public class KinesisLens extends Artifact {

	private static final int statusNum = 1;
	private static final int slotCost = 2;
	
	private static final float projSpdReduction = -0.4f;
	private static final float bonusProjLifespan = 0.4f;
	private static final float homePower = 120.0f;

	public KinesisLens() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, final BodyData b) {
		enchantment[0] = new StatusComposite(state, b, 
				new StatChangeStatus(state, Stats.RANGED_PROJ_SPD, projSpdReduction, b),
				new StatChangeStatus(state, Stats.RANGED_PROJ_LIFESPAN, bonusProjLifespan, b),
				new Status(state, b) {
			
			@Override
			public void onHitboxCreation(Hitbox hbox) {
				
				if (!hbox.isEffectsMovement()) { return; } 
				
				hbox.addStrategy(new HomingMouse(state, hbox, inflicted, homePower));
				hbox.setGravity(0.0f);
			}
		});
		
		return enchantment;
	}
}
