package com.mygdx.hadal.equip.melee;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.MeleeWeapon;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.strategies.HitboxStrategy;
import com.mygdx.hadal.strategies.hitbox.ControllerDefault;
import com.mygdx.hadal.strategies.hitbox.DamageStandardRepeatable;

public class MorningStar extends MeleeWeapon {

	private final static float swingCd = 0.5f;
	private final static float windup = 0.2f;
	
	private final static Vector2 projectileSize = new Vector2(60, 60);
	private final static Vector2 chainSize = new Vector2(20, 20);
	
	private final static Sprite weaponSprite = Sprite.MT_DEFAULT;
	private final static Sprite eventSprite = Sprite.P_DEFAULT;

	private final static Sprite chainSprite = Sprite.ORB_BLUE;
	private final static Sprite projSprite = Sprite.ORB_BLUE;
	
	private final static float baseDamage = 50.0f;
	private final static float knockback = 60.0f;

	private final static float swingForce = 2000.0f;
	private final static float range = 60.0f;
	private final static float chainLength = 0.5f;

	//this is the hitbox that this weapon extends
	private Hitbox base, star;
	private Hitbox[] links = new Hitbox[6];
	
	//is the hitbox active?
	private boolean active = false;

	public MorningStar(Schmuck user) {
		super(user, swingCd, windup, weaponSprite, eventSprite);
	}
	
	private Vector2 projOffset = new Vector2();
	@Override
	public void mouseClicked(float delta, PlayState state, final BodyData shooter, short faction, Vector2 mouseLocation) {
		super.mouseClicked(delta, state, shooter, faction, mouseLocation);
		
		if (!active) {
			active = true;
			activate(state, shooter, mouseLocation);
		}
		if (star != null) {
			star.applyForceToCenter(projOffset.set(mouseLocation).sub(shooter.getSchmuck().getPixelPosition()).nor().scl(swingForce));
		}
	}
	
	@Override
	public void execute(PlayState state, BodyData shooter) {}
	
	@Override
	public void unequip(PlayState state) {
		active = false;
		deactivate(state);
	}
	
	private void activate(PlayState state, final BodyData shooter, Vector2 mouseLocation) {
		
		projOffset.set(mouseLocation).sub(shooter.getSchmuck().getPixelPosition()).nor().scl(range);
		base = new Hitbox(state, shooter.getSchmuck().getPixelPosition(), chainSize, 0, new Vector2(0, 0), shooter.getSchmuck().getHitboxfilter(), true, false, user, chainSprite);
		base.setDensity(0.1f);
		base.makeUnreflectable();
		base.setSyncDefault(false);
		base.setSyncInstant(true);
		
		base.addStrategy(new HitboxStrategy(state, base, user.getBodyData()) {
			
			private boolean linked = false;

			@Override
			public void controller(float delta) {
				
				if (!user.isAlive()) {
					hbox.die();
					deactivate(state);
				}
				
				if (!linked) {
					if (user.getBody() != null) {
						linked = true;
						RevoluteJointDef joint1 = new RevoluteJointDef();
						joint1.bodyA = user.getBody();
						joint1.bodyB = base.getBody();
						joint1.collideConnected = false;
						
						joint1.localAnchorA.set(-chainLength, 0);
						joint1.localAnchorB.set(chainLength, 0);
						
						state.getWorld().createJoint(joint1);
					}
				}
			}
			
			@Override
			public void die() {
				hbox.queueDeletion();
			}
		});
		
		for (int i = 0; i < links.length; i++) {
			final int currentI = i;
			links[i] = new Hitbox(state, shooter.getSchmuck().getPixelPosition(), chainSize, 0, new Vector2(0, 0), shooter.getSchmuck().getHitboxfilter(), true, false, user, chainSprite);
			links[i].setDensity(0.1f);
			links[i].makeUnreflectable();
			links[i].setSyncDefault(false);
			links[i].setSyncInstant(true);
			
			links[i].addStrategy(new HitboxStrategy(state, links[i], user.getBodyData()) {
				
				private boolean linked = false;
				
				@Override
				public void controller(float delta) {
					
					if (!linked) {
						if (currentI == 0) { 
							if (base.getBody() != null) {
								linked = true;
								RevoluteJointDef joint1 = new RevoluteJointDef();
								joint1.bodyA = base.getBody();
								joint1.bodyB = hbox.getBody();
								joint1.collideConnected = false;
								
								joint1.localAnchorA.set(-chainLength, 0);
								joint1.localAnchorB.set(chainLength, 0);
								
								state.getWorld().createJoint(joint1);
							}
						} else {
							if (links[currentI - 1].getBody() != null) {
								linked = true;
								
								RevoluteJointDef joint1 = new RevoluteJointDef();
								joint1.bodyA = links[currentI - 1].getBody();
								joint1.bodyB = hbox.getBody();
								joint1.collideConnected = false;
								joint1.localAnchorA.set(-chainLength, 0);
								joint1.localAnchorB.set(chainLength, 0);
								
								state.getWorld().createJoint(joint1);
							}
						}
					}
				}
				
				@Override
				public void die() {
					hbox.queueDeletion();
				}
			});
		}
		
		star = new Hitbox(state, shooter.getSchmuck().getPixelPosition(), projectileSize, 0, new Vector2(0, 0), shooter.getSchmuck().getHitboxfilter(), false, true, user, projSprite);
		star.setGravity(0.5f);
		star.setDensity(0.1f);
		star.makeUnreflectable();
		star.setSyncDefault(false);
		star.setSyncInstant(true);
		
		star.addStrategy(new DamageStandardRepeatable(state, star, user.getBodyData(), baseDamage, knockback, DamageTypes.MELEE));
		star.addStrategy(new HitboxStrategy(state, star, user.getBodyData()) {
			private boolean linked = false;
			
			@Override
			public void controller(float delta) {
				
				if (!linked) {
					if (links[5].getBody() != null) {
						linked = true;
						
						RevoluteJointDef joint1 = new RevoluteJointDef();
						joint1.bodyA = links[5].getBody();
						joint1.bodyB = hbox.getBody();
						joint1.collideConnected = false;
						joint1.localAnchorA.set(-chainLength, 0);
						joint1.localAnchorB.set(chainLength, 0);
						
						state.getWorld().createJoint(joint1);
					}
				}
			}
			
			@Override
			public void die() {
				hbox.queueDeletion();
			}
		});
	}
	
	private void deactivate(PlayState state) {
		active = false;
		
		if (base != null) {
			base.die();
		}
		
		if (star != null) {
			star.setLifeSpan(2.0f);
			star.addStrategy(new ControllerDefault(state, star, user.getBodyData()));
		}
		for (int i = 0; i < links.length; i++) {
			if (links[i] != null) {
				links[i].setLifeSpan(2.0f);
				links[i].addStrategy(new ControllerDefault(state, links[i], user.getBodyData()));
			}
		}
		
	}
}