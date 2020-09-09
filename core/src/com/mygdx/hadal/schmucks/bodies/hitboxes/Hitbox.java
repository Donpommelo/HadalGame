package com.mygdx.hadal.schmucks.bodies.hitboxes;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.schmucks.bodies.HadalEntity;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.bodies.ClientIllusion.alignType;
import com.mygdx.hadal.schmucks.userdata.HadalData;
import com.mygdx.hadal.schmucks.userdata.HitboxData;
import com.mygdx.hadal.server.Packets;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.states.ClientState.ObjectSyncLayers;
import com.mygdx.hadal.statuses.ProcTime;
import com.mygdx.hadal.strategies.HitboxStrategy;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.Stats;
import com.mygdx.hadal.utils.b2d.BodyBuilder;
import com.mygdx.hadal.utils.b2d.FixtureBuilder;

import java.util.ArrayList;

/**
 * A hitbox is a box that hits things.
 * @author Zachary Tu
 */
public class Hitbox extends HadalEntity {

	//Initial velocity of the hitbox
	protected Vector2 startVelo;
		
	//lifespan is the time in seconds that the hitbox will exist before timing out.
	protected float maxLifespan, lifeSpan;
	
	//filter describes the type of schmuck the hitbox will register a hit on .(player, enemy or neutral)
	protected short filter;
	
	//passability describes what types of entities the hitbox can collide with.
	protected short passability = (short) (Constants.BIT_PROJECTILE | Constants.BIT_WALL | Constants.BIT_PLAYER | Constants.BIT_ENEMY | Constants.BIT_SENSOR);
	
	//default properties. these can be changed using a setter right after the hbox is initialized.
	private final static float defaultGravity = 0.0f;
	private final static float defaultDensity = 0.0f;
	private final static int defaultDurability = 1;
	private final static float defaultFriction = 0.0f;
	private final static float defaultScale = 1.0f;
	private final static float defaultResitution = 0.0f;
	private final static float defaultDamageMultiplier = 1.0f;
	
	//grav is the effect of gravity on the hitbox. 1 = normal gravity. 0 = no gravity.
	private float gravity = defaultGravity;
	
	//density is the used for certain phyic-related hboxes. stuff that needs to rotate based on physics should have a nonzero density
	private float density = defaultDensity;
		
	//durability is the number of things the hitbox can hit before disappearing.
	private int durability = defaultDurability;
	
	//restitution is the hitbox bounciness.
	private float restitution = defaultResitution;
	
	//friction is the hitbox slipperyness.
	private float friction = defaultFriction;
	
	//scale is the hitbox size multiplier.
	private float scale = defaultScale;
	
	//scale is the hitbox size multiplier.
	private float damageMultiplier = defaultDamageMultiplier;
		
	//sensor is whether the hitbox passes through things it registers a hit on.
	private boolean sensor;
	
	//procEffects is whether the hitbox activates statuses. The others decide which types of effects should apply to this hbox
	private boolean procEffects;
	private boolean effectsVisual = true;
	private boolean effectsHit = true;
	private boolean effectsMovement = true;
		
	//can this hbox be reflected by reflection effects?
	private boolean reflectable = true;
	
	//Should this hbox's angle be set at creation to match velocity?
	private boolean adjustAngle = false;
		
	//hitbox user data. This contains on-hit method
	protected HitboxData data;
	
	//This is the Schmuck that created the hitbox
	protected Schmuck creator;
	
	//strategies contains a bunch of effects that modify a hitbox.
	//add+remove are strategies that will be added/removed from the hitbox next world-step
	private ArrayList<HitboxStrategy> strategies, add, remove;
	
	//this is the projectile's Sprite and corresponding frames
	protected Animation<TextureRegion> projectileSprite;
	private Sprite sprite;

	//this is the size of the sprite. Usually drawn to be the size of the hbox, but can be made larger/smaller
	private Vector2 spriteSize = new Vector2();
	
