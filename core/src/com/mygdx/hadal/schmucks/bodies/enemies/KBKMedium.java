package com.mygdx.hadal.schmucks.bodies.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.EnemyUtils;
import com.mygdx.hadal.event.SpawnerSchmuck;
import com.mygdx.hadal.managers.GameStateManager;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity.particleSyncType;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DeathParticles;
import com.mygdx.hadal.statuses.Invulnerability;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.Stats;

public class KBKMedium extends EnemySwimming {

	private final static int baseHp = 60;
	private final static String name = "SWIMMING KAMABOKO";
	
	private final static int scrapDrop = 0;

	private static final int width = 512;
	private static final int height = 512;
	
	private static final int hboxWidth = 280;
	private static final int hboxHeight = 120;
	
	private static final float attackCd = 1.0f;
	private static final float airSpeed = -0.2f;
	
	private static final float scale = 0.25f;
	private static final float noiseRadius = 4.0f;

	private static final Sprite sprite = Sprite.KAMABOKO_SWIM;
	
	private TextureRegion faceSprite;
	
	public KBKMedium(PlayState state, Vector2 startPos, float startAngle, short filter, SpawnerSchmuck spawner) {
		super(state, startPos, new Vector2(width, height).scl(scale), new Vector2(hboxWidth, hboxHeight).scl(scale), name, sprite, EnemyType.SWIMMER1, startAngle, filter, baseHp, attackCd, scrapDrop, spawner);
		faceSprite = Sprite.KAMABOKO_FACE.getFrames().get(GameStateManager.generator.nextInt(5));
		EnemyUtils.setSwimmingChaseState(this, 1.0f, minRange, maxRange, 0.0f);
		
		setNoiseRadius(noiseRadius);
	}
	
	@Override
	public void create() {
		super.create();
		getBodyData().addStatus(new Invulnerability(state, 0.1f, getBodyData(), getBodyData()));
		getBodyData().addStatus(new DeathParticles(state, getBodyData(), Particle.KAMABOKO_IMPACT, 1.0f));
		getBodyData().addStatus(new StatChangeStatus(state, Stats.AIR_SPD, airSpeed, getBodyData()) {
			
			@Override
			public void onDeath(BodyData perp) {
				
				new ParticleEntity(state, new Vector2(inflicted.getSchmuck().getPixelPosition()), Particle.KAMABOKO_IMPACT, 2.0f, true, particleSyncType.CREATESYNC);
				
				EnemyType.SPLITTER_SMALL.generateEnemy(state, inflicted.getSchmuck().getPixelPosition(), Constants.ENEMY_HITBOX, 0.0f, null);
				EnemyType.SPLITTER_SMALL.generateEnemy(state, inflicted.getSchmuck().getPixelPosition(), Constants.ENEMY_HITBOX, 0.0f, null);
				EnemyType.SPLITTER_SMALL.generateEnemy(state, inflicted.getSchmuck().getPixelPosition(), Constants.ENEMY_HITBOX, 0.0f, null);
			}
		});
	}
	
	private static final float minRange = 0.0f;
	private static final float maxRange = 2.0f;
	
	private static final int charge1Damage = 10;
	private static final float attackInterval = 1.0f;
	private static final int defaultMeleeKB = 15;
	@Override
	public void attackInitiate() {
		EnemyUtils.meleeAttackContinuous(state, this, charge1Damage, attackInterval, defaultMeleeKB, attackCd);
	};
	
	@Override
	public void render(SpriteBatch batch) {
		super.render(batch);
		
		boolean flip = true;
		double realAngle = getAngle() % (Math.PI * 2);
		if ((realAngle > Math.PI / 2 && realAngle < 3 * Math.PI / 2) || (realAngle < -Math.PI / 2 && realAngle > -3 * Math.PI / 2)) {
			flip = false;
		}

		batch.draw(faceSprite, 
				(flip ? size.x : 0) + getPixelPosition().x - size.x / 2, 
				getPixelPosition().y - size.y / 2, 
				(flip ? -1 : 1) * size.x / 2, 
				size.y / 2,
				(flip ? -1 : 1) * size.x, size.y, 1, 1, 
				(flip ? 0 : 180) + (float) Math.toDegrees(getAngle()));
	}
}
