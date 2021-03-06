package com.mygdx.hadal.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.hadal.save.UnlockCharacter;
import com.mygdx.hadal.schmucks.MoveState;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.schmucks.bodies.Ragdoll;
import com.mygdx.hadal.server.AlignmentFilter;
import com.mygdx.hadal.states.ClientState;
import com.mygdx.hadal.states.PlayState;

/**
 * The PlayerSpriteHelper helps draw the player sprite. Each player has a PlayerSpriteHelper.
 * This creates a frame buffer object whenever the player spawns or changes costume (character or color)
 * This frame buffer is used to draw palette swaps for the player body and frags.
 * @author Clecton Cloppet
 */
public class PlayerSpriteHelper {

    //Dimension of player sprite parts.
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

    private final Player player;
    private final float scale;

    private TextureRegion bodyBackSprite, armSprite, gemSprite;
    private Animation<TextureRegion> bodyStillSprite, bodyRunSprite, headSprite;
    private FrameBuffer fbo;

    private int armWidth, armHeight, headWidth, headHeight, bodyWidth, bodyHeight, bodyBackWidth, bodyBackHeight,
        gemWidth, gemHeight;
    private final int toolWidth, toolHeight;

    private UnlockCharacter character;
    private AlignmentFilter team;

    public PlayerSpriteHelper(Player player, float scale) {
        this.player = player;
        this.scale = scale;

        this.toolWidth = player.getToolSprite().getRegionWidth();
        this.toolHeight = player.getToolSprite().getRegionHeight();
    }

    /**
     * This is called when the player spawns or changes costumes
     * @param batch: the sprite batch to use to draw
     * @param character: the new character to draw
     * @param team: the new team color to draw
     */
    public void setBodySprite(SpriteBatch batch, UnlockCharacter character, AlignmentFilter team) {
        boolean replace = false;

        //replace frame buffer if the input contains a new character or team
        if (this.character != character && character != null) {
            replace = true;
        }

        if (this.team != team && team != null) {
            replace = true;
        }

        if (replace) {
            replaceBodySprite(batch, character, team);
        }
    }

    /**
     * This actually draws the player's new frame buffer
     * @param batch: the sprite batch to draw the new sprite to
     * @param newCharacter: the new character to draw
     * @param newTeam: the new team color to draw
     */
    public void replaceBodySprite(SpriteBatch batch, UnlockCharacter newCharacter, AlignmentFilter newTeam) {

        //dispose of old frame buffer
        if (fbo != null) {
            fbo.dispose();
        }

        if (newCharacter != null) {
            this.character = newCharacter;
        }
        if (newTeam != null) {
            this.team = newTeam;
        }

        //obtain new texture and create new frame buffer object
        Texture tex = character.getTexture();
        TextureAtlas atlas = character.getAtlas();

        fbo = new FrameBuffer(Pixmap.Format.RGBA4444, tex.getWidth(), tex.getHeight(), true);

        fbo.begin();

        //clear buffer, set camera
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());

        //use shader to apply new team color
        batch.begin();
        ShaderProgram shader = null;
        if (team.isTeam() && team != AlignmentFilter.NONE) {
            shader = team.getShader(character);
            batch.setShader(shader);
        }

        batch.draw(tex, 0, 0);

        if (shader != null) {
            batch.setShader(null);
        }
        batch.end();
        fbo.end();

        if (shader != null) {
            shader.dispose();
        }

        //use new frame buffer to create texture regions for each body part.
        TextureRegion fboRegion = new TextureRegion(fbo.getColorBufferTexture());

        bodyRunSprite =  copyFrames(fboRegion, atlas, "body_run", PlayState.spriteAnimationSpeed);
        bodyStillSprite =  copyFrames(fboRegion, atlas, "body_stand", PlayState.spriteAnimationSpeed);
        headSprite =  copyFrames(fboRegion, atlas, "head", PlayState.spriteAnimationSpeed);
        bodyBackSprite = copyFrame(fboRegion, atlas, "body_background");
        armSprite = copyFrame(fboRegion, atlas, "arm");
        gemSprite = copyFrame(fboRegion, atlas, "gem_active");

