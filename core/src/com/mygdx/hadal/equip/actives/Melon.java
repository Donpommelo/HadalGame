package com.mygdx.hadal.equip.actives;

import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.equip.ActiveItem;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.Regeneration;

/**
 * @author Grurrault Ghineydew
 */
public class Melon extends ActiveItem {

	private static final float usecd = 0.0f;
	private static final float usedelay = 0.2f;
	private static final float maxCharge = 25.0f;
	
	private static final float duration = 8.0f;
	private static final float power = 4.0f;
	
	public Melon(Schmuck user) {
		super(user, usecd, usedelay, maxCharge, chargeStyle.byDamageInflict);
	}
	
	@Override
	public void useItem(PlayState state, PlayerBodyData user) {
		SoundEffect.EATING.playUniversal(state, user.getPlayer().getPixelPosition(), 0.8f, false);

		user.addStatus(new Regeneration(state, duration, user, user, power));
	}
	
	@Override
	public float getUseDuration() { return duration; }
}
