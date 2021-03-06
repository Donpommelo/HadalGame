package com.mygdx.hadal.equip.ranged;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.RangedWeapon;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity.particleSyncType;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.bodies.hitboxes.RangedHitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.statuses.FiringWeapon;
import com.mygdx.hadal.strategies.hitbox.AdjustAngle;
import com.mygdx.hadal.strategies.hitbox.ContactUnitLoseDurability;
import com.mygdx.hadal.strategies.hitbox.ContactWallDie;
import com.mygdx.hadal.strategies.hitbox.ControllerDefault;
import com.mygdx.hadal.strategies.hitbox.DamageStandard;
import com.mygdx.hadal.strategies.hitbox.DieParticles;

public class ColaCannon extends RangedWeapon {

	private static final int clipSize = 1;
	private static final int ammoSize = 17;
	private static final float shootCd = 0.1f;
	private static final float shootDelay = 0.0f;
	private static final float reloadTime = 2.0f;
	private static final int reloadAmount = 0;
	private static final float baseDamage = 11.0f;
	private static final float recoil = 18.0f;
	private static final float knockback = 7.5f;
	private static final float projectileSpeed = 55.0f;
	private static final Vector2 projectileSize = new Vector2(55, 32);
	private static final float lifespan = 2.0f;

	private static final float procCd = .05f;
	private static final float fireDuration = 2.0f;
	private static final float veloDeprec = 1.0f;
	private static final float minVelo = 10.0f;
	private static final float minDuration = 0.5f;

	private static final Sprite projSprite = Sprite.COLA;
	private static final Sprite weaponSprite = Sprite.MT_SLODGEGUN;
	private static final Sprite eventSprite = Sprite.P_SLODGEGUN;
	
	private static final float maxCharge = 7000.0f;
	private static final float noiseThreshold = 1000.0f;

	private final Vector2 lastMouse = new Vector2();
	private float lastNoise;

	public ColaCannon(Schmuck user) {
		super(user, clipSize, ammoSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, true, weaponSprite, eventSprite, projectileSize.x, maxCharge);
	}
	
	@Override
	public void mouseClicked(float delta, PlayState state, BodyData shooter, short faction, Vector2 mouseLocation) {
		super.mouseClicked(delta, state, shooter, faction, mouseLocation);

		if (reloading || getClipLeft() == 0) { return; }
		
		charging = true;
		
		//While held, gain charge equal to mouse movement from location last update
		if (chargeCd < getChargeTime()) {
			chargeCd += lastMouse.dst(mouseLocation);
			if (chargeCd >= getChargeTime()) {
				chargeCd = getChargeTime();
			}
			
			if (chargeCd > lastNoise + noiseThreshold) {
				lastNoise += noiseThreshold;
				SoundEffect.SHAKE.playUniversal(state, user.getPixelPosition(), 1.0f, false);
				new ParticleEntity(state, new Vector2(user.getPixelPosition()), Particle.COLA_IMPACT, 1.0f, true, particleSyncType.CREATESYNC);
			}
		}
		lastMouse.set(mouseLocation);
	}
	
	@Override
	public void execute(PlayState state, BodyData shooter) {}
	
	@Override
	public void release(PlayState state, BodyData bodyData) {
		
		//when released, spray weapon at mouse. Spray duration and velocity scale to charge
		if (processClip(bodyData)) {
			SoundEffect.POPTAB.playUniversal(state, user.getPixelPosition(), 0.8f, false);

			final float duration = fireDuration * chargeCd / getChargeTime() + minDuration;
			final float velocity = projectileSpeed * chargeCd / getChargeTime() + minVelo;
			
			bodyData.addStatus(new FiringWeapon(state, duration, bodyData, bodyData, velocity, minVelo, veloDeprec, projectileSize.x, procCd, this));
			
			charging = false;
			chargeCd = 0;
			lastNoise = 0;

			setReloadCd(reloadTime - duration);
		}
	}
	
	@Override
	public void fire(PlayState state, Schmuck user, Vector2 startPosition, Vector2 startVelocity, short filter) {
		
		Hitbox hbox = new RangedHitbox(state, startPosition, projectileSize, lifespan, startVelocity, filter, true, true, user, projSprite);
		hbox.setGravity(1.0f);
		
		hbox.addStrategy(new ControllerDefault(state, hbox, user.getBodyData()));
		hbox.addStrategy(new AdjustAngle(state, hbox, user.getBodyData()));
		hbox.addStrategy(new ContactWallDie(state, hbox, user.getBodyData()));
		hbox.addStrategy(new ContactUnitLoseDurability(state, hbox, user.getBodyData()));
		hbox.addStrategy(new DieParticles(state, hbox, user.getBodyData(), Particle.COLA_IMPACT));
		hbox.addStrategy(new DamageStandard(state, hbox, user.getBodyData(), baseDamage, knockback, DamageTypes.COLA, DamageTypes.RANGED));
	}
}
