package com.mygdx.hadal.event;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.mygdx.hadal.equip.WeaponUtils;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

/**
 * @author Zachary Tu
 *
 */
public class SpawnerScrap extends Event {

	private int scrap;
	
	public SpawnerScrap(PlayState state, Vector2 startPos, Vector2 size, int scrap) {
		super(state, startPos, size);
		this.scrap = scrap;
	}
	
	@Override
	public void create() {
		this.eventData = new EventData(this) {
			
			@Override
			public void onActivate(EventData activator, Player p) {
				WeaponUtils.spawnScrap(state, scrap, event.getPixelPosition());
			}
		};
		
		this.body = BodyBuilder.createBox(world, startPos, size, 0, 1, 0, true, true, Constants.BIT_SENSOR, (short) (0), (short) 0, true, eventData);
		body.setType(BodyType.KinematicBody);
	}
}