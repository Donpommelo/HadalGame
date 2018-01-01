package com.mygdx.hadal.states;

import static com.mygdx.hadal.utils.Constants.PPM;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.hadal.handlers.WorldContactListener;
import com.mygdx.hadal.managers.GameStateManager;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.schmucks.bodies.HadalEntity;
import com.mygdx.hadal.utils.CameraStyles;
import com.mygdx.hadal.utils.TiledObjectUtil;

import box2dLight.RayHandler;

/**
 * The PlayState is the main state of the game and holds the Box2d world, all characters + gameplay.
 * @author Zachary Tu
 *
 */
public class PlayState extends GameState {
	
	//This is an entity representing the player. Atm, player is not initialized here, but rather by a "Player Spawn" event in the map.
	public Player player;
	
	//These process and store the map parsed from the Tiled file.
	private TiledMap map;
	OrthogonalTiledMapRenderer tmr;
	
	//The font is for writing text.
    public BitmapFont font;
    
    //The hud is an alternate camera that does not move with the player and will draw ui elements.
 //   public OrthographicCamera hud;
    
    //TODO: rays will eventually implement lighting.
	private RayHandler rays;
	
	//world manages the Box2d world and physics. b2dr renders debug lines for testing
	private Box2DDebugRenderer b2dr;
	private World world;
		
	//These represent the set of entities to be added to/removed from the world. This is necessary to ensure we do this between world steps.
	private Set<HadalEntity> removeList;
	private Set<HadalEntity> createList;
	
	//This is a set of all entities in the world
	private Set<HadalEntity> entities;
	
	//TODO: Temporary tracker of number of enemies defeated. Will replace eventually
	public int score = 0;
	
//	public Set<Zone> zones;
	
//	private float controllerCounter = 0;
	
	/**
	 * Constructor is called upon player beginning a game.
	 * @param gsm: StateManager
	 */
	public PlayState(GameStateManager gsm) {
		super(gsm);
		
		//Initialize font and text camera for ui purposes.
        font = new BitmapFont();
//        hud = new OrthographicCamera();
//        hud.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        //Initialize box2d world and related stuff
		world = new World(new Vector2(0, -9.81f), false);
		world.setContactListener(new WorldContactListener());
		rays = new RayHandler(world);
        rays.setAmbientLight(.4f);
		b2dr = new Box2DDebugRenderer();
		
		//Initialize sets to keep track of active entities
		removeList = new HashSet<HadalEntity>();
		createList = new HashSet<HadalEntity>();
		entities = new HashSet<HadalEntity>();
		
		//TODO: Load a map from Tiled file. Eventually, this will take an input map that the player chooses.
		
		map = new TmxMapLoader().load("Maps/test_map_large.tmx");
//		map = new TmxMapLoader().load("Maps/test_map.tmx");
		
		tmr = new OrthogonalTiledMapRenderer(map);
		
		TiledObjectUtil.parseTiledObjectLayer(world, map.getLayers().get("collision-layer").getObjects());
		
		TiledObjectUtil.parseTiledEventLayer(this, world, camera, rays, map.getLayers().get("event-layer").getObjects());		
	}

	/**
	 * Every engine tick, the GameState must process all entities in it according to the time elapsed.
	 */
	@Override
	public void update(float delta) {
		
		//The box2d world takes a step. This handles collisions + physics stuff. Maybe change delta to set framerate? 
		world.step(delta, 6, 2);

		//All entities that are set to be removed are removed.
		for (HadalEntity entity : removeList) {
			entities.remove(entity);
			entity.dispose();
		}
		removeList.clear();
		
		//All entities that are set to be added are added.
		for (HadalEntity entity : createList) {
			entities.add(entity);
			entity.create();
		}
		createList.clear();
		
		
/*		controllerCounter += delta;
		
		if (controllerCounter >= 1/60f) {
			controllerCounter  -= 1/60f;
			for (HadalEntity schmuck : schmucks) {
				schmuck.controller(1 / 60f);
			}
		}*/
		
		//This processes all entities in the world. (for example, player input/cooldowns/enemy ai)
		for (HadalEntity entity : entities) {
			entity.controller(delta);
		}
		
		//Update the game camera and batch.
		cameraUpdate();
		tmr.setView(camera);
		batch.setProjectionMatrix(camera.combined);
//		rays.setCombinedMatrix(camera.combined.cpy().scl(PPM));
	}
	
	/**
	 * This method renders stuff to the screen after updating.
	 * TODO: atm, this is mostly debug info + temporary ui. Will replace eventually
	 */
	@Override
	public void render() {
		Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//Render Tiled Map + world
		tmr.render();				

		//Render debug lines for box2d objects.
		b2dr.render(world, camera.combined.scl(PPM));

		//Update rays. Does nothing yet.
		rays.updateAndRender();
		
		//Iterate through entities in the world to render
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		for (HadalEntity schmuck : entities) {
			schmuck.render(batch);
		}
		
		batch.setProjectionMatrix(hud.combined);
		
		//Draw player information for temporary ui.
		//Check for null because player is not immediately spawned in a map.
		if (player != null) {
			if (player.getPlayerData() != null) {
				font.getData().setScale(1);
				font.draw(batch, "Score: " + score+ " Hp: " + player.getPlayerData().currentHp + " Fuel: " + player.getPlayerData().currentFuel, 20, 80);
				font.draw(batch, player.getPlayerData().currentTool.getText(), 20, 60);
				if (player.momentums.size != 0) {
					font.draw(batch, "Saved Momentum: " + player.momentums.first(), 20, 40);
				}
				if (player.currentEvent != null) {
					font.draw(batch, player.currentEvent.getText(), 20, 20);
				}
			}
		}
		
		batch.end();
		
	}	
	
	/**
	 * This is called every update. This resets the camera zoom and makes it move towards the player.
	 */
	private void cameraUpdate() {
		camera.zoom = 1.0f;
		if (player != null) {
			if (player.getBody() != null) {
				CameraStyles.lerpToTarget(camera, player.getBody().getPosition().scl(PPM));
			}
		}
	}
	
	/**
	 * This is called upon exiting. Dispose of all created fields.
	 */
	@Override
	public void dispose() {
		b2dr.dispose();
		
		for (HadalEntity schmuck : entities) {
			schmuck.dispose();
		}
		
		world.dispose();
		tmr.dispose();
		map.dispose();
	}
	
	/**
	 * This method is called by entities to be added to the set of entities to be deleted next engine tick.
	 * @param entity: delet this
	 */
	public void destroy(HadalEntity entity) {
		removeList.add(entity);
	}
	
	/**
	 * This method is called by entities to be added to the set of entities to be created next engine tick.
	 * @param entity: entity to be created
	 */
	public void create(HadalEntity entity) {
		createList.add(entity);
	}
	
	/**
	 * Getter for the player. This will return null if the player has not been spawned
	 * @return: The Player entity.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Tentative tracker of player kill number.
	 * @param i: Number to increase score by.
	 */
	public void incrementScore(int i) {
		score += i;
	}
	
}
