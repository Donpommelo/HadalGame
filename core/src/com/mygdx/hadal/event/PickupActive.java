package com.mygdx.hadal.event;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.ActiveItem;
import com.mygdx.hadal.equip.actives.NothingActive;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.event.userdata.InteractableEventData;
import com.mygdx.hadal.event.utility.TriggerAlt;
import com.mygdx.hadal.save.UnlockActives;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.server.Packets;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.UnlocktoItem;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

/**
 * This event, when interacted with, will give the player a new active item.
 * If the player's slots are full, this will replace currently held active item.
 * 
 * Triggered Behavior: When triggered, this event is toggled on/off to unlock/lock pickup
 * Triggering Behavior: This event will trigger its connected event when picked up.
 * 
 * Fields:
 * pool: String, comma separated list of equipunlock enum names of all items that could appear here.
 * 	if this is equal to "", return any weapon in the random pool.
 * startOn: boolean of whether the event starts on or off. Optiona;. Default: True.
 * 
 * @author Zachary Tu
 *
 */
public class PickupActive extends Event {

	//This is the weapon that will be picked up when interacting with this event.
	private ActiveItem item;
	private UnlockActives unlock;
	
	private String pool;
	
	public PickupActive(PlayState state, Vector2 startPos, String pool) {
		super(state, startPos, new Vector2(Event.defaultPickupEventSize, Event.defaultPickupEventSize));
		this.pool = pool;
		
		//Set this pickup to a random weapon in the input pool
		unlock = UnlockActives.valueOf(UnlockActives.getRandItemFromPool(state.getGsm().getRecord(), pool));
		setActive(UnlocktoItem.getUnlock(unlock, null));
	}
	
	@Override
	public void create() {
		this.eventData = new InteractableEventData(this) {
			
			@Override
			public void onInteract(Player p) {
				preActivate(null, p);
			}
			
			@Override
			public void onActivate(EventData activator, Player p) {
				
				if (activator != null) {
					if (activator.getEvent() instanceof TriggerAlt) {
						String msg = ((TriggerAlt)activator.getEvent()).getMessage();
						if (msg.equals("roll")) {
							unlock = UnlockActives.valueOf(UnlockActives.getRandItemFromPool(state.getGsm().getRecord(), pool));
							setActive(UnlocktoItem.getUnlock(unlock, null));
						} else {
							unlock = UnlockActives.valueOf(UnlockActives.getRandItemFromPool(state.getGsm().getRecord(), pool));
							setActive(UnlocktoItem.getUnlock(unlock, null));
						}
					}
					return;
				}
				
				if (item instanceof NothingActive) {
					return;
				}
				
				//If player inventory is full, replace their current weapon.
				item.setUser(p);
				ActiveItem temp = p.getPlayerData().pickup(item);
				setActive(temp);
			}
			
			@Override
			public void preActivate(EventData activator, Player p) {
				onActivate(activator, p);
				HadalGame.server.sendToAllTCP(new Packets.SyncPickup(entityID.toString(), UnlockActives.getUnlockFromActive(item.getClass()).toString()));
			}
		};
		
		this.body = BodyBuilder.createBox(world, startPos, size, 1, 1, 0, true, true, Constants.BIT_SENSOR, (short) (Constants.BIT_PLAYER),	(short) 0, true, eventData);
	}

	@Override
	public void render(SpriteBatch batch) {
		if (!(item instanceof NothingActive)) {
			super.render(batch);
		}
		
		HadalGame.SYSTEM_FONT_SPRITE.getData().setScale(1.0f);
		float y = getPixelPosition().y + size.y / 2;
		HadalGame.SYSTEM_FONT_SPRITE.draw(batch, item.getName(), getPixelPosition().x - size.x / 2, y);
	}
	
	@Override
	public Object onServerCreate() {
		return new Packets.CreatePickup(entityID.toString(), getPixelPosition(), PickupType.ACTIVE, unlock.toString());
	}
	
	@Override
	public void onClientSync(Object o) {
		if (o instanceof Packets.SyncPickup) {
			Packets.SyncPickup p = (Packets.SyncPickup) o;
			setActive(UnlocktoItem.getUnlock(UnlockActives.valueOf(p.newPickup), null));
		} else {
			super.onClientSync(o);
		}
	}
	
	public void setActive(ActiveItem item) {
		this.item = item;
		if (item instanceof NothingActive) {
			if (standardParticle != null) {
				standardParticle.turnOff();
			}
		} else {
			if (standardParticle != null) {
				standardParticle.turnOn();
			}
		}
	}
	
	@Override
	public void loadDefaultProperties() {
		setEventSprite(Sprite.CUBE);
	}
}
