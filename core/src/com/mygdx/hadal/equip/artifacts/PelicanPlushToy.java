package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.statuses.Status;

public class PelicanPlushToy extends Artifact {

	private static final int statusNum = 1;
	private static final int slotCost = 1;
	
	private final float amount = 1.5f;
	
	public PelicanPlushToy() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new Status(state, b) {
			
			@Override
			public float onHeal(float damage, BodyData perp, DamageTypes... tags) { 
				return damage * amount; 
			}
		};
		return enchantment;
	}
}
