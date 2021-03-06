package com.mygdx.hadal.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.actors.DialogBox.DialogType;
import com.mygdx.hadal.actors.Text;
import com.mygdx.hadal.actors.WindowTable;
import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.input.PlayerAction;
import com.mygdx.hadal.managers.GameStateManager;
import com.mygdx.hadal.managers.GameStateManager.Mode;
import com.mygdx.hadal.managers.GameStateManager.State;
import com.mygdx.hadal.save.UnlockLevel;
import com.mygdx.hadal.server.Packets;
import com.mygdx.hadal.server.User;
import com.mygdx.hadal.states.PlayState.TransitionState;

/**
 * The PauseState is pulled up by pausing in game.
 * From here, the player can resume, change settings, exit game.
 * If a client, they can also use the spectator mode.
 * @author Yaptista Yihorn
 */
public class PauseState extends GameState {

	//This table contains the ui elements of the pause screen
	private Table table;
	
	//These are all of the display and buttons visible to the player.
	private Text pause, resumeOption, hubOption, extraOption, settingOption, spectateOption, joinOption, exitOption;
	
	//This is the playstate that the pause state must be placed on top of.
	private final PlayState ps;
	
	//This is the name of the player who paused
	private final String pauser;
	
	//This determines whether the pause state should be removed or not next engine tick.
	//We do this instead of removing right away in case we remove as a result of receiving a packet from another player unpausing (which can happen whenever).
	private boolean toRemove;
	
	//is the game paused underneath this menu?
	// In multiplayer, the pause menu will be brought up, but the game will not be paused (unless changed in settings)
	private final boolean paused;
	
	//Dimensions of the pause menu
	private static final float width = 500;
	private static final float height = 275;
	private static final int extraRowHeight = 55;
	private static final int optionHeight = 48;
	private static final int optionPad = 5;
	private static final float pauseTextScale = 0.3f;
	
	/**
	 * Constructor will be called whenever a player pauses.
	 */
	public PauseState(final GameStateManager gsm, PlayState ps, String pauser, boolean paused) {
		super(gsm);
		this.ps = ps;
		this.pauser = pauser;
		this.paused = paused;
		
		//When the server pauses, it sends a message to all clients to pause them as well.
		if (ps.isServer() && paused) {
			HadalGame.server.sendToAllTCP(new Packets.Paused(pauser));
		}
		
		SoundEffect.POSITIVE.play(gsm, 1.0f, false);
	}

	@Override
	public void show() {
		
		final PauseState me = this;

		//b/c the pause state can get shown multiple times without getting removed, we must get rid of stage if already created
		if (stage != null) {
			stage.dispose();
		}

		stage = new Stage() {
			{
				//make the menu size adjust based on how many options are available
				float menuHeight = height;
				
				//extra "return to hub" option is added if the hub has been reached or if the player is in multiplayer mode.
				if (ps.isServer() && (gsm.getRecord().getFlags().get("HUB_REACHED").equals(1) || GameStateManager.currentMode == Mode.MULTI)) {
					menuHeight += extraRowHeight;
				}
				
				//extra "spectate" option is added if the player is a client and in multiplayer mode.
				if (ps.isHub() && !ps.isServer() && GameStateManager.currentMode == Mode.MULTI) {
					menuHeight += extraRowHeight;
				}
				
				table = new WindowTable();
				table.setLayoutEnabled(true);
				table.setPosition(HadalGame.CONFIG_WIDTH / 2 - width / 2, HadalGame.CONFIG_HEIGHT / 2 - menuHeight / 2);
				table.setSize(width, menuHeight);
				addActor(table);
				
				//text indicates if the game is actually paused or not (if multiplayer pause is disabled in settings)
				if (paused) {
					pause = new Text("PAUSED BY \n" + pauser, 0, 0, false);
				} else {
					pause = new Text("GAME NOT PAUSED", 0, 0, false);
				}
				pause.setScale(pauseTextScale);
				
				resumeOption = new Text("RESUME", 0, 0, true);
				hubOption = new Text("RETURN TO HUB", 0, 0, true);
				settingOption = new Text("SETTINGS", 0, 0, true);
				extraOption = new Text("EXTRA", 0, 0, true);
				spectateOption = new Text("SPECTATE", 0, 0, true);
				joinOption = new Text("JOIN", 0, 0, true);
				exitOption = new Text("EXIT TO TITLE", 0, 0, true);
				
				resumeOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) { unpause(); }
			    });
				
				hubOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) {
			        	gsm.removeState(PauseState.class);
			        	
			        	if (ps.isServer()) {
			        		//If the server unpauses, send a message and notification to all players to unpause.
			        		HadalGame.server.sendToAllTCP(new Packets.Unpaused(ps.getPlayer().getName()));
			        		
			        		if (GameStateManager.currentMode == Mode.SINGLE) {
				        		ps.loadLevel(UnlockLevel.SSTUNICATE1, TransitionState.NEWLEVEL, "");
				        	}
				        	if (GameStateManager.currentMode == Mode.MULTI) {
				        		ps.loadLevel(UnlockLevel.HUB_MULTI, TransitionState.NEWLEVEL, "");
				        	}
	    				}
			        	SoundEffect.NEGATIVE.play(gsm, 1.0f, false);
			        }
			    });
				
				settingOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) {
			        	
			        	//Setting pops a setting state on top of the pause state.
			        	gsm.addState(State.SETTING, me);
			        	SoundEffect.UISWITCH1.play(gsm, 1.0f, false);
			        }
			    });

				extraOption.addListener(new ClickListener() {

					@Override
					public void clicked(InputEvent e, float x, float y) {

						//Setting pops a setting state on top of the pause state.
						gsm.addState(State.ABOUT, me);
						SoundEffect.UISWITCH1.play(gsm, 1.0f, false);
					}
				});
				
				spectateOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) {
						unpause();
						if (ps.isServer()) {
							ps.becomeSpectator(ps.getPlayer(), true);
						} else {
							HadalGame.client.sendTCP(new Packets.StartSpectate());
						}
			        }
			    });
				
				joinOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) {
						unpause();
						if (ps.isServer()) {
							ps.exitSpectator(HadalGame.server.playerToUser(ps.getPlayer()));
						} else {
							HadalGame.client.sendTCP(new Packets.EndSpectate());
						}
			        }
			    });

				exitOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) {
			        	
			        	//Exiting returns to the title state and stops the server/client, disconnecting.
			        	gsm.removeState(PauseState.class);
			        	ps.returnToTitle(0.0f);
			        	
			        	SoundEffect.NEGATIVE.play(gsm, 1.0f, false);
			        }
			    });
				
				table.add(pause).pad(5).expand().top().row();
				table.add(resumeOption).height(optionHeight).pad(optionPad).row();
				
				//don't add return to hub option in singleplayer if hub hasn't been reached yet
				if (ps.isServer() && (gsm.getRecord().getFlags().get("HUB_REACHED").equals(1) || GameStateManager.currentMode == Mode.MULTI)) {
					table.add(hubOption).height(optionHeight).pad(optionPad).row();
				}
				table.add(settingOption).height(optionHeight).pad(optionPad).row();
				table.add(extraOption).height(optionHeight).pad(optionPad).row();

				//atm, only clients can manually join spectator mode
				if (ps.isHub() && !ps.isServer() && GameStateManager.currentMode == Mode.MULTI) {
					if (ps.isSpectatorMode()) {
						table.add(joinOption).height(optionHeight).pad(optionPad).row();
					} else {
						table.add(spectateOption).height(optionHeight).pad(optionPad).row();
					}
				}
				table.add(exitOption).height(optionHeight).pad(optionPad).row();
			}
		};
		app.newMenu(stage);
		
		//We get the playstate input processor so users can send messages + view score when paused
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		
		inputMultiplexer.addProcessor(stage);
		inputMultiplexer.addProcessor(ps.stage);
		
		inputMultiplexer.addProcessor(new InputProcessor() {

			@Override
			public boolean keyDown(int keycode) {				
				
				if (paused) {
					if (keycode == PlayerAction.MESSAGE_WINDOW.getKey()) {
						ps.getController().keyDown(keycode);
					} else if (keycode == PlayerAction.SCORE_WINDOW.getKey()) {
						ps.getScoreWindow().setVisibility(true);
					}
				}
				return false;
			}

			@Override
			public boolean keyUp(int keycode) {	
				if (keycode == PlayerAction.PAUSE.getKey()) {
					unpause();
					
					//we return true here so that the client does not process another pause after the unpause
					return true;
				} else if (keycode == PlayerAction.SCORE_WINDOW.getKey()) {
					ps.getScoreWindow().setVisibility(false);
				}
				return false; 
			}

			@Override
			public boolean keyTyped(char character) { return false; }

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

			@Override
			public boolean mouseMoved(int screenX, int screenY) { return false; }

			@Override
			public boolean scrolled(float amountX, float amountY) {	return false; }
		});
		
		//if the game isn't actually paused, the player can still control the game underneath
		if (!paused) {
			inputMultiplexer.addProcessor(ps.controller);
		}
		
		Gdx.input.setInputProcessor(inputMultiplexer);
	}
	
	@Override
	public void update(float delta) {
		
		//The playstate underneath should have their camera focus and ui act (letting dialog appear + disappear)
		if (ps != null) {
			if (paused) {
				ps.cameraUpdate();
			} else {
				ps.update(delta);
			}
			ps.stage.act();
		}
		
		//If the state has been unpaused, remove it
		if (toRemove) {
			SoundEffect.NEGATIVE.play(gsm, 1.0f, false);
			
			//the following code makes sure that, if the host changes artifact slot number, these changes sync immediately.
			if (ps != null) {
				if (ps.isServer() && ps.isHub()) {
					ps.getPlayer().getPlayerData().syncArtifacts(false, true);
					for (User user : HadalGame.server.getUsers().values()) {
						if (user.getPlayer() != null) {
							if (user.getPlayer().getPlayerData() != null) {
								user.getPlayer().getPlayerData().syncArtifacts(false, true);
							}
						}
					}
				}
				ps.getUiHub().refreshHub();
			}
			gsm.removeState(PauseState.class);
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

	/**
	 * Run when the game is unpaused.
	 */
	public void unpause() {
    	
    	if (ps.isServer()) {
    		toRemove = true;

    		if (paused) {
    			//If the server unpauses, send a message and notification to all players to unpause.
        		HadalGame.server.sendToAllTCP(new Packets.Unpaused(ps.getPlayer().getName()));
    			HadalGame.server.addNotificationToAll(ps, ps.getPlayer().getName(), "UNPAUSED THE GAME!", DialogType.SYSTEM);
    		}
		} else {
			if (paused) {
				//If a client unpauses, tell the server so it can echo it to everyone else
				HadalGame.client.sendTCP(new Packets.Unpaused(ps.getPlayer().getName()));
			} else {
				toRemove = true;
			}
		}
	}
	
	//This is called when the pause state is designated to be removed.
	public void setToRemove(boolean toRemove) {	this.toRemove = toRemove; }

	public PlayState getPs() { return ps; }

	public String getPauser() {	return pauser; }	
	
	//if the game isn't really paused, we want to process transitions (if the player dies or level transitions level while menu is visible)
	@Override
	public boolean processTransitions() { return !paused; }
}
