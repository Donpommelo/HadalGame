package com.mygdx.hadal.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.actors.*;
import com.mygdx.hadal.audio.MusicPlayer;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.equip.Loadout;
import com.mygdx.hadal.managers.AssetList;
import com.mygdx.hadal.managers.GameStateManager;
import com.mygdx.hadal.save.UnlockArtifact;
import com.mygdx.hadal.save.UnlockEquip;
import com.mygdx.hadal.server.Packets;
import com.mygdx.hadal.server.SavedPlayerFields;
import com.mygdx.hadal.server.SavedPlayerFieldsExtra;
import com.mygdx.hadal.server.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Results screen appears at the end of levels and displays the player's results
 * In this screen, the player can return to the hub when all players are ready.
 * @author Juppstein Jodswallop
 */
public class ResultsState extends GameState {

	//This table contains the options for the title.
	private Table table, tableInfo, tableInfoOuter, tableArtifact, tableExtra;
	private ScrollPane infoScroll, charactersScroll;

	private Text infoPlayerName;
	
	//This is the playstate that the results state is placed on top of. Used to access the state's message window
	private final PlayState ps;
	
	//This is a list of all the saved player fields (scores) from the completed playstate
	private final ArrayList<SavedPlayerFields> scores;

    private final ArrayList<PlayerResultsIcon> icons;
	private final ArrayList<PooledEffect> effects;

    //This i sa mapping of players in the completed playstate mapped to whether they're ready to return to the hub.
	private final HashMap<SavedPlayerFields, Boolean> ready;
	
	//this text is displayed at the top of the state and usually indicates victory or loss
	private final String text;

	//if the results text is equal to the magic word, calculate the results text based on score
	public static final String magicWord = "fug";

	//Dimensions and position of the results menu
	private static final int tableX = 20;
	private static final int tableY = 240;
	private static final int tableWidth = 1240;
	private static final int tableHeight = 460;
	private static final int characterScrollHeight = 400;

	private static final int infoXEnabled = 880;
	private static final int infoYEnabled = 20;
	private static final int infoX = 1280;
	private static final int infoY = 20;
	private static final int infoWidth = 380;
	private static final int infoHeight = 200;

	private static final int infoRowHeight = 20;
	private static final float infoTextScale = 0.25f;
	private static final float infoPadY = 15.0f;
	private static final float infoPadYSmall = 5.0f;

	public static final int infoNameHeight = 30;
	public static final int infoScrollHeight = 100;
	public static final int infoNamePadding = 15;

	private static final int tableExtraX = 420;
	private static final int tableExtraY = 20;
	private static final int tableExtraWidth = 440;
	private static final int tableExtraHeight = 200;

	private static final int titleHeight = 40;
	private static final float resultsScale = 0.6f;
	private static final float scale = 0.4f;
	private static final int maxNameLen = 30;

	private static final int optionHeight = 50;

	public static final float artifactTagSize = 40.0f;
	private static final float artifactTagOffsetX = -100.0f;
	private static final float artifactTagOffsetY = 60.0f;
	private static final float artifactTagTargetWidth = 200.0f;

	private static final float particleOffsetX = 75.0f;
	private static final float particleOffsetY = 150.0f;

	private static final float scrollAcceleration = 8.0f;

	private static final int messageX = 20;
	private static final int messageY = 20;
	/**
	 * Constructor will be called whenever the game transitions into a results state
	 * @param text: this is the string that is displayed at the top of the result state
	 */
	public ResultsState(final GameStateManager gsm, String text, PlayState ps) {
		super(gsm);
		this.text = text;
		this.ps = ps;
		
		//First, we obtain the list of scores, depending on whether we are the server or client.
		scores = new ArrayList<>();
		icons = new ArrayList<>();
		effects = new ArrayList<>();

		if (ps.isServer()) {
			for (User user: HadalGame.server.getUsers().values()) {
				if (!user.isSpectator()) {
					scores.add(user.getScores());
				}
			}
			gsm.getRecord().updateScore(scores.get(0).getScore(), ps.level);
		} else {
			for (User user: HadalGame.client.getUsers().values()) {
				if (!user.isSpectator()) {
					scores.add(user.getScores());
				}
			}
		}

		//Then, we sort according to score and give the winner(s) a win. Being on the winning team overrides score
		scores.sort((a, b) -> {
			int cmp = (b.isWonLast() ? 1 : 0) - (a.isWonLast() ? 1 : 0);
			if (cmp == 0) { cmp = b.getTeamScore() - a.getTeamScore(); }
			if (cmp == 0) { cmp = b.getScore() - a.getScore(); }
			return cmp;
		});

		//Finally we initialize the ready map with everyone set to not ready.
		ready = new HashMap<>();
		for (SavedPlayerFields score: scores) {
			ready.put(score, false);
		}
	}
	
