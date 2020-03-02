package com.mygdx.hadal.strategies.hitbox;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.strategies.HitboxStrategy;

/**
 * This is the default strategy that most hboxes have
 * @author Zachary Tu
 *
 */
public class ControllerStatic extends HitboxStrategy{
	
	public ControllerStatic(PlayState state, Hitbox proj, BodyData user) {
		super(state, proj, user);
	}
	
	@Override
	public void controller(float delta) {
		
		//default behavior of hboxes is to die when lifespan expires
		hbox.setLifeSpan(hbox.getLifeSpan() - delta);
		if (hbox.getLifeSpan() <= 0) {
			hbox.die();
		}
	}
	
	@Override
	public void onHit(HadalData fixB) {}
	
	@Override
	public void die() {
		hbox.queueDeletion();
	}
	
	@Override
	public void render(SpriteBatch batch) {}
}