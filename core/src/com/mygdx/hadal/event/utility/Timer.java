package com.mygdx.hadal.event.utility;

import com.mygdx.hadal.event.Event;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.states.PlayState;

/**
 * The Timer keeps track of time.
 * 
 * Triggered Behavior: When triggered, the timer will stop or start. This resets the timer
 * Triggering Behavior: When the timer reaches a specified value, this event will trigger its connected event
 * 
 * Fields:
 * interval: The time in seconds before this activates its connected event.
 * startOn: Does this timer start on or off? Optional. Default: true
 * @author Drolgwood Dolgunaldo
 */
public class Timer extends Event {
	
	//How frequently will this event trigger its connected event?
	private final float interval;
	
	//These keep track of how long until this triggers its connected event and how many times it can trigger again.
	private float timeCount = 0;
	
	//Is the timer running
	private boolean on;

	public Timer(PlayState state, float interval, boolean startOn) {
		super(state);
		this.interval = interval;
		this.on = startOn;
	}
	
	@Override
	public void create() {

		this.eventData = new EventData(this){
			
			@Override
			public void onActivate(EventData activator, Player p) {
				
				if (activator.getEvent() instanceof TriggerAlt) {
					String msg = ((TriggerAlt) activator.getEvent()).getMessage();
					switch (msg) {
						case "on":
							((Timer) event).on = true;
							break;
						case "off":
							((Timer) event).on = false;
							break;
						case "reset":
							timeCount = 0;
							break;
					}
				} else {
					((Timer) event).on = !((Timer) event).on;
					timeCount = 0;
				}
			}
		};
	}
	
	@Override
	public void controller(float delta) {
		if (on) {
			timeCount += delta;
			if (timeCount >= interval) {
				timeCount = 0;
				if (getConnectedEvent() != null) {
					getConnectedEvent().getEventData().preActivate(eventData, null);
				}
			}
		}
	}
}
