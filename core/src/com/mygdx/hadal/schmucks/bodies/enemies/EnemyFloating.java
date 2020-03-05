package com.mygdx.hadal.schmucks.bodies.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.event.SpawnerSchmuck;
import com.mygdx.hadal.schmucks.bodies.Ragdoll;
import com.mygdx.hadal.states.PlayState;

/**
 * Enemies are Schmucks that attack the player.
 * Floating enemies are the basic fish-enemies of the game.
 * These enemies can rotate to face the player.
 * @author Zachary Tu
 *
 */
public class EnemyFloating extends Enemy {
				
	//the angle that the boss is facing and the angle that it lerps towards.
	private float angle;

    private float desiredAngle;
	
    //the speed that the boss spins when spinning
	private int spinSpeed;
	
	//The boss's current state in terms of passive behavior (is it tracking the player, still, spinning etc)
	private FloatingState currentState;
	
	//this is the boss's sprite
	private Animation<TextureRegion> floatingSprite;

	public EnemyFloating(PlayState state, Vector2 startPos, Vector2 size, Vector2 hboxSize, Sprite sprite, EnemyType type, short filter, int hp, float attackCd, int scrapDrop, SpawnerSchmuck spawner) {
		super(state, startPos, size, hboxSize, sprite, type, filter, hp, attackCd, scrapDrop, spawner);
		
		this.angle = 0;
		this.desiredAngle = 0;
		
		this.currentState = FloatingState.TRACKING_PLAYER;
		
		if (!sprite.equals(Sprite.NOTHING)) {
			this.floatingSprite = new Animation<TextureRegion>(PlayState.spriteAnimationSpeed, sprite.getFrames());
			this.floatingSprite.setPlayMode(PlayMode.LOOP_PINGPONG);
		}
	}


	@Override
	public void controller(float delta) {		
		super.controller(delta);
		
		//lerp towards desired angle
		float dist = (desiredAngle - angle) % 360;
		angle = angle + (2 * dist % 360 - dist) * 0.04f;		
		
		//when spinning, spin at a constant speed. When tracking, set desired angle to face player
		switch(currentState) {
		case ROTATING:
			desiredAngle += spinSpeed;
			break;
		case SPINNING:
			angle += spinSpeed;
			break;
		case TRACKING_PLAYER:
			if (target != null) {				
				if (target.isAlive()) {
					desiredAngle = (float)(Math.atan2(
							target.getPosition().y - getPosition().y ,
							target.getPosition().x - getPosition().x) * 180 / Math.PI);
				}
			}
			break;
		default:
			break;
		}
		setOrientation((float) ((angle) * Math.PI / 180));
	}
	
	/**
	 * draws enemy
	 */
	@Override
	public void render(SpriteBatch batch) {
		
		boolean flip = true;
		double realAngle = getOrientation() % (Math.PI * 2);
		if ((realAngle > Math.PI / 2 && realAngle < 3 * Math.PI / 2) || (realAngle < -Math.PI / 2 && realAngle > -3 * Math.PI / 2)) {
			flip = false;
		}
		batch.draw((TextureRegion) floatingSprite.getKeyFrame(animationTime, true), 
				(flip ? size.x : 0) + getPixelPosition().x - size.x / 2, 
				getPixelPosition().y - size.y / 2, 
				(flip ? -1 : 1) * size.x / 2, 
				size.y / 2,
				(flip ? -1 : 1) * size.x, size.y, 1, 1, 
				(flip ? 0 : 180) + (float) Math.toDegrees(getOrientation()));
	}
	
	@Override
	public boolean queueDeletion() {
		if (alive) {
			new Ragdoll(state, getPixelPosition(), size, sprite, getLinearVelocity(), 0.5f, 1.0f, false);
		}
		return super.queueDeletion();
	}
	
	public void setCurrentState(FloatingState currentState) { this.currentState = currentState; }
	
	public float getAngle() { return angle; }

	public void setAngle(float angle) { this.angle = angle; }
	
	public float getDesiredAngle() { return desiredAngle; }

	public void setDesiredAngle(float desiredAngle) { this.desiredAngle = desiredAngle; }

	public void setSpinSpeed(int spinSpeed) { this.spinSpeed = spinSpeed; }
	
	@Override
	public float getAttackAngle() {	return angle; }
	
	public enum FloatingState {
		TRACKING_PLAYER,
		FREE,
		ROTATING,
		SPINNING
	}
}
