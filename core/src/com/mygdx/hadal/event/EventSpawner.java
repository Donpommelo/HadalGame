package com.mygdx.hadal.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class EventSpawner extends Event {
	
	private int spawnX, spawnY;
	
	private int id;
	private boolean reset;
	
	private String args;
	
	private static final String name = "Event Spawner";

	public EventSpawner(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y, int id, boolean resetActivator, String extraArgs) {
		super(state, world, camera, rays, name, width, height, x, y);
		this.spawnX = x;
		this.spawnY = y;
		this.id = id;
		this.reset = resetActivator;
		this.args = extraArgs;
	}
	
	@Override
	public void create() {

		this.eventData = new EventData(world, this) {
			
			@Override
			public void onActivate(EventData activator) {
				Event event = null;
				
				switch(id) {
				case 0:
					
					break;
				case 1:
					event = new Medpak(state, world, camera, rays, spawnX, spawnY);
					break;
				case 2:
					event = new AirBubble(state, world, camera, rays, spawnX, spawnY);
					break;
						
				}
				
				if (reset) {
					event.setConnectedEvent(activator.getEvent());
				}
			}
		};
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, true, true, Constants.BIT_SENSOR, 
				(short) (Constants.BIT_PLAYER | Constants.BIT_ENEMY | Constants.BIT_PROJECTILE),
				(short) 0, true, eventData);
	}

	public String getArgs() {
		return args;
	}	
}