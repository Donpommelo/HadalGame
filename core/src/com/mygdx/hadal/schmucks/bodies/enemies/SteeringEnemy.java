package com.mygdx.hadal.schmucks.bodies.enemies;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.event.SpawnerSchmuck;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

/**
 * A Steering enemy is an enemy with ai that steers it, probably towards the player.
 * @author Zachary Tu
 *
 */
public class SteeringEnemy extends Enemy {
	
	public SteeringEnemy(PlayState state, Vector2 startPos, Vector2 size, Vector2 hboxSize, enemyType type, float maxLinSpd, float maxLinAcc, short filter, int baseHp, SpawnerSchmuck spawner) {
		super(state, startPos, size, hboxSize, type, filter, baseHp, spawner);
		
		this.maxLinearSpeed = maxLinSpd;
		this.maxLinearAcceleration = maxLinAcc;
		
		this.tagged = false;
		
		this.steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
	}

	@Override
	public void create() {
		super.create();
		
		this.body = BodyBuilder.createBox(world, startPos, hboxSize, 0, 1, 0.0f, false, true, Constants.BIT_ENEMY, 
				(short) (Constants.BIT_WALL | Constants.BIT_SENSOR | Constants.BIT_PROJECTILE | Constants.BIT_PLAYER | Constants.BIT_ENEMY),
				hitboxfilter, false, bodyData);
	}
	
	@Override
	public void controller (float delta) {
		super.controller(delta);
		if (behavior != null && bodyData != null) {
			behavior.calculateSteering(steeringOutput);
			applySteering(delta);
		}
	}
}
