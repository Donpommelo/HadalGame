package com.mygdx.hadal.statuses.artifact;

import com.mygdx.hadal.equip.Equipable;
import com.mygdx.hadal.equip.misc.GrenadeDropTest;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.Status;

public class AnarchistCookbookStatus extends Status {

	private static String name = "Cookin' with Explosives";
	private float procCdCount;
	private float procCd = .5f;

	private Equipable weapon;
	
	public AnarchistCookbookStatus(PlayState state, int i, BodyData p, BodyData v, int pr) {
		super(state, i, name, false, false, true, true, p, v, pr);
		weapon = new GrenadeDropTest(perp.getSchmuck());
		this.procCdCount = 0;
	}
	
	public AnarchistCookbookStatus(PlayState state, BodyData p, BodyData v, int pr) {
		super(state, 0, name, true, false, false, false, p, v, pr);
		weapon = new GrenadeDropTest(perp.getSchmuck());
		this.procCdCount = 0;
	}
	
	@Override
	public void timePassing(float delta) {
		if (procCdCount >= procCd) {
			procCdCount -= procCd;
			vic.getSchmuck().useToolStart(delta, weapon, (short) vic.getSchmuck().getHitboxfilter(), (int)0, (int)0, false);
			weapon.reload(delta);
		}
		procCdCount += delta;
	}
	
}
