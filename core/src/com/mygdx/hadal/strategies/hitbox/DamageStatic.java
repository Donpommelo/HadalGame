package com.mygdx.hadal.strategies.hitbox;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.strategies.HitboxStrategy;

/**
 * This strategy is used by explosives.
 * Explosions deal damage to all units but less damage to the user.
 * Because explosions are static, their knock back is based on positioning rather than their own momentum
 * @author Zachary Tu
 *
 */
public class DamageStatic extends HitboxStrategy{
	
	//the amount of damage and knockback this hbox will inflict
	private float baseDamage, knockback;
	
	//damage tags determine the type of damage inflicted and is used for certain effects
	private DamageTypes[] tags;
	
	public DamageStatic(PlayState state, Hitbox proj, BodyData user, float damage, float knockback, DamageTypes... tags) {
		super(state, proj, user);
		this.baseDamage = damage;
		this.knockback = knockback;
		this.tags = tags;
	}
	
	private Vector2 kb = new Vector2();
	@Override
	public void onHit(HadalData fixB) {
		if (fixB != null) {
			kb.set(fixB.getEntity().getPixelPosition().x - this.hbox.getPixelPosition().x, fixB.getEntity().getPixelPosition().y - this.hbox.getPixelPosition().y);
			fixB.receiveDamage(baseDamage, kb.nor().scl(knockback),	creator, true, tags);
		}
	}
}