	@Override
	public void show() {
		stage = new Stage() {
			{
				addActor(new Backdrop(AssetList.RESULTS_CARD.toString()));

				table = new WindowTable();
				table.setPosition(tableX, tableY);
				table.setSize(tableWidth, tableHeight);
				addActor(table);
				syncScoreTable();

				tableInfoOuter = new WindowTable();

				infoPlayerName = new Text("", 0, 0, false);
				infoPlayerName.setScale(infoTextScale);
				
				tableInfo = new Table();
				tableArtifact = new Table();

				infoScroll = new ScrollPane(tableInfo, GameStateManager.getSkin());
				infoScroll.setFadeScrollBars(false);

				infoScroll.addListener(new InputListener() {

					@Override
					public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
						stage.setScrollFocus(infoScroll);
					}
				});

				tableInfoOuter.add(infoPlayerName).pad(infoNamePadding).height(infoNameHeight).row();
				tableInfoOuter.add(tableArtifact).height(infoNameHeight).row();
				tableInfoOuter.add(infoScroll).width(infoWidth).height(infoScrollHeight);
				tableInfoOuter.setPosition(infoX, infoY);
				tableInfoOuter.setSize(infoWidth, infoHeight);
				
				addActor(tableInfoOuter);

				tableExtra = new WindowTable();

				//These are all of the display and buttons visible to the player.
				final Text readyOption = new Text("RETURN TO LOADOUT?", 0, 0, true);

				readyOption.addListener(new ClickListener() {

					@Override
					public void clicked(InputEvent e, float x, float y) {

						//When pressed, the ready option indicates to the server that that player is ready.
						if (ps.isServer()) {
							readyPlayer(0);
						} else {
							HadalGame.client.sendTCP(new Packets.ClientReady());
						}
					}
				});
				readyOption.setScale(scale);

				final Text forceReadyOption = new Text("FORCE RETURN?", 0, 0, true);

				forceReadyOption.addListener(new ClickListener() {

					@Override
					public void clicked(InputEvent e, float x, float y) {

						//When pressed, the force ready option forces a transition.
						returnToHub();
					}
				});
				forceReadyOption.setScale(scale);

				tableExtra.add(readyOption).height(optionHeight).row();
				if (ps.isServer()) {
					tableExtra.add(forceReadyOption).height(optionHeight);
				}

				tableExtra.setPosition(tableExtraX, tableExtraY);
				tableExtra.setSize(tableExtraWidth, tableExtraHeight);
				addActor(tableExtra);
			}
		};
		
		//we pull up and lock the playstate message window so players can chat in the aftergame.
		if (!ps.getMessageWindow().isActive()) {
			ps.getMessageWindow().toggleWindow();
		}

		HadalGame.musicPlayer.playSong(MusicPlayer.MusicState.NOTHING, 1.0f);

		ps.getMessageWindow().setLocked(true);
		ps.getMessageWindow().table.setPosition(messageX, messageY);
		stage.addActor(ps.getMessageWindow().table);
		gsm.getApp().fadeIn();
		app.newMenu(stage);

		InputMultiplexer inputMultiplexer = new InputMultiplexer();

		inputMultiplexer.addProcessor(stage);

		inputMultiplexer.addProcessor(new InputProcessor() {

			@Override
			public boolean keyDown(int keycode) { return false;	}

			@Override
			public boolean keyUp(int keycode) { return false; }

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
			public boolean scrolled(float amountX, float amountY) {
				if (charactersScroll != null) {
					charactersScroll.setScrollX(charactersScroll.getScrollX() + amountY * scrollAcceleration);
					return true;
				}
				return false;
			}
		});
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	/**
	 * This is called whenever we set the displayed table of scores.
	 * This is done once at the start and once again whenever a player readies themselves.
	 */
	public void syncScoreTable() {
		table.clear();

		final Table tableCharacters = new Table();
		charactersScroll = new ScrollPane(tableCharacters, GameStateManager.getSkin());
		charactersScroll.setFadeScrollBars(false);

		charactersScroll.addListener(new InputListener() {

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				stage.setScrollFocus(null);
			}
		});

		Text title = new Text(text, 0, 0, false);
		title.setScale(resultsScale);

