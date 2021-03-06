package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.utils.Stats;

public class BackpackBuddy extends Artifact {

	private static final int statusNum = 1;
	private static final int slotCost = 0;
	
	private static final float hpReduction = -0.25f;
	private static final int bonusArtifactSlots = 1;
	
	public BackpackBuddy() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new StatusComposite(state, b,
				new StatChangeStatus(state, Stats.MAX_HP_PERCENT, hpReduction, b),
				new StatChangeStatus(state, Stats.ARTIFACT_SLOTS, bonusArtifactSlots, b));
		return enchantment;
	}
}
