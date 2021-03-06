package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.utils.Stats;

public class NiceShoes extends Artifact {

	private static final int statusNum = 1;
	private static final int slotCost = 2;
	
	private static final float bonusSpd = 0.25f;
	private static final float bonusAccel = 0.25f;
	
	public NiceShoes() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new StatusComposite(state, b, 
				new StatChangeStatus(state, Stats.GROUND_SPD, bonusSpd, b), 
				new StatChangeStatus(state, Stats.GROUND_ACCEL, bonusAccel, b));
		return enchantment;
	}
}
