package com.mygdx.hadal.event;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.schmucks.UserDataTypes;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

import box2dLight.RayHandler;

public class Rock extends Event {

	private static final String name = "Rock";

	public Rock(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height, int x, int y) {
		super(state, world, camera, rays, name, width, height, x, y);
	}
	
	public void create() {

		this.eventData = new EventData(world, this, UserDataTypes.WALL);
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, 1, 0, false, true, Constants.BIT_WALL, 
				(short) (Constants.BIT_PLAYER | Constants.BIT_ENEMY | Constants.BIT_PROJECTILE | Constants.BIT_WALL),
				(short) 0, false, eventData);
	}
}
