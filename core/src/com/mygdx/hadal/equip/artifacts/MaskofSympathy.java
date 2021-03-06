package com.mygdx.hadal.equip.artifacts;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.statuses.Status;

public class MaskofSympathy extends Artifact {

	private static final int statusNum = 1;
	private static final int slotCost = 1;
	
	private static final float amount = 0.5f;
	
	public MaskofSympathy() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new Status(state, b) {

			@Override
			public float onReceiveDamage(float damage, BodyData perp, DamageTypes... tags) {

				if (!perp.equals(inflicted) && damage > 0 && perp instanceof PlayerBodyData) {
					((PlayerBodyData) perp).fuelSpend(damage * amount);
				}
				return damage;
			}
		};
		return enchantment;
	}
}
