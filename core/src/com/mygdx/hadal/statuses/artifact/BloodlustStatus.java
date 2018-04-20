package com.mygdx.hadal.statuses.artifact;

import com.mygdx.hadal.equip.RangedWeapon;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.Status;

public class BloodlustStatus extends Status {

	private static String name = "Bloodlust";
	private static final float cliprefill = 0.50f;
	
	public BloodlustStatus(PlayState state, BodyData p, BodyData v, int pr) {
		super(state, 0, name, true, false, false, false, p, v, pr);
	}
	
	@Override
	public void onKill(BodyData vic) {
		if (this.vic instanceof PlayerBodyData) {
			if (((PlayerBodyData)this.vic).getCurrentTool() instanceof RangedWeapon) {
				RangedWeapon weapon = (RangedWeapon)((PlayerBodyData)this.vic).getCurrentTool();
				weapon.gainAmmo((int)(weapon.getClipSize() * cliprefill));
			}
		}
	}
	
}
