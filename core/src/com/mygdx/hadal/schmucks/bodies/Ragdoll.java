package com.mygdx.hadal.schmucks.bodies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.schmucks.UserDataTypes;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.server.Packets;
import com.mygdx.hadal.states.ClientState;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A Ragdoll is a miscellaneous entity that doesn't do a whole heck of a lot.
 * Its main job is to be visible and obey physics. This is useful for on-death ragdoll/frags
 * It also has a couple of other applications like current bubble particle generators
 * @author Pugma Plilzburger
 */
public class Ragdoll extends HadalEntity {
	
	//This is the sprite that will be displayed
	private Sprite sprite;
	private TextureRegion ragdollSprite;
	
	//spread is for giving the initial ragdoll a random velocity
	private static final int spread = 120;
	
	//how long does the ragdoll last
	private float ragdollDuration;
	private final float gravity;
	
	//starting multiplier on starting velocity and direction
	private static final float veloAmp = 10.0f;
	private static final float angleAmp = 2.0f;
	private static final float baseAngle = 8.0f;
	
	private final Vector2 startVelo;
	private final float startAngle;
	
	//is the ragdoll a sensor? (i.e does it have collision)
	private final boolean sensor;
	
	//do we set the velocity of the ragdoll upon spawning or just change its angle? 
	private final boolean setVelo;
	
	//when this ragdoll is created on the server, does the client create a ragdoll of its own (this is false for stuff like currents)
	private final boolean synced;
	
	public Ragdoll(PlayState state, Vector2 startPos, Vector2 size, Sprite sprite, Vector2 startVelo, float duration, float gravity, boolean setVelo, boolean sensor, boolean synced) {
		super(state, startPos, size);
		this.startVelo = startVelo;
		this.startAngle = baseAngle;
		this.ragdollDuration = duration;
		this.gravity = gravity;
		this.sprite = sprite;
		this.sensor = sensor;
		this.setVelo = setVelo;
		this.synced = synced;
		if (!sprite.equals(Sprite.NOTHING)) {
			ragdollSprite = sprite.getFrame();
		}
		
		setSyncDefault(false);
	}

	/**
	 * This alternate constructor is used for ragdolls that do not use a designated sprite (i.e. from a frame buffer)
	 * Because there is no Sprite, these are not serializable and must be made on both client and server.
	 * Also, remember to manually dispose of the frame buffer object that is used for this ragdoll
	 */
	public Ragdoll(PlayState state, Vector2 startPos, Vector2 size, TextureRegion textureRegion, Vector2 startVelo, float duration, float gravity, boolean setVelo, boolean sensor) {
		super(state, startPos, size);
		this.startVelo = startVelo;
		this.startAngle = baseAngle;
		this.ragdollDuration = duration;
		this.gravity = gravity;
		this.sensor = sensor;
		this.setVelo = setVelo;
		ragdollSprite = textureRegion;

		this.synced = false;
		setSyncDefault(false);
	}

	private final Vector2 newVelocity = new Vector2();
	@Override
	public void create() {
		this.hadalData = new HadalData(UserDataTypes.BODY, this);
		this.body = BodyBuilder.createBox(world, startPos, size, gravity, 1, 0.5f, false, false, Constants.BIT_SENSOR,	(short) (Constants.BIT_WALL | Constants.BIT_SENSOR), (short) -1, sensor, hadalData);

		//this makes ragdolls spin and move upon creation
		setAngularVelocity(startAngle * angleAmp);
		float newDegrees = startVelo.angleDeg() + (ThreadLocalRandom.current().nextInt(-spread, spread + 1));
		newVelocity.set(startVelo).add(1, 1);
		
		if (setVelo) {
			setLinearVelocity(newVelocity.nor().scl(veloAmp).setAngleDeg(newDegrees));
		} else {
			setLinearVelocity(newVelocity.setAngleDeg(newDegrees));
		}
	}

	@Override
	public void controller(float delta) {
		ragdollDuration -= delta;
		
		if (ragdollDuration <= 0) {
			queueDeletion();
		}
	}

	@Override
	public void clientController(float delta) {
		super.clientController(delta);
		ragdollDuration -= delta;
		
		if (ragdollDuration <= 0) {
			((ClientState) state).removeEntity(entityID.toString());
		}
	}
	
	private final Vector2 entityLocation = new Vector2();
	@Override
	public void render(SpriteBatch batch) {
		
		if (ragdollSprite != null) {
			entityLocation.set(getPixelPosition());
			batch.draw(ragdollSprite, 
					entityLocation.x - size.x / 2, 
					entityLocation.y - size.y / 2, 
					size.x / 2, size.y / 2,
					size.x, size.y, 1, 1,
				MathUtils.radDeg * getAngle());
		}
	}
	
	/**
	 * As Default: Upon created, the frag tells the client to create a client illusion tracking it
	 */
	@Override
	public Object onServerCreate() {
		if (synced) {
			return new Packets.CreateRagdoll(entityID.toString(), getPixelPosition(), size, sprite, startVelo, ragdollDuration, gravity, setVelo, sensor);
		} else {
			return null;
		}
	}
}
