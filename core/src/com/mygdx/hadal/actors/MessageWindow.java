package com.mygdx.hadal.actors;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.actors.DialogBox.DialogType;
import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.equip.WeaponUtils;
import com.mygdx.hadal.input.ClientController;
import com.mygdx.hadal.input.PlayerAction;
import com.mygdx.hadal.input.PlayerController;
import com.mygdx.hadal.managers.AssetList;
import com.mygdx.hadal.managers.GameStateManager;
import com.mygdx.hadal.server.Packets;
import com.mygdx.hadal.server.User;
import com.mygdx.hadal.states.ClientState;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.ConsoleCommandUtil;

import java.util.ArrayList;

/**
 * The MessageWindow is a ui actor that pops up when the player presses the chat button (default binding shift).
 * This window lets the player type and send messages.
 * @author Clehoff Cenderbender
 */
public class MessageWindow {

	private static final int width = 380;
	private static final int scrollWidth = 360;
	private static final int scrollBarPadding = 10;
	private static final int height = 200;

	private static final int windowX = 0;
	private static final int windowY = 120;

	public static final float logScale = 0.25f;

	public static final float logPadding = 7.5f;
	private static final int inputWidth = 200;
	public static final float inputHeight = 15.0f;
	public static final float inputPad = 5.0f;

	private final PlayState state;
	private final Stage stage;
	
	public Table table, tableLog;
	
	private TextField enterMessage;
	private ScrollPane textLog;
	
	//is this window currently active/invisible? is this window locked and unable to be toggled?
	private boolean active, invisible, locked;

	private static final int maxMessageLength = 80;
	private static final int padding = 10;

	private static final float inactiveTransparency = 0.5f;
	private static final float inactiveFadeDelay = 5.0f;
	private float inactiveFadeCount;

	private static final ArrayList<String> textRecord = new ArrayList<>();
	private final TextureRegion grey;

	public MessageWindow(PlayState state, Stage stage) {
		this.state = state;
		this.stage = stage;
		this.grey = new TextureRegion((Texture) HadalGame.assetManager.get(AssetList.GREY.toString()));

		this.table = new Table() {

			@Override
			public void act(float delta) {
				super.act(delta);
				if (!active) {
					if (inactiveFadeCount > inactiveFadeDelay) {
						invisible = true;
					}

					if (inactiveFadeCount <= inactiveFadeDelay) {
						inactiveFadeCount += delta;
					}
				}
			}

			@Override
			public void draw(Batch batch, float parentAlpha) {

				if (invisible) { return; }

				if (!active) {
					batch.setColor(1.0f,  1.0f, 1.0f, inactiveTransparency);
				}

				batch.draw(grey, getX() - padding / 2.0f, getY() - padding / 2.0f, getWidth() + padding, getHeight() + padding);

				if (active) {
					super.draw(batch, parentAlpha);
				} else {
					super.draw(batch, inactiveTransparency);
				}

				if (!active) {
					batch.setColor(1.0f,  1.0f, 1.0f, 1.0f);
				}
			}
		};

		table.center();
		this.tableLog = new Table().center();

		addTable();
	}
	
	/**
	 * This toggles the window on and off. It is run when the player presses the chat button
	 * This sets the keyboard focus to the window/back to the playstate
	 */
	public void toggleWindow() {
		
		//window is locked in the results state
		if (locked) { return; }

		textLog.scrollTo(0, 0, 0, 0);
		if (active) {
			stage.setKeyboardFocus(null);
			if (stage.getScrollFocus() == textLog) {
				stage.setScrollFocus(null);
			}

			if (state.getController() != null) {
				if (state.isServer()) {
					((PlayerController) state.getController()).syncController();
				} else {
					((ClientController) state.getController()).syncController();
				}
			}
			fadeOut();
		} else {
			stage.setKeyboardFocus(enterMessage);
			stage.setScrollFocus(textLog);

			if (state.getController() != null) {
				if (state.isServer()) {
					((PlayerController) state.getController()).resetController();
				} else {
					((ClientController) state.getController()).resetController();
				}
			}
			fadeIn();
		}

		SoundEffect.UISWITCH2.play(state.getGsm(), 1.0f, false);
		enterMessage.setText("");
	}
	
	/**
	 * Sends a message.
	 * Server sends the message to all clients.
	 * Clients send a message to the server (who then relays it to all clients)
	 */
	public void sendMessage() {
		if (active) {
			if (!enterMessage.getText().equals("")) {
				if (state.isServer()) {
					if (ConsoleCommandUtil.parseChatCommand(state, state.getPlayer(), enterMessage.getText()) == -1) {
						if (state.getGsm().getSetting().isConsoleEnabled()) {
							if (ConsoleCommandUtil.parseConsoleCommand(state, enterMessage.getText()) == -1) {
								HadalGame.server.addChatToAll(state, enterMessage.getText(), DialogType.DIALOG, 0);
							}
						} else {
							HadalGame.server.addChatToAll(state, enterMessage.getText(), DialogType.DIALOG, 0);
						}
					}
				} else {
					if (ConsoleCommandUtil.parseChatCommandClient((ClientState) state, state.getPlayer(), enterMessage.getText()) == -1) {
						HadalGame.client.sendTCP(new Packets.ClientChat(enterMessage.getText(), DialogType.DIALOG));
					}
				}
			}
		}
		enterMessage.setText("");
	}
	
