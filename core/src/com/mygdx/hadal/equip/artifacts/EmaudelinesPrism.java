package com.mygdx.hadal.equip.artifacts;

import static com.mygdx.hadal.utils.Constants.PPM;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.equip.Equipable;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatChangeStatus;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.statuses.StatusComposite;
import com.mygdx.hadal.utils.Stats;

public class EmaudelinesPrism extends Artifact {

	private final static String name = "Emaudeline's Prism";
	private final static String descr = "Split Shot";
	private final static String descrLong = "";
	private final static int statusNum = 1;
	
	private final static int spread = 30;
	
	public EmaudelinesPrism() {
		super(name, descr, descrLong, statusNum);
	}

	@Override
	public Status[] loadEnchantments(PlayState state, BodyData b) {
		enchantment[0] = new StatusComposite(state, name, descr, b, 
				new StatChangeStatus(state, Stats.RANGED_ATK_SPD, -2.5f, b),
				new Status(state, name, descr, b) {
			
			private Vector2 projAngle = new Vector2();
			private Vector2 projVelo= new Vector2();
			
			@Override
			public void onShoot(Equipable tool) {				
				projVelo.set(tool.getWeaponVelo());
				projAngle.set(projVelo).setAngle(tool.getWeaponVelo().angle() + spread);
				
				inflicted.getCurrentTool().fire(state, inflicted.getSchmuck(), projAngle,
						inflicted.getSchmuck().getPosition().x * PPM, 
						inflicted.getSchmuck().getPosition().y * PPM,
						inflicted.getSchmuck().getHitboxfilter());
				
				projAngle.set(projVelo).setAngle(tool.getWeaponVelo().angle() - spread);
				inflicted.getCurrentTool().fire(state, inflicted.getSchmuck(), 
						projAngle.set(tool.getWeaponVelo()).setAngle(tool.getWeaponVelo().angle() - 20),
						inflicted.getSchmuck().getPosition().x * PPM, 
						inflicted.getSchmuck().getPosition().y * PPM,
						inflicted.getSchmuck().getHitboxfilter());
			}
		});
		return enchantment;
	}
}
