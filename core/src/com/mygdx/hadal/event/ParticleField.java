package com.mygdx.hadal.event;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity.particleSyncType;
import com.mygdx.hadal.states.ClientState;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.states.ClientState.ObjectSyncLayers;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

/**
 * A Particle Field spawns a bunch of particles in its area. This is strictly for visual effect.
 * 
 * Triggered Behavior: N/A
 * Triggering Behavior: N/A
 * 
 * Fields:
 * particle: String name of the particle effect to use. Default: NOTHING
 * speed: float rate that particles are spawned. Default: 1.0f
 * duration: float duration of each particle effect. Default: 1.0f
 * 
 * @author Zachary Tu
 *
 */
public class ParticleField extends Event {
	
	private Particle particle;
	private float duration;
	private float currParticleSpawnTimer = 0f, spawnTimerLimit;
	
	public ParticleField(PlayState state, Vector2 startPos, Vector2 size, Particle particle, float speed, float duration) {
		super(state, startPos, size);
		this.particle = particle;
		this.duration = duration;
		spawnTimerLimit = 4096f / (size.x * size.y) / speed;
	}
	
	@Override
	public void create() {

		this.eventData = new EventData(this);
		
		this.body = BodyBuilder.createBox(world, startPos, size, 0, 0, 0, false, false, Constants.BIT_SENSOR, (short) (Constants.BIT_PLAYER | Constants.BIT_ENEMY), (short)0, true, eventData);
	}
	
	@Override
	public void controller(float delta) {
		
		//if specified, spawn random particles in the event's vicinity
		currParticleSpawnTimer += delta;
		while (currParticleSpawnTimer >= spawnTimerLimit) {
			currParticleSpawnTimer -= spawnTimerLimit;
			float randX = (float) ((Math.random() * size.x) - (size.x / 2) + getPixelPosition().x);
			float randY = (float) ((Math.random() * size.y) - (size.y / 2) + getPixelPosition().y);
			new ParticleEntity(state, new Vector2(randX, randY), particle, duration, true, particleSyncType.NOSYNC);
		}
	}
	
	/**
	 * Client particle field should randomly spawn particles itself to avoid overhead.
	 */
	@Override
	public void clientController(float delta) {
		
		currParticleSpawnTimer += delta;
		while (currParticleSpawnTimer >= spawnTimerLimit) {
			currParticleSpawnTimer -= spawnTimerLimit;
			float randX = (float) ((Math.random() * size.x) - (size.x / 2) + getPixelPosition().x);
			float randY = (float) ((Math.random() * size.y) - (size.y / 2) + getPixelPosition().y);
			ParticleEntity field = new ParticleEntity(state, new Vector2(randX, randY), particle, duration, true, particleSyncType.NOSYNC);
			((ClientState)state).addEntity(field.getEntityID().toString(), field, ObjectSyncLayers.STANDARD);
		}
	}
	
	@Override
	public void loadDefaultProperties() {
		setSyncType(eventSyncTypes.ALL);
	}
}