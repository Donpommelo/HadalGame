package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.statuses.Status;

public class CursedCilice extends Artifact {

	private final static int statusNum = 1;
	private final static int slotCost = 1;
	
	private final float amount = 0.5f;
	
	public CursedCilice() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new Status(state, b) {
			
			@Override
			public float onReceiveDamage(float damage, BodyData perp, DamageTypes... tags) {
				if (inflicted instanceof PlayerBodyData && damage > 0) {
					((PlayerBodyData)inflicted).fuelGain(damage * amount);
				}
				return damage;
			}
		};
		return enchantment;
	}
}
