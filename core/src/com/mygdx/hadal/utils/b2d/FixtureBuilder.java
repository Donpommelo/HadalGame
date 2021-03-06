package com.mygdx.hadal.utils.b2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import static com.mygdx.hadal.utils.Constants.PPM;

/**
 * This creates and returns fixtures. Used when we need to add an extra fixture to a body created by the BodyBuilder.
 * @author Blickadee Brobottoms
 */
public class FixtureBuilder {

	/**
	 * This creates a FixtureDef to be used to create a Fixture on a body.
	 * @param body: body that this fixture will be attached to.
	 * @param center: Center of the body to stick the fixture.
	 * @param size: size of the fixture
	 * @param sensor: Will this fixture pass through fixtures it collide with?
	 * @param angle: The angle to turn the fixture compared to the body.
	 * @param density: float density of the fixture
	 * @param restitution: bounciness of the fixture
	 * @param friction: slipperiness of the friction
	 * @param cBits: What type of fixture is this?
	 * @param mBits: What types of fixture does this collide with?
	 * @param gIndex: Extra filter. less than 0 = never collide with fixture with same value. greater than 0 = always collide with fixture with same value.
	 * @return a Fixture.
	 */
	public static Fixture createFixtureDef(Body body, Vector2 center, Vector2 size, boolean sensor, float angle,
										   float density, float restitution, float friction, short cBits, short mBits, short gIndex) {
		FixtureDef fixtureDef = new FixtureDef();
		
		PolygonShape pShape = new PolygonShape();
		fixtureDef.shape = pShape;
		
		pShape.setAsBox(size.x / PPM / 2, size.y / PPM / 2, new Vector2(center).scl(1 / PPM), angle);
		
		fixtureDef.isSensor = sensor;
		fixtureDef.density = density;
        fixtureDef.restitution = restitution;		
        fixtureDef.friction = friction;		
		fixtureDef.filter.categoryBits = cBits;
        fixtureDef.filter.maskBits = mBits;
        fixtureDef.filter.groupIndex = gIndex;
		
        Fixture fixture = body.createFixture(fixtureDef);
        pShape.dispose();
        
		return fixture;
	}
}
