package com.mygdx.hadal.equip.misc;

import static com.mygdx.hadal.utils.Constants.PPM;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.equip.MeleeWeapon;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.hitboxes.MeleeHitbox;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.schmucks.userdata.HitboxData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.utils.HitboxFactory;

public class Airblaster extends MeleeWeapon {

	private final static String name = "Airblaster";
	private final static float swingCd = 0.25f;
	private final static float windup = 0.0f;
	private final static float backSwing = 1.0f;
	private final static float baseDamage = 0.0f;
	private final static int hitboxSize = 300;
	private final static int swingArc = 300;
	private final static float knockback = 25.0f;
	private final static float momentum = -37.5f;
	
	
	private final static HitboxFactory onSwing = new HitboxFactory() {

		@Override
		public void makeHitbox(final Schmuck user, PlayState state, Vector2 startAngle, float x, float y, short filter) {
						
			MeleeHitbox hbox = new MeleeHitbox(state, x, y, hitboxSize, swingArc, swingCd, backSwing, startAngle, 
					startAngle.nor().scl(hitboxSize / 4 / PPM), (short) 0, user);
			
			hbox.setUserData(new HitboxData(state, hbox) {
				
				@Override
				public void onHit(HadalData fixB) {
					if (fixB != null) {
						if (!fixB.equals(user.getBodyData())) {
							fixB.receiveDamage(baseDamage, this.hbox.getBody().getLinearVelocity().nor().scl(knockback), 
									user.getBodyData(), false, DamageTypes.AIR, DamageTypes.DEFLECT, DamageTypes.REFLECT);
						}
					}
				}
				
			});
		}

	};
	
	public Airblaster(Schmuck user) {
		super(user, name, swingCd, windup, momentum, onSwing);
	}

}
