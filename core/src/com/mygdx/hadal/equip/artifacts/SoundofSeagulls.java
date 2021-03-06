package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.utils.Stats;

public class SoundofSeagulls extends Artifact {

	private static final int statusNum = 1;
	private static final int slotCost = 1;

	private static final float hoverCostReduction = -0.2f;

	public SoundofSeagulls() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new StatusComposite(state, b, 
				new StatChangeStatus(state, Stats.HOVER_CONTROL, 1.0f, b),
				new StatChangeStatus(state, Stats.HOVER_COST, hoverCostReduction, b));
		return enchantment;
	}
}