		for (SavedPlayerFields score: scores) {

			int connId = score.getConnID();

			SavedPlayerFields field = null;
			SavedPlayerFieldsExtra fieldExtra = null;
			User user;
			if (ps.isServer()) {
				user =  HadalGame.server.getUsers().get(connId);
			} else {
				user =  HadalGame.client.getUsers().get(connId);
			}
			if (user != null) {
				field = user.getScores();
				fieldExtra = user.getScoresExtra();
			}

			if (field != null && fieldExtra != null) {

				PooledEffect effect = null;
				if (field.isWonLast()) {
					effect = Particle.PARTY.getParticle();
					effects.add(effect);
				}

				final PooledEffect finalEffect = effect;
				PlayerResultsIcon icon = new PlayerResultsIcon(this, batch, field, fieldExtra) {

					@Override
					public void draw(Batch batch, float alpha) {
						super.draw(batch, alpha);
						if (finalEffect != null) {
							finalEffect.setPosition(getX() + particleOffsetX, getY() + particleOffsetY);
							finalEffect.draw(batch, 0.0f);
						}
					}
				};

				icon.addListener(new ClickListener() {

					@Override
					public void clicked (InputEvent event, float x, float y) {
						super.clicked(event, x, y);
						syncInfoTable(score.getConnID());
					}
				});

				tableCharacters.add(icon);

				icons.add(icon);
			}
		}

