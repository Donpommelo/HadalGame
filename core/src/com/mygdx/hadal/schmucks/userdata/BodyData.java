package com.mygdx.hadal.schmucks.userdata;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.effects.Shader;
import com.mygdx.hadal.equip.ActiveItem;
import com.mygdx.hadal.equip.ActiveItem.chargeStyle;
import com.mygdx.hadal.equip.Equippable;
import com.mygdx.hadal.equip.RangedWeapon;
import com.mygdx.hadal.save.UnlockArtifact;
import com.mygdx.hadal.schmucks.UserDataTypes;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.server.SavedPlayerFieldsExtra;
import com.mygdx.hadal.server.User;
import com.mygdx.hadal.statuses.DamageTypes;
import com.mygdx.hadal.statuses.ProcTime;
import com.mygdx.hadal.statuses.ProcTime.InflictDamage;
import com.mygdx.hadal.statuses.ProcTime.ReceiveDamage;
import com.mygdx.hadal.statuses.ProcTime.ReceiveHeal;
import com.mygdx.hadal.statuses.Status;
import com.mygdx.hadal.utils.Stats;

import java.util.ArrayList;

/**
 * Body data contains the stats and methods of any unit; player or enemy.
 * @author Pangosteen Placerola
 */
public class BodyData extends HadalData {

	//The Schmuck that owns this data
	protected Schmuck schmuck;
	
	//schmuck stats
	private final float[] baseStats;
	private final float[] buffedStats;
	
	//Speed on ground
	private static final float maxGroundXSpeed = 15.0f;
	private static final float maxAirXSpeed = 10.0f;
		
	//Accelerating on the ground/air
	private static final float groundXAccel = 0.11f;
	private static final float airXAccel = 0.07f;
	private static final float groundXDeaccel = 0.1f;
	private static final float airXDeaccel = 0.01f;
	
	private static final float groundYAccel = 0.1f;
	private static final float airYAccel = 0.15f;
	private static final float groundYDeaccel = 0.05f;
	private static final float airYDeaccel = 0.01f;

	//fast falling
	private static final float fastFallPow = 40.0f;

	//Hp and regen
	private static final float hpRegen = 0.0f;
	
	private static final int maxFuel = 100;
	private static final float fuelRegen = 8.0f;
	
	//duration of flash on receiving damage
	private static final float flashDuration = 0.1f;
	
	//variance multiplier applied to every instance of damage
	private static final float damageVariance = 0.1f;
	
	protected float currentHp, currentFuel;

	//statuses inflicted on the unit. statuses checked is used to recursive activate each status effect
	protected ArrayList<Status> statuses;
	protected ArrayList<Status> statusesChecked;	
	
	//the currently equipped tool
	protected Equippable currentTool;
	
	//This is the last schmuck who damaged this entity. Used for kill credit
	private BodyData lastDamagedBy;
	
	//this is the hp value used by the client ui
	private float overrideHpPercent;

	/**
	 * This is created upon the create() method of any schmuck.
	 * Schmucks are the Body data type.
	 * @param schmuck: the entity that has this data
	 * @param maxHp: the unit's hp
	 */
	public BodyData(Schmuck schmuck, float maxHp) {
		super(UserDataTypes.BODY, schmuck);
		this.schmuck = schmuck;	
		
		this.baseStats = new float[52];
		this.buffedStats = new float[52];
		
		baseStats[0] = maxHp;
		baseStats[1] = maxFuel;
		baseStats[2] = hpRegen;
		baseStats[3] = fuelRegen;
		
		this.statuses = new ArrayList<>();
		this.statusesChecked = new ArrayList<>();
		
		calcStats();

		currentHp = getStat(Stats.MAX_HP);
		currentFuel = getStat(Stats.MAX_FUEL);
		
		lastDamagedBy = schmuck.getState().getWorldDummy().getBodyData();
	}
	
