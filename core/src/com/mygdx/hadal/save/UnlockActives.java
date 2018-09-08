package com.mygdx.hadal.save;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Array;
import com.mygdx.hadal.save.UnlockManager.UnlockTag;
import com.mygdx.hadal.equip.ActiveItem;
import com.mygdx.hadal.equip.actives.*;

public enum UnlockActives {
	
	HONEYCOMB(Honeycomb.class),
	FIREBALL(Fireball.class),
	MELON(Melon.class),
	NOTHING(Empty.class),
	MISSILE_POD(MissilePod.class),
	RELOADER(Reloader.class),
	RESERVED_FUEL(ReservedFuel.class),

	;
	
	private Class<? extends ActiveItem> active;
	private InfoItem info;
	
	UnlockActives(Class<? extends ActiveItem> active) {
		this.active = active;
	}
	
	public static Array<UnlockActives> getUnlocks(boolean unlock, UnlockTag... tags) {
		Array<UnlockActives> items = new Array<UnlockActives>();
		
		for (UnlockActives u : UnlockActives.values()) {
			boolean get = false;
			
			for (int i = 0; i < tags.length; i++) {
				for (int j = 0; j < u.getTags().size(); j++) {
					if (tags[i].equals(u.getTags().get(j))) {
						get = true;
					}
				}
			}
			
			if (unlock && !u.isUnlocked()) {
				get = false;
			}
			
			if (get) {
				items.add(u);
			}
		}
		
		return items;
	}
	
	public Class<? extends ActiveItem> getActive() {
		return active;
	}
	
	public InfoItem getInfo() {
		return info;
	}
	
	public void setInfo(InfoItem info) {
		this.info = info;
	}
	
	public boolean isUnlocked() {
		return info.isUnlocked();
	}
	
	public ArrayList<UnlockTag> getTags() {
		return info.getTags();
	}
	
	public String getName() {
		return info.getName();
	}
	
	public String getDescr() {
		return info.getDescription();
	}
	
	public int getCost() {
		return info.getCost();
	}
	
	public void setUnlocked(boolean unlock) {
		info.setUnlocked(unlock);
	}
	
}

