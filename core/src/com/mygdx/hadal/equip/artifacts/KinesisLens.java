package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.strategies.hitbox.AdjustAngle;
import com.mygdx.hadal.strategies.hitbox.HomingMouse;
import com.mygdx.hadal.utils.Stats;

public class KinesisLens extends Artifact {

	private final static int statusNum = 1;
	private final static int slotCost = 1;
	
	private static final float maxLinSpd = 300;
	private static final float maxLinAcc = 2000;
	private static final float maxAngSpd = 270;
	private static final float maxAngAcc = 180;
	
	private static final int boundingRad = 100;
	private static final int decelerationRadius = 0;
	
	private final static float projSpdReduction = -0.5f;
	private final static float bonusProjLifespan = 0.5f;
	
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
				hbox.addStrategy(new HomingMouse(state, hbox, inflicted, maxLinSpd, maxLinAcc, maxAngSpd, maxAngAcc, boundingRad, decelerationRadius));
				hbox.addStrategy(new AdjustAngle(state, hbox, inflicted));
				hbox.setGravity(0.0f);
			}
		});
		
		return enchantment;
	}
}