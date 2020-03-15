package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.utils.Stats;

public class CompoundVitreous extends Artifact {

	private final static int statusNum = 1;
	private final static int slotCost = 1;
	
	private final static float hpVisibility = 1.0f;
	
	public CompoundVitreous() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new StatusComposite(state, b, 
				new StatChangeStatus(state, Stats.HEALTH_VISIBILITY, hpVisibility, b));
		return enchantment;
	}
}