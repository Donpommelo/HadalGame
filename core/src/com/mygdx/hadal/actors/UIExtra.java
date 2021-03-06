package com.mygdx.hadal.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.actors.UITag.uiType;
import com.mygdx.hadal.equip.modeMods.GunGame;
import com.mygdx.hadal.schmucks.bodies.Player;
import com.mygdx.hadal.server.AlignmentFilter;
import com.mygdx.hadal.server.SavedPlayerFields;
import com.mygdx.hadal.server.User;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.statuses.DamageTypes;

import java.util.ArrayList;

/**
 * The UIExtra is an extra ui actor displayed in the upper left hand side.
 * It displays list of strings decided by the uiTags list which can be modified in level with events.
 * @author Yacardo Yarabba
 */
public class UIExtra extends AHadalActor {

	private final PlayState state;
	private final BitmapFont font;
	
	private static final int x = 10;
	private static final int width = 200;
	private static final int y = 10;
	private static final float scale = 0.25f;
	
	//List of tags that are to be displayed
	private final ArrayList<UITag> uiTags;
	
	//These variables are all fields that are displayed in the default tags for the ui
	private int scrap, score, hiscore, lives, wins;
	
	//Timer is used for timed scripted events. timerIncr is how much the timer should tick every update cycle (usually -1, 0 or 1)
	private float maxTimer, timer, timerIncr;

	//this is the displayed time
	private int currentTimer;
	private String displayedTimer;

	public UIExtra(PlayState state) {
		this.state = state;
		this.font = HadalGame.SYSTEM_FONT_UI;
		
		uiTags = new ArrayList<>();
	}
	
	private final StringBuilder text = new StringBuilder();
	@Override
    public void draw(Batch batch, float alpha) {

		if (state.getGsm().getSetting().isHideHUD()) { return; }

		font.getData().setScale(scale);
		font.draw(batch, text.toString(), x, HadalGame.CONFIG_HEIGHT - y, width, Align.left, true);
	}

	/**
	 * This is run whenever the contents of the ui change. It sets the text according to updated tags and info
	 */
	private void syncUIText() {
		text.setLength(0);

		for (UITag uiTag : uiTags) {
			switch (uiTag.getType()) {
				case SCRAP:
					text.append("SCRAP: ").append(scrap).append("\n");
					break;
				case LIVES:
					text.append("LIVES: ").append(lives).append("\n");
					break;
				case SCORE:
					text.append("SCORE: ").append(score).append("\n");
					break;
				case HISCORE:
					text.append("HISCORE: ").append(hiscore).append("\n");
					break;
				case WINS:
					text.append("WINS: ").append(wins).append("\n");
					break;
				case TIMER:
					text.append("TIMER: ").append(displayedTimer).append("\n");
					break;
				case MISC:
					text.append(uiTag.getMisc()).append("\n");
					break;
				case LEVEL:
					text.append(state.getMode().getText()).append(" ").append(state.getLevel().getInfo().getName()).append("\n");
					break;
				case TEAMSCORE:
					if (teamText.isEmpty()) {
						sortTeamScores();
					}
					text.append(teamText.toString());
					break;
				case GUNGAME:
					text.append("SCORE: ").append(score).append("/").append(GunGame.weaponOrder.length).append("\n")
					.append("NEXT WEAPON: ");

					//display next weapon in gun-game queue, unless we are on the last weapon
					if (score + 1 < GunGame.weaponOrder.length) {
						text.append(GunGame.weaponOrder[score + 1].toString()).append("\n");
					} else {
						text.append("VICTORY\n");
					}
					break;
				case SCOREBOARD:
					sortIndividualScores(text);
					break;
				case EMPTY:
					text.append("\n");
					break;
				default:
					break;
			}
		}
	}

	/**
	 * This adds tags to the ui
	 * @param tags: These are the tags that should be added. This is a comma-separated string of each uiType.
	 * @param clear: Do we clear all the existing ui tags first?
	 */
	public void changeTypes(String tags, boolean clear) {
		if (clear) {
			uiTags.clear();
		}
		
		for (String type : tags.split(",")) {
			
			boolean found = false;
			
			for (UITag.uiType tag: UITag.uiType.values()) {
				if (tag.toString().equals(type)) {
					found = true;
					uiTags.add(new UITag(tag));
				}
			}

			//If a string matches no tag types, we just display the text as is.
			if (!found) {
				uiTags.add(new UITag(uiType.MISC, type));
			}
		}
		syncUIText();
	}
	
	private final StringBuilder tags = new StringBuilder();
	/**
	 * This returns a comma-separated string of the current tags in order.
	 * When a new client connects, the server uses this to send them the current ui status to sync with.
	 */
	public String getCurrentTags() {
		tags.setLength(0);
		
		for (UITag tag : uiTags) {
			if (tag.getType().equals(uiType.MISC)) {
				tags.append(tag.getMisc()).append(",");
			} else {
				tags.append(tag.getType().toString()).append(",");
			}
		}
		return tags.toString();
	}

	/**
	 * This syncs the info that this ui displays.
	 * The ui contains values for each displayed field and must update them when these fields change.
	 * This occurs when any player field is changed by changeFields() or when the score window is synced.
	 * Also when certain record values are changed (atm this is just for currency changing) 
	 */
	public void syncData() {
		scrap = state.getGsm().getRecord().getScrap();
		
		if (state.getGsm().getRecord().getHiScores().containsKey(state.getLevel().toString())) {
			hiscore = state.getGsm().getRecord().getHiScores().get(state.getLevel().toString());
		}
		
		SavedPlayerFields field = null;
		User user;
		if (state.isServer()) {
			user = HadalGame.server.getUsers().get(0);
		} else {
			user = HadalGame.client.getUsers().get(HadalGame.client.connID);
		}
		if (user != null) {
			field = user.getScores();
		}
		if (field != null) {
			score = field.getScore();
			wins = field.getWins();
			lives = field.getLives();
		}

		syncUIText();
	}
	
