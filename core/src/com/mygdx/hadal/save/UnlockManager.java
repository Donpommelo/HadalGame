package com.mygdx.hadal.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.mygdx.hadal.actors.DialogBox.DialogType;
import com.mygdx.hadal.managers.GameStateManager;
import com.mygdx.hadal.states.PlayState;

import java.util.ArrayList;
import java.util.Objects;

/**
 * The UnlockManager manages the player's unlocked weapons, artifacts, etc
 * @author Proctavio Prolkner
 */
public class UnlockManager {
	
	/**
	 * This retrieves the player's unlocks from a file
	 */
	public static void retrieveItemInfo() {
				
		for (JsonValue d : GameStateManager.reader.parse(Gdx.files.internal("save/Equips.json"))) {
			UnlockEquip.valueOf(d.name()).setInfo(GameStateManager.json.fromJson(InfoItem.class, d.toJson(OutputType.minimal)));
		}
		for (JsonValue d : GameStateManager.reader.parse(Gdx.files.internal("save/Artifacts.json"))) {
			UnlockArtifact.valueOf(d.name()).setInfo(GameStateManager.json.fromJson(InfoItem.class, d.toJson(OutputType.minimal)));
		}
		for (JsonValue d : GameStateManager.reader.parse(Gdx.files.internal("save/Actives.json"))) {
			UnlockActives.valueOf(d.name()).setInfo(GameStateManager.json.fromJson(InfoItem.class, d.toJson(OutputType.minimal)));
		}
		for (JsonValue d : GameStateManager.reader.parse(Gdx.files.internal("save/Characters.json"))) {
			UnlockCharacter.valueOf(d.name()).setInfo(GameStateManager.json.fromJson(InfoItem.class, d.toJson(OutputType.minimal)));
		}
		for (JsonValue d : GameStateManager.reader.parse(Gdx.files.internal("save/Levels.json"))) {
			UnlockLevel.valueOf(d.name()).setInfo(GameStateManager.json.fromJson(InfoItem.class, d.toJson(OutputType.minimal)));
		}
	}	
	
	/**
	 * This acquires the information about an unlock based on its type and name
	 */
	public static InfoItem getInfo(UnlockType type, String name) {
		switch(type) {
		case ACTIVE:
			return UnlockActives.valueOf(name).getInfo();
		case ARTIFACT:
			return UnlockArtifact.valueOf(name).getInfo();
		case CHARACTER:
			return UnlockCharacter.valueOf(name).getInfo();
		case EQUIP:
			return UnlockEquip.valueOf(name).getInfo();
		case LEVEL:
			return UnlockLevel.valueOf(name).getInfo();
		default:
			return null;
		}
	}
	
	/**
	 * This is used to determine which unlockitems will be available from a given hub event
	 * @param item: the item to check
	 * @param tags: a list of tags
	 * @return whether the item contains any of the tags
	 */
	public static boolean checkTags(InfoItem item, ArrayList<UnlockTag> tags) {

		for (UnlockTag tag : tags) {
			boolean tagPresent = false;

			if (item == null) {
				return false;
			}

			for (int j = 0; j < item.getTags().size(); j++) {
				if (tag.equals(item.getTags().get(j))) {
					tagPresent = true;
				}
			}
			if (!tagPresent) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This returns if a certain unlock item is unlocked or not
	 */
	public static boolean checkUnlock(PlayState state, UnlockType type, String name) {
		switch(type) {
		case ACTIVE:
			return state.getGsm().getRecord().getUnlockActive().getOrDefault(name, false);
		case ARTIFACT:
			return state.getGsm().getRecord().getUnlockArtifact().getOrDefault(name, false);
		case CHARACTER:
			return state.getGsm().getRecord().getUnlockCharacter().getOrDefault(name, false);
		case EQUIP:
			return state.getGsm().getRecord().getUnlockEquip().getOrDefault(name, false);
		case LEVEL:
			return state.getGsm().getRecord().getUnlockLevel().getOrDefault(name, false);
		default:
			return false;
		}
	}
	
	/**
	 * This sets a certain unlock item to be unlocked or locked. If unlocked, a notification appears
	 * After setting, the data is saved into the player's saves
	 */
	public static void setUnlock(PlayState state, UnlockType type, String name, boolean unlock) {
		switch(type) {
		case ACTIVE:
			state.getGsm().getRecord().getUnlockActive().put(name, unlock);
			if (unlock) {
				state.getDialogBox().addDialogue("", "UNLOCKED ACTIVE: " + Objects.requireNonNull(getInfo(type, name)).getName(),
					"", true, true, true, 3.0f, null, null, DialogType.SYSTEM);
			}
			break;
		case ARTIFACT:
			state.getGsm().getRecord().getUnlockArtifact().put(name, unlock);
			if (unlock) {
				state.getDialogBox().addDialogue("", "UNLOCKED ARTIFACT: " + Objects.requireNonNull(getInfo(type, name)).getName(),
					"", true, true, true, 3.0f, null, null, DialogType.SYSTEM);
			}
			break;
		case CHARACTER:
			state.getGsm().getRecord().getUnlockCharacter().put(name, unlock);
			if (unlock) {
				state.getDialogBox().addDialogue("", "UNLOCKED CHARACTER: " + Objects.requireNonNull(getInfo(type, name)).getName(),
					"", true, true, true, 3.0f, null, null, DialogType.SYSTEM);
			}
			break;
		case EQUIP:
			state.getGsm().getRecord().getUnlockEquip().put(name, unlock);
			if (unlock) {
				state.getDialogBox().addDialogue("", "UNLOCKED EQUIP: " + Objects.requireNonNull(getInfo(type, name)).getName(),
					"", true, true, true, 3.0f, null, null, DialogType.SYSTEM);
			}
			break;
		case LEVEL:
			state.getGsm().getRecord().getUnlockLevel().put(name, unlock);
			if (unlock) {
				state.getDialogBox().addDialogue("", "UNLOCKED LEVEL: " + Objects.requireNonNull(getInfo(type, name)).getName(),
					"", true, true, true, 3.0f, null, null, DialogType.SYSTEM);
			}
			break;
		default:
		}
		state.getGsm().getRecord().saveRecord();
	}
	
	public enum UnlockTag {
		ARMORY,
		RANDOM_POOL,
		RANGED,
		MELEE,
		RELIQUARY,
		DISPENSARY,
		DORMITORY,
		NAVIGATIONS,
		PAINTER,
		TRAINING,
		CAMPAIGN,
		QUARTERMASTER,
		NASU,
		MISC,

		BIRD,
		PVP,
		ARENA,
		MULTIPLAYER,
		SINGLEPLAYER,
		BOSS,
		SANDBOX,

		OFFENSE,
		DEFENSE,
		MOBILITY,
		FUEL,
		HEAL,
		ACTIVE_ITEM,
		AMMO,
		WEAPON_DAMAGE,
		PASSIVE_DAMAGE,
		PROJECTILE_MODIFIER,
		GIMMICK,
	}
	
	public enum UnlockType {
		EQUIP,
		ARTIFACT,
		ACTIVE,
		CHARACTER,
		LEVEL
	}
}
