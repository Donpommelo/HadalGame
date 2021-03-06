package com.mygdx.hadal.event;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.schmucks.UserDataTypes;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

import java.util.ArrayList;

/**
 * This is a platform that continuously moves towards its connected event.
 * 
 * Triggered Behavior: When triggered, the platform's connected event will be set to the event that triggered it.
 * Triggering Behavior: This event will move continually to the position of its connected event. When it touches its connected event,
 * 	the platform will set its connected event to its connected event's connected event if possible .
 * 
 * Fields:
 * speed: float determining the platform's speed. Optional. Default: 1.0f
 * pause: boolean of whether the platform will stop moving if it activates an event. Default false.
 * syncConnected: boolean of whether events connected to this should be synced with the client or not. Default true
 * 
 * connections: This is a comma-separated list of triggeredIds of events that are "connected" to this platform and will move along it.
 * 	NOTES: DO NOT CONNECT ANY EVENTS THAT DO NOT HAVE A BODY; TIMERS, COUNTERS, ETC.
 * 
 * @author Nijfruit Nerzupoulos
 *
 */
public class MovingPoint extends Event {

	private final float speed;
	private final boolean pause, syncConnected;
	
	private final ArrayList<Event> connected = new ArrayList<>();
	
	public MovingPoint(PlayState state, Vector2 startPos, Vector2 size, float speed, boolean pause, boolean syncConnected) {
		super(state, startPos, size);
		this.speed = speed;
		this.pause = pause;
		this.syncConnected = syncConnected;
	}

	@Override
	public void create() {

		this.eventData = new EventData(this, UserDataTypes.WALL) {
			
			@Override
			public void onActivate(EventData activator, Player p) {
				event.setConnectedEvent(activator.getEvent());
			}
		};
		
		this.body = BodyBuilder.createBox(world, startPos, size, 1, 1, 0, false, true, Constants.BIT_SENSOR, (short) 0, (short) 0, false, eventData);
		this.body.setType(BodyDef.BodyType.KinematicBody);
	}
	
	
	private final Vector2 dist = new Vector2();
	@Override
	public void controller(float delta) {
		if (getConnectedEvent() != null) {
			if (getConnectedEvent().getBody() != null) {
				dist.set(getConnectedEvent().getPixelPosition()).sub(getPixelPosition());

				//If this platform is close enough to its connected event, move to the next event in the chain.
				if ((int) dist.len2() <= 1) {
					
					//If no more connected events, make the platform and all connected events stop moving.
					if (getConnectedEvent().getConnectedEvent() == null) {
						setLinearVelocity(0, 0);
						
						//Move all connected events by same amount.
						for (Event e : connected) {
							e.setLinearVelocity(0, 0);
						}
					} else {
						
						//activate connected event and connect this event to it (to follow the next dummy point in a path)
						getConnectedEvent().getConnectedEvent().getEventData().preActivate(eventData, null);
						setTransform(getConnectedEvent().getPosition(), 0);
						
						if (getConnectedEvent().getConnectedEvent().getBody() != null) {
							setConnectedEvent(getConnectedEvent().getConnectedEvent());
						} else {
							if (pause) {
								setLinearVelocity(0, 0);

								for (Event e : connected) {
									e.setLinearVelocity(0, 0);
								}
							}
							setConnectedEvent(null);
						}
					}
				} else {
					
					//Continually move towards connected event.				
					setLinearVelocity(dist.nor().scl(speed));
					
					//Move all connected events by same amount.
					for (Event e : connected) {
						e.setLinearVelocity(dist.nor().scl(speed));
					}
				}
			}
		} 
	}
	
	/**
	 * Add another event to be connected to this moving point
	 */
	public void addConnection(Event e) {
		if (e != null) {
			connected.add(e);
			
			if (syncConnected) {
				e.setSynced(true);
				e.setIndependent(false);
			}
		}
	}

	/**
	 * This returns all connected events. This is so that event movers can move all connected events at once.
	 */
	public ArrayList<Event> getConnected() { return connected; }
}
