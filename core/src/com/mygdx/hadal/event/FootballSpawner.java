package com.mygdx.hadal.event;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.equip.WeaponUtils;
import com.mygdx.hadal.event.userdata.EventData;
import com.mygdx.hadal.schmucks.bodies.hitboxes.Hitbox;
import com.mygdx.hadal.server.Packets;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.Constants;
import com.mygdx.hadal.utils.b2d.BodyBuilder;

/**
 * This event spawns a naval-mine-football for the football game mode.
 * When the mine is destroyed, another will be spawned
 *
 * Triggered Behavior: N/A
 * Triggering Behavior: N/A
 *
 * Fields: N/A
 *
 * @author Bacott Brembino
 */
public class FootballSpawner extends Event {

    private static final float projectileSize = 120;
    private static final float lifespan = 180.0f;

    private static final int explosionRadius = 400;
    private static final float explosionDamage = 75.0f;
    private static final float explosionKnockback = 40.0f;

    public FootballSpawner(PlayState state, Vector2 startPos, Vector2 size) {
        super(state, startPos, size);
    }

    @Override
    public void create() {
        this.eventData = new EventData(this);

        this.body = BodyBuilder.createBox(world, startPos, size, 1, 1, 0, true, true, Constants.BIT_SENSOR, (short) 0, (short) 0, true, eventData);
        this.body.setType(BodyDef.BodyType.KinematicBody);
    }

    private Hitbox ball;
    private float spawnCountdown;
    private static final float spawnDelay = 2.5f;
    @Override
    public void controller(float delta) {

        //ball is spawned after a set delay
        if (spawnCountdown > 0.0f) {
            spawnCountdown -= delta;
            if (spawnCountdown <= 0.0f) {
                spawnBall();
            }
        } else {

            //spawn a ball if it is dead or nonexistent
            boolean ballded = false;
            if (ball == null) {
                ballded = true;
            } else if (!ball.isAlive()) {
                ballded = true;
            }

            if (ballded) {
                spawnCountdown = spawnDelay;

                if (getStandardParticle() != null) {
                    getStandardParticle().onForBurst(spawnDelay);
                }
            }
        }
    }

    private static final float pushMultiplier = 0.6f;

    /**
     * Spawn a ball at our current location and set the objective marker to track the ball
     */
    private void spawnBall() {
        ball = WeaponUtils.createNauticalMine(state, getPixelPosition(), state.getWorldDummy(), new Vector2(),
            projectileSize, lifespan, explosionDamage, explosionKnockback, explosionRadius, pushMultiplier, true);

        state.getUiObjective().addObjective(ball, Sprite.CLEAR_CIRCLE_ALERT, true, false);

        if (state.isServer()) {
            HadalGame.server.sendToAllTCP(new Packets.SyncObjectiveMarker(ball.getEntityID().toString(),
                new Vector3(), true, false, Sprite.CLEAR_CIRCLE_ALERT));
        }
    }

    @Override
    public void loadDefaultProperties() {
        setStandardParticle(Particle.TELEPORT);
    }
}
