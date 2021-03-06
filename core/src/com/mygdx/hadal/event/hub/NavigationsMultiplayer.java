package com.mygdx.hadal.event.hub;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.actors.DialogBox;
import com.mygdx.hadal.actors.ModeSettingSelection;
import com.mygdx.hadal.actors.Text;
import com.mygdx.hadal.actors.UIHub;
import com.mygdx.hadal.actors.UIHub.hubTypes;
import com.mygdx.hadal.effects.Particle;
import com.mygdx.hadal.map.GameMode;
import com.mygdx.hadal.save.UnlockLevel;
import com.mygdx.hadal.save.UnlockManager.UnlockTag;
import com.mygdx.hadal.schmucks.bodies.ParticleEntity;
import com.mygdx.hadal.server.Packets;
import com.mygdx.hadal.states.PlayState;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The NavigationsMultiplayer is a HubEvent that allows the player to select a mode and then a corresponding map
 * @author Plonnigan Pludardus
 */
public class NavigationsMultiplayer extends HubEvent {

	//this is the selected game mode
	private static GameMode modeChosen = GameMode.DEATHMATCH;

	private static final String modesTitle = "GAME MODES";

	public NavigationsMultiplayer(PlayState state, Vector2 startPos, Vector2 size, String title, String tag, boolean closeOnLeave) {
		super(state, startPos, size, title, tag, false, closeOnLeave, hubTypes.NAVIGATIONS);
	}

	@Override
	public void addOptions(String search, int slots, UnlockTag tag) {
		super.addOptions(search, slots, tag);

		ArrayList<UnlockTag> newTags = new ArrayList<>(tags);
		if (tag != null) {
			newTags.add(tag);
		}

		Pattern pattern = Pattern.compile(search);
		final UIHub hub = state.getUiHub();
		hub.setTitle(modeChosen.toString());
		final NavigationsMultiplayer me = this;

		for (UnlockLevel c: UnlockLevel.getUnlocks(state, checkUnlock, newTags)) {
			final UnlockLevel selected = c;

			boolean appear = false;
			if (search.equals("")) {
				appear = true;
			} else {
				Matcher matcher = pattern.matcher(selected.getInfo().getName().toLowerCase());
				if (matcher.find()) {
					appear = true;
				}
			}

			//iterate through the modes that this map can be played with to see if we add it to the options
			boolean modeCompliant = false;
			for (int i = 0; i < selected.getModes().length; i++) {
				if (selected.getModes()[i] == modeChosen.getCheckCompliance()) {
					modeCompliant = true;
					break;
				}
			}

			if (appear && modeCompliant) {
				Text itemChoose = new Text(selected.getInfo().getName(), 0, 0, true);

				itemChoose.addListener(new ClickListener() {

					@Override
					public void clicked(InputEvent e, float x, float y) {

						if (state.isServer()) {
							state.loadLevel(selected, modeChosen, PlayState.TransitionState.NEWLEVEL, "");
							//play a particle when the player uses this event
							new ParticleEntity(state, me, Particle.TELEPORT, 0.0f, 3.0f, true, ParticleEntity.particleSyncType.CREATESYNC, new Vector2(0, - me.getSize().y / 2));
						} else {

							//clients suggest maps when clicking
							HadalGame.client.sendTCP(new Packets.ClientChat("Suggests Map: " + selected.getInfo().getName(), DialogBox.DialogType.SYSTEM));
						}
						leave();
					}

					@Override
					public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
						super.enter(event, x, y, pointer, fromActor);
						hub.setInfo(selected.getInfo().getName() + ": " + selected.getInfo().getDescription() + " \n \n" + selected.getInfo().getDescriptionLong());
					}
				});
				itemChoose.setScale(UIHub.optionsScale);
				hub.getTableOptions().add(itemChoose).height(UIHub.optionHeight).pad(UIHub.optionPad, 0, UIHub.optionPad, 0).row();
			}
		}
		hub.getTableOptions().add(new Text("", 0, 0, false)).height(UIHub.optionsHeight).row();
	}

	@Override
	public void enter() {
		super.enter();
		final UIHub hub = state.getUiHub();
		hub.setTitle(modesTitle);
		final NavigationsMultiplayer me = this;

		//bring up all game modes that can be selected from the hub
		for (GameMode c: GameMode.values()) {

			if (!c.isInvisibleInHub()) {
				final GameMode selected = c;
				Text itemChoose = new Text(c.toString(), 0, 0, true);

				itemChoose.addListener(new ClickListener() {

					@Override
					public void clicked(InputEvent e, float x, float y) {

						//when selected, we open up a ModeSettingSelection with the given mode's settings
						modeChosen = selected;

						state.getUiHub().setType(type);
						state.getUiHub().enter(tag, true, false, false, me);
						addOptions(lastSearch, -1, lastTag);

						ModeSettingSelection.addTable(state, modeChosen, me);
					}
				});
				itemChoose.setScale(UIHub.optionsScale);
				hub.getTableOptions().add(itemChoose).height(UIHub.optionHeight).pad(UIHub.optionPad, 0, UIHub.optionPad, 0).row();
			}
		}
		hub.getTableOptions().add(new Text("", 0, 0, false)).height(UIHub.optionsHeight).row();
	}
}
