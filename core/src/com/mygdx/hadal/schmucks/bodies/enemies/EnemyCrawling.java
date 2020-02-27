package com.mygdx.hadal.schmucks.bodies.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.event.SpawnerSchmuck;
import com.mygdx.hadal.schmucks.bodies.Ragdoll;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;

/**
 * Enemies are Schmucks that attack the player.
 * Crawling enemies move right and left along the floor.
 * These enemies can rotate to face the player.
 * @author Zachary Tu
 *
 */
public class EnemyCrawling extends Enemy {
	
	//this the frequency that the physics occurs
	private final static float controllerInterval = 1 / 60f;
		
	//this is the boss's sprite
	private Animation<TextureRegion> floatingSprite;

	private float moveDirection;
	private CrawlingState currentState;
	
	public EnemyCrawling(PlayState state, Vector2 startPos, Vector2 size, Vector2 hboxSize, Sprite sprite, EnemyType type, short filter, int hp, float attackCd, SpawnerSchmuck spawner) {
		super(state, startPos, size, hboxSize, sprite, type, filter, hp, attackCd, spawner);
		
		this.moveDirection = 1.0f;
		this.currentState = CrawlingState.STILL;
		
		if (!sprite.equals(Sprite.NOTHING)) {
			this.floatingSprite = new Animation<TextureRegion>(PlayState.spriteAnimationSpeed, sprite.getFrames());
		}
	}

	@Override
	public void create() {
		super.create();
		body.setGravityScale(1.0f);
	}

	private Vector2 force = new Vector2();
	private Vector2 currentVel = new Vector2();
	@Override
	public void controller(float delta) {		
		super.controller(delta);
		
		switch(currentState) {
		case AVOID_PITS:
			processCollision(true);
			break;
		case BACK_FORTH:
			processCollision(false);
			break;
		case CHASE_PLAYER:
			if (target != null) {				
				if (target.isAlive()) {
					if (target.getPosition().x > getPosition().x) {
						moveDirection = 1.0f;
					} else {
						moveDirection = -1.0f;
					}
				}
			}
			break;
		case STILL:
			moveDirection = 0;
			break;
		default:
			break;
		}

		//This line ensures that this runs every 1/60 second regardless of computer speed.
		controllerCount += delta;
		while (controllerCount >= controllerInterval) {
			controllerCount -= controllerInterval;
						
			currentVel.set(getLinearVelocity());

			float desiredXVel = getBodyData().getXGroundSpeed() * moveDirection;
			
			float accelX = 0.0f;
			
			if (Math.abs(desiredXVel) > Math.abs(currentVel.x)) {
				accelX = getBodyData().getXGroundAccel();
			} else {
				accelX = getBodyData().getXGroundDeaccel();
			}
			
			float newX = accelX * desiredXVel + (1 - accelX) * currentVel.x;
			force.set(newX - currentVel.x, 0).scl(getMass());

			applyLinearImpulse(force);
		}
	}
	
	private Vector2 endPt = new Vector2();
	private final static float distCheck = 2.0f;
	private float shortestFraction;
	private void processCollision(boolean avoidPits) {
		
		endPt.set(getPosition()).add(distCheck * moveDirection, 0);
		shortestFraction = 1.0f;
		
		if (getPosition().x != endPt.x || getPosition().y != endPt.y) {
			
			state.getWorld().rayCast(new RayCastCallback() {

				@Override
				public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
					if (fixture.getFilterData().categoryBits == (short)Constants.BIT_WALL) {
						if (fraction < shortestFraction) {
							shortestFraction = fraction;
							return fraction;
						}
					}
					return -1.0f;
				}
			}, getPosition(), endPt);
		}
		
		if (shortestFraction < 1.0f) {
			moveDirection = -moveDirection;
		} else if (avoidPits) {
			
			endPt.set(getPosition()).add(moveDirection * distCheck, -distCheck);
			shortestFraction = 1.0f;
			if (getPosition().x != endPt.x || getPosition().y != endPt.y) {
				state.getWorld().rayCast(new RayCastCallback() {

					@Override
					public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
						if (fixture.getFilterData().categoryBits == (short)Constants.BIT_WALL) {
							if (fraction < shortestFraction) {
								shortestFraction = fraction;
								return fraction;
							}
						}
						return -1.0f;
					}
				}, getPosition(), endPt);
			}
			
			if (shortestFraction == 1.0f) {
				moveDirection = -moveDirection;
			}
		}
	}
	
	/**
	 * draws enemy
	 */
	@Override
	public void render(SpriteBatch batch) {
		
		boolean flip = false;
		
		if (moveDirection < 0) {
			flip = true;
		} else if (moveDirection > 0) {
			flip = false;
		}

		batch.draw((TextureRegion) floatingSprite.getKeyFrame(animationTime, true), 
				(flip ? 0 : size.x) + getPixelPosition().x - hboxSize.x / 2, 
				getPixelPosition().y - hboxSize.y / 2, 
				hboxSize.x / 2,
				(flip ? 1 : -1) * hboxSize.y / 2, 
				(flip ? 1 : -1) * size.x, size.y, 1, 1, 0);
	}
	
	@Override
	public boolean queueDeletion() {
		if (alive) {
			new Ragdoll(state, getPixelPosition(), size, sprite, getLinearVelocity(), 0.5f, 1.0f, false);
		}
		return super.queueDeletion();
	}
	
	public void setMoveDirection(float moveDirection) { this.moveDirection = moveDirection; }

	public void setCurrentState(CrawlingState currentState) { this.currentState = currentState; }

	public enum CrawlingState {
		BACK_FORTH,
		AVOID_PITS,
		CHASE_PLAYER,
		STILL
	}
}