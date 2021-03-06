package com.mygdx.hadal.equip.ranged;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.RangedWeapon;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.bodies.hitboxes.RangedHitbox;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.strategies.hitbox.*;

public class SniperRifle extends RangedWeapon {

	private static final int clipSize = 1;
	private static final int ammoSize = 21;
	private static final float shootCd = 0.5f;
	private static final float shootDelay = 0.0f;
	private static final float reloadTime = 1.3f;
	private static final int reloadAmount = 0;
	private static final float baseDamage = 55.0f;
	private static final float recoil = 15.0f;
	private static final float knockback = 45.0f;
	private static final float projectileSpeed = 80.0f;
	private static final Vector2 projectileSize = new Vector2(120, 12);
	private static final float lifespan = 1.0f;
	
	private static final Sprite projSprite = Sprite.BULLET;
	private static final Sprite weaponSprite = Sprite.MT_SPEARGUN;
	private static final Sprite eventSprite = Sprite.P_SPEARGUN;

	private static final float bonusDamage = 1.75f;
	private static final float maxCharge = 0.22f;

	public SniperRifle(Schmuck user) {
		super(user, clipSize, ammoSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, true, weaponSprite, eventSprite, projectileSize.x);
	}
	
	@Override
	public void fire(PlayState state, Schmuck user, Vector2 startPosition, Vector2 startVelocity, short filter) {
		SoundEffect.GUN1.playUniversal(state, startPosition, 0.5f, false);

		Hitbox hbox = new RangedHitbox(state, startPosition, projectileSize, lifespan, startVelocity, filter, true, true, user, projSprite);
		hbox.setRestitution(1.0f);
		
		hbox.addStrategy(new ControllerDefault(state, hbox, user.getBodyData()));
		hbox.addStrategy(new AdjustAngle(state, hbox, user.getBodyData()));
		hbox.addStrategy(new ContactWallParticles(state, hbox, user.getBodyData(), Particle.SPARKS));
		hbox.addStrategy(new ContactWallDie(state, hbox, user.getBodyData()));
		hbox.addStrategy(new DamageHeadshot(state, hbox, user.getBodyData(), bonusDamage, maxCharge));
		hbox.addStrategy(new DamageStandard(state, hbox, user.getBodyData(), baseDamage, knockback, DamageTypes.SNIPE, DamageTypes.RANGED));
		hbox.addStrategy(new ContactWallSound(state, hbox, user.getBodyData(), SoundEffect.WALL_HIT1, 0.5f));
		hbox.addStrategy(new CreateParticles(state, hbox, user.getBodyData(), Particle.BULLET_TRAIL, 0.0f, 0.5f).setRotate(true));

	}
}
