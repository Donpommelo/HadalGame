package com.mygdx.hadal.schmucks.bodies.enemies;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.equip.Equipable;
import com.mygdx.hadal.managers.AssetList;
import com.mygdx.hadal.schmucks.MoveStates;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.schmucks.userdata.HitboxData;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.FixtureBuilder;

import box2dLight.RayHandler;

/**
 * Enemies are Schmucks that attack the player.
 * @author Zachary Tu
 *
 */
public class FloatingEnemy extends SteeringEnemy {
				
	//This is the weapon that the enemy will attack player with next. Can change freely from enemy to enemy.
	protected Equipable weapon;
    
    public Vector2 direction;
    
    public static final float moveCd = 0.75f;
    public float moveCdCount = 0;
    
    public static final float aiCd = 0.75f;
    public float aiCdCount = 0;
    
    public static final float moveMag = 7.5f;
    
    //Fixtures and user data
  	protected FixtureDef sensorDef;
  	protected Fixture sensor;
  	protected HitboxData sensorData;
    
  	private floatingState aiState;
  	private Vector2 wallhug;
  	
  	float shortestFraction;
  	Fixture closestFixture;
  	
  	private TextureAtlas atlas;
	private TextureRegion fishSprite;
	
	public int width, height, hbWidth, hbHeight;
	
	public float scale;

	/**
	 * Enemy constructor is run when an enemy spawner makes a new enemy.
	 * @param state: current gameState
	 * @param world: box2d world
	 * @param camera: game camera
	 * @param rays: game rayhandler
	 * @param width: width of enemy
	 * @param height: height of enemy
	 * @param x: enemy starting x position.
	 * @param y: enemy starting x position.
	 */
	public FloatingEnemy(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int x, int y,
			int width, int height, int hbWidth, int hbHeight, float scale, String spriteId,
			float maxLinSpd, float maxLinAcc, float maxAngSpd, float maxAngAcc, float boundingRad, float decelerationRad) {
		super(state, world, camera, rays, hbWidth * scale, hbHeight * scale, x, y,
				maxLinSpd, maxLinAcc, maxAngSpd, maxAngAcc, boundingRad, decelerationRad);
		
		this.width = width;
		this.height = height;
		this.hbWidth = hbWidth;
		this.hbHeight = hbHeight;
		this.scale = scale;
		
		this.aiState = floatingState.ROAMING;
		
		atlas = (TextureAtlas) HadalGame.assetManager.get(AssetList.FISH_ATL.toString());
		fishSprite = atlas.findRegion(spriteId);
	}
	
	/**
	 * Create the enemy's body and initialize enemy's user data.
	 */
	public void create() {
		
		super.create();

		this.sensorData = new HitboxData(state, world, null) {
			public void onHit(HadalData fixB) {
				if (fixB == null) {
					if (aiState.equals(floatingState.ROAMING)) {
						aiState = floatingState.WALLHUGGING;
						aiCdCount = aiCd;
						wallhug = getBody().getLinearVelocity().nor().scl(moveMag / 2).rotate(45);
					}
				}
			}
		};
		
		sensorDef = FixtureBuilder.createFixtureDef(hbWidth * scale, hbHeight * scale, new Vector2(0,0), true, 0, 
				Constants.BIT_SENSOR, (short)(Constants.BIT_WALL | Constants.BIT_PLAYER), Constants.PLAYER_HITBOX);
		sensor = body.createFixture(sensorDef);
		sensor.setUserData(sensorData);
		
	}

	/**
	 * Enemy ai goes here. Default enemy behaviour just walks right/left towards player and fires weapon.
	 */
	public void controller(float delta) {
		
		moveState = MoveStates.STAND;
		
		switch (aiState) {
		case ROAMING:
			
			if (sensorData.getNumContacts() > 0) {
				aiState = floatingState.WALLHUGGING;
				aiCdCount = aiCd;
				wallhug = direction.rotate(180);
			}
			
			direction = new Vector2(
					state.getPlayer().getBody().getPosition().x - getBody().getPosition().x,
					state.getPlayer().getBody().getPosition().y - getBody().getPosition().y).nor().scl(moveMag);			
			break;
		case WALLHUGGING:
			direction = wallhug;
			break;
		case CHASING:
			Vector3 target = new Vector3(state.getPlayer().getBody().getPosition().x, state.getPlayer().getBody().getPosition().y, 0);
			camera.project(target);
			
			useToolStart(delta, weapon, Constants.ENEMY_HITBOX, (int)target.x, (int)target.y, true);
			
			super.controller(delta);
			
			break;
		default:
			break;
		
		}
		
		if (moveCdCount < 0) {
			moveCdCount += moveCd;
			switch (aiState) {
			case ROAMING:
			case WALLHUGGING:
				push(direction.x, direction.y);
//				body.setTransform(body.getPosition(), 90);
				break;
			case CHASING:
				break;
			}
		}
		
		if (aiCdCount < 0) {
			aiCdCount += aiCd;
			aiState = floatingState.ROAMING;
			
			shortestFraction = 1.0f;
			
			if (getBody().getPosition().x != state.getPlayer().getBody().getPosition().x || 
					getBody().getPosition().y != state.getPlayer().getBody().getPosition().y) {
				world.rayCast(new RayCastCallback() {

					@Override
					public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
						if (fixture.getUserData() == null) {
							if (fraction < shortestFraction) {
								shortestFraction = fraction;
								closestFixture = fixture;
								return fraction;
							}
						} else if (fixture.getUserData() instanceof PlayerBodyData) {
							if (fraction < shortestFraction) {
								shortestFraction = fraction;
								closestFixture = fixture;
								return fraction;
							}
							
						} 
						return -1.0f;
					}
					
				}, getBody().getPosition(), state.getPlayer().getBody().getPosition());
				
				if (closestFixture != null) {
					if (closestFixture.getUserData() instanceof PlayerBodyData ) {
						aiState = floatingState.CHASING;
					}
				}		
			}
				
		}

		shootCdCount-=delta;
		shootDelayCount-=delta;
		
		//If the delay on using a tool just ended, use thte tool.
		if (shootDelayCount <= 0 && usedTool != null) {
			useToolEnd();
		}
		
		if (weapon.reloading) {
			weapon.reload(delta);
		}
		
		moveCdCount -= delta;
		aiCdCount -= delta;
	}
	
	/**
	 * draws enemy
	 */
	public void render(SpriteBatch batch) {
		batch.setProjectionMatrix(state.hud.combined);
		Vector3 bodyScreenPosition = new Vector3(body.getPosition().x, body.getPosition().y, 0);
		camera.project(bodyScreenPosition);
							
		batch.draw(fishSprite, 
				bodyScreenPosition.x - hbHeight * scale / 2, 
				bodyScreenPosition.y - hbWidth * scale / 2, 
				hbHeight * scale / 2, hbWidth * scale / 2,
				width * scale, height * scale,
				1, 1, 
				(float) Math.toDegrees(body.getAngle()) - 90);
	}
	
	public enum floatingState {		CHASING,
		ROAMING,
		WALLHUGGING
	}
}