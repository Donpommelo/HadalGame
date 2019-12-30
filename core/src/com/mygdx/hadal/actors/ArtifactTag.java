package com.mygdx.hadal.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.effects.Sprite;
import com.mygdx.hadal.save.UnlockArtifact;

/**
 * An Artifact tag is a ui element that corresponds to a single artifact in the player's inventory.
 * @author Zachary Tu
 *
 */
public class ArtifactTag extends AHadalActor {

	private UnlockArtifact artifact;
	
	private BitmapFont font;
	
	private float scale = 0.25f;
	private Color color;
	
	private TextureRegion ready;
	
	private boolean mouseOver;
	
	public ArtifactTag(UnlockArtifact artifact) {
		this.artifact = artifact;
		
		font = HadalGame.SYSTEM_FONT_UI;
		color = Color.WHITE;
		
		this.ready = Sprite.UI_MO_READY.getFrame();
		
		mouseOver = false;
		
		addListener(new ClickListener() {
			@Override
			public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				super.enter(event, x, y, pointer, fromActor);
				mouseOver = true;
			}

			@Override
			public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
				super.enter(event, x, y, pointer, toActor);
				mouseOver = false;
			}
		});
	}
	
	@Override
    public void draw(Batch batch, float alpha) {
		batch.draw(ready, getX(), getY(), getWidth(), getHeight());
         
         if (mouseOver) {
        	 font.setColor(color);
        	 font.getData().setScale(scale);
        	 font.draw(batch, artifact.getInfo().getName() + ": " + artifact.getInfo().getDescription(), getX() + 30, getY() + 45);
         }
    }

	public UnlockArtifact getArtifact() { return artifact; }
}
