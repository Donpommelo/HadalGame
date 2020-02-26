package com.mygdx.hadal.equip;

import com.mygdx.hadal.schmucks.bodies.enemies.Scissorfish;
import com.mygdx.hadal.schmucks.bodies.enemies.Spittlefish;
import com.mygdx.hadal.schmucks.bodies.enemies.Torpedofish;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.Ablaze;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.strategies.HitboxStrategy;
import com.mygdx.hadal.strategies.hitbox.ContactUnitStatus;
import com.mygdx.hadal.strategies.hitbox.ContactWallDie;
import com.mygdx.hadal.strategies.hitbox.ControllerDefault;
import com.mygdx.hadal.strategies.hitbox.CreateParticles;
import com.mygdx.hadal.strategies.hitbox.DamageStandard;
import com.mygdx.hadal.strategies.hitbox.DamageStatic;
import com.mygdx.hadal.strategies.hitbox.FixedToUser;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.event.Event;
import com.mygdx.hadal.event.Poison;
import com.mygdx.hadal.managers.GameStateManager;
import com.mygdx.hadal.schmucks.bodies.HadalEntity;
import com.mygdx.hadal.schmucks.bodies.enemies.Enemy;
import com.mygdx.hadal.schmucks.bodies.enemies.EnemyAction;
import com.mygdx.hadal.schmucks.bodies.enemies.EnemyCrawling;
import com.mygdx.hadal.schmucks.bodies.enemies.EnemyCrawling.CrawlingState;
import com.mygdx.hadal.schmucks.bodies.enemies.EnemyFloating;
import com.mygdx.hadal.schmucks.bodies.enemies.EnemyFloating.FloatingState;
import com.mygdx.hadal.schmucks.bodies.enemies.EnemyType;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.bodies.hitboxes.RangedHitbox;
import com.mygdx.hadal.utils.Constants;

/**
 * This contains several statichelper methods for creating enemy attack patterns
 * @author Zachary Tu
 *
 */
public class EnemyUtils {

