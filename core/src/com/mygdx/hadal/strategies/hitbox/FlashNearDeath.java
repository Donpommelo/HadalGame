package com.mygdx.hadal.strategies.hitbox;

import com.mygdx.hadal.effects.Shader;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.strategies.HitboxStrategy;

/**
 * this strategy makes a hitbox flash when its lifespan is below a specified threshold. It is usually used for explosive projectiles
 * @author Hugdanoff Hapodilla
 */
public class FlashNearDeath extends HitboxStrategy {
	
	//the hbox will start flashing when its hp falls below this
	private final float flashLifespan;
	
	//the duration of each flash
	private static final float flashDuration = 0.1f;
	
	public FlashNearDeath(PlayState state, Hitbox proj, BodyData user, float flashLifespan) {
		super(state, proj, user);
		this.flashLifespan = flashLifespan;
	}
	
	@Override
	public void controller(float delta) {

		if (hbox.getLifeSpan() <= flashLifespan) {
			if (hbox.getShaderCount() < -flashDuration) {
				hbox.setShader(Shader.WHITE, flashDuration);
			}
		}
	}
}