	/**
	 * This constructor is run whenever a hitbox is created. Usually by a schmuck using a weapon.
	 * @param : pretty much the same as the fields above.
	 */
	public Hitbox(PlayState state, Vector2 startPos, Vector2 size, float lifespan, Vector2 startVelo, short filter, boolean sensor, boolean procEffects, Schmuck creator, Sprite sprite) {
		super(state, startPos, size);
		this.maxLifespan = lifespan;
		this.lifeSpan = lifespan;
		this.filter = filter;
		this.sensor = sensor;
		this.procEffects = procEffects;
		this.creator = creator;
		
		//Create a new vector to avoid issues with multi-projectile attacks using same velo for all projectiles.
		this.startVelo = new Vector2(startVelo);
		
		this.strategies = new ArrayList<HitboxStrategy>();
		this.add = new ArrayList<HitboxStrategy>();
		this.remove = new ArrayList<HitboxStrategy>();
		
		//use Sprite.Nothing for spriteless hitboxes (like ones that just use particles)
		this.sprite = sprite;
		if (!sprite.equals(Sprite.NOTHING)) {
			projectileSprite = new Animation<TextureRegion>(sprite.getAnimationSpeed(), sprite.getFrames());
			projectileSprite.setPlayMode(sprite.getPlayMode());
		}
		this.spriteSize.set(size);
	}
	
	/**
	 * Create the hitbox body.
	 */
	public void create() {

		if (procEffects) {
			creator.getBodyData().statusProcTime(new ProcTime.CreateHitbox(this));
		}
		
		this.data = new HitboxData(state, this);
		
		this.size.scl(scale);
		
		this.body = BodyBuilder.createBox(world, startPos, size, gravity, density, 0.0f, 0.0f, false, false, Constants.BIT_PROJECTILE, passability, filter, true, data);

		//Non-sensor hitboxes have a non-sensor fixture attached to it. This is used for hboxes that collide with walls but should pass through enemies
		if (!sensor) {
			FixtureBuilder.createFixtureDef(body, new Vector2(), new Vector2(size), false, 0, 0, restitution, friction, Constants.BIT_PROJECTILE, Constants.BIT_WALL, filter).setUserData(data);
		}
		
		setLinearVelocity(startVelo);
		
		//hboxes that adjust their angle start off transformed.
		if (adjustAngle) {
			setTransform(getPosition(), (float) (Math.atan2(getLinearVelocity().y, getLinearVelocity().x)));
		}
	}
	
	/**
	 * Hitboxes track of lifespan.
	 * This is also where hbox strategies are added/removed to avoid having that happen in world.step
	 */
	public void controller(float delta) {
		
		for (HitboxStrategy s : add) {
			strategies.add(s);
			s.create();
		}
		add.clear();
		
		for (HitboxStrategy s : remove) {
			strategies.remove(s);
		}
		remove.clear();
		
		for (HitboxStrategy s : strategies) {
			s.controller(delta);
		}
	}
	
	@Override
	public void push(Vector2 push) {
		
		if (!alive) { return; }

		for (HitboxStrategy s : strategies) {
			s.push(push);
		}
	}

	@Override
	public void render(SpriteBatch batch) {
		
		if (!alive) { return; }
		
		if (projectileSprite != null) {
			batch.draw((TextureRegion) projectileSprite.getKeyFrame(animationTime, false), 
					getPixelPosition().x - spriteSize.x / 2, 
					getPixelPosition().y - spriteSize.y / 2, 
					spriteSize.x / 2, spriteSize.y / 2,
					spriteSize.x, spriteSize.y, -1, 1, 
					(float) Math.toDegrees(getAngle()));
		}
	}
	
	public void die() {
		
		if (!alive) { return; }
		
		for (HitboxStrategy s : strategies) {
			s.die();
			remove.add(s);
		}
	}
	
	@Override
	public Fixture getMainFixture() {
		if (sensor) {
			return super.getMainFixture();
		} else {
			return body.getFixtureList().get(1);
		}
	}
	
