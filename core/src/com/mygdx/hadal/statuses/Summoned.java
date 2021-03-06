package com.mygdx.hadal.statuses;

import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;

/**
 * Temporary designates a schmuck as a summon that awards a summoner the score/kills it gets
 * @author Lornard Landwich
 */
public class Summoned extends Status {

	//this is the player that summoned this unit (and will get credit for its kills)
	private final Player summoner;
	
	public Summoned(PlayState state, BodyData i, Player summoner) {
		super(state, i);
		this.summoner = summoner;
	}
	
	@Override
	public float onDealDamage(float damage, BodyData vic, DamageTypes... tags) { 
		summoner.playHitSound(damage);
		return damage;	
	}
	
	@Override
	public void onKill(BodyData vic) {
		if (vic.getSchmuck() instanceof Player) {
			HadalGame.server.registerKill(summoner, null);
		}
	}
}
