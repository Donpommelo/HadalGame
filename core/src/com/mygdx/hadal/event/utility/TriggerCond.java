package com.mygdx.hadal.event.utility;

import com.badlogic.gdx.math.MathUtils;
import com.mygdx.hadal.event.Event;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.states.PlayState;

import java.util.HashMap;
import java.util.Map;

/**
 * A Conditional trigger is like a multi-trigger, except it only triggers one of the events in its list. Which event it triggers is
 * determined by its condition string field which can be set by triggering it with an alt-trigger.
 * 
 * Triggered Behavior: When triggered, this will trigger the event mapped to its condition field.
 * Triggering Behavior: N/A. Similar to multi-triggers, this event does nothing with its connected event. Instead, it holds a reference
 * 	to a list of events mapped to their string ids created upon parsing a map. This trigger will activate a event from the list. 
 * Alt-Triggered Behavior: When alt-triggered, this trigger changes which event it triggers to match the alt-trigger's message.
 * 
 * Fields:
 * start: String that determines the id of the event in its list that this trigger will start off triggering.
 * 	Optional. Default: "". This means that the trigger will start off triggering nothing.
 * 	Also, if this is set to "random", this will trigger a random event on its list.
 * 
 * triggeringId: Like a multi-trigger, this string should be a comma-separated list of triggeredIds of events that can be triggered.
 * 
 * @author Kardamom Kotonio
 */
public class TriggerCond extends Event {

	private final Map<String, Event> triggered = new HashMap<>();
	private String condition;
	
	public TriggerCond(PlayState state, String start) {
		super(state);
		this.condition = start;
	}
	
	@Override
	public void create() {
		this.eventData = new EventData(this) {
			
			@Override
			public void onActivate(EventData activator, Player p) {
				if (activator.getEvent() instanceof TriggerAlt) {
					condition = ((TriggerAlt) activator.getEvent()).getMessage();
				} else {
					if (condition.equals("random")) {
						Object[] values = triggered.values().toArray();
						((Event) values[MathUtils.random(values.length - 1)]).getEventData().preActivate(this, p);
					} else {
						if (triggered.get(condition) != null) {
							triggered.get(condition).getEventData().preActivate(this, p);
						}
					}	
				}
			}
		};
	}
	
	public void addTrigger(String s, Event e) {	
		if (e != null) {
			triggered.put(s, e);
		}
	}
}
