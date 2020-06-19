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
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.utils.Stats;

public class KBKBouncer extends EnemyCrawling {

	private final static int baseHp = 100;
	private final static String name = "CRAWLING KAMABOKO";

	private final static int scrapDrop = 1;

	private static final int width = 512;
	private static final int height = 512;
	
	private static final int hboxWidth = 280;
	private static final int hboxHeight = 120;

	private static final int smileOffset = 200;

	private static final float scale = 0.25f;
	
	private static final float attackCd = 2.0f;
	private static final float groundSpeed = -0.25f;
	private static final float drag = -1.0f;
	
	private static final Sprite sprite = Sprite.KAMABOKO_CRAWL;
	
	private TextureRegion faceSprite;
	
	public KBKBouncer(PlayState state, Vector2 startPos, float startAngle, short filter, SpawnerSchmuck spawner) {
		super(state, startPos, new Vector2(width, height).scl(scale), new Vector2(hboxWidth, hboxHeight).scl(scale), name, sprite, EnemyType.CRAWLER1, startAngle, filter, baseHp, attackCd, scrapDrop, spawner);
		faceSprite = Sprite.KAMABOKO_FACE.getFrames().get(GameStateManager.generator.nextInt(5));
		setCurrentState(CrawlingState.BACK_FORTH);
	}
	
	@Override
	public void create() {
		super.create();
		getMainFixture().setRestitution(1.0f);
		getBodyData().addStatus(new StatChangeStatus(state, Stats.GROUND_SPD, groundSpeed, getBodyData()));
		getBodyData().addStatus(new StatChangeStatus(state, Stats.AIR_DRAG, drag, getBodyData()));
	}
	
	private static final int charge1Damage = 10;
	private static final float attackInterval = 1.0f;
	private static final int defaultMeleeKB = 20;
	@Override
	public void attackInitiate() {
		EnemyUtils.meleeAttackContinuous(state, this, charge1Damage, attackInterval, defaultMeleeKB, attackCd);
	};
	
	@Override
	public void render(SpriteBatch batch) {
		super.render(batch);
		
		boolean flip = false;
		
		if (getMoveDirection() < 0) {
			flip = true;
		} else if (getMoveDirection() > 0) {
			flip = false;
		}

		batch.draw(faceSprite, 
				(flip ? 0 : size.x) + getPixelPosition().x - size.x / 2, 
				getPixelPosition().y - getHboxSize().y / 2 - smileOffset * scale, 
				size.x / 2,
				(flip ? 1 : -1) * size.y / 2, 
				(flip ? 1 : -1) * size.x, size.y, 1, 1, 0);
		
	}
	
	@Override
	public boolean queueDeletion() {
		if (alive) {
			new ParticleEntity(state, new Vector2(getPixelPosition()), Particle.KAMABOKO_IMPACT, 1.0f, true, particleSyncType.CREATESYNC);
		}
		return super.queueDeletion();
	}
}
