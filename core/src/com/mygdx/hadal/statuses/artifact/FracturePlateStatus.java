package com.mygdx.hadal.statuses.artifact;

import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.statuses.Status;

public class FracturePlateStatus extends Status {

	private static String name = "Fractured Plate";
	private float procCdCount;
	private float cd = 8.0f;
	
	public FracturePlateStatus(PlayState state, BodyData p, BodyData v, int pr) {
		super(state, 0, name, true, false, false, false, p, v, pr);
		this.procCdCount = 0;
	}
	
	@Override
	public void timePassing(float delta) {
		procCdCount -= delta;
	}
	
	@Override
	public float onReceiveDamage(float damage, BodyData perp, DamageTypes... tags) {
		if (damage > 0 && procCdCount <= 0) {
			procCdCount = cd;
			damage = 0;
		}
		return damage;
	}

}
