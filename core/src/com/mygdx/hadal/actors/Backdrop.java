package com.mygdx.hadal.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.effects.Sprite;

/**
 * Static background actor for any screen that just needs a still image
 */
public class Backdrop extends AHadalActor {
	
	private final TextureRegion backgroundTexture;
	private float width = HadalGame.CONFIG_WIDTH;
	private float height = HadalGame.CONFIG_HEIGHT;

	private boolean mirror;

	public Backdrop(String backdropName, float width, float height) {
		this(backdropName);
		this.width = width;
		this.height = height;
		setWidth(width);
		setHeight(height);
	}
	
	public Backdrop(String backdropName) {
		this.backgroundTexture = new TextureRegion((Texture) HadalGame.assetManager.get(backdropName));
	}

	public Backdrop(Sprite sprite, float width, float height, int frameIndex) {
		this.backgroundTexture = sprite.getFrames().get(frameIndex);
		this.width = width;
		this.height = height;
		setWidth(width);
		setHeight(height);
	}

	@Override
    public void draw(Batch batch, float alpha) {
        if (mirror) {
			batch.draw(backgroundTexture, getX(), getY(), width / 2, height / 2, width, height, -1, 1, 0);
		} else {
			batch.draw(backgroundTexture, getX(), getY(), width, height);
		}
    }

    public Backdrop setMirror() {
		this.mirror = true;
		return this;
	}
}
