package com.mygdx.hadal.equip.ranged;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.equip.RangedWeapon;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.hitboxes.HitboxImage;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.schmucks.userdata.HitboxData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.utils.HitboxFactory;

public class ChargeBeam extends RangedWeapon {

	private final static String name = "Charge Beam";
	private final static int clipSize = 4;
	private final static float shootCd = 0.0f;
	private final static float shootDelay = 0.0f;
	private final static float reloadTime = 1.75f;
	private final static int reloadAmount = 0;
	private final static float baseDamage = 25.0f;
	private final static float recoil = 7.5f;
	private final static float knockback = 22.5f;
	private final static float projectileSpeed = 18.0f;
	private final static int projectileWidth = 30;
	private final static int projectileHeight = 30;
	private final static float lifespan = 3.0f;
	private final static float gravity = 0;
	
	private final static int projDura = 5;
	
	private static float chargeDura = 0.0f;
	private static int chargeStage = 0;
	private static final float maxCharge = 1.0f;
	
	private final static String weapSpriteId = "chargebeam";
	private final static String projSpriteId = "orb_yellow";
	
	private final static HitboxFactory onShoot = new HitboxFactory() {

		@Override
		public void makeHitbox(final Schmuck user, PlayState state, Vector2 startVelocity, float x, float y, short filter) {			
			
			if (chargeDura >= maxCharge) {
				chargeStage = 3;
			} else if (chargeDura >= 0.8f) {
				chargeStage = 2;
			} else if (chargeDura >= 0.4f) {
				chargeStage = 1;
			} else {
				chargeStage = 0;
			}
			
			float sizeMultiplier = 1;
			float speedMultiplier = 1;
			float damageMultiplier = 1;
			float kbMultiplier = 1;

			switch(chargeStage) {
			case 3:
				sizeMultiplier = 4.0f;
				speedMultiplier = 3.0f;
				damageMultiplier = 3.5f;
				kbMultiplier = 3.0f;
				break;
			case 2:
				sizeMultiplier = 3.0f;
				speedMultiplier = 2.0f;
				damageMultiplier = 2.5f;
				kbMultiplier = 2.0f;
				break;
			case 1:
				sizeMultiplier = 2.0f;
				speedMultiplier = 1.5f;
				damageMultiplier = 1.5f;
				kbMultiplier = 1.0f;
				break;
			}
			
			final float damageMultiplier2 = damageMultiplier;
			final float kbMultiplier2 = kbMultiplier;
			
			HitboxImage proj = new HitboxImage(state, x, y, (int)(projectileWidth * sizeMultiplier), (int)(projectileHeight * sizeMultiplier), gravity, lifespan, projDura, 0, startVelocity.scl(speedMultiplier),
					filter, true, user, projSpriteId);
			
			proj.setUserData(new HitboxData(state, proj) {
				
				@Override
				public void onHit(HadalData fixB) {
					if (fixB != null) {
						fixB.receiveDamage(baseDamage * damageMultiplier2, 
								this.hbox.getBody().getLinearVelocity().nor().scl(knockback * kbMultiplier2), 
								user.getBodyData(), true, DamageTypes.RANGED);
					}
					super.onHit(fixB);
				}
			});		
		}
		
	};
	
	public ChargeBeam(Schmuck user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, onShoot, weapSpriteId);
	}
	
	@Override
	public void mouseClicked(float delta, PlayState state, BodyData shooter, short faction, int x, int y) {
		if (chargeDura < maxCharge && !reloading) {
			chargeDura+=delta;
		}
		super.mouseClicked(delta, state, shooter, faction, x, y);
	}
	
	@Override
	public void execute(PlayState state, BodyData shooter) {

	}
	
	@Override
	public void release(PlayState state, BodyData bodyData) {
		super.execute(state, bodyData);
		chargeDura = 0;
	}
	
	@Override
	public String getText() {
		if (reloading) {
			return clipLeft + "/" + clipSize + " CHARGE: " + Math.round(chargeDura * 100 / maxCharge) + "% RELOADING";
		} else {
			return clipLeft + "/" + clipSize + " CHARGE: " + Math.round(chargeDura * 100 / maxCharge) + "%";

		}
	}
}
