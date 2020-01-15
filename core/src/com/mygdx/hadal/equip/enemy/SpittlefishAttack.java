package com.mygdx.hadal.equip.enemy;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.RangedWeapon;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.bodies.hitboxes.RangedHitbox;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.strategies.hitbox.ContactUnitLoseDurability;
import com.mygdx.hadal.strategies.hitbox.ContactWallDie;
import com.mygdx.hadal.strategies.hitbox.ControllerDefault;
import com.mygdx.hadal.strategies.hitbox.DamageStandard;

public class SpittlefishAttack extends RangedWeapon {

	private final static int clipSize = 12;
	private final static int ammoSize = 1000;
	private final static float shootCd = 0.6f;
	private final static float shootDelay = 0;
	private final static float reloadTime = 0.5f;
	private final static int reloadAmount = 6;
	private final static float baseDamage = 7.0f;
	private final static float recoil = 0.0f;
	private final static float knockback = 4.5f;
	private final static float projectileSpeed = 12.0f;
	private final static Vector2 projectileSize = new Vector2(30, 20);
	private final static float lifespan = 5.0f;
	
	private final static Sprite projSprite = Sprite.SPIT;
	
	public SpittlefishAttack(Schmuck user) {
		super(user, clipSize, ammoSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, true, Sprite.MT_DEFAULT, Sprite.P_DEFAULT, projectileSize.x);
	}

	@Override
	public void fire(PlayState state, Schmuck user, Vector2 startPosition, Vector2 startVelocity, short filter) {
		Hitbox hbox = new RangedHitbox(state, startPosition, projectileSize, lifespan, startVelocity,	filter, true, true, user, projSprite);
		
		hbox.addStrategy(new ControllerDefault(state, hbox, user.getBodyData()));
		hbox.addStrategy(new ContactUnitLoseDurability(state, hbox, user.getBodyData()));
		hbox.addStrategy(new ContactWallDie(state, hbox, user.getBodyData()));
		hbox.addStrategy(new DamageStandard(state, hbox, user.getBodyData(), baseDamage, knockback, DamageTypes.RANGED));	
	}
}
