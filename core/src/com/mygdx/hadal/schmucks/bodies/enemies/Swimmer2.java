package com.mygdx.hadal.schmucks.bodies.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.EnemyUtils;
import com.mygdx.hadal.event.SpawnerSchmuck;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity.particleSyncType;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.utils.Stats;

public class Swimmer2 extends EnemySwimming {

	private static final int baseHp = 174;

	private static final int scrapDrop = 1;

	private static final int width = 512;
	private static final int height = 512;
	
	private static final int hboxWidth = 280;
	private static final int hboxHeight = 120;
	
	private static final float attackCd = 2.0f;
	private static final float airSpeed = 0.1f;
	
	private static final float scale = 0.25f;
	private static final float noiseRadius = 6.0f;

	private static final Sprite sprite = Sprite.KAMABOKO_SWIM;
	
	private final TextureRegion faceSprite;
	
	public Swimmer2(PlayState state, Vector2 startPos, float startAngle, short filter, SpawnerSchmuck spawner) {
		super(state, startPos, new Vector2(width, height).scl(scale), new Vector2(hboxWidth, hboxHeight).scl(scale), sprite, EnemyType.SWIMMER2, startAngle, filter, baseHp, attackCd, scrapDrop, spawner);
		faceSprite = Sprite.KAMABOKO_FACE.getFrames().get(MathUtils.random(4));
		EnemyUtils.setSwimmingChaseState(this, 1.0f, minRange, maxRange, 0.0f);
		
		setNoiseRadius(noiseRadius);
	}
	
	@Override
	public void create() {
		super.create();
		getBodyData().addStatus(new StatChangeStatus(state, Stats.AIR_SPD, airSpeed, getBodyData()));
	}
	
	private static final float minRange = 3.0f;
	private static final float maxRange = 8.0f;
	
	private static final float defaultTrack = 0.04f;
	private static final float attackTrack = 0.01f;

	private static final float attackWindup = 1.0f;
	private static final float attackSwingAngle = 30.0f;
	
	private static final int fireballDamage = 10;
	private static final int fireSpeed = 10;
	private static final int fireKB = 6;
	private static final int fireSize = 50;
	private static final float fireLifespan = 1.25f;

	private static final int fireballNumber = 8;
	private static final float fireballInterval = 0.15f;
	@Override
	public void attackInitiate() {
		
		EnemyUtils.changeSwimmingState(this, SwimmingState.STILL, 0.0f, 0.0f);
		
		EnemyUtils.windupParticles(state, this, attackWindup, Particle.KAMABOKO_SHOWER, 120.0f);
		
		EnemyUtils.changeFloatingTrackSpeed(this, attackTrack, 0.0f);
		
		EnemyUtils.createSoundEntity(state, this, 0.0f, fireballNumber * fireballInterval * 2, 0.6f, 2.0f, SoundEffect.OOZE, true);
		
		EnemyUtils.changeFloatingFreeAngle(this, attackSwingAngle, 0.0f);
		for (int i = 0; i < fireballNumber; i++) {
			EnemyUtils.projectile(state, this, fireballDamage, fireSpeed, fireKB, fireSize, fireLifespan, fireballInterval, Particle.KAMABOKO_SHOWER);
		}
		
		EnemyUtils.changeFloatingFreeAngle(this, - 2 * attackSwingAngle, 0.0f);
		for (int i = 0; i < fireballNumber; i++) {
			EnemyUtils.projectile(state, this, fireballDamage, fireSpeed, fireKB, fireSize, fireLifespan, fireballInterval, Particle.KAMABOKO_SHOWER);
		}
		
		EnemyUtils.setSwimmingChaseState(this, 1.0f, minRange, maxRange, 0.0f);
		EnemyUtils.changeFloatingTrackSpeed(this, defaultTrack, 0.0f);
		EnemyUtils.changeFloatingState(this, FloatingState.TRACKING_PLAYER, 0.0f, 0.0f);
	}
	
	private final Vector2 entityLocation = new Vector2();
	@Override
	public void render(SpriteBatch batch) {
		super.render(batch);

		boolean flip = true;
		float realAngle = getAngle() % (MathUtils.PI * 2);
		if ((realAngle > MathUtils.PI / 2 && realAngle < 3 * MathUtils.PI / 2) || (realAngle < -MathUtils.PI / 2 && realAngle > -3 * MathUtils.PI / 2)) {
			flip = false;
		}

		entityLocation.set(getPixelPosition());
		batch.draw(faceSprite, 
				(flip ? size.x : 0) + entityLocation.x - size.x / 2, 
				entityLocation.y - size.y / 2, 
				(flip ? -1 : 1) * size.x / 2, 
				size.y / 2,
				(flip ? -1 : 1) * size.x, size.y, 1, 1, 
				(flip ? 0 : 180) + MathUtils.radDeg * getAngle());
	}
	
	@Override
	public boolean queueDeletion() {
		if (alive) {
			new ParticleEntity(state, new Vector2(getPixelPosition()), Particle.KAMABOKO_IMPACT, 1.0f, true, particleSyncType.CREATESYNC);
		}
		return super.queueDeletion();
	}
}
