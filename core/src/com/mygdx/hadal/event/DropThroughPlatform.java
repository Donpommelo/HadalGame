package com.mygdx.hadal.event;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.schmucks.bodies.HadalEntity;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.schmucks.userdata.FeetData;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

/**
 * This event is a solid block that can be passed by hitboxes, but not the player.
 * When the player presses crouch when standing on it, they will pass through it.
 * 
 * Triggered Behavior: N/A
 * Triggering Behavior: N/A
 * 
 * Fields:
 * N/A
 * 
 * @author Thaditya Troseidon
 */
public class DropThroughPlatform extends Event {
	
	public DropThroughPlatform(PlayState state, Vector2 startPos, Vector2 size) {
		super(state, startPos, size);
	}
	
	@Override
	public void create() {

		this.eventData = new EventData(this) {
			
			/**
			 * When touching the player's foot sensor, this event sets the player's filter to collide with dropthrough platforms.
			 * This also makes it so that when coming from below, the player can pass through until their feet touch it.
			 * This sudden collision causes the player to "pop" above the platform. Make these platforms skinny.
			 */
			@Override
			public void onTouch(HadalData fixB) {
				if (fixB != null) {
					if (fixB instanceof FeetData) {
						
						HadalEntity entity = fixB.getEntity();
						
						//let a fastfalling player drop through without stopping
						if (entity instanceof Player) {
							Player p = (Player) entity;
							
							if (p.isFastFalling()) { return; }
						}
						Filter filter = entity.getMainFixture().getFilterData();
						filter.maskBits = (short) (filter.maskBits | Constants.BIT_DROPTHROUGHWALL);

						entity.getMainFixture().setFilterData(filter);
						
						((FeetData) fixB).getTerrain().add(this.event);
					}
				}
			}
			
			/**
			 * When the player's feet sensor leaves the platform, its filter becomes passable by dropthrough again.
			 */
			@Override
			public void onRelease(HadalData fixB) {
				if (fixB != null) {
					if (fixB instanceof FeetData) {
						HadalEntity entity = fixB.getEntity();
						Filter filter = entity.getMainFixture().getFilterData();
						filter.maskBits = (short) (filter.maskBits &~ Constants.BIT_DROPTHROUGHWALL);
						entity.getMainFixture().setFilterData(filter);
						
						((FeetData) fixB).getTerrain().remove(this.event);
					}
				}
			}
			
			/**
			 * When the player crouches on this event, this platform sets its filter to no longer collide.
			 */
			@Override
			public void onInteract(Player p) {
				Filter filter = p.getMainFixture().getFilterData();
				filter.maskBits = (short) (filter.maskBits &~ Constants.BIT_DROPTHROUGHWALL);
				p.getMainFixture().setFilterData(filter);
			}
		};

		this.body = BodyBuilder.createBox(world, startPos, size, 1, 1, 0, false, true,
			Constants.BIT_DROPTHROUGHWALL, (short) (Constants.BIT_WALL | Constants.BIT_SENSOR | Constants.BIT_PLAYER | Constants.BIT_PROJECTILE | Constants.BIT_ENEMY),
				(short) 0, false, eventData);
		
		this.body.setType(BodyDef.BodyType.KinematicBody);
	}	
	
	@Override
	public void loadDefaultProperties() {
		setEventSprite(Sprite.UI_RELOAD_BAR);
		setScaleAlign("CENTER_STRETCH");
		setSyncType(eventSyncTypes.ALL);
	}
}
