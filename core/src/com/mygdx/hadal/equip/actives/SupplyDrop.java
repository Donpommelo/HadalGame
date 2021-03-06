package com.mygdx.hadal.equip.actives;

import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.equip.ActiveItem;
import com.mygdx.hadal.event.PickupEquip;
import com.mygdx.hadal.save.UnlockEquip;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;

/**
 * @author Preewenhoek Phistein
 */
public class SupplyDrop extends ActiveItem {

	private static final float usecd = 0.0f;
	private static final float usedelay = 0.0f;
	private static final float maxCharge = 10.0f;

	private static final float equipDropLifepan = 12.0f;
	private static final float bonusAtkSpd = 0.4f;

	public SupplyDrop(Schmuck user) {
		super(user, usecd, usedelay, maxCharge, chargeStyle.byTime);
		setCurrentCharge(maxCharge);
	}
	
	@Override
	public void useItem(PlayState state, PlayerBodyData user) {
		SoundEffect.MAGIC1_ACTIVE.playUniversal(state, user.getPlayer().getPixelPosition(), 1.0f, false);
		new PickupEquip(state, user.getPlayer().getPixelPosition(), UnlockEquip.getByName(UnlockEquip.getRandWeapFromPool(state, "")), equipDropLifepan);
	}
}
