package com.mygdx.hadal.equip;

import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.hadal.schmucks.UserDataTypes;
import com.mygdx.hadal.schmucks.bodies.Hitbox;
import com.mygdx.hadal.schmucks.bodies.HadalEntity;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.schmucks.userdata.HitboxData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.HitboxFactory;

import box2dLight.RayHandler;

public class Scattergun extends RangedWeapon {

	private final static String name = "CR4P Cannon";
	private final static int clipSize = 2;
	private final static float shootCd = 0.25f;
	private final static float shootDelay = 0.0f;
	private final static float reloadTime = 0.8f;
	private final static int reloadAmount = 2;
	private final static float baseDamage = 9.0f;
	private final static float recoil = 3.0f;
	private final static float knockback = 2.0f;
	private final static float projectileSpeed = 20.0f;
	private final static int projectileWidth = 10;
	private final static int projectileHeight = 10;
	private final static float lifespan = 0.5f;
	private final static float gravity = 0.5f;
	
	private final static int projDura = 2;
	
	private final static int numProj = 10;
	private final static int spread = 10;
	
	private final static HitboxFactory onShoot = new HitboxFactory() {

		@Override
		public Hitbox makeHitbox(PlayState state, Vector2 startVelocity, float x, float y, short filter,
				World world, OrthographicCamera camera,
				RayHandler rays) {
			
			for (int i = 0; i < numProj; i++) {
				
				float newDegrees = (float) (startVelocity.angle() + (ThreadLocalRandom.current().nextInt(-spread, spread + 1)));
				
				Hitbox proj = new Hitbox(state, x, y, projectileWidth, projectileHeight, gravity, lifespan, projDura, 0, startVelocity.setAngle(newDegrees),
						filter, true, world, camera, rays);
				proj.setUserData(new HitboxData(state, world, proj) {
					
					public void onHit(HadalData fixB) {
						if (fixB != null) {
							if (fixB.getType().equals(UserDataTypes.BODY)) {
								((BodyData) fixB).receiveDamage(baseDamage, this.hbox.body.getLinearVelocity().nor().scl(knockback));
							}
						}
						super.onHit(fixB);
					}
				});		
			}
			return null;
		}
		
	};
	
	public Scattergun(HadalEntity user) {
		super(user, name, clipSize, reloadTime, recoil, projectileSpeed, shootCd, shootDelay, reloadAmount, onShoot);
	}

}
