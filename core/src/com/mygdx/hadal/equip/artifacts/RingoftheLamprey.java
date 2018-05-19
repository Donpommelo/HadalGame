package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.Lifesteal;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;

public class RingoftheLamprey extends Artifact {

	private final static String name = "Ring of the Lamprey";
	private final static String descr = "3% Lifesteal, -20 Max Hp";
	private final static String descrLong = "";
	private final static int statusNum = 1;
	
	public RingoftheLamprey() {
		super(name, descr, descrLong, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new StatusComposite(state, name, b, 
				new Lifesteal(state, 0.03f, b),
				new StatChangeStatus(state, 0, -25, b));
		return enchantment;
	}
}
