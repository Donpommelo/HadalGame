package com.mygdx.hadal.equip.ranged;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.mygdx.hadal.equip.Equipable;
import com.mygdx.hadal.equip.RangedWeapon;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.bodies.hitboxes.HitboxImage;
import com.mygdx.hadal.schmucks.strategies.HitboxDefaultStrategy;
import com.mygdx.hadal.schmucks.strategies.HitboxOnContactStickStrategy;
import com.mygdx.hadal.schmucks.strategies.HitboxOnDieExplodeStrategy;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.HitboxFactory;
import static com.mygdx.hadal.utils.Constants.PPM;

public class StickyBombLauncher extends RangedWeapon {

	private final static String name = "Stickybomb Launcher";
	private final static int clipSize = 6;
	private final static float shootCd = 0.25f;
	private final static float shootDelay = 0.0f;
	private final static float reloadTime = 0.4f;
	private final static int reloadAmount = 1;
	private final static float recoil = 0.0f;
	private final static float projectileSpeed = 25.0f;
	private final static int projectileWidth = 50;
	private final static int projectileHeight = 50;
	private final static float lifespanx = 20.0f;
	private final static float gravity = 1;
	
	private final static int projDura = 1;
	private final static int maxBombs = 6;
	
	private final static int explosionRadius = 250;
	private final static float explosionDamage = 40.0f;
	private final static float explosionKnockback = 18.0f;	
	
	private final static String weapSpriteId = "stickybomblauncher";
	private final static String weapEventSpriteId = "event_stickybomb";
	private final static String projSpriteId = "orb_yellow";

	private static Queue<Hitbox> bombsLaid = new Queue<Hitbox>();

	private final static HitboxFactory onShoot = new HitboxFactory() {		
		
		@Override
		public void makeHitbox(final Schmuck user, PlayState state, Equipable tool, Vector2 startVelocity, float x, float y, final short filter) {
			
			Hitbox hbox = new HitboxImage(state, x, y, projectileWidth, projectileHeight, gravity, lifespanx, projDura, 0, startVelocity,
					filter, true, true, user, projSpriteId);
			
			hbox.addStrategy(new HitboxDefaultStrategy(state, hbox, user.getBodyData()));
			hbox.addStrategy(new HitboxOnDieExplodeStrategy(state, hbox, user.getBodyData(), tool, explosionRadius, explosionDamage, explosionKnockback, (short)0));
			hbox.addStrategy(new HitboxOnContactStickStrategy(state, hbox, user.getBodyData(), true, true));
			
			if (bombsLaid.size >= maxBombs) {
				bombsLaid.removeFirst().die();
			}
			bombsLaid.addLast(hbox);
			
		}
		
	};
	
	public StickyBombLauncher(Schmuck user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, onShoot, weapSpriteId, weapEventSpriteId);
	}
	
	@Override
	public void execute(PlayState state, BodyData shooter) {
		//Check clip size. empty clip = reload instead. This makes reloading automatic.
		if (clipLeft > 0 && weaponVelo != null) {
			
			shooter.statusProcTime(8, null, 0, null, this, null);
			
			//Generate the hitbox(s). This method's return is unused, so it may not return a hitbox or whatever at all.
			onShoot.makeHitbox(user, state, this, weaponVelo, 
					shooter.getSchmuck().getBody().getPosition().x * PPM, 
					shooter.getSchmuck().getBody().getPosition().y * PPM, 
					faction);
			
			clipLeft--;
			
			//If player fires in the middle of reloading, reset reload progress
			reloading = false;
			reloadCd = getReloadTime();
			
			//process weapon recoil.
			user.recoil(x, y, recoil * (1 + shooter.getBonusRecoil()));
		} 
	}
	
	@Override
	public void reload(float delta) {
		
		for (Hitbox bomb : bombsLaid) {
			bomb.die();
		}
		bombsLaid.clear();
		
		super.reload(delta);
	}

}
