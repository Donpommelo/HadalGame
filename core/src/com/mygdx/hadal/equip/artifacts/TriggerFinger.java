package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;

public class TriggerFinger extends Artifact {

	private final static String name = "Trigger Finger";
	private final static String descr = "+30% Ranged Attack Speed";
	private final static String descrLong = "";
	private final static int statusNum = 1;
	
	public TriggerFinger() {
		super(name, descr, descrLong, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new StatChangeStatus(state, 27, 0.25f, b, b, 50);
		return enchantment;
	}
}
