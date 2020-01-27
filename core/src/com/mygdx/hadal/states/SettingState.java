package com.mygdx.hadal.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.actors.Text;
import com.mygdx.hadal.input.PlayerAction;
import com.mygdx.hadal.managers.GameStateManager;

/**
 * The Control State allows the player to change their key bindings.
 * @author Zachary Tu
 *
 */
public class SettingState extends GameState {
	
	//These are all of the display and buttons visible to the player.
	private Text exitOption, saveOption, resetOption, windowOption;
	
	//This scrollpane holds the options for key bindings
	private ScrollPane options;
	
	//This is the option that the player has selected to change
	private PlayerAction currentlyEditing;
	
	private PlayState ps;
	
	public SettingState(GameStateManager gsm, PlayState ps) {
		super(gsm);
		this.ps = ps;
	}

	@Override
	public void show() {
		stage = new Stage() {
			{
				exitOption = new Text("EXIT?", 100, HadalGame.CONFIG_HEIGHT - 260, Color.WHITE);
				exitOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
			        	getGsm().removeState(SettingState.class);
			        }
			    });
				exitOption.setScale(0.5f);
				
				saveOption = new Text("SAVE?", 100, HadalGame.CONFIG_HEIGHT - 300, Color.WHITE);
				saveOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
			        	PlayerAction.saveKeys();
			        }
			    });
				saveOption.setScale(0.5f);
				
				resetOption = new Text("RESET?", 100, HadalGame.CONFIG_HEIGHT - 340, Color.WHITE);
				resetOption.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
			        	PlayerAction.resetKeys();
			        	refreshBinds();
			        }
			    });
				resetOption.setScale(0.5f);
				
				windowOption = new Text("FULLSCREEN?", 100, HadalGame.CONFIG_HEIGHT - 380, Color.WHITE);
				windowOption.addListener(new ClickListener() {
					
					@Override
			        public void clicked(InputEvent e, float x, float y) {
			        	Monitor currMonitor = Gdx.graphics.getMonitor();
			        	DisplayMode displayMode = Gdx.graphics.getDisplayMode(currMonitor);
			        	if (Gdx.graphics.isFullscreen()) {
			        		Gdx.graphics.setWindowedMode(HadalGame.CONFIG_WIDTH, HadalGame.CONFIG_HEIGHT);
			        	} else {
			        		Gdx.graphics.setFullscreenMode(displayMode);
			        	}
			        }
			    });
				windowOption.setScale(0.5f);
				
				addActor(exitOption);
				addActor(saveOption);
				addActor(resetOption);
				addActor(windowOption);
			}
		};
		app.newMenu(stage);
		
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(new InputProcessor() {

			@Override
			public boolean keyDown(int keycode) {
				//If the player is currently editing an action, bind it to the pressed key
				if (currentlyEditing != null) {
					currentlyEditing.setKey(keycode);
					refreshBinds();
					currentlyEditing = null;
				}
				return false;
			}

			@Override
			public boolean keyUp(int keycode) {	return false; }

			@Override
			public boolean keyTyped(char character) { return false; }

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) { return keyDown(button); }

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {	return false; }

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

			@Override
			public boolean mouseMoved(int screenX, int screenY) { return false; }

			/**
			 * This is just a janky way of implementing setting mouse wheel as a hotkey.
			 */
			@Override
			public boolean scrolled(int amount) { return keyDown(amount * 1000); }
			
		});
		inputMultiplexer.addProcessor(Gdx.input.getInputProcessor());
		Gdx.input.setInputProcessor(inputMultiplexer);
		refreshBinds();
	}
	
	/**
	 * This is called whenever a bind is changed to update the ui.
	 */
	public void refreshBinds() {
		VerticalGroup actions = new VerticalGroup().space(10).pad(50);
		actions.addActor(new Text("CONTROLS", 0, 0));
		
		for (PlayerAction a : PlayerAction.values()) {
			
			final PlayerAction action = a;
			Text actionChoose = new Text(a.name() + ":==   " + getKey(a.getKey()) , 0, 0);
			
			actionChoose.addListener(new ClickListener() {
				
				@Override
				public void clicked(InputEvent e, float x, float y) {
					
					//Clicking any option will highlight it and designate it as the next to update.
					((Text)e.getListenerActor()).setText(action.name() + ":==   " + getKey(action.getKey()) + " <--");					
					currentlyEditing = action;
				}
			});
			
			actionChoose.setScale(0.4f);
			actions.addActor(actionChoose);
		}
		
		if (options != null) {
			options.remove();
		}
		
		options = new ScrollPane(actions, getGsm().getSkin());
		options.setFadeScrollBars(false);
		options.setPosition(200, 0);
		options.setSize(HadalGame.CONFIG_WIDTH - 200, HadalGame.CONFIG_HEIGHT);
		
		stage.addActor(options);
		stage.setScrollFocus(options);
	}
	
	/**
	 * This converts a keycode to s readable string
	 * @param keycode: key to read
	 * @return: string to return 
	 */
	public String getKey(int keycode) {
		
		if (keycode == 0) {	return "MOUSE_LEFT"; }
		
		if (keycode == 1) { return "MOUSE_RIGHT"; }		
		
		if (keycode == 2) {	return "MOUSE_MIDDLE"; }
		
		if (keycode == -1000) {	return "M_WHEEL_UP"; }
		
		if (keycode == 1000) { return "M_WHEEL_DOWN"; }
		
		return Input.Keys.toString(keycode);
	}
	
	@Override
	public void update(float delta) {
		//The playstate underneath should have their camera focus and ui act (letting dialog appear + disappear)
		if (ps != null) {
			ps.cameraUpdate();
			ps.stage.act();
		}
	}

	@Override
	public void render(float delta) {
		//Render the playstate and playstate ui underneath
		if (ps != null) {
			ps.render(delta);
			ps.stage.getViewport().apply();
			ps.stage.draw();
		}
	}
	
	@Override
	public void dispose() { stage.dispose(); }
	
	public PlayState getPs() { return ps; }
}