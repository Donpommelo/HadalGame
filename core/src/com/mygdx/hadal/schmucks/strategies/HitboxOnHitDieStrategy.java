package com.mygdx.hadal.schmucks.strategies;

import com.mygdx.hadal.schmucks.UserDataTypes;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.states.PlayState;

public class HitboxOnHitDieStrategy extends HitboxStrategy{
	
	public HitboxOnHitDieStrategy(PlayState state, Hitbox proj, BodyData user) {
		super(state, proj, user);
	}
	
	@Override
	public void onHit(HadalData fixB) {
		if (fixB == null) {
			
		} else if (fixB.getType().equals(UserDataTypes.BODY)) {
			hbox.setDura(0);
		}
		if (hbox.getDura() <= 0 && hbox.isAlive()) {
			hbox.die();
		}
	}
}