	/**
	 * Status proc time is called at certain points of the game that could activate any effect.
	 * @param o: the type of proc time that this is
	 * This fields of this are the various info needed for each status. fields will be null when unused
	 * @return a ProcTime for certain statuses that pass along a modified value (like on damage effects)
	 */
	public ProcTime statusProcTime(ProcTime o) {
				
		ProcTime finalProcTime = o;
		
		ArrayList<Status> oldChecked = new ArrayList<>();
		for (Status s : this.statusesChecked) {
			this.statuses.add(0, s);
			oldChecked.add(s);
		}
		this.statusesChecked.clear();
		
		while (!this.statuses.isEmpty()) {
			Status tempStatus = this.statuses.get(0);
			
			finalProcTime = tempStatus.statusProcTime(o);
			
			if (this.statuses.contains(tempStatus)) {
				this.statuses.remove(tempStatus);
				this.statusesChecked.add(tempStatus);
			}
		}
		
		for (Status s : this.statusesChecked) {
			if(!oldChecked.contains(s)) {
				this.statuses.add(s);
			}
		}
		this.statusesChecked.clear();
		this.statusesChecked.addAll(oldChecked);

		return finalProcTime;		
	}
	
	/**
	 * Add a status to this schmuck
	 * @param s: Status to add
	 */
	public void addStatus(Status s) {
		
		if (!schmuck.getState().isServer()) { return; }
		
		boolean added = false;
		
		//in the case of re-adding a status, the behavior depends on the status' stack type
		Status old = getStatus(s.getClass());
		if (old != null) {
			switch(s.getStackType()) {
			case ADD:
				added = true;
				break;
			case IGNORE:
				break;
			case REPLACE:
				old.setDuration(s.getDuration());
				break;			
			}
		} else {
			added = true;
		}
		
		if (added) {
			statuses.add(s);
			s.onInflict();
			calcStats();
		}
	}
	
	/**
	 * Removes a status from this schmuck
	 */
	public void removeStatus(Status s) {
		
		if (!schmuck.getState().isServer()) { return; }
		
		s.onRemove();
		statuses.remove(s);
		statusesChecked.remove(s);
		calcStats();
	}
	
	/**
	 * Removes a status from this schmuck
	 */
	public void removeArtifactStatus(UnlockArtifact artifact) {
		
		if (!schmuck.getState().isServer()) { return; }
		
		ArrayList<Status> toRemove = new ArrayList<>();
		
		for (Status s: statuses) {
			if (s.getArtifact() != null) {
				if (s.getArtifact().equals(artifact)) {
					toRemove.add(s);
				}
			}
		}
		
		for (Status s: statusesChecked) {
			if (s.getArtifact() != null) {
				if (s.getArtifact().equals(artifact)) {
					toRemove.add(s);
				}
			}
		}
		
		for(Status st: toRemove) {
			removeStatus(st);
		}
		
		calcStats();
	}
	
	/**
	 * This checks if this schmuck is afflicted by a status.
	 * If so, the status is returned. Otherwise return null
	 */
	public Status getStatus(Class<? extends Status> s) {
		for (Status st : statuses) {
			if (st.getClass().equals(s)) {
				return st;
			}
		}
		for (Status st : statusesChecked) {
			if (st.getClass().equals(s)) {
				return st;
			}
		} 
		return null;
	}
	
	/**
	 * Whenever anything that could modify a schmuck's stats happens, we call this to recalc all of the unit' stats.
	 * This occurs when statuses are added/removed or equipment is equipped
	 */
	public void calcStats() {

		//Keep Hp% and fuel% constant in case of changing max values
		float hpPercent = currentHp / getStat(Stats.MAX_HP);
		float fuelPercent = currentFuel / getStat(Stats.MAX_FUEL);

		System.arraycopy(baseStats, 0, buffedStats, 0, buffedStats.length);
		statusProcTime(new ProcTime.StatCalc());

		//this is used for percentage based hp modifiers
		setStat(Stats.MAX_HP, getStat(Stats.MAX_HP) * (1.0f + getStat(Stats.MAX_HP_PERCENT)));

		currentHp = hpPercent * getStat(Stats.MAX_HP);
		currentFuel = fuelPercent * getStat(Stats.MAX_FUEL);
		
		if (currentTool instanceof RangedWeapon) {
			((RangedWeapon) currentTool).setClipLeft();
			((RangedWeapon) currentTool).setAmmoLeft();
		}
	}
	
