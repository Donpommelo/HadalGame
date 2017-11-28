package com.mygdx.hadal;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import static com.mygdx.hadal.utils.Constants.PPM;

public class HadalGame extends ApplicationAdapter {
	
	private boolean DEBUG = true;
	private final float SCALE = 2.0f;
	
	private OrthographicCamera camera;
	
	private Box2DDebugRenderer b2dr;
	private World world;
	private Body player, platform;
	
	private SpriteBatch batch;
	private Texture tex;
	
	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, w / SCALE, h / SCALE);
		
		world = new World(new Vector2(0, -9.81f), false);
		b2dr = new Box2DDebugRenderer();
		
		player = createBox(8, 10, 32, 32, false);
		platform = createBox(0, 0, 64, 32, true);
		
		batch = new SpriteBatch();
		tex = new Texture("test/dummy_side_mirror.gif");
		
	}

	@Override
	public void render() {
		update(Gdx.graphics.getDeltaTime());
		
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		batch.draw(tex, player.getPosition().x * PPM - (tex.getWidth() / 2) , player.getPosition().y * PPM - (tex.getHeight() / 2));
		batch.end();
		
		b2dr.render(world, camera.combined.scl(PPM));
		
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) { Gdx.app.exit(); }
	}
	
	@Override
	public void resize (int width, int height) {	camera.setToOrtho(false, width / SCALE, height / SCALE); }
	
	@Override
	public void dispose () {
		world.dispose();
		b2dr.dispose();
		tex.dispose();
	}
	
	private void update(float deltaTime) {
		world.step(1 / 60f, 6, 2);
		
		inputUpdate(deltaTime);
		
		cameraUpdate(deltaTime);
		
		batch.setProjectionMatrix(camera.combined);
	}
	
	public void inputUpdate(float deltaTime) {
		
		int horizontalForce = 0;
		
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			horizontalForce -= 1;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			horizontalForce += 1;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			player.applyForceToCenter(0, 300, false);
		}
		
		player.setLinearVelocity(horizontalForce * 5, player.getLinearVelocity().y);
		
	}
	
	private void cameraUpdate(float deltaTime) {
		Vector3 position = camera.position;
		position.x = player.getPosition().x * PPM;
		position.y = player.getPosition().y * PPM;
		
		camera.position.set(position);
		
		camera.update();
	}

	public Body createBox(int x, int y, int width, int height, boolean isStatic) {
		Body pbody;
		BodyDef def = new BodyDef();
		if (isStatic) {
			def.type = BodyDef.BodyType.StaticBody;
		} else{
			def.type = BodyDef.BodyType.DynamicBody;
		}
		def.position.set(x / PPM, y / PPM);
		def.fixedRotation = true;
		pbody = world.createBody(def);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2 / PPM, height / 2 / PPM);
		
		pbody.createFixture(shape, 1.0f);
		shape.dispose();
		
		return pbody;
	}

}
