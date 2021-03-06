package com.mygdx.hadal.equip.actives;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.ActiveItem;
import com.mygdx.hadal.schmucks.bodies.HadalEntity;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.enemies.TurretVolley;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.bodies.hitboxes.RangedHitbox;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.Summoned;
import com.mygdx.hadal.statuses.Temporary;
import com.mygdx.hadal.strategies.HitboxStrategy;
import com.mygdx.hadal.strategies.hitbox.ControllerDefault;
import com.mygdx.hadal.strategies.hitbox.DieSound;
import com.mygdx.hadal.utils.Constants;

/**
 * @author Bichnold Boppigginbotham
 */
public class PortableSentryVolley extends ActiveItem {

	private static final float usecd = 0.0f;
	private static final float usedelay = 0.1f;
	private static final float maxCharge = 20.0f;
	
	private static final Vector2 projectileSize = new Vector2(70, 70);
	private static final float lifespan = 3.0f;

	private static final float projectileSpeed = 60.0f;
	private static final float turretLifespan = 20.0f;
	
	private static final Sprite projSprite = Sprite.ORB_BLUE;

	public PortableSentryVolley(Schmuck user) {
		super(user, usecd, usedelay, maxCharge, chargeStyle.byDamageInflict);
	}
	
	@Override
	public void useItem(PlayState state, PlayerBodyData user) {
		
		final boolean faceRight = weaponVelo.x > 0;
		
		Hitbox hbox = new RangedHitbox(state, user.getPlayer().getPixelPosition(), projectileSize, lifespan,  new Vector2(0, -projectileSpeed), user.getPlayer().getHitboxfilter(), false, false, user.getPlayer(), projSprite);
		hbox.setPassability((short) (Constants.BIT_WALL | Constants.BIT_DROPTHROUGHWALL));
		hbox.setGravity(3.0f);
		
		hbox.addStrategy(new ControllerDefault(state, hbox, user));
		hbox.addStrategy(new DieSound(state, hbox, user, SoundEffect.CYBER2, 0.4f));
		hbox.addStrategy(new HitboxStrategy(state, hbox, user) {
			
			private HadalEntity floor;
			
			@Override
			public void onHit(HadalData fixB) {
				if (fixB != null) {
					floor = fixB.getEntity();
					hbox.die();
				}
			}
			
			@Override
			public void die() {
				new TurretVolley(state, hbox.getPixelPosition(), faceRight ? 0 : 180, hbox.getFilter(), null) {
					
					@Override
					public void create() {
						super.create();
						body.setType(BodyDef.BodyType.DynamicBody);
						getBodyData().addStatus(new Temporary(state, turretLifespan, getBodyData(), getBodyData(), turretLifespan));
						getBodyData().addStatus(new Summoned(state, getBodyData(), user.getPlayer()));
						
						if (floor != null) {
							if (floor.getBody() != null) {
								WeldJointDef joint = new WeldJointDef();
								joint.bodyA = floor.getBody();
								joint.bodyB = getBody();
								joint.localAnchorA.set(new Vector2(getPosition()).sub(floor.getPosition()));
								joint.localAnchorB.set(0, 0);
								state.getWorld().createJoint(joint);
							}
						}
					}
				};
			}
		});
		hbox.setFriction(1.0f);
	}
}
