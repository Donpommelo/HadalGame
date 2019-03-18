package com.mygdx.hadal.schmucks.strategies;


import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;

public class HitboxTravelDistanceDieStrategy extends HitboxStrategy {
	
	private Vector2 startLocation, endLocation;
	private float distance;
	private boolean initialized;
	
	public HitboxTravelDistanceDieStrategy(PlayState state, Hitbox proj, BodyData user, Vector2 endLocation) {
		super(state, proj, user);
		this.endLocation = endLocation;
		initialized = false;
	}
	
	@Override
	public void controller(float delta) {
		
		if (!initialized) {
			initialized = true;
			this.startLocation = new Vector2(hbox.getBody().getPosition());
			this.distance = startLocation.dst(endLocation);
		}

		if (startLocation.dst(hbox.getBody().getPosition()) >= distance) {
			hbox.die();
		}
	}
}