		table.add(title).height(titleHeight).row();
		table.add(charactersScroll).expandX().height(characterScrollHeight).row();
	}
	
	/**
	 * This fills the window with stats for the designated player
	 */
	private int currentConnId = -1;
	private static final float transitionDuration = 0.1f;
	private static final Interpolation intp = Interpolation.fastSlow;
	public void syncInfoTable(int connId) {

		if (currentConnId == connId) { return; }
		currentConnId = connId;

		tableInfoOuter.addAction(Actions.sequence(Actions.moveTo(infoX, infoY, transitionDuration, intp), Actions.run(() -> {
			tableInfo.clear();
			tableArtifact.clear();

			SavedPlayerFields field = null;
			SavedPlayerFieldsExtra fieldExtra = null;
			User user;
			if (ps.isServer()) {
				user =  HadalGame.server.getUsers().get(connId);

			} else {
				user =  HadalGame.client.getUsers().get(connId);
			}
			if (user != null) {
				field = user.getScores();
				fieldExtra = user.getScoresExtra();
			}

			if (field != null && fieldExtra != null) {

				infoPlayerName.setText(field.getNameAbridged(maxNameLen));

				Text damageDealtField = new Text("DAMAGE DEALT: ", 0, 0, false);
				damageDealtField.setScale(infoTextScale);

				Text damageAllyField = new Text("FRIENDLY FIRE: ", 0, 0, false);
				damageAllyField.setScale(infoTextScale);

				Text damageSelfField = new Text("SELF-DAMAGE: ", 0, 0, false);
				damageSelfField.setScale(infoTextScale);

				Text damageReceivedField = new Text("DAMAGE RECEIVED: ", 0, 0, false);
				damageReceivedField.setScale(infoTextScale);

				Text damageDealt = new Text("" + (int) fieldExtra.getDamageDealt(), 0, 0, false);
				damageDealt.setScale(infoTextScale);

				Text damageAlly = new Text("" + (int) fieldExtra.getDamageDealtAllies(), 0, 0, false);
				damageAlly.setScale(infoTextScale);

				Text damageSelf = new Text("" + (int) fieldExtra.getDamageDealtSelf(), 0, 0, false);
				damageSelf.setScale(infoTextScale);

				Text damageReceived = new Text("" + (int) fieldExtra.getDamageReceived(), 0, 0, false);
				damageReceived.setScale(infoTextScale);

				tableInfo.add(damageDealtField).height(infoRowHeight).padBottom(infoPadY);
				tableInfo.add(damageDealt).height(infoRowHeight).padBottom(infoPadY).row();

				tableInfo.add(damageAllyField).height(infoRowHeight).padBottom(infoPadY);
				tableInfo.add(damageAlly).height(infoRowHeight).padBottom(infoPadY).row();

				tableInfo.add(damageSelfField).height(infoRowHeight).padBottom(infoPadY);
				tableInfo.add(damageSelf).height(infoRowHeight).padBottom(infoPadY).row();

				tableInfo.add(damageReceivedField).height(infoRowHeight).padBottom(infoPadY);
				tableInfo.add(damageReceived).height(infoRowHeight).padBottom(infoPadY).row();

				//display player's loadout (if synced properly)
				if (fieldExtra.getLoadout() != null) {

					for (UnlockArtifact c: fieldExtra.getLoadout().artifacts) {
						if (!c.equals(UnlockArtifact.NOTHING)) {
							ArtifactIcon newTag = new ArtifactIcon(c, c.getInfo().getName() + "\n" + c.getInfo().getDescription(), artifactTagOffsetX, artifactTagOffsetY, artifactTagTargetWidth);
							tableArtifact.add(newTag).width(artifactTagSize).height(artifactTagSize);
						}
					}

					for (int i = 0; i < Loadout.maxWeaponSlots; i++) {
						if (!fieldExtra.getLoadout().multitools[i].equals(UnlockEquip.NOTHING)) {
							Text weaponField = new Text("WEAPON" + (i + 1) + ": ", 0, 0, false);
							weaponField.setScale(infoTextScale);
							Text weapon = new Text(fieldExtra.getLoadout().multitools[i].name(), 0, 0, false);
							weapon.setScale(infoTextScale);
							tableInfo.add(weaponField).height(infoRowHeight).left().padBottom(infoPadYSmall);
							tableInfo.add(weapon).height(infoRowHeight).left().padBottom(infoPadYSmall).row();
						}
					}
					Text activeField = new Text("ACTIVE: ", 0, 0, false);
					activeField.setScale(infoTextScale);
					Text active = new Text(fieldExtra.getLoadout().activeItem.name(), 0, 0, false);
					active.setScale(infoTextScale);
					tableInfo.add(activeField).height(infoRowHeight).left().padBottom(infoPadYSmall);
					tableInfo.add(active).height(infoRowHeight).left().padBottom(infoPadYSmall).row();
				}
			} else {
				infoPlayerName.setText("");
			}
		}), Actions.moveTo(infoXEnabled, infoYEnabled, transitionDuration, intp)));
	}
	
	/**
	 * This is pressed whenever a player gets ready.
	 * @param playerId: If this is run by the server, this is the player's connID (or 0, if the host themselves).
	 * For the client, playerId is the index in scores of the player that readies.
	 */
	public void readyPlayer(int playerId) {
		if (ps.isServer()) {
			
			//The server finds the player that readies, sets their readiness and informs all clients by sending that player's index
			User user = HadalGame.server.getUsers().get(playerId);
			if (user != null && !user.isSpectator()) {
				SavedPlayerFields field = user.getScores();
				ready.put(field, true);
				int iconId = scores.indexOf(field);
				icons.get(iconId).setReady(true);

				HadalGame.server.sendToAllTCP(new Packets.ClientReady(iconId));
			}
		} else {
			
			//Clients just find the player based on that index and sets them as ready.
			ready.put(scores.get(playerId), true);
			icons.get(playerId).setReady(true);
		}
		
		//When all players are ready, reddy will be true and we return to the hub
		boolean reddy = true;
		for (boolean b: ready.values()) {
			if (!b) {
				reddy = false;
				break;
			}
		}

		//When the server is ready, we return to hub and tell all clients to do the same.
		if (reddy) {
			returnToHub();
		}
	}

	/**
	 * This returns us to the hub when everyone readies up
	 */
	public void returnToHub() {
		if (ps.isServer()) {
			gsm.getApp().setRunAfterTransition(() -> {
				gsm.removeState(ResultsState.class, false);
				gsm.gotoHubState(LobbyState.class);
				gsm.gotoHubState(TitleState.class);
			});
		}
		gsm.getApp().fadeOut();
	}

	//we update the message window to take input
	private static final float particleCooldown = 1.5f;
	private float particleCounter;
	@Override
	public void update(float delta) {
		ps.getMessageWindow().table.act(delta);

		//this lets us continue to process packets. (mostly used for disconnects)
		ps.processCommonStateProperties(delta, true);

		particleCounter += delta;

		if (particleCounter >= particleCooldown) {
			particleCounter = 0.0f;
			for (PooledEffect effect: effects) {
				effect.start();
			}
		}
		for (PooledEffect effect: effects) {
			effect.update(delta);
		}
	}

	@Override
	public void render(float delta) {}

	@Override
	public void dispose() {
	    stage.dispose();

	    for (PlayerResultsIcon icon: icons) {
	        icon.dispose();
        }

		for (PooledEffect effect: effects) {
			effect.free();
		}
	}
	
	public PlayState getPs() { return ps; }
}
