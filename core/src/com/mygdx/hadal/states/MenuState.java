package com.mygdx.hadal.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.actors.MenuBackdrop;
import com.mygdx.hadal.actors.Text;
import com.mygdx.hadal.managers.GameStateManager;

/**
 * The MenuState is pulled up by pausing in game.
 * @author Zachary Tu
 *
 */
public class MenuState extends GameState {

	private Stage stage;
	
	//Temporary links to other modules for testing.
	private Actor resumeOption, exitOption;
		
	/**
	 * Constructor will be called once upon initialization of the StateManager.
	 * @param gsm
	 */
	public MenuState(final GameStateManager gsm) {
		super(gsm);
		
		stage = new Stage() {
			{
				addActor(new MenuBackdrop(HadalGame.assetManager));
				resumeOption = new Text(HadalGame.assetManager, "RESUME?", 150, HadalGame.CONFIG_HEIGHT - 300);
				exitOption = new Text(HadalGame.assetManager, "EXIT?", 150, HadalGame.CONFIG_HEIGHT - 340);
				
				resumeOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
			        	gsm.removeState();
			        }
			    });
				resumeOption.setScale(0.5f);
				
				exitOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
			        	Gdx.app.exit();
			        }
			    });
				exitOption.setScale(0.5f);
				
				addActor(resumeOption);
				addActor(exitOption);
			}
		};
		app.newMenu(stage);
	}

	/**
	 * 
	 */
	@Override
	public void update(float delta) {
		stage.act();
	}

	/**
	 * This state will draw the image.
	 */
	@Override
	public void render() {
		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(hud.combined);
		batch.begin();
		stage.draw();
		batch.end();
	}

	/**
	 * Delete the image texture.
	 */
	@Override
	public void dispose() {
		stage.dispose();
	}

}