	/**
	 * This is run when any info displayed by this ui is changed. atm, this is just run when a playerChanger event activates for a specific player
	 * @param p: The player whose field has changed. If null, this change applies to all players.
	 * @param score, lives, timerSet, timerIncrement: Amount to change. default 0.
	 */
	public void changeFields(Player p, int score, int lives, float timerSet, float timerIncrement, boolean changeTimer) {
		
		if (!state.isServer()) { return; }
		
		SavedPlayerFields field;
		
		if (p == null) {
			for (User user: HadalGame.server.getUsers().values()) {
				SavedPlayerFields eachField = user.getScores();
				eachField.setScore(eachField.getScore() + score);
				eachField.setLives(eachField.getLives() + lives);

				//tell score window to update next interval
				user.setScoreUpdated(true);

				//If all players are losing lives at once and they have 0 lives, they get a game over.
				if (eachField.getLives() <= 0) {
					state.levelEnd("GAME OVER", false);
					break;
				}
			}
		} else {
			User user = HadalGame.server.getUsers().get(p.getConnID());
			if (user != null) {
				field = user.getScores();
				field.setScore(field.getScore() + score);
				field.setLives(field.getLives() + lives);

				//If a single player runs out of lives, they die
				if (field.getLives() <= 0 && lives < 0) {
					p.getPlayerData().die(state.getWorldDummy().getBodyData(), DamageTypes.LIVES_OUT);
				}

				//if the player has reached the score goal, end the game
				if (state.getScoreCap() > 0) {
					if (field.getScore() >= state.getScoreCap()) {
						if (state.getGlobalTimer() != null) {
							state.getGlobalTimer().getEventData().preActivate(null, null);
						}
					}
				}

				//tell score window to update next interval
				user.setScoreUpdated(true);
			}
		}
		
		if (changeTimer) {
			maxTimer = timerSet;
			timer = timerSet;
			timerIncr = timerIncrement;
		}

		syncUIText();
	}

	/**
	 * Change the team score field in the ui
	 * @param teamIndex: the index of the team we are changing
	 * @param scoreChange: how much to change their score by
	 */
	public void changeTeamField(int teamIndex, int scoreChange) {
		if (teamIndex < AlignmentFilter.teamScores.length && teamIndex >= 0) {
			int newScore = AlignmentFilter.teamScores[teamIndex] + scoreChange;
			AlignmentFilter.teamScores[teamIndex] =  newScore;

			//if the team has reached the score goal, end the game
			if (state.getTeamScoreCap() > 0) {
				if (newScore >= state.getTeamScoreCap()) {
					if (state.getGlobalTimer() != null) {
						state.getGlobalTimer().getEventData().preActivate(null, null);
					}
				}
			}
			sortTeamScores();
			syncUIText();
		}
	}

	//display a time warning when the time is low
	private final static float notificationThreshold = 10.0f;
	/**
	 * This increments the timer for timed levels. When time runs out, we want to run an event designated in the map (if it exists)
	 * @param delta: amount of time that has passed since last update
	 */
	public void incrementTimer(float delta) {

		if (timer > notificationThreshold && timer + (timerIncr * delta) < notificationThreshold) {
			state.getKillFeed().addNotification("10 SECONDS REMAINING", false);
		}

		timer += (timerIncr * delta);

		if ((int) timer != currentTimer) {
			currentTimer = (int) timer;

			//convert the time to minutes:seconds
			int seconds = currentTimer % 60;
			if (seconds < 10) {
				displayedTimer = currentTimer / 60 + ": 0" + seconds;

			} else {
				displayedTimer = currentTimer / 60 + ": " + seconds;
			}
			syncUIText();
		}

		//upon timer running out, a designated event activates
		if (timer <= 0 && timerIncr < 0) {
			if (state.getGlobalTimer() != null) {
				state.getGlobalTimer().getEventData().preActivate(null, null);
				timerIncr = 0;
			}
		}
	}

	private static final int maxNameLen = 25;
	private static final int maxScores = 5;

	/**
	 * For modes with a scoreboard ui tag, we add a sorted list of player scores.
	 * @param text: the stringbuilder we will be appending the scoreboard text to
	 */
	private void sortIndividualScores(StringBuilder text) {
		if (state.getScoreWindow() != null) {
			int scoreNum = 0;
			for (User user: state.getScoreWindow().getOrderedUsers()) {
				text.append(user.getNameAbridgedColored(maxNameLen)).append(": ").append(user.getScores().getScore()).append("\n");
				scoreNum++;
				if (scoreNum > maxScores) {
					break;
				}
			}
		}
	}

	private static final Vector3 rgb = new Vector3();
	private final StringBuilder teamText = new StringBuilder();
	private void sortTeamScores() {
		teamText.setLength(0);
		for (int i = 0; i < AlignmentFilter.teamScores.length; i++) {
			rgb.set(AlignmentFilter.currentTeams[i].getColor1RGB());
			String hex = "#" + Integer.toHexString(Color.rgb888(rgb.x, rgb.y, rgb.z));
			teamText.append("[").append(hex).append("]").append(AlignmentFilter.currentTeams[i].toString())
				.append("[]").append(": ").append(AlignmentFilter.teamScores[i]).append("\n");
		}
	}

	public float getTimer() { return timer; }

	public float getMaxTimer() { return maxTimer; }

	public void setTimer(float timer) { this.timer = timer; }

	public float getTimerIncr() { return timerIncr; }

	public void setTimerIncr(float timerIncr) { this.timerIncr = timerIncr; }
}
