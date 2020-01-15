package com.mygdx.hadal.equip.actives;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.ActiveItem;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.bodies.hitboxes.RangedHitbox;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.strategies.hitbox.ContactUnitDie;
import com.mygdx.hadal.strategies.hitbox.ControllerDefault;
import com.mygdx.hadal.strategies.hitbox.DamageStandard;
import com.mygdx.hadal.strategies.hitbox.DieExplode;

public class NauticalMine extends ActiveItem {

	private final static float usecd = 0.0f;
	private final static float usedelay = 0.1f;
	private final static float maxCharge = 11.0f;
	
	private final static float baseDamage = 15.0f;
	private final static float knockback = 0.0f;
	private final static Vector2 projectileSize = new Vector2(120, 120);
	private final static float lifespan = 5.0f;
	
	private final static int explosionRadius = 500;
	private final static float explosionDamage = 45.0f;
	private final static float explosionKnockback = 40.0f;
	
	private final static float projectileSpeed = 5.0f;
	
	private final static Sprite projSprite = Sprite.GRENADE;

	public NauticalMine(Schmuck user) {
		super(user, usecd, usedelay, maxCharge, chargeStyle.byTime);
	}
	
	@Override
	public void useItem(PlayState state, PlayerBodyData user) {
		
		Hitbox hbox = new RangedHitbox(state, user.getPlayer().getPixelPosition(), projectileSize, lifespan, this.weaponVelo.scl(projectileSpeed), 
				user.getPlayer().getHitboxfilter(), false, false, user.getPlayer(), projSprite);
		
		hbox.setGravity(0.1f);
		hbox.setRestitution(1.0f);
		
		hbox.addStrategy(new ControllerDefault(state, hbox, user));
		hbox.addStrategy(new ContactUnitDie(state, hbox, user));
		hbox.addStrategy(new DamageStandard(state, hbox, user, baseDamage, knockback, DamageTypes.RANGED));
		hbox.addStrategy(new DieExplode(state, hbox, user, explosionRadius, explosionDamage, explosionKnockback, (short)0));
	}
}
