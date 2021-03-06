package com.mygdx.hadal.server;

import com.mygdx.hadal.equip.Loadout;

/**
 * This class contains all the fields that are tracked during a match.
 * These are synced when the results state appears and let clients display information about each player's performance.
 * @author Shimpernickel Slatatron
 *
 */
public class SavedPlayerFieldsExtra {

	private float damageDealtEnemies, damageDealtAllies, damageDealtSelf, damageReceived;
	private Loadout loadout;

	public SavedPlayerFieldsExtra() {}
	
	public void newLevelReset() {
		damageDealtEnemies = 0.0f;
		damageDealtAllies = 0.0f;
		damageDealtSelf = 0.0f;
		damageReceived = 0.0f;
	}

	public float getDamageDealt() { return damageDealtEnemies; }

	public void setDamageDealt(float damageDealtEnemies) { this.damageDealtEnemies = damageDealtEnemies; }

	public void incrementDamageDealt(float damageDealt) { this.damageDealtEnemies += damageDealt; }
	
	public float getDamageDealtSelf() { return damageDealtSelf; }

	public void incrementDamageDealtSelf(float damageDealt) { this.damageDealtSelf += damageDealt; }
	
	public float getDamageDealtAllies() { return damageDealtAllies; }

	public void setDamageDealtAllies(float damageDealtAllies) { this.damageDealtAllies = damageDealtAllies; }

	public void incrementDamageDealtAllies(float damageDealt) { this.damageDealtAllies += damageDealt; }

	public float getDamageReceived() { return damageReceived; }

	public void setDamageReceived(float damageReceived) { this.damageReceived = damageReceived; }

	public void incrementDamageReceived(float damageReceived) {	this.damageReceived += damageReceived; }

	public Loadout getLoadout() { return loadout; }

	public void setLoadout(Loadout loadout) { this.loadout = loadout; }
}