        //obtain body dimensions
        this.armWidth = armSprite.getRegionWidth();
        this.armHeight = armSprite.getRegionHeight();
        this.headWidth = headSprite.getKeyFrame(0).getRegionWidth();
        this.headHeight = headSprite.getKeyFrame(0).getRegionHeight();
        this.bodyWidth = bodyRunSprite.getKeyFrame(0).getRegionWidth();
        this.bodyHeight = bodyRunSprite.getKeyFrame(0).getRegionHeight();
        this.bodyBackWidth = bodyBackSprite.getRegionWidth();
        this.bodyBackHeight = bodyBackSprite.getRegionHeight();
        this.gemHeight = gemSprite.getRegionHeight();
        this.gemWidth = gemSprite.getRegionWidth();
    }

    /**
     *
     * @param batch: sprite batch to render player to
     * @param attackAngle: the angle of the player's arm
     * @param moveState: is the player currently moving?
     * @param animationTime: the animation frame of the current sprite
     * @param animationTimeExtra: the animation frame of the current sprite's head
     * @param grounded: is the player on the ground?
     * @param playerLocation: where is the player located at?
     */
    public void render(SpriteBatch batch, float attackAngle, MoveState moveState, float animationTime, float animationTimeExtra,
                       boolean grounded, Vector2 playerLocation) {

        //flip determines if the player is facing left or right
        boolean flip = Math.abs(attackAngle) > 90;

        //Depending on which way the player is facing, the connection points of various body parts are slightly offset.
        float armConnectXReal = armConnectX;
        float headConnectXReal = headConnectX;
        float armRotateXReal = armRotateX;

        float realAttackAngle = attackAngle;
        if (flip) {
            armConnectXReal = bodyWidth - armWidth - armConnectX - 200;
            headConnectXReal = bodyWidth - headWidth - headConnectX - 200;
            armRotateXReal = armWidth - armRotateX;
            realAttackAngle += 180;
        }

        //This switch determines the total body y-offset to make the body bob up and down when running.
        //offset head is separate for some characters to have head bobbing
        float yOffset;
        float yOffsetHead;
        boolean moving = moveState.equals(MoveState.MOVE_LEFT) || moveState.equals(MoveState.MOVE_RIGHT);
        int bodyFrame = bodyRunSprite.getKeyFrameIndex(animationTime);
        int headFrame = bodyRunSprite.getKeyFrameIndex(animationTimeExtra);

        yOffset = character.getWobbleOffsetBody(bodyFrame, grounded, moving);
        yOffsetHead = character.getWobbleOffsetHead(bodyFrame, headFrame, grounded, moving);

        //Draw a bunch of stuff
        batch.draw(player.getToolSprite(),
            (flip ? toolWidth * scale : 0) + playerLocation.x - hbWidth * scale / 2 + armConnectXReal * scale,
            playerLocation.y - hbHeight * scale / 2 + armConnectY * scale + yOffset,
            (flip ? -armWidth * scale : 0) + armRotateXReal * scale , armRotateY * scale,
            (flip ? -1 : 1) * toolWidth * scale, toolHeight * scale, 1, 1, realAttackAngle);

        batch.draw(bodyBackSprite,
            (flip ? bodyBackWidth * scale : 0) + playerLocation.x - hbWidth * scale / 2 + bodyConnectX * scale,
            playerLocation.y - hbHeight * scale / 2 + bodyConnectY + yOffset,
            0, 0,
            (flip ? -1 : 1) * bodyBackWidth * scale, bodyBackHeight * scale, 1, 1, 0);

        batch.draw(armSprite,
            (flip ? armWidth * scale : 0) + playerLocation.x - hbWidth * scale / 2 + armConnectXReal * scale,
            playerLocation.y - hbHeight * scale / 2 + armConnectY * scale + yOffset,
            (flip ? -armWidth * scale : 0) + armRotateXReal * scale, armRotateY * scale,
            (flip ? -1 : 1) * armWidth * scale, armHeight * scale, 1, 1, realAttackAngle);

        batch.draw(gemSprite,
            (flip ? gemWidth * scale : 0) + playerLocation.x - hbWidth * scale / 2  + bodyConnectX * scale,
            playerLocation.y - hbHeight * scale / 2 + bodyConnectY + yOffset,
            0, 0,
            (flip ? -1 : 1) * gemWidth * scale, gemHeight * scale, 1, 1, 0);

        //reverse determines whether the player is running forwards or backwards.
        if (moveState.equals(MoveState.MOVE_LEFT)) {

            if (Math.abs(realAttackAngle) > 90) {
                bodyRunSprite.setPlayMode(Animation.PlayMode.LOOP_REVERSED);
            } else {
                bodyRunSprite.setPlayMode(Animation.PlayMode.LOOP);
            }

            batch.draw(bodyRunSprite.getKeyFrame(animationTime),
                (flip ? bodyWidth * scale : 0) + playerLocation.x - hbWidth * scale / 2  + bodyConnectX * scale,
                playerLocation.y - hbHeight * scale / 2  + bodyConnectY + yOffset,
                0, 0,
                (flip ? -1 : 1) * bodyWidth * scale, bodyHeight * scale, 1, 1, 0);
        } else if (moveState.equals(MoveState.MOVE_RIGHT)) {
            if (Math.abs(realAttackAngle) < 90) {
                bodyRunSprite.setPlayMode(Animation.PlayMode.LOOP_REVERSED);
            } else {
                bodyRunSprite.setPlayMode(Animation.PlayMode.LOOP);
            }

            batch.draw(bodyRunSprite.getKeyFrame(animationTime),
                (flip ? bodyWidth * scale : 0) + playerLocation.x - hbWidth * scale / 2  + bodyConnectX * scale,
                playerLocation.y - hbHeight * scale / 2  + bodyConnectY + yOffset,
                0, 0,
                (flip ? -1 : 1) * bodyWidth * scale, bodyHeight * scale, 1, 1, 0);
        } else {
            bodyRunSprite.setPlayMode(Animation.PlayMode.LOOP);
            batch.draw(grounded ? bodyStillSprite.getKeyFrame(animationTime, true) :
                    bodyRunSprite.getKeyFrame(player.getFreezeFrame(false)),
                (flip ? bodyWidth * scale : 0) + playerLocation.x - hbWidth * scale / 2  + bodyConnectX * scale,
                playerLocation.y - hbHeight * scale / 2  + bodyConnectY + yOffset,
                0, 0,
                (flip ? -1 : 1) * bodyWidth * scale, bodyHeight * scale, 1, 1, 0);
        }

        batch.draw(headSprite.getKeyFrame(animationTimeExtra, true),
            (flip ? headWidth * scale : 0) + playerLocation.x - hbWidth * scale / 2 + headConnectXReal * scale,
            playerLocation.y - hbHeight * scale / 2 + headConnectY * scale + yOffsetHead,
            0, 0,
            (flip ? -1 : 1) * headWidth * scale, headHeight * scale, 1, 1, 0);
    }

    /**
     * This is run when the player is disposed.
     * In the case of the player being disposed upon level transition, we want to make sure the fbo is cleaned up
     */
    public void dispose(DespawnType despawn) {
        if (despawn.equals(DespawnType.LEVEL_TRANSITION)) {
            if (fbo != null) {
                fbo.dispose();
            }
        }
    }

    /**
     * This is run when the player despawns from disconnecting or dying.
     */
    public void despawn(DespawnType type, Vector2 playerLocation, Vector2 playerVelocity) {
        switch (type) {
            case GIB:
                createGibs(playerLocation, playerVelocity);
                break;
            case TELEPORT:

                //if the player disconnects/becomes a spectator, we dispose of the fbo right away.
                if (fbo != null) {
                    fbo.dispose();
                }
                break;
        }
    }

    private static final float gibDuration = 3.0f;
    private static final float gibGravity = 1.0f;
    private void createGibs(Vector2 playerLocation, Vector2 playerVelocity) {
        Ragdoll headRagdoll = new Ragdoll(player.getState(), playerLocation, new Vector2(headWidth, headHeight).scl(scale),
					headSprite.getKeyFrame(0), playerVelocity, gibDuration, gibGravity, true, false) {

            //we need to dispose of the fbo when the ragdolls are done
            @Override
            public void dispose() {
                super.dispose();
                if (fbo != null) {
                    fbo.dispose();
                }
            }
        };

        Ragdoll bodyRagdoll = new Ragdoll(player.getState(), playerLocation, new Vector2(bodyWidth, bodyHeight).scl(scale),
                bodyStillSprite.getKeyFrame(0), playerVelocity, gibDuration, gibGravity, true, false);

        Ragdoll armRagdoll = new Ragdoll(player.getState(), playerLocation, new Vector2(armWidth, armHeight).scl(scale),
                armSprite, playerVelocity, gibDuration, gibGravity, true, false);

        Ragdoll backRagdoll = new Ragdoll(player.getState(), playerLocation, new Vector2(bodyBackWidth, bodyBackHeight).scl(scale),
                bodyBackSprite, playerVelocity, gibDuration, gibGravity, true, false);

        Ragdoll toolRagdoll = new Ragdoll(player.getState(), playerLocation, new Vector2(toolWidth, toolHeight).scl(scale),
                player.getToolSprite(), playerVelocity, gibDuration, gibGravity, true, false);

        //the client needs to create ragdolls separately b/c we can't serialize the frame buffer object.
        if (!player.getState().isServer()) {
            ((ClientState) player.getState()).addEntity(headRagdoll.getEntityID().toString(), headRagdoll, false, ClientState.ObjectSyncLayers.STANDARD);
            ((ClientState) player.getState()).addEntity(bodyRagdoll.getEntityID().toString(), bodyRagdoll, false, ClientState.ObjectSyncLayers.STANDARD);
            ((ClientState) player.getState()).addEntity(armRagdoll.getEntityID().toString(), armRagdoll, false, ClientState.ObjectSyncLayers.STANDARD);
            ((ClientState) player.getState()).addEntity(backRagdoll.getEntityID().toString(), backRagdoll, false, ClientState.ObjectSyncLayers.STANDARD);
            ((ClientState) player.getState()).addEntity(toolRagdoll.getEntityID().toString(), toolRagdoll, false, ClientState.ObjectSyncLayers.STANDARD);
        }
    }

    /**
     * These 2 methods copy animations and still images from the new frame buffer using the old texture atlas.
     */
    private Animation<TextureRegion> copyFrames(TextureRegion tex, TextureAtlas atlas, String region, float animationSpeed) {
        Array<TextureRegion> frames = new Array<>();
        for (TextureRegion texRegion: atlas.findRegions(region)) {
            frames.add(new TextureRegion(tex, texRegion.getRegionX(), tex.getRegionHeight() - texRegion.getRegionY(),
                texRegion.getRegionWidth(), -texRegion.getRegionHeight()));
        }
        return new Animation<>(animationSpeed, frames);
    }

    private TextureRegion copyFrame(TextureRegion tex, TextureAtlas atlas, String region) {
        TextureRegion texRegion = atlas.findRegion(region);
        return new TextureRegion(tex, texRegion.getRegionX(), tex.getRegionHeight() - texRegion.getRegionY(),
            texRegion.getRegionWidth(), -texRegion.getRegionHeight());
    }

    public enum DespawnType {
        LEVEL_TRANSITION,
        GIB,
        TELEPORT,
    }
}
