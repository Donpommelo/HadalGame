package com.mygdx.hadal.event.utility;

import com.mygdx.hadal.event.Event;
import com.mygdx.hadal.event.userdata.InteractableEventData;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

/**
 * A switch is an activating event that will activate a connected event when the player interacts with it.
 * 
 * Triggered Behavior: N/A.
 * Triggering Behavior: When interacted with by the player, this event will trigger its connected event.
 * 
 * Fields:
 * N/A
 * 
 * @author Zachary Tu
 *
 */
public class Switch extends Event {

	private static final String name = "Switch";
	
	public Switch(PlayState state, int width, int height, int x, int y) {
		super(state, name, width, height, x, y);
	}
	
	@Override
	public void create() {
		this.eventData = new InteractableEventData(this) {
			
			@Override
			public void onInteract(Player p) {
				if (event.getConnectedEvent() != null) {
					event.getConnectedEvent().getEventData().onActivate(this);
					
					if (standardParticle != null) {
						standardParticle.onForBurst(1.0f);
					}
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 0, 0, 0, false, false, Constants.BIT_SENSOR, 
				(short) (Constants.BIT_PLAYER),	(short) 0, true, eventData);
	}
	
	@Override
	public String getText() {
		return name + " (E TO ACTIVATE)";
	}
}