	/**
	 * This method is called when this schmuck receives damage.
	 * @param basedamage: amount of damage received
	 * @param knockback: amount of knockback to apply.
	 * @param perp: the schmuck who inflicted damage
	 * @param procEffects: should this damage proc on-damage effects?
	 * @param tags: varargs of damage tags
	 */
	@Override
	public float receiveDamage(float basedamage, Vector2 knockback, BodyData perp, Boolean procEffects, DamageTypes... tags) {
		
		if (!schmuck.isAlive()) { return 0.0f; }
		
		//calculate damage
		float damage = basedamage;
		damage -= basedamage * (getStat(Stats.DAMAGE_RES));
		damage += basedamage * (perp.getStat(Stats.DAMAGE_AMP));
		damage += basedamage * (-damageVariance + MathUtils.random() * 2 * damageVariance);
		
		//proc effects and inflict damage
		if (procEffects) {
			damage = ((InflictDamage) perp.statusProcTime(new ProcTime.InflictDamage(damage, this, tags))).damage;
			damage = ((ReceiveDamage) statusProcTime(new ProcTime.ReceiveDamage(damage, perp, tags))).damage;
		}
		currentHp -= damage;
		
		//Make schmuck flash upon receiving damage
		if (damage > 0 && schmuck.getShaderCount() < -flashDuration) {
			schmuck.setShader(Shader.WHITE, flashDuration);
			schmuck.impact.onForBurst(0.25f);
		}
		
		//apply knockback
		float kbScale = 1;
		kbScale -= getStat(Stats.KNOCKBACK_RES);
		kbScale += perp.getStat(Stats.KNOCKBACK_AMP);
		schmuck.applyLinearImpulse(new Vector2(knockback).scl(kbScale));
		
		//Give credit for kills to last schmuck (besides self) who damaged this schmuck
		if (!perp.equals(this) && !perp.equals(schmuck.getState().getWorldDummy().getBodyData())) {
			lastDamagedBy = perp;
		}
		
		if (currentHp <= 0) {
			
			//this makes stat tracking not account for overkill damage
			damage += currentHp;
			
			currentHp = 0;
			die(lastDamagedBy, tags);
		}
		
		if (schmuck.getState().isServer()) {
			//charge on-damage active item
			if (perp instanceof PlayerBodyData) {
				if (((PlayerBodyData) perp).getActiveItem().getStyle().equals(chargeStyle.byDamageInflict)) {
					
					//active item charges less against non-player enemies
					if (this instanceof PlayerBodyData) {
						((PlayerBodyData) perp).getActiveItem().gainCharge(damage * ActiveItem.damageChargeMultiplier);
					} else {
						((PlayerBodyData) perp).getActiveItem().gainCharge(damage * ActiveItem.damageChargeMultiplier * ActiveItem.enemyDamageChargeMultiplier);
					}
				}
				User userPerp = HadalGame.server.getUsers().get(((PlayerBodyData) perp).getPlayer().getConnID());
				if (userPerp != null) {
					SavedPlayerFieldsExtra field = userPerp.getScoresExtra();
					//play on-hit sounds. pitched up automatically if fatal. No sounds for self or friendly fire.
					if (perp.getSchmuck().getHitboxfilter() != schmuck.getHitboxfilter()) {
						if (currentHp == 0) {
							((PlayerBodyData) perp).getPlayer().playHitSound(999);
						} else {
							((PlayerBodyData) perp).getPlayer().playHitSound(damage);
						}

						//track perp's damage dealt
						if (field != null && damage > 0.0f) {
							field.incrementDamageDealt(damage);
						}

					} else {
						if (field != null && damage > 0.0f) {
							if (perp.getSchmuck().equals(schmuck)) {
								field.incrementDamageDealtSelf(damage);
							} else {
								field.incrementDamageDealtAllies(damage);
							}
						}
					}
				}
			}
		}
		return damage;
	}
	
