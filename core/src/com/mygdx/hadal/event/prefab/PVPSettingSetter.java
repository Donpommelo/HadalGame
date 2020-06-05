package com.mygdx.hadal.event.prefab;

import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.mygdx.hadal.save.Setting;
import com.mygdx.hadal.states.PlayState;
import com.mygdx.hadal.utils.TiledObjectUtil;


/**
 * The PVP Setting Setter reads the player's pvp settings to set up the rules of the pvp match. 
 * This includes rules about timers, number of lives and sets according ui elements.
 * One of these should be placed in every pvp map.
 * @author Zachary Tu
 *
 */
public class PVPSettingSetter extends Prefabrication {

	private final static String eggplantTimerId = "EGGPLANT_SPAWNER";
	private final static String weaponTimerId = "spawnWeapons";
	private final static String playerStartId = "playerstart";
	private final static float eggplantSpawnTimer = 2.0f;
	private final static float weaponSpawnTimer = 25.0f;
	private final static float pvpMatchZoom = 1.2f;
	
	public PVPSettingSetter(PlayState state) {
		super(state);
	}
	
	@Override
	public void generateParts() {
		String timerId = TiledObjectUtil.getPrefabTriggerId();
		String multiId = TiledObjectUtil.getPrefabTriggerId();
		String uiTimerId = TiledObjectUtil.getPrefabTriggerId();
		String uiLivesId = TiledObjectUtil.getPrefabTriggerId();
		String gameTimerId = TiledObjectUtil.getPrefabTriggerId();
		String gameLivesId = TiledObjectUtil.getPrefabTriggerId();
		String gameCameraId = TiledObjectUtil.getPrefabTriggerId();
		String gameObjectiveId = TiledObjectUtil.getPrefabTriggerId();
		
		RectangleMapObject playerstart = new RectangleMapObject();
		playerstart.setName("Multitrigger");
		playerstart.getProperties().put("triggeredId", playerStartId);
		playerstart.getProperties().put("triggeringId", "bounds1,bounds2," + gameCameraId + "," + gameObjectiveId);
		
		RectangleMapObject camera1 = new RectangleMapObject();
		camera1.setName("Camera");
		camera1.getProperties().put("zoom", pvpMatchZoom);
		camera1.getProperties().put("triggeredId", gameCameraId);
		
		RectangleMapObject timer = new RectangleMapObject();
		timer.setName("Timer");
		timer.getProperties().put("interval", 0.0f);
		timer.getProperties().put("triggeredId", timerId);
		timer.getProperties().put("triggeringId", multiId);
		
		RectangleMapObject multi = new RectangleMapObject();
		multi.setName("Multitrigger");
		multi.getProperties().put("triggeredId", multiId);
		multi.getProperties().put("triggeringId", timerId + "," + uiTimerId + "," + uiLivesId + "," + gameTimerId + "," + gameLivesId + "," + weaponTimerId);
		
		RectangleMapObject weaponTimer = new RectangleMapObject();
		weaponTimer.setName("Timer");
		weaponTimer.getProperties().put("interval", weaponSpawnTimer);
		weaponTimer.getProperties().put("triggeringId", weaponTimerId);
		
		int startTimer = state.getGsm().getSetting().getTimer();
		int startLives = state.getGsm().getSetting().getLives();
		int gameMode = state.getGsm().getSetting().getPVPMode();
		
		RectangleMapObject game = new RectangleMapObject();
		game.setName("Game");
		game.getProperties().put("sync", "ALL");
		game.getProperties().put("triggeredId", gameTimerId);

		if (startTimer != 0) {
			RectangleMapObject ui = new RectangleMapObject();
			ui.setName("UI");
			ui.getProperties().put("tags", "Fight!,EMPTY,LEVEL,SCORE,TIMER");
			ui.getProperties().put("triggeredId", uiTimerId);
			
			game.getProperties().put("timer", Setting.indexToTimer(startTimer));
			game.getProperties().put("timerIncr", -1.0f);
			
			TiledObjectUtil.parseTiledEvent(state, ui);
		} else {
			RectangleMapObject ui = new RectangleMapObject();
			ui.setName("UI");
			ui.getProperties().put("tags", "Fight!,EMPTY,LEVEL,SCORE");
			ui.getProperties().put("triggeredId", uiTimerId);
			
			TiledObjectUtil.parseTiledEvent(state, ui);
		}
		
		if (startLives != 0) {
			RectangleMapObject ui = new RectangleMapObject();
			ui.setName("UI");
			ui.getProperties().put("tags", "LIVES");
			ui.getProperties().put("triggeredId", uiLivesId);
			
			game.getProperties().put("lives", startLives - 1);
			
			TiledObjectUtil.parseTiledEvent(state, ui);
		} else {
			state.setUnlimitedLife(true);
		}
		
		if (gameMode == 1) {
			RectangleMapObject eggplantTimer = new RectangleMapObject();
			eggplantTimer.setName("Timer");
			eggplantTimer.getProperties().put("interval", eggplantSpawnTimer);
			eggplantTimer.getProperties().put("triggeringId", eggplantTimerId);
			
			RectangleMapObject eggplantMarker = new RectangleMapObject();
			eggplantMarker.setName("Objective");
			eggplantMarker.getProperties().put("display", true);
			eggplantMarker.getProperties().put("triggeredId", gameObjectiveId);
			eggplantMarker.getProperties().put("triggeringId", eggplantTimerId);
			
			TiledObjectUtil.parseTiledEvent(state, eggplantTimer);
			TiledObjectUtil.parseTiledEvent(state, eggplantMarker);
		}
		
		TiledObjectUtil.parseTiledEvent(state, game);
		
		RectangleMapObject end = new RectangleMapObject();
		end.setName("End");
		end.getProperties().put("text", "Match Over");
		end.getProperties().put("triggeredId", "runOnGlobalTimerConclude");
		
		TiledObjectUtil.parseTiledEvent(state, playerstart);
		TiledObjectUtil.parseTiledEvent(state, camera1);
		TiledObjectUtil.parseTiledEvent(state, timer);
		TiledObjectUtil.parseTiledEvent(state, multi);
		TiledObjectUtil.parseTiledEvent(state, weaponTimer);
		TiledObjectUtil.parseTiledEvent(state, end);
	}
}