	@Override
	public HadalData getHadalData() { return data; }	
	
	public ArrayList<HitboxStrategy> getStrategies() { return strategies; }
	
	public void addStrategy(HitboxStrategy strat) {	add.add(strat); }
	
	public void removeStrategy(HitboxStrategy strat) { remove.add(strat); }

	public void removeStrategy(Class<? extends HitboxStrategy> stratType) {
		for (HitboxStrategy strat : strategies) {
			if (strat.getClass().equals(stratType)) {
				remove.add(strat);
			}
		}
	}
	
	/**
	 * As Default: Upon created, the hitbox tells the client to create a client illusion tracking it
	 */
	@Override
	public Object onServerCreate() {
		if (isSyncDefault() || isSyncInstant()) {
			return new Packets.CreateEntity(entityID.toString(), spriteSize, getPixelPosition(), getAngle(), sprite, true, ObjectSyncLayers.HBOX, alignType.HITBOX);
		} else {
			return null;
		}
	}
	
	/**
	 * Certain strategies lower the hbox durability. hbox dies when durability reaches 0.
	 */
	public void lowerDurability() {
		this.durability--;
		if (durability <= 0) {
			die();
		}
	}
	
	public void setStartVelo(Vector2 startVelo) { this.startVelo = startVelo; }

	public void setLifeSpan(float lifeSpan) { this.lifeSpan = lifeSpan; }

	public void setDurability(int durability) { this.durability = (int) (durability + creator.getBodyData().getStat(Stats.RANGED_PROJ_DURABILITY)); }
	
	public void setRestitution(float restitution) {	this.restitution = Math.min(1.0f, restitution + creator.getBodyData().getStat(Stats.RANGED_PROJ_RESTITUTION)); }
	
	public void setGravity(float gravity) { this.gravity = gravity + creator.getBodyData().getStat(Stats.RANGED_PROJ_GRAVITY); }
	
	public void setDensity(float density) { this.density = density; }

	public void setScale(float scale) { this.scale = scale; }
	
	public void setFriction(float friction) { this.friction = friction; }
	
	public void setFilter(short filter) { this.filter = filter; }

	public void setPassability(short passability) { this.passability = passability; }

	public void setDamageMultiplier(float damageMultiplier) { this.damageMultiplier = damageMultiplier; }

	public Vector2 getStartVelo() { return startVelo; }

	public float getMaxLifespan() { return maxLifespan; }
	
	public float getLifeSpan() { return lifeSpan; }
	
	public int getDurability() { return durability; }
	
	public float getRestitution() { return restitution; }
	
	public float getGravity() { return gravity; }

	public float getScale() { return scale; }
	
	public Sprite getSprite() { return sprite; }
	
	public short getFilter() { return filter; }

	public short getPassability() { return passability; }

	public float getDamageMultiplier() { return damageMultiplier; }
	
	public boolean isSensor() { return sensor; }

	public void setSensor(boolean sensor) { this.sensor = sensor; }

	public boolean isEffectsVisual() { return effectsVisual; }

	public void setEffectsVisual(boolean effectsVisual) { this.effectsVisual = effectsVisual; }

	public boolean isEffectsHit() { return effectsHit; }

	public void setEffectsHit(boolean effectsHit) {	this.effectsHit = effectsHit; }

	public boolean isEffectsMovement() { return effectsMovement; }

	public void setEffectsMovement(boolean effectsMovement) { this.effectsMovement = effectsMovement; }

	public Schmuck getCreator() { return creator; }

	public void makeUnreflectable() { reflectable = false; }
	
	public boolean isReflectable() { return reflectable; }
	
	public void setAdjustAngle(boolean adjustAngle) { this.adjustAngle = adjustAngle; }
	
	public void setSpriteSize(Vector2 spriteSize) { this.spriteSize.set(spriteSize).scl(scale); }
}
