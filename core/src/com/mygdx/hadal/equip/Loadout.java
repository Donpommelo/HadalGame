package com.mygdx.hadal.equip;

import java.util.ArrayList;

import com.mygdx.hadal.save.Record;
import com.mygdx.hadal.save.UnlockActives;
import com.mygdx.hadal.save.UnlockArtifact;
import com.mygdx.hadal.save.UnlockCharacter;
import com.mygdx.hadal.save.UnlockEquip;

public class Loadout {

	private final static int numSlots = 3;
	
	public UnlockEquip[] multitools;
	
	public UnlockArtifact startifact;
	public ArrayList<UnlockArtifact> artifacts;
	
	public UnlockActives activeItem;
	
	public UnlockCharacter character;
	
	public Loadout() {}
		
	public Loadout(Record record) {
		multitools = new UnlockEquip[numSlots];
		multitools[0] = UnlockEquip.NOTHING;
		multitools[1] = UnlockEquip.NOTHING;
		multitools[2] = UnlockEquip.NOTHING;
		
		artifacts = new ArrayList<UnlockArtifact>();
		
		for (int i = 0; i < numSlots; i++) {
			if (record.getEquips().length > i) {
				multitools[i] = UnlockEquip.valueOf(record.getEquips()[i]);
			}
		}
		
		startifact = UnlockArtifact.valueOf(record.getArtifact());
		activeItem = UnlockActives.valueOf(record.getActive());
		character = UnlockCharacter.valueOf(record.getCharacter());
	}
	
	public Loadout(UnlockEquip... tools) {
		multitools = new UnlockEquip[numSlots];
		multitools[0] = UnlockEquip.NOTHING;
		multitools[1] = UnlockEquip.NOTHING;
		multitools[2] = UnlockEquip.NOTHING;
		
		artifacts = new ArrayList<UnlockArtifact>();

		for (int i = 0; i < numSlots; i++) {
			if (tools.length > i) {
				multitools[i] = tools[i];
			}
		}
		
		startifact = UnlockArtifact.NOTHING;
		activeItem = UnlockActives.NOTHING;
		character = UnlockCharacter.MOREAU;
	}
	
	public static int getNumSlots() {
		return numSlots;
	}
}
