package com.mygdx.hadal.equip.artifacts;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.utils.Stats;

public class LampreyIdol extends Artifact {

	private static final int statusNum = 1;
	private static final int slotCost = 2;
	
	private static final float lifestealPlayer = 0.1f;
	private static final float lifestealEnemy = 0.02f;
	private static final float damage = 2.5f;
	private static final float hpThreshold = 0.5f;
	
	public LampreyIdol() {
		super(slotCost, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new StatusComposite(state, b, 
				new Status(state, b) {
			
			private float procCdCount;
			private static final float procCd = 1.0f;
			
			@Override
			public void timePassing(float delta) {
				if (procCdCount >= procCd) {
					procCdCount -= procCd;
					
					if ((inflicter.getCurrentHp() / inflicter.getStat(Stats.MAX_HP)) >= hpThreshold) {
						inflicter.receiveDamage(damage, new Vector2(0, 0), inflicter, true);
					}
				}
				procCdCount += delta;
			}
			
			@Override
			public float onDealDamage(float damage, BodyData vic, DamageTypes... tags) {
				if (vic instanceof PlayerBodyData) {
					inflicter.regainHp(lifestealPlayer * damage, inflicter, true, DamageTypes.LIFESTEAL);
				} else {
					inflicter.regainHp(lifestealEnemy * damage, inflicter, true, DamageTypes.LIFESTEAL);
				}
				return damage;
			}
			
		});
		return enchantment;
	}
}
