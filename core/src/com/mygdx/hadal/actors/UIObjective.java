package com.mygdx.hadal.actors;

import static com.mygdx.hadal.utils.Constants.PPM;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.managers.AssetList;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.SteeringUtil;

/**
 * UIMomentum appears in the bottom right screen and displays information about the player's momentum freezing cd and stored momentums
 * @author Zachary Tu
 *
 */
public class UIObjective extends AHadalActor{

	private Player player;
	private PlayState state;
	
	private TextureAtlas atlas;
	
	private TextureRegion base, ready, overlay;
	private Array<AtlasRegion> arrow;
	
	private float scale = 0.25f;
	
	private float corner;
	
	public UIObjective(AssetManager assetManager, PlayState state, Player player) {
		super(assetManager);
		this.player = player;
		this.state = state;
		
		this.atlas = (TextureAtlas) HadalGame.assetManager.get(AssetList.UIATLAS.toString());
		this.base = atlas.findRegion("UI_momentum_base");
		this.ready = atlas.findRegion("UI_momentum_ready");
		this.overlay = atlas.findRegion("UI_momentum_overlay");
		
		this.arrow = atlas.findRegions("UI_momentum_arrow");
		
		this.corner = SteeringUtil.vectorToAngle(new Vector2(HadalGame.CONFIG_WIDTH, HadalGame.CONFIG_HEIGHT));
	}
	
	@Override
    public void draw(Batch batch, float alpha) {
		batch.setProjectionMatrix(state.hud.combined);

		float x = 500;
		float y = 500;
		
		if (state.getObjectiveTarget() != null) {
			
			float xDist = (player.getBody().getPosition().x * PPM) - (state.getObjectiveTarget().getBody().getPosition().x * PPM);
			float yDist = (player.getBody().getPosition().y * PPM) - (state.getObjectiveTarget().getBody().getPosition().y * PPM);		
			
			if (Math.abs(xDist) > HadalGame.CONFIG_WIDTH / 2 || Math.abs(yDist) > HadalGame.CONFIG_HEIGHT / 2) {
				Vector2 toObjective = new Vector2(xDist, yDist);
				
				float angle = SteeringUtil.vectorToAngle(toObjective);
				
				if (angle < corner && angle > -(Math.PI + corner)) {
					x = (float) (base.getRegionWidth() * scale);
					y = (float) (HadalGame.CONFIG_HEIGHT / 2 + Math.tan(Math.abs(angle) - Math.PI / 2) * (HadalGame.CONFIG_WIDTH / 2 - base.getRegionWidth() * scale));
				}
				if (angle > -corner && angle < (Math.PI + corner)) {
					x = (float) (HadalGame.CONFIG_WIDTH - base.getRegionWidth() * scale);
					y = (float) (HadalGame.CONFIG_HEIGHT / 2 + Math.tan(angle - Math.PI / 2) * (HadalGame.CONFIG_WIDTH / 2 - base.getRegionWidth() * scale));
				}
				if (angle <= -corner && angle >= corner) {
					x = (float) (HadalGame.CONFIG_WIDTH / 2 + Math.tan(angle) * (HadalGame.CONFIG_HEIGHT / 2 - base.getRegionHeight() * scale));
					y = (float) (base.getRegionHeight() * scale);
				}
				if (angle >= (Math.PI + corner) || angle <= -(Math.PI + corner)) {				
					x = (float) (HadalGame.CONFIG_WIDTH / 2 + (angle > 0 ? -1 : 1) * Math.tan(Math.abs(angle) - Math.PI) * (HadalGame.CONFIG_HEIGHT / 2 - base.getRegionHeight() * scale));
					y = (float) (HadalGame.CONFIG_HEIGHT - base.getRegionHeight() * scale);
				}	
			} else {
				batch.setProjectionMatrix(state.sprite.combined);
				x = state.getObjectiveTarget().getBody().getPosition().x * PPM;
				y = state.getObjectiveTarget().getBody().getPosition().y * PPM;
			}
			
			batch.draw(base, x - base.getRegionWidth() * scale / 2, y - base.getRegionHeight() * scale / 2, base.getRegionWidth() * scale, base.getRegionHeight() * scale);
			batch.draw(ready, x - base.getRegionWidth() * scale / 2, y - base.getRegionHeight() * scale / 2, base.getRegionWidth() * scale, base.getRegionHeight() * scale);
			batch.draw(overlay, x - base.getRegionWidth() * scale / 2, y - base.getRegionHeight() * scale / 2, base.getRegionWidth() * scale, base.getRegionHeight() * scale);
			batch.draw(arrow.get(0), x - base.getRegionWidth() * scale / 2, y - base.getRegionHeight() * scale / 2, base.getRegionWidth() * scale / 2, base.getRegionHeight() * scale / 2,
					base.getRegionWidth() * scale, base.getRegionWidth() * scale, 1, 1, 0);
		}
		
		//ARGHARGHARGHARGHARGH
		batch.setProjectionMatrix(state.hud.combined);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

}