package com.mygdx.hadal.equip.ranged;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.RangedWeapon;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.bodies.hitboxes.RangedHitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.strategies.hitbox.*;

import java.util.concurrent.ThreadLocalRandom;

public class Flounderbuss extends RangedWeapon {

	private static final int clipSize = 1;
	private static final int ammoSize = 15;
	private static final float shootCd = 0.0f;
	private static final float shootDelay = 0.0f;
	private static final float reloadTime = 0.8f;
	private static final int reloadAmount = 0;
	private static final float baseDamage = 15.0f;
	private static final float recoil = 30.0f;
	private static final float knockback = 12.0f;
	private static final float projectileSpeed = 30.0f;
	private static final Vector2 projectileSize = new Vector2(36, 30);
	private static final float lifespan = 2.0f;
	
	private static final Sprite[] projSprites = {Sprite.FLOUNDER_A, Sprite.FLOUNDER_B};
	private static final Sprite weaponSprite = Sprite.MT_SHOTGUN;
	private static final Sprite eventSprite = Sprite.P_SHOTGUN;
	
	private static final float maxCharge = 0.5f;
	private static final float veloSpread = 0.6f;

	private static final int maxNumProj = 15;
	private static final int spread = 20;
	
	public Flounderbuss(Schmuck user) {
		super(user, clipSize, ammoSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, true, weaponSprite, eventSprite, projectileSize.x, maxCharge);
	}
	
	@Override
	public void mouseClicked(float delta, PlayState state, BodyData shooter, short faction, Vector2 mousePosition) {
		super.mouseClicked(delta, state, shooter, faction, mousePosition);

		if (reloading || getClipLeft() == 0) { return; }
		
		charging = true;
		
		//while held, build charge until maximum (if not reloading)
		if (chargeCd < getChargeTime()) {
			setChargeCd(chargeCd + delta);
		}
	}
	
	@Override
	public void execute(PlayState state, BodyData shooter) {}
	
	@Override
	public void release(PlayState state, BodyData bodyData) {
		super.execute(state, bodyData);
		charging = false;
		chargeCd = 0;
	}
	
	private final Vector2 newVelocity = new Vector2();
	@Override
	public void fire(PlayState state, Schmuck user, Vector2 startPosition, Vector2 startVelocity, short filter) {
		SoundEffect.SHOTGUN.playUniversal(state, startPosition, 0.75f, 0.75f, false);

		//amount of projectiles scales to charge percent
		for (int i = 0; i < maxNumProj * chargeCd / getChargeTime() + 1; i++) {
			
			int randomIndex = ThreadLocalRandom.current().nextInt(projSprites.length);
			Sprite projSprite = projSprites[randomIndex];
			newVelocity.set(startVelocity).scl((ThreadLocalRandom.current().nextFloat() * veloSpread + 1 - veloSpread / 2));
			
			Hitbox hbox = new RangedHitbox(state, startPosition, projectileSize, lifespan, new Vector2(newVelocity), filter, true, true, user, projSprite);
			hbox.setGravity(1.5f);
			hbox.setDurability(2);

			hbox.addStrategy(new ControllerDefault(state, hbox, user.getBodyData()));
			hbox.addStrategy(new AdjustAngle(state, hbox, user.getBodyData()));
			hbox.addStrategy(new ContactWallParticles(state, hbox, user.getBodyData(), Particle.SPARKS));
			hbox.addStrategy(new ContactUnitLoseDurability(state, hbox, user.getBodyData()));
			hbox.addStrategy(new ContactWallDie(state, hbox, user.getBodyData()));
			hbox.addStrategy(new DamageStandard(state, hbox, user.getBodyData(), baseDamage, knockback, DamageTypes.FISH, DamageTypes.RANGED));
			hbox.addStrategy(new ContactUnitSound(state, hbox, user.getBodyData(), SoundEffect.DAMAGE1, 0.25f, true));
			hbox.addStrategy(new Spread(state, hbox, user.getBodyData(), spread));
		}
	}
}
