package com.mygdx.hadal.save;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.mygdx.hadal.managers.GameStateManager;

/**
 * A record represents the player's persistent saved data.
 * @author Zachary Tu
 *
 */
public class Record {

	//This is the amount of currency the player has accrued
	private int scrap;
	
	//This is a map of the player's quest flags
	private Map<String, Integer> flags;
	
	//This is a map of the player's unlocks
	private Map<String, Integer> hiScores;
	private Map<String, Boolean> unlockEquip;
	private Map<String, Boolean> unlockArtifact;
	private Map<String, Boolean> unlockActive;
	private Map<String, Boolean> unlockCharacter;
	private Map<String, Boolean> unlockLevel;
	
	public Record() {}
	
	public boolean updateScore(int score, UnlockLevel level) {
		if (hiScores.containsKey(level.toString())) {
			if (score > hiScores.get(level.toString())) {
				hiScores.put(level.toString(), score);
				
				saveRecord();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This increments the player's scrap and saves
	 */
	public void incrementScrap(int amount) {
		scrap = scrap + amount;
		saveRecord();
	}
	
	public void setScrap(int scrap) {
		this.scrap = scrap;
		saveRecord();
	}
	
	/**
	 * This simple saves the record in a designated file
	 */
	public void saveRecord() {
		Gdx.files.local("save/Records.json").writeString(GameStateManager.json.prettyPrint(this), false);
	}
	
	/**
	 * a new record is created if no valid record is found
	 * This new record has default values for all fields
	 */
	public static void createNewRecord() {
		Record newRecord = new Record();
		newRecord.scrap = 0;
		
		newRecord.flags = new HashMap<String, Integer>();
		newRecord.flags.put("ARTIFACTSLOT1", 1);
		newRecord.flags.put("ARTIFACTSLOT2", 1);
		newRecord.flags.put("ARTIFACTSLOT3", 1);
		newRecord.flags.put("ARTIFACTSLOT4", 1);
		newRecord.flags.put("ARTIFACTSLOT5", 0);
		newRecord.flags.put("HUB_REACHED", 1);
		newRecord.flags.put("BOSS1", 1);
		newRecord.flags.put("WRECK0SC", 1);
		newRecord.flags.put("WRECK1SC", 1);
		newRecord.flags.put("DERELICTTA1", 1);
		newRecord.flags.put("DERELICTTA2", 1);
		newRecord.flags.put("DERELICTTB1", 1);
		newRecord.flags.put("DERELICTTB2", 1);
		newRecord.flags.put("DERELICTTB3", 1);
		newRecord.flags.put("DERELICTTB4", 1);
		newRecord.flags.put("DERELICTTB5", 1);
		newRecord.flags.put("DERELICTTB6", 1);
		newRecord.flags.put("PLENUMTURBINE", 0);
		
		newRecord.hiScores = new HashMap<String, Integer>();
		newRecord.unlockEquip = new HashMap<String, Boolean>();
		newRecord.unlockArtifact = new HashMap<String, Boolean>();
		newRecord.unlockActive = new HashMap<String, Boolean>();
		newRecord.unlockCharacter = new HashMap<String, Boolean>();
		newRecord.unlockLevel = new HashMap<String, Boolean>();
		
		for (UnlockEquip equip: UnlockEquip.values()) {
			newRecord.unlockEquip.put(equip.toString(), true);
		}
		
		for (UnlockArtifact artifact: UnlockArtifact.values()) {
			newRecord.unlockArtifact.put(artifact.toString(), true);
		}
		
		for (UnlockActives active: UnlockActives.values()) {
			newRecord.unlockActive.put(active.toString(), true);
		}
		
		for (UnlockCharacter character: UnlockCharacter.values()) {
			newRecord.unlockCharacter.put(character.toString(), true);
		}
		
		for (UnlockLevel level: UnlockLevel.values()) {
			newRecord.unlockLevel.put(level.toString(), true);
		}
		
		newRecord.hiScores.put("ARENA_HORIZON", 0);
		newRecord.hiScores.put("ARENA_LAGAN", 0);
		
		Gdx.files.local("save/Records.json").writeString(GameStateManager.json.prettyPrint(newRecord), false);
	}
	
	public int getScrap() { return scrap; }
	
	public int getSlotsUnlocked() { 
		
		int slots = 0;
		
		if (flags.get("ARTIFACTSLOT1") == 1) {
			slots++;
		}
		
		if (flags.get("ARTIFACTSLOT2") == 1) {
			slots++;
		}
		
		if (flags.get("ARTIFACTSLOT3") == 1) {
			slots++;
		}
		
		if (flags.get("ARTIFACTSLOT4") == 1) {
			slots++;
		}
		
		if (flags.get("ARTIFACTSLOT5") == 1) {
			slots++;
		}
		
		return slots; 
	}
	
	public Map<String, Integer> getFlags() { return flags; }

	public Map<String, Integer> getHiScores() { return hiScores; }
	
	public Map<String, Boolean> getUnlockEquip() { return unlockEquip; }

	public Map<String, Boolean> getUnlockArtifact() { return unlockArtifact; }

	public Map<String, Boolean> getUnlockActive() {	return unlockActive; }

	public Map<String, Boolean> getUnlockCharacter() { return unlockCharacter; }
	
	public Map<String, Boolean> getUnlockLevel() { return unlockLevel; }	
}
