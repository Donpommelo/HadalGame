package com.mygdx.hadal.strategies.hitbox;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.schmucks.bodies.enemies.Enemy;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.strategies.HitboxStrategy;

/**
 * @author Zachary Tu
 */
public class FixedToOrigin extends HitboxStrategy {
	
	//does this hbox rotate when the user does?
	private boolean rotate;
	
	private Enemy enemy;
	
	public FixedToOrigin(PlayState state, Hitbox proj, Enemy enemy, boolean rotate) {
		super(state, proj, enemy.getBodyData());
		this.rotate = rotate;
		
		hbox.setSyncDefault(false);
		hbox.setSyncInstant(true);
		
		this.enemy = enemy;
	}
	
	private Vector2 attackAngle = new Vector2(0, 1);
	private Vector2 hbLocation = new Vector2();
	@Override
	public void create() {
		if (enemy.isAlive()) {
			hbLocation.set(enemy.getProjectileOrigin(attackAngle.setAngle(enemy.getAttackAngle()), hbox.getSize().x)).scl(1.0f / 32);
			if (rotate) {
				hbox.setTransform(hbLocation, hbox.getStartVelo().angleRad());
			} else {
				hbox.setTransform(hbLocation, 0);
			}
		}
	}
	
	@Override
	public void controller(float delta) {
		if (!enemy.isAlive()) {
			hbox.die();
		} else {
			hbLocation.set(enemy.getProjectileOrigin(attackAngle.setAngle(enemy.getAttackAngle()), hbox.getSize().x)).scl(1.0f / 32);
			if (rotate) {
				hbox.setTransform(hbLocation, hbox.getStartVelo().angleRad());
			} else {
				hbox.setTransform(hbLocation, 0);
			}
		}
	}
}