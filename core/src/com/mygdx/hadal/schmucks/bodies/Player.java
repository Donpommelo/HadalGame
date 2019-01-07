package com.mygdx.hadal.schmucks.bodies;

import static com.mygdx.hadal.utils.Constants.PPM;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.equip.ActiveItem.chargeStyle;
import com.mygdx.hadal.equip.misc.Airblaster;
import com.mygdx.hadal.event.Event;
import com.mygdx.hadal.managers.GameStateManager;
import com.mygdx.hadal.save.UnlockCharacter;
import com.mygdx.hadal.schmucks.MoveStates;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.StatusProcTime;
import com.mygdx.hadal.statuses.artifact.ScalingScalesStatus;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

/**
 * The player is the entity that the player controls.
 * @author Zachary Tu
 *
 */
public class Player extends PhysicsSchmuck {
	
	
	private final static float playerDensity = 1.0f;
	
	public static final int hbWidth = 216;
	public static final int hbHeight = 516;
		
	private static final int bodyConnectX = -100;
	private static final int bodyConnectY = 0;
	
	private static final int headConnectX = -26;
	private static final int headConnectY = 330;
	
	private static final int armConnectX = -304;
	private static final int armConnectY = 218;
	
	private static final int armRotateX = 330;
	private static final int armRotateY = 50;
		
	public static final float scale = 0.15f;
	
	private final float spriteAnimationSpeed = 0.08f;
	
	//counters for various cooldowns.
	private float hoverCd = 0.08f;
	private float jumpCd = 0.25f;
	private float jumpCdCount = 0;
	
	private float fastFallCd = 0.25f;
	private float fastFallCdCount = 0;
	
	private float airblastCd = 0.25f;
	private float airblastCdCount = 0;
	
	protected float interactCd = 0.15f;
	protected float interactCdCount = 0;
	
	private float attackAngle = 0;
	
	//user data
	private PlayerBodyData playerData;
	
	//The event that the player last collided with. Used for active events that the player interacts with by pressing 'E'
	private Event currentEvent;
	
	//Equipment that the player has built in to their toolset.
	private Airblaster airblast;
	
	private TextureAtlas atlasBody;
	private TextureRegion bodyBackSprite, armSprite, gemSprite, gemInactiveSprite, toolSprite;
	
	private Animation<TextureRegion> bodyStillSprite, bodyRunSprite, headSprite;
	
	private int armWidth, armHeight, headWidth, headHeight, bodyWidth, bodyHeight, bodyBackWidth, bodyBackHeight,
	toolHeight, toolWidth, gemHeight, gemWidth;
	
	//This counter keeps track of elapsed time so the entity behaves the same regardless of engine tick time.
	private float controllerCount = 0;
	
	//Is the player currently shooting/hovering?
	private boolean shooting = false;
	private boolean hovering = false;
	
	private ParticleEntity hoverBubbles;
	
	/**
	 * This constructor is called by the player spawn event that must be located in each map
	 * @param state: current gameState
	 * @param world: box2d world
	 * @param camera: game camera
	 * @param rays: game rayhandler
	 * @param x: player starting x position.
	 * @param y: player starting x position.
	 */
	public Player(PlayState state, int x, int y, UnlockCharacter character, PlayerBodyData oldData) {
		super(state, hbWidth * scale, hbHeight * scale, x, y, Constants.PLAYER_HITBOX);
		
		airblast = new Airblaster(this);
		
		toolSprite = GameStateManager.multitoolAtlas.findRegion("default");
		
		this.toolHeight = toolSprite.getRegionHeight();
		this.toolWidth = toolSprite.getRegionWidth();
		
		this.moveState = MoveStates.STAND;

		if (oldData != null) {
			this.playerData = oldData;
		}
		
		setBodySprite(character.getSprite());
		loadParticles();
	}
	
	/**
	 * This method prepares the player sprite from various texture regions.
	 * @param playerSprite
	 */
	public void setBodySprite(String playerSprite) {
		atlasBody = (TextureAtlas) HadalGame.assetManager.get(playerSprite);
		bodyRunSprite = new Animation<TextureRegion>(spriteAnimationSpeed, atlasBody.findRegions("body_run"));	
		bodyStillSprite = new Animation<TextureRegion>(spriteAnimationSpeed, atlasBody.findRegions("body_stand"));	
		bodyBackSprite = atlasBody.findRegion("body_background");
		armSprite = atlasBody.findRegion("arm");
		headSprite = new Animation<TextureRegion>(spriteAnimationSpeed, atlasBody.findRegions("head"));	
		gemSprite = atlasBody.findRegion("gem_active");
		gemInactiveSprite = atlasBody.findRegion("gem_inactive");
		
		this.armWidth = armSprite.getRegionWidth();
		this.armHeight = armSprite.getRegionHeight();
		this.headWidth = headSprite.getKeyFrame(animationTime).getRegionWidth();
		this.headHeight = headSprite.getKeyFrame(animationTime).getRegionHeight();
		this.bodyWidth = bodyRunSprite.getKeyFrame(animationTime).getRegionWidth();
		this.bodyHeight = bodyRunSprite.getKeyFrame(animationTime).getRegionHeight();
		this.bodyBackWidth = bodyBackSprite.getRegionWidth();
		this.bodyBackHeight = bodyBackSprite.getRegionHeight();
		this.gemHeight = gemSprite.getRegionHeight();
		this.gemWidth = gemSprite.getRegionWidth();
		
		//This line is used when the player swaps skins in loadout screen. It ensures the tool sprite is properly aligned.
		if (playerData != null) {
			playerData.setEquip();
		}		
	}
	
	/**
	 * This method prepares the various particle emitting entities attached to the player.
	 */
	public void loadParticles() {
		
		hoverBubbles = new ParticleEntity(state, this, Particle.BUBBLE_TRAIL, 0.0f, 0.0f, false);
		hoverBubbles.getEffect().findEmitter("bubble0").setContinuous(false);
		hoverBubbles.getEffect().findEmitter("bubble0").duration = 10;
		/*
		final ParticleEffect smoke = new ParticleEffect();
		smoke.load(Gdx.files.internal(AssetList.SMOKE_PUFF.toString()), particleAtlas);
		
		jumpSmoke = new ParticleEntity(state, world, camera, rays, this, smoke, 3.0f);
		jumpSmoke.turnOff();*/
	}
	
	/**
	 * Create the player's body and initialize player's user data.
	 */
	@Override
	public void create() {
		
		state.resetController();
		
		if (playerData == null) {
			this.playerData = new PlayerBodyData(this, state.getLoadout());
		} else {
			playerData.resetData(this, world);
		}
		this.bodyData = playerData;
		playerData.initLoadout();
		
		this.body = BodyBuilder.createBox(world, startX, startY, width, height, 1, playerDensity, 0, 0, false, true, Constants.BIT_PLAYER, 
				(short) (Constants.BIT_WALL | Constants.BIT_SENSOR | Constants.BIT_PROJECTILE | Constants.BIT_ENEMY),
				Constants.PLAYER_HITBOX, false, playerData);
				
		super.create();
		
		if (state.isRealFite()) {
			playerData.statusProcTime(StatusProcTime.LEVEL_START, null, 0, null, null, null);
		}
	}
	
	/**
	 * The player's controller currently polls for input.
	 */
	@Override
	public void controller(float delta) {
		
		controllerCount+=delta;
		if (controllerCount >= 1/60f) {
			controllerCount -= 1/60f;

			if (hovering) {
				hover();
			}
		}
		
		if (shooting) {
			shoot(delta);
		}
		
		//Determine if the player is in the air or on ground.
		if (playerData.getStatus(ScalingScalesStatus.class) != null) {
			grounded = feetData.getNumContacts() > 0 || leftData.getNumContacts() > 0 || rightData.getNumContacts() > 0;
		} else {
			grounded = feetData.getNumContacts() > 0;
		}
		
		//player's jumps are refreshed on the ground
		if (grounded) {
			playerData.setExtraJumpsUsed(0);
		}
				
		//process fuel regen
		playerData.fuelGain(playerData.getFuelRegen() * delta);
		
		//If player is reloading, run the reload method of the current equipment.
		if (playerData.getCurrentTool().isReloading()) {
			playerData.getCurrentTool().reload(delta);
		}
		
		if (playerData.getActiveItem().getStyle().equals(chargeStyle.byTime)) {
			playerData.getActiveItem().gainCharge(delta);
		}
		
		//process cds
		jumpCdCount-=delta;
		fastFallCdCount-=delta;
		airblastCdCount-=delta;
		interactCdCount-=delta;
		
		super.controller(delta);
	}
	
	/**
	 * Player's Hover power. Costs fuel and continuously pushes the player upwards.
	 */
	public void hover() {
		if (playerData.getExtraJumpsUsed() >= playerData.getExtraJumps() &&
				playerData.getCurrentFuel() >= playerData.getHoverCost()) {
			if (jumpCdCount < 0) {
				
				//Player will continuously do small upwards bursts that cost fuel.
				playerData.fuelSpend(playerData.getHoverCost());
				jumpCdCount = hoverCd;
				push(0, playerData.getHoverPower());
				
				hoverBubbles.onForBurst(0.5f);
			}
		}
	}
	
	/**
	 * Player's jump. Player moves up if they have jumps left.
	 */
	public void jump() {
		if (grounded) {
			if (jumpCdCount < 0) {
				jumpCdCount = jumpCd;
				push(0, playerData.getJumpPower());
//				jumpSmoke.turnOn();
			}
		} else {
			if (playerData.getExtraJumpsUsed() < playerData.getExtraJumps()) {
				if (jumpCdCount < 0) {
					jumpCdCount = jumpCd;
					playerData.setExtraJumpsUsed(playerData.getExtraJumpsUsed() + 1);;
					push(0, playerData.getJumpPower());
//					jumpSmoke.turnOn();
				}
			}
		}
	}
	
	/**
	 * Player falls rapidly if in the air. If grounded, this also interacts with terrain events.
	 */
	public void fastFall() {
		if (fastFallCdCount < 0) {
			fastFallCdCount = fastFallCd;
			push(0, -playerData.getFastFallPower());
		}
		if (feetData.getTerrain() != null) {
			feetData.getTerrain().getEventData().onInteract(this);
		}
	}
	
	/**
	 * Point and shoot
	 * @param delta: How long has it been since the lst engine tick if the player is holding fire. This is used for charge weapons
	 */
	public void shoot(float delta) {
		useToolStart(delta, playerData.getCurrentTool(), Constants.PLAYER_HITBOX, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), true);
	}
	
	/**
	 * Player releases mouse. This is used to fire charge weapons.
	 */
	public void release() {
		useToolRelease(playerData.getCurrentTool(), Constants.PLAYER_HITBOX, Gdx.input.getX() , Gdx.graphics.getHeight() - Gdx.input.getY());
	}
	
	/**
	 * Player's airblast power. Boosts player, knocks enemies/hitboxes.
	 */
	public void airblast() {
		if (airblastCdCount < 0) {
			if (playerData.getCurrentFuel() >= playerData.getAirblastCost()) {
				playerData.fuelSpend(playerData.getAirblastCost());
				airblastCdCount = airblastCd;
				useToolStart(0, airblast, Constants.PLAYER_HITBOX, Gdx.input.getX() , Gdx.graphics.getHeight() - Gdx.input.getY(), false);
			}
		}
	}
	
	/**
	 * Player interacts with an event they are overlapping with
	 */
	public void interact() {
		if (currentEvent != null && interactCdCount < 0) {
			interactCdCount = interactCd;
			currentEvent.getEventData().onInteract(this);
		}
	}
	
	/**
	 * Player uses active item.
	 */
	public void activeItem() {
		useToolStart(0, playerData.getActiveItem(), Constants.PLAYER_HITBOX, Gdx.input.getX() , Gdx.graphics.getHeight() - Gdx.input.getY(), false);
	}
	
	/**
	 * Player begins reloading.
	 */
	public void reload() {
		playerData.getCurrentTool().setReloading(true);
	}
	
	/**
	 * Player switches to their last equiped weapon. (does nothing if they have no previously equipped weapon.)
	 */
	public void switchToLast() {
		playerData.switchToLast();
	}
	
	/**
	 * Switches to the weapon in a specific slot.
	 * @param slot
	 */
	public void switchToSlot(int slot) {
		playerData.switchWeapon(slot);
	}
	
	/**
	 * This returns the angle of the player's arm. What was this needed for again?
	 */
	@Override
	public float getAttackAngle() {
		if (armSprite.isFlipX()) {
			return (float) Math.toRadians(attackAngle - 180);
		} else {
			return (float) Math.toRadians(attackAngle);
		}
	}
	
	private Vector3 bodyPosition = new Vector3();
	private float armConnectXReal;
	private float headConnectXReal;
	private float armRotateXReal;
	@Override
	public void render(SpriteBatch batch) {
		batch.setProjectionMatrix(state.sprite.combined);
		
		bodyPosition.set(body.getPosition().x,
				body.getPosition().y, 0);
		
		camera.project(bodyPosition);
		
		//Determine player mouse location and hence where the arm should be angled.
		attackAngle = (float)(Math.atan2(
				bodyPosition.y - (Gdx.graphics.getHeight() - Gdx.input.getY()),
				bodyPosition.x - Gdx.input.getX()) * 180 / Math.PI);
		
/*		for (ParticleEmitter p : hoverBubbles.getEffect().getEmitters()) {
			p.getAngle().setHighMax(attackAngle);
            p.getAngle().setHighMin(attackAngle);
            p.getAngle().setLow(attackAngle, attackAngle);
		}*/
		
		boolean flip = false;
		
		if (Math.abs(attackAngle) > 90) {
			flip = true;
		}
		
		//Depending on which way the player is facing, the connection points of various body parts are slightly offset.
		armConnectXReal = armConnectX;
		headConnectXReal = headConnectX;
		armRotateXReal = armRotateX;
		
		if (flip) {
			armConnectXReal = bodyWidth - armWidth - armConnectX - 200;
			headConnectXReal = bodyWidth - headWidth - headConnectX - 200;
			armRotateXReal = armWidth - armRotateX;
			attackAngle = attackAngle + 180;
		}
		
		//This switch determins the total body y-offset to make the body bob up and down when running.
		int yOffset = 0;
		if (moveState.equals(MoveStates.MOVE_LEFT) || moveState.equals(MoveStates.MOVE_RIGHT)) {
			switch(bodyRunSprite.getKeyFrameIndex(animationTime)) {
			case 0:
			case 1:
				yOffset++;
			case 2:
				yOffset++;
			case 3:
				yOffset++;
			case 4:
				yOffset++;
			case 5:
				yOffset--;
			case 6:
				yOffset--;
			case 7:
				yOffset--;
			}
		}
		
		//This makes bodies flash red when receiving damage.
		if (flashingCount > 0) {
			batch.setShader(HadalGame.shader);
		}
		
		//Draw a bunch of stuff
		batch.draw(toolSprite, 
				(flip ? toolWidth * scale : 0) + body.getPosition().x * PPM - hbWidth * scale / 2 + armConnectXReal * scale, 
				body.getPosition().y * PPM - hbHeight * scale / 2 + armConnectY * scale + yOffset, 
				(flip ? -armWidth * scale : 0) + armRotateXReal * scale , armRotateY * scale,
				(flip ? -1 : 1) * toolWidth * scale, toolHeight * scale, 1, 1, attackAngle);
		
		batch.draw(bodyBackSprite, 
				(flip ? bodyBackWidth * scale : 0) + body.getPosition().x * PPM - hbWidth * scale / 2 + bodyConnectX * scale, 
				body.getPosition().y * PPM - hbHeight * scale / 2 + bodyConnectY + yOffset, 
				0, 0,
				(flip ? -1 : 1) * bodyBackWidth * scale, bodyBackHeight * scale, 1, 1, 0);
		
		batch.draw(armSprite, 
				(flip ? armWidth * scale : 0) + body.getPosition().x * PPM - hbWidth * scale / 2 + armConnectXReal * scale, 
				body.getPosition().y * PPM - hbHeight * scale / 2 + armConnectY * scale + yOffset, 
				(flip ? -armWidth * scale : 0) + armRotateXReal * scale, armRotateY * scale,
				(flip ? -1 : 1) * armWidth * scale, armHeight * scale, 1, 1, attackAngle);
		
		batch.draw(playerData.getActiveItem().isReady() ? gemSprite : gemInactiveSprite, 
				(flip ? gemWidth * scale : 0) + body.getPosition().x * PPM - hbWidth * scale / 2  + bodyConnectX * scale, 
				body.getPosition().y * PPM - hbHeight * scale / 2 + bodyConnectY + yOffset, 
				0, 0,
				(flip ? -1 : 1) * gemWidth * scale, gemHeight * scale, 1, 1, 0);
		
		boolean reverse = false;
		
		if (moveState.equals(MoveStates.MOVE_LEFT)) {
			
			if (Math.abs(attackAngle) > 90) {
				bodyRunSprite.setPlayMode(PlayMode.LOOP_REVERSED);
				reverse = true;
			} else {
				bodyRunSprite.setPlayMode(PlayMode.LOOP);
			}
			
			batch.draw((TextureRegion) bodyRunSprite.getKeyFrame(grounded ? animationTime : getFreezeFrame(reverse), true), 
					(flip ? bodyWidth * scale : 0) + body.getPosition().x * PPM - hbWidth * scale / 2  + bodyConnectX * scale, 
					body.getPosition().y * PPM - hbHeight * scale / 2  + bodyConnectY + yOffset, 
					0, 0,
					(flip ? -1 : 1) * bodyWidth * scale, bodyHeight * scale, 1, 1, 0);
		} else if (moveState.equals(MoveStates.MOVE_RIGHT)) {
			if (Math.abs(attackAngle) < 90) {
				bodyRunSprite.setPlayMode(PlayMode.LOOP_REVERSED);
				reverse = true;
			} else {
				bodyRunSprite.setPlayMode(PlayMode.LOOP);
			}
			
			batch.draw((TextureRegion) bodyRunSprite.getKeyFrame(grounded ? animationTime : getFreezeFrame(reverse), true), 
					(flip ? bodyWidth * scale : 0) + body.getPosition().x * PPM - hbWidth * scale / 2  + bodyConnectX * scale, 
					body.getPosition().y * PPM - hbHeight * scale / 2  + bodyConnectY + yOffset, 
					0, 0,
					(flip ? -1 : 1) * bodyWidth * scale, bodyHeight * scale, 1, 1, 0);
		} else {
			bodyRunSprite.setPlayMode(PlayMode.LOOP);
			batch.draw(grounded ? (TextureRegion) bodyStillSprite.getKeyFrame(animationTime, true) : 
					(TextureRegion) bodyRunSprite.getKeyFrame(getFreezeFrame(reverse)), 
					(flip ? bodyWidth * scale : 0) + body.getPosition().x * PPM - hbWidth * scale / 2  + bodyConnectX * scale, 
					body.getPosition().y * PPM - hbHeight * scale / 2  + bodyConnectY + yOffset, 
					0, 0,
					(flip ? -1 : 1) * bodyWidth * scale, bodyHeight * scale, 1, 1, 0);
		}
		
		batch.draw((TextureRegion) headSprite.getKeyFrame(animationTime, true), 
				(flip ? headWidth * scale : 0) + body.getPosition().x * PPM - hbWidth * scale / 2 + headConnectXReal * scale, 
				body.getPosition().y * PPM - hbHeight * scale / 2 + headConnectY * scale + yOffset, 
				0, 0,
				(flip ? -1 : 1) * headWidth * scale, headHeight * scale, 1, 1, 0);
		
		if (flashingCount > 0) {
			batch.setShader(null);
		}
	}
	
	public int getFreezeFrame(boolean reverse) {
		if (Math.abs(body.getLinearVelocity().x) > Math.abs(body.getLinearVelocity().y)) {
			return reverse ? 5 : 2;
		} else {
			return reverse ? 1 : 6;
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	public PlayerBodyData getPlayerData() {
		return playerData;
	}
	
	public void setToolSprite(TextureRegion sprite) {
		toolSprite = sprite;
	}

	public TextureRegion getToolSprite() {
		return toolSprite;
	}
	
	public void setArmSprite(TextureRegion sprite) {
		armSprite = sprite;
	}

	public TextureRegion getArmSprite() {
		return armSprite;
	}

	public Event getCurrentEvent() {
		return currentEvent;
	}

	public void setCurrentEvent(Event currentEvent) {
		this.currentEvent = currentEvent;
	}

	public boolean isHovering() {
		return hovering;
	}

	public void setHovering(boolean hovering) {
		this.hovering = hovering;
	}

	public boolean isShooting() {
		return shooting;
	}

	public void setShooting(boolean shooting) {
		this.shooting = shooting;
	}	
}
