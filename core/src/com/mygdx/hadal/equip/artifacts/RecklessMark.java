package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;

public class RecklessMark extends Artifact {

	private final static String name = "Reckless Mark";
	private final static String descr = "Deal and take +40% more damage.";
	private final static String descrLong = "";
	private final static int statusNum = 1;
	
	public RecklessMark() {
		super(name, descr, descrLong, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {		
		enchantment[0] = new StatusComposite(state, name, b, 
				new StatChangeStatus(state, 21, 0.4f, b), 
				new StatChangeStatus(state, 22, -0.4f, b));
		return enchantment;
	}
}
