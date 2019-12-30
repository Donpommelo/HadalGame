package com.mygdx.hadal.equip.actives;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.equip.ActiveItem;
import com.mygdx.hadal.event.Poison;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;

public class TaintedWater extends ActiveItem {

	private final static float usecd = 0.0f;
	private final static float usedelay = 0.0f;
	private final static float maxCharge = 10.0f;
	
	private final static Vector2 poisonSize = new Vector2(300, 300);
	private final static float poisonDamage = 30/60f;
	private final static float poisonDuration = 4.0f;
	
	public TaintedWater(Schmuck user) {
		super(user, usecd, usedelay, maxCharge, chargeStyle.byTime);
	}
	
	@Override
	public void useItem(PlayState state, PlayerBodyData user) {		
		new Poison(state, user.getSchmuck().getPixelPosition(), poisonSize, poisonDamage, poisonDuration, user.getSchmuck(), true, user.getSchmuck().getHitboxfilter());
	}
}