	/**
	 * This method is called when the schmuck is healed
	 * @param baseheal: amount of Hp to regenerate
	 * @param perp: the schmuck who healed
	 * @param procEffects: should this damage proc on-damage effects?
	 * @param tags: varargs of damage tags
	 */
	public void regainHp(float baseheal, BodyData perp, Boolean procEffects, DamageTypes... tags) {
		
		float heal = baseheal;
		
		if (procEffects) {
			heal = ((ReceiveHeal) statusProcTime(new ProcTime.ReceiveHeal(heal, perp, tags))).heal;
		}
		
		//prevent overheal
		currentHp += heal;
		if (currentHp >= getStat(Stats.MAX_HP)) {
			currentHp = getStat(Stats.MAX_HP);
		}
	}
	
	/**
	 * This method is called when the schmuck dies. Queue up to be deleted next engine tick.
	 * @param tags: the tags that apply to the fatal damage instance
	 */
	public void die(BodyData perp, DamageTypes... tags) {
		if (schmuck.queueDeletion()) {
			perp.statusProcTime(new ProcTime.Kill(this));
			statusProcTime(new ProcTime.Death(perp));
		}		
	}
	
	public Schmuck getSchmuck() { return schmuck; }
		
	public float getCurrentHp() { return currentHp; }

	public void setCurrentHp(float currentHp) { this.currentHp = currentHp;	}

	public float getCurrentFuel() {	return currentFuel;	}

	public float getStat(int index) { return buffedStats[index]; }
	
	/**
	 * Set a buffed stat for calcs. If hp or fuel, make sure the current amount does not exceed the max amount
	 * @param index: the number of the stat being modified
	 * @param amount: the amount to modify the stat by
	 */
	public void setStat(int index, float amount) {
		buffedStats[index] = amount;
		
		//prevent overheal and overfuel
		if (index == Stats.MAX_HP) {
			currentHp = currentHp / buffedStats[index] * amount;
		}
		if (index == Stats.MAX_FUEL) {
			currentFuel = currentFuel / buffedStats[index] * amount;
		}
	}
	
	public Equippable getCurrentTool() { return currentTool; }
	
	public void setCurrentTool(Equippable currentTool) { this.currentTool = currentTool; }
	
	public float getXGroundSpeed() { return maxGroundXSpeed * (1 + getStat(Stats.GROUND_SPD)); }
	
	public float getXAirSpeed() { return maxAirXSpeed * (1 + getStat(Stats.AIR_SPD)); }
	
	public float getXGroundAccel() { return groundXAccel * (1 + getStat(Stats.GROUND_ACCEL)); }
	
	public float getXAirAccel() { return airXAccel * (1 + getStat(Stats.AIR_ACCEL)); }
	
	public float getXGroundDeaccel() { return groundXDeaccel * (1 + getStat(Stats.GROUND_DRAG)); }
	
	public float getXAirDeaccel() {	return airXDeaccel * (1 + getStat(Stats.AIR_DRAG)); }
	
	public float getYGroundAccel() { return groundYAccel * (1 + getStat(Stats.GROUND_ACCEL)); }
	
	public float getYAirAccel() { return airYAccel * (1 + getStat(Stats.AIR_ACCEL)); }
	
	public float getYGroundDeaccel() { return groundYDeaccel * (1 + getStat(Stats.GROUND_DRAG)); }
	
	public float getYAirDeaccel() {	return airYDeaccel * (1 + getStat(Stats.AIR_DRAG)); }
	
	public float getFastFallPower() { return fastFallPow * (1 + getStat(Stats.FASTFALL_POW)); }
	
	public float getOverrideHpPercent() { return overrideHpPercent; }

	public void setOverrideHpPercent(float overrideHpPercent) {	this.overrideHpPercent = overrideHpPercent;	}
}