	public static void moveToDummy(final PlayState state, final Enemy boss, final String dummyId, final int speed, float duration) {
		
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				Event dummy = state.getDummyPoint(dummyId);
				
				if (dummy != null) {
					enemy.setMovementTarget(dummy.getPixelPosition());
					enemy.setMoveSpeed(speed);
				}
			}
		});
	}
	
	public static void changeFloatingState(final EnemyFloating bossFloating, final FloatingState state, final float angle, float duration) {
		
		bossFloating.getActions().add(new EnemyAction(bossFloating, duration) {
			
			@Override
			public void execute() {
				bossFloating.setCurrentState(state);
				bossFloating.setAngle(normalizeAngle((int) bossFloating.getAngle()));
				switch (state) {
				case FREE:
					bossFloating.setDesiredAngle(angle);
					break;
				case SPINNING:
				case ROTATING:
					bossFloating.setSpinSpeed((int)angle);
					break;
				case LOCKED:
					bossFloating.setAngle(angle);
					break;
				case TRACKING_PLAYER:
					break;
				default:
					break;
				}
			}
		});
	}
	
	public static void changeCrawlingState(final EnemyCrawling bossCrawling, final CrawlingState state, final float direction, float duration) {
		
		bossCrawling.getActions().add(new EnemyAction(bossCrawling, duration) {
			
			@Override
			public void execute() {
				bossCrawling.setCurrentState(state);
				switch (state) {
				case BACK_FORTH:
				case AVOID_PITS:
				case CHASE_PLAYER:
					bossCrawling.setMoveDirection(direction);
					break;
				case STILL:
					bossCrawling.setMoveDirection(0);
					break;
				default:
					break;
				}
			}
		});
	}
	
	public static void spawnAdds(final PlayState state, Enemy boss, final EnemyType type, final int amount, float duration) {
		
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				
				for (int i = 0; i < amount; i++) {
					switch (type) {
					case SCISSORFISH:
						new Scissorfish(state, enemy.getPixelPosition(), Constants.ENEMY_HITBOX, null);
						break;
					case SPITTLEFISH:
						new Spittlefish(state, enemy.getPixelPosition(), Constants.ENEMY_HITBOX, null);
						break;
					case TORPEDOFISH:
						new Torpedofish(state, enemy.getPixelPosition(), Constants.ENEMY_HITBOX, null);
						break;
					default:
						break;
					}
				}
			}
		});
	}
	
	public static void moveToPlayer(final PlayState state, Enemy boss, final HadalEntity target, final int moveSpeed, final float duration) {
		
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				
				if (target == null) {
					return;
				}
				
				Vector2 dist = target.getPixelPosition().sub(enemy.getPixelPosition());
				enemy.setLinearVelocity(dist.nor().scl(moveSpeed));
			}
		});
	}
	
	public static void trackPlayerXY(final PlayState state, Enemy boss, final HadalEntity target, final int moveSpeed, final float duration, final boolean x) {
		
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				
				if (target == null) {
					return;
				}
				
				enemy.setMovementTarget(null);
				Vector2 dist = target.getPixelPosition().sub(enemy.getPixelPosition());
				if (x) {
					enemy.setLinearVelocity(new Vector2(dist.nor().scl(moveSpeed).x, 0));
				} else {
					enemy.setLinearVelocity(new Vector2(0, dist.nor().scl(moveSpeed).y));
				}
			}
		});
	}

	public static void meleeAttack(final PlayState state, Enemy boss, final float damage, final float knockback, final float duration) {
		
		boss.getActions().add(new EnemyAction(boss, 0) {
			
			@Override
			public void execute() {
				
				Hitbox hbox = new Hitbox(state, enemy.getPixelPosition(), enemy.getSize(), duration, enemy.getLinearVelocity(), enemy.getHitboxfilter(), true, true, enemy, Sprite.NOTHING);
				hbox.makeUnreflectable();
				hbox.addStrategy(new ControllerDefault(state, hbox, enemy.getBodyData()));
				hbox.addStrategy(new DamageStatic(state, hbox, enemy.getBodyData(), damage, knockback, DamageTypes.MELEE));
				hbox.addStrategy(new FixedToUser(state, hbox, enemy.getBodyData(), new Vector2(0, 1), new Vector2(), true));
			}
		});
	}

	public static void meleeAttackContinuous(final PlayState state, Enemy boss, final float damage, final float attackInterval, final float knockback, final float duration) {
		
		boss.getActions().add(new EnemyAction(boss, 0) {
			
			@Override
			public void execute() {
				
				Hitbox hbox = new Hitbox(state, enemy.getPixelPosition(), enemy.getSize(), duration, enemy.getLinearVelocity(), enemy.getHitboxfilter(), true, true, enemy, Sprite.NOTHING);
				hbox.makeUnreflectable();
				hbox.addStrategy(new ControllerDefault(state, hbox, enemy.getBodyData()));
				hbox.addStrategy(new DamageStatic(state, hbox, enemy.getBodyData(), damage, knockback, DamageTypes.MELEE));
				hbox.addStrategy(new FixedToUser(state, hbox, enemy.getBodyData(), new Vector2(0, 1), new Vector2(), true));
				hbox.addStrategy((new HitboxStrategy(state, hbox, enemy.getBodyData()) {
				
					private float controllerCount = 0;
				
					@Override
					public void controller(float delta) {
						
						controllerCount += delta;
						
						while (controllerCount >= attackInterval) {
							controllerCount -= attackInterval;
							
							Hitbox pulse = new Hitbox(state, hbox.getPixelPosition(), enemy.getSize(), attackInterval, new Vector2(0, 0), enemy.getHitboxfilter(), true, true, enemy, Sprite.NOTHING);
							pulse.addStrategy(new ControllerDefault(state, pulse, enemy.getBodyData()));
							pulse.addStrategy(new DamageStatic(state, pulse, enemy.getBodyData(), damage, knockback, DamageTypes.MELEE));
							pulse.addStrategy(new FixedToUser(state, pulse, enemy.getBodyData(), new Vector2(0, 1), new Vector2(), true));
						}
					}
				}));
			}
		});
	}

	public static void fireball(final PlayState state, Enemy boss, final float baseDamage, final float fireDamage, final float projSpeed, final float knockback, final int size,
			final float lifespan, final float fireDuration, final float duration) {
		
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				
				RangedHitbox hbox = new RangedHitbox(state, enemy.getPixelPosition(), new Vector2(size, size), lifespan, new Vector2(projSpeed, projSpeed).setAngle(enemy.getAttackAngle()),
						enemy.getHitboxfilter(), false, true, enemy, Sprite.NOTHING);
				
				hbox.addStrategy(new ControllerDefault(state, hbox, enemy.getBodyData()));
				hbox.addStrategy(new ContactUnitStatus(state, hbox, enemy.getBodyData(), 
						new Ablaze(state, fireDuration, enemy.getBodyData(), enemy.getBodyData(), fireDamage)));
				hbox.addStrategy(new DamageStandard(state, hbox, enemy.getBodyData(), baseDamage, knockback, DamageTypes.RANGED, DamageTypes.FIRE));
				hbox.addStrategy(new CreateParticles(state, hbox, enemy.getBodyData(), Particle.FIRE, 3.0f));
			}
		});
	}
	
	public static void fireLaser(final PlayState state, Enemy boss, final float baseDamage, final float projSpeed, final float knockback, final int size, final float lifespan, final float duration, final Particle particle) {
		
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				
				RangedHitbox hbox = new RangedHitbox(state, enemy.getPixelPosition(), new Vector2(size, size), lifespan, new Vector2(projSpeed, projSpeed).setAngle(enemy.getAttackAngle()),
						enemy.getHitboxfilter(), true, true, enemy, Sprite.NOTHING);
				
				hbox.addStrategy(new ControllerDefault(state, hbox, enemy.getBodyData()));
				hbox.addStrategy(new DamageStandard(state, hbox, enemy.getBodyData(), baseDamage, knockback, DamageTypes.RANGED));
				hbox.addStrategy(new ContactWallDie(state, hbox, enemy.getBodyData()));
				hbox.addStrategy(new CreateParticles(state, hbox, enemy.getBodyData(), particle, lifespan));
			}
		});
	}
	
	public static void bouncingBall(final PlayState state, Enemy boss, final float baseDamage, final float projSpeed, final float knockback, final int size, final float lifespan, final float duration) {
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				Hitbox hbox = new Hitbox(state, enemy.getPixelPosition(), new Vector2(size, size), lifespan, new Vector2(projSpeed, projSpeed).setAngle(enemy.getAttackAngle()),
						enemy.getHitboxfilter(), false, true, enemy, Sprite.ORB_RED);
				hbox.setGravity(10.0f);
				hbox.setRestitution(1);
				
				hbox.addStrategy(new ControllerDefault(state, hbox, enemy.getBodyData()));
				hbox.addStrategy(new DamageStandard(state, hbox, enemy.getBodyData(), baseDamage, knockback, DamageTypes.RANGED));
				hbox.addStrategy(new CreateParticles(state, hbox, enemy.getBodyData(), Particle.FIRE, lifespan));
				
			}
		});
	}
	
	public static void shootBullet(final PlayState state, Enemy boss, final float baseDamage, final float projSpeed, final float knockback, final int size, final float lifespan, final float duration) {
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				Hitbox hbox = new Hitbox(state, enemy.getPixelPosition(), new Vector2(size, size), lifespan, new Vector2(projSpeed, projSpeed).setAngle(enemy.getAttackAngle()),
						enemy.getHitboxfilter(), true, true, enemy, Sprite.ORB_RED);
				
				hbox.addStrategy(new ControllerDefault(state, hbox, enemy.getBodyData()));
				hbox.addStrategy(new DamageStandard(state, hbox, enemy.getBodyData(), baseDamage, knockback, DamageTypes.RANGED));
				hbox.addStrategy(new ContactWallDie(state, hbox, enemy.getBodyData()));
			}
		});
	}
	
	public static void vengefulSpirit(final PlayState state, Enemy boss, final Vector2 pos, final float baseDamage, final float knockback, final float lifespan, final float duration) {
		
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				WeaponUtils.releaseVengefulSpirits(state, pos, lifespan, baseDamage, knockback, enemy.getBodyData(), enemy.getHitboxfilter());
			}
		});
	}
	
	public static void createExplosion(final PlayState state, Enemy boss, final Vector2 pos, final float size, final float baseDamage, final float knockback, final float duration) {
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				WeaponUtils.createExplosion(state, pos, size, enemy, baseDamage, knockback, enemy.getHitboxfilter());
			}
		});
	}
	
	public static void createPoison(final PlayState state, Enemy boss, final Vector2 pos, final Vector2 size, final float damage, final float lifespan, final float duration) {
		
		boss.getSecondaryActions().add(new EnemyAction(boss, duration) {
			@Override
			public void execute() {
				new Poison(state, pos, size, damage, lifespan, enemy, true, enemy.getHitboxfilter());
			}
		});
	}
	
	private final static Sprite[] debrisSprites = {Sprite.SCRAP_A, Sprite.SCRAP_B, Sprite.SCRAP_C, Sprite.SCRAP_D};
	public static void fallingDebris(final PlayState state, Enemy boss, final float baseDamage, final int size, final float knockback, final float lifespan, final float duration) {
		
		boss.getSecondaryActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				
				Event ceiling = state.getDummyPoint("ceiling");
				
				if (ceiling != null) {
					
					int randomIndex = GameStateManager.generator.nextInt(debrisSprites.length);
					Sprite projSprite = debrisSprites[randomIndex];
					Hitbox hbox = new Hitbox(state, new Vector2(ceiling.getPixelPosition()).add(new Vector2((GameStateManager.generator.nextFloat() -  0.5f) * ceiling.getSize().x, 0)),
							new Vector2(size, size), lifespan, new Vector2(),	enemy.getHitboxfilter(), true, true, enemy, projSprite);
					
					hbox.setGravity(1.0f);
					
					hbox.addStrategy(new ControllerDefault(state, hbox, enemy.getBodyData()));
					hbox.addStrategy(new DamageStandard(state, hbox, enemy.getBodyData(), baseDamage, knockback, DamageTypes.RANGED));
					hbox.addStrategy(new ContactWallDie(state, hbox, enemy.getBodyData()));
				}
			}
		});
	}
	
	public static int moveToRandomCorner(PlayState state, Enemy boss, int speed, float duration) {
		int rand = GameStateManager.generator.nextInt(4);
		switch(rand) {
		case 0:
			EnemyUtils.moveToDummy(state, boss, "0", speed, duration);
			break;
		case 1:
			EnemyUtils.moveToDummy(state, boss, "2", speed, duration);
			break;
		case 2:
			EnemyUtils.moveToDummy(state, boss, "6", speed, duration);
			break;
		case 3:
			EnemyUtils.moveToDummy(state, boss, "8", speed, duration);
			break;
		default:
		}
		return rand;
	}
	
	public static int moveToRandomWall(PlayState state, Enemy boss, int speed, float duration) {
		int rand = GameStateManager.generator.nextInt(2);
		switch(rand) {
		case 0:
			EnemyUtils.moveToDummy(state, boss, "3", speed, duration);
			break;
		case 1:
			EnemyUtils.moveToDummy(state, boss, "5", speed, duration);
			break;
		}
		return rand;
	}
	
	public static void stopStill(Enemy boss, final float duration) {
		boss.getActions().add(new EnemyAction(boss, duration) {
			
			@Override
			public void execute() {
				enemy.setLinearVelocity(0, 0);
			}
		});
	}
	
	public static int normalizeAngle(int angle)
	{
	    int newAngle = angle;
	    while (newAngle <= -180) newAngle += 360;
	    while (newAngle > 180) newAngle -= 360;
	    return newAngle;
	}
	
	public static float ceilingHeight(PlayState state) {
		
		Event ceiling = state.getDummyPoint("ceiling");
		
		if (ceiling != null) {
			return ceiling.getPixelPosition().y;
		} else {
			return 0.0f;
		}
	}
	
	public static float floorHeight(PlayState state) {
		
		Event floor = state.getDummyPoint("floor");
		
		if (floor != null) {
			return floor.getPixelPosition().y;
		} else {
			return 0.0f;
		}
	}
	
	public static float getLeftSide(PlayState state) {
		Event floor = state.getDummyPoint("floor");
		
		if (floor != null) {
			return floor.getPixelPosition().x - floor.getSize().x / 2;
		} else {
			return 0.0f;
		}
	}
	
	public static float getRightSide(PlayState state) {
		Event floor = state.getDummyPoint("floor");
		
		if (floor != null) {
			return floor.getPixelPosition().x + floor.getSize().x / 2;
		} else {
			return 0.0f;
		}
	}
}