	/**
	 * This adds the table to the stage.
	 * It is called when the actor is instantiated
	 */
	private void addTable() {
		table.clear();
		stage.addActor(table);

		table.setPosition(windowX, windowY);
		table.setWidth(width);
		table.setHeight(height);

		tableLog.padBottom(logPadding);

		textLog = new ScrollPane(tableLog, GameStateManager.getSkin());
		textLog.setFadeScrollBars(true);

		enterMessage = new TextField("", GameStateManager.getSkin()) {
			
			private boolean typing;
			private static final float typingInterval = 0.5f;
			private float typeCount;
			@Override
			protected InputListener createInputListener () {
				
				return new TextFieldClickListener() {
					
					@Override
		            public boolean keyDown(InputEvent event, int keycode) {
						if (keycode != Keys.ENTER && keycode != PlayerAction.EXIT_MENU.getKey()) {
							typing = true;
						}

						//window scrolls to bottom when typing
						textLog.scrollTo(0, 0, 0, 0);

						return super.keyDown(event, keycode);
					}
					
					@Override
		            public boolean keyUp(InputEvent event, int keycode) {
		                if (keycode == Keys.ENTER) {
		                	sendMessage();
		                } else if (keycode == PlayerAction.EXIT_MENU.getKey()) {
		                	toggleWindow();
		                }
		                return super.keyUp(event, keycode);
		            }
		        };
			}
			
			@Override
            public void act(float delta) {
            	super.act(delta);
            	typeCount += delta;
            	if (typeCount >= typingInterval) {
            		typeCount = 0;
            		if (typing) {
            			typing = false;
            			state.getPlayer().startTyping();
            			if (state.isServer()) {
            				HadalGame.server.sendToAllUDP(new Packets.SyncTyping(state.getPlayer().getEntityID().toString()));
            			} else {
            				HadalGame.client.sendUDP(new Packets.SyncTyping(state.getPlayer().getEntityID().toString()));
            			}
            		}
            	}
            }
		};

		enterMessage.setMaxLength(maxMessageLength);

		table.add(textLog).width(scrollWidth).expandY().pad(inputPad).top().left().row();
		table.add(enterMessage).width(inputWidth).height(inputHeight).bottom().center();

		//windows starts off retracted
		fadeOut();
		
		//load previously sent messages so chat log doesn't clear on level transition
		for (String s: textRecord) {
			addTextLine(s);
		}
		
		state.getStage().addCaptureListener(new InputListener() {

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (!table.isAscendantOf(event.getTarget())) {
		        	if (active) {
		        		toggleWindow();
		        	}
		        }
		        return false;
			}
		});
	}
	
	/**
	 * This adds a text to the text log. Called whenever a dialog is added to the dialog box.
	 * @param text: the new string we add to the message window
	 */
	public void addText(String text, DialogType type, int connID) {
		User user;
		if (state.isServer()) {
			user = HadalGame.server.getUsers().get(connID);
		} else {
			user = HadalGame.client.getUsers().get(connID);
		}

		//do not display messages from muted players
		if (user != null) {
			if (!user.isMuted()) {
				textRecord.add(user.getPlayer().getName() + ": " + text);

				if (type.equals(DialogType.SYSTEM)) {
					addTextLine("[RED]" + user.getPlayer().getName() + ": " + text + " []");
				} else {
					addTextLine(WeaponUtils.getPlayerColorName(user.getPlayer()) + ": " + text + " []");
				}
			}
		}
	}
	
	/**
	 * After adding a text to the dialog record, we create a text actor for it and add that to the dialog box actor.
	 */
	private void addTextLine(String text) {
		Text newEntry = new Text(text, 0, 0, false, true, scrollWidth - scrollBarPadding);
		newEntry.setScale(logScale);
		newEntry.setFont(HadalGame.SYSTEM_FONT_UI_SMALL);

		tableLog.add(newEntry).pad(logPadding, 0, logPadding, scrollBarPadding).width(scrollWidth - scrollBarPadding).left().row();
		textLog.scrollTo(0, 0, 0, 0);

		invisible = false;
		inactiveFadeCount = 0.0f;
	}

	private void fadeOut() {
		textLog.setTouchable(Touchable.disabled);
		enterMessage.setVisible(false);
		active = false;

		invisible = false;
		inactiveFadeCount = 0.0f;
	}

	private void fadeIn() {
		enterMessage.setVisible(true);
		textLog.setTouchable(Touchable.enabled);
		active = true;
		invisible = false;
		inactiveFadeCount = 0.0f;
	}

	public static ArrayList<String> getTextRecord() { return textRecord; }	
	
	public boolean isActive() { return active; }
	
	/**
	 * this is used to create the result's version of the message window
	 * this makes the window locked into being active
	 */
	public void setLocked(boolean locked) { this.locked = locked; }
}
