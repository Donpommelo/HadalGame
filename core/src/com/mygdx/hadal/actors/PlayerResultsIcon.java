package com.mygdx.hadal.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.save.UnlockCharacter;
import com.mygdx.hadal.server.AlignmentFilter;
import com.mygdx.hadal.server.SavedPlayerFields;
import com.mygdx.hadal.server.SavedPlayerFieldsExtra;
import com.mygdx.hadal.states.ResultsState;

/**
 * A PlayerResultsIcon represents a single player in the results screen.
 * These are ordered by score
 */
public class PlayerResultsIcon extends AHadalActor {

	private final BitmapFont font;

	private static final float fontScale = 0.22f;
	private static final float spriteScale = 0.25f;
	private static final int maxNameLen = 25;

	private static final float readyWidth = 96.0f;
	private static final float readyHeight = 96.0f;

	private static final float spriteHeight = 922.0f;
	private static final float spriteWidth = 614.0f;

	private static final float textOffsetX = 20.0f;
	private static final float textOffsetY = -20.0f;
	private static final float textWidth = 125.0f;

	private static final float readyOffsetX = 20.0f;
	private static final float readyOffsetY = 140.0f;

	protected GlyphLayout layout;

	private TextureRegion playerSprite;
	private final TextureRegion readyIcon;

	//this string identifies the player as well as their score information
	private String name;

	private final FrameBuffer fbo;

	private boolean ready;

	public PlayerResultsIcon(ResultsState state, SpriteBatch batch, SavedPlayerFields fields, SavedPlayerFieldsExtra fieldsExtra) {
		font = HadalGame.SYSTEM_FONT_UI;

		this.name = fields.getNameAbridged(maxNameLen);
		name += "\nK/D: " + fields.getKills() + " / " + fields.getDeaths();

		//if this is a pvp match with eggplants, we want to add that information to the text
		if (state.getPs().isEggplantDrops()) {
			name += "\nEggplants: " + fields.getScore();
		} else {
			name += "\nScore: " + fields.getScore();
		}

		this.readyIcon = Sprite.EMOTE_READY.getFrame();

		UnlockCharacter character = fieldsExtra.getLoadout().character;

		//if the player won the game, we display a winning sprite. Otherwise: sluggo.
		if (fields.isWonLast()) {
			this.playerSprite = character.getBuffSprite().getFrame();
		} else {
			this.playerSprite = character.getSlugSprite().getFrame();
		}

		setHeight(spriteHeight * spriteScale);
		setWidth(spriteWidth * spriteScale);

		//Based on the player color, we create an fbo to accurately display their sprite.
		AlignmentFilter team = fieldsExtra.getLoadout().team;

		fbo = new FrameBuffer(Pixmap.Format.RGBA4444, playerSprite.getRegionWidth(), playerSprite.getRegionHeight(), true);

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

		batch.draw(playerSprite, 0, 0);

		if (shader != null) {
			batch.setShader(null);
		}
		batch.end();
		fbo.end();

		if (shader != null) {
			shader.dispose();
		}

		TextureRegion fboRegion = new TextureRegion(fbo.getColorBufferTexture());

		playerSprite = new TextureRegion(fboRegion, fboRegion.getRegionX(), fboRegion.getRegionHeight() - fboRegion.getRegionY(),
			fboRegion.getRegionWidth(), - fboRegion.getRegionHeight());
	}
	
	@Override
    public void draw(Batch batch, float alpha) {
		batch.draw(playerSprite, getX(), getY(), playerSprite.getRegionWidth() * spriteScale, playerSprite.getRegionHeight() * spriteScale);

		font.getData().setScale(fontScale);
		font.draw(batch, name, getX() + textOffsetX,getY() + textOffsetY, textWidth, Align.center, true);

		if (ready) {
			batch.draw(readyIcon, getX() + readyWidth + readyOffsetX, getY() + readyOffsetY, -readyWidth, readyHeight);
		}
    }

    public void dispose() {
		fbo.dispose();
	}

	public void setReady(boolean ready) { this.ready = ready; }
}
