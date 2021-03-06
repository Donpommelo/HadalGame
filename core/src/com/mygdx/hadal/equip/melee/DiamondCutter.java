package com.mygdx.hadal.equip.melee;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.MeleeWeapon;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.SoundEntity;
import com.mygdx.hadal.schmucks.bodies.SoundEntity.soundSyncType;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.strategies.HitboxStrategy;
import com.mygdx.hadal.strategies.hitbox.ControllerDefault;
import com.mygdx.hadal.strategies.hitbox.CreateParticles;
import com.mygdx.hadal.strategies.hitbox.DamageStandard;

import static com.mygdx.hadal.utils.Constants.PPM;

public class DiamondCutter extends MeleeWeapon {

	private static final float swingCd = 0.0f;
	private static final float windup = 0.0f;
	
	private static final Vector2 projectileSize = new Vector2(120, 120);
	
	private static final Sprite weaponSprite = Sprite.MT_DEFAULT;
	private static final Sprite eventSprite = Sprite.P_DEFAULT;

	private static final Sprite projSprite = Sprite.BUZZSAW;
	
	private static final float baseDamage = 8.5f;
	private static final float knockback = 0.0f;

	private static final float range = 90.0f;
	private static final float spinSpeed = 8.0f;
	private static final float spinInterval = 1 / 60f;
	
	//this is the hitbox that this weapon extends
	private Hitbox hbox;
	
	//is the player holding their mouse?
	private boolean held = false;
	
	private SoundEntity sawSound;

	public DiamondCutter(Schmuck user) {
		super(user, swingCd, windup, weaponSprite, eventSprite);
	}
	
	private final Vector2 projOffset = new Vector2();
	@Override
	public void mouseClicked(float delta, PlayState state, BodyData shooter, short faction, Vector2 mouseLocation) {
		
		if (sawSound == null) {
			sawSound = new SoundEntity(state, user, SoundEffect.DRILL, 0.8f, 1.0f, true, true, soundSyncType.TICKSYNC);
		} else {
			sawSound.turnOn();
		}
		
		if (!held) {
			held = true;
			
			if (hbox != null) {
				if (hbox.isAlive()) {
					return;
				}
			}
			
			projOffset.set(mouseLocation).sub(shooter.getSchmuck().getPixelPosition()).nor().scl(range);

			hbox = new Hitbox(state, projOffset, projectileSize, 0, new Vector2(0, 0), shooter.getSchmuck().getHitboxfilter(), true, true, user, projSprite);
			hbox.makeUnreflectable();
			
			hbox.addStrategy(new CreateParticles(state, hbox, user.getBodyData(), Particle.SPARK_TRAIL, 0.0f, 1.0f));
			hbox.addStrategy(new HitboxStrategy(state, hbox, user.getBodyData()) {
				
				private float controllerCount;
				@Override
				public void create() { hbox.setAngularVelocity(spinSpeed); }
				
				private final Vector2 entityLocation = new Vector2();
				private final Vector2 pulseVelocity = new Vector2();
				@Override
				public void controller(float delta) {
					
					if (!user.isAlive()) {
						hbox.die();
						held = false;
					}
					
					projOffset.set(mouseLocation).sub(shooter.getSchmuck().getPixelPosition()).nor().scl(range);
					entityLocation.set(shooter.getSchmuck().getPosition());
					hbox.setTransform(
							entityLocation.x + projOffset.x / PPM,  
							entityLocation.y + projOffset.y / PPM,
							hbox.getAngle());
					
					controllerCount += delta;
					while (controllerCount >= spinInterval) {
						controllerCount -= spinInterval;
						
						Hitbox pulse = new Hitbox(state, hbox.getPixelPosition(), projectileSize, spinInterval, pulseVelocity, shooter.getSchmuck().getHitboxfilter(), true, true, user, Sprite.NOTHING);
						pulse.setSyncDefault(false);
						pulse.setEffectsVisual(false);
						pulse.makeUnreflectable();
						
						pulse.addStrategy(new ControllerDefault(state, pulse, user.getBodyData()));
						pulse.addStrategy(new DamageStandard(state, pulse, user.getBodyData(), baseDamage, knockback, DamageTypes.MELEE).setStaticKnockback(true));
					}
				}
				
				@Override
				public void die() {
					hbox.queueDeletion();
				}
			});
		}
	}
	
	@Override
	public void execute(PlayState state, BodyData shooter) {}
	
	@Override
	public void release(PlayState state, BodyData bodyData) {
		held = false;
		if (hbox != null) {
			hbox.die();
		}
		if (sawSound != null) {
			sawSound.turnOff();
		}
	}
	
	@Override
	public void unequip(PlayState state) {
		held = false;
		if (hbox != null) {
			hbox.die();
		}
		if (sawSound != null) {
			sawSound.terminate();
			sawSound = null;
		}
	}
}
