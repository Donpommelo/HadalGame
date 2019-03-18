package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.Status;

public class BrigglesBladedBoot extends Artifact {

	private final static String name = "Briggle's Bladed Boot";
	private final static String descr = "Damage Enemies by Stompin";
	private final static String descrLong = "";
	private final static int statusNum = 1;
	
	public BrigglesBladedBoot() {
		super(name, descr, descrLong, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new Status(state, name, descr, b) {
			
			@Override
			public void onInflict(Status s) {
				inflicted.getSchmuck().setStomping(true);
			}
			
			@Override
			public void onRemove(Status s) {
				inflicted.getSchmuck().setStomping(false);
			}
		};
		return enchantment;
	}
}