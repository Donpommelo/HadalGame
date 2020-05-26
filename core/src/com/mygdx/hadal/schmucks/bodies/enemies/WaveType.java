package com.mygdx.hadal.schmucks.bodies.enemies;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Array;
import com.mygdx.hadal.event.SpawnerWave;
import com.mygdx.hadal.managers.GameStateManager;
import com.mygdx.hadal.utils.Constants;

public enum WaveType {

	WAVE1(WaveTag.STANDARD) {
		{
			this.enemies.add(new WaveEnemy(EnemyType.SPLITTER_LARGE, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SPLITTER_LARGE, 4, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SPLITTER_LARGE, 8, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SPLITTER_LARGE, 16, waveLimit, 1, 2, 3));
		}
	},
	
	WAVE2(WaveTag.STANDARD) {
		{
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 2, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 4, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 6, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 8, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 12, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 16, waveLimit, 1, 2, 3));

		}
	},
	
	WAVE3(WaveTag.STANDARD) {
		{
			this.enemies.add(new WaveEnemy(EnemyType.SPAWNER, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SPAWNER, 4, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SPAWNER, 8, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SPAWNER, 16, waveLimit, 1, 2, 3));
		}
	},
	
	WAVE4(WaveTag.STANDARD) {
		{
			this.enemies.add(new WaveEnemy(EnemyType.SWIMMER2, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SWIMMER2, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SWIMMER2, 4, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SWIMMER2, 8, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.SWIMMER2, 16, waveLimit, 1, 2, 3));
		}
	},
	
	WAVE5(WaveTag.STANDARD) {
		{
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 1, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.TURRET_VOLLEY, 1, 4, 4, 5));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 6, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 6, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 6, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.TURRET_VOLLEY, 8, waveLimit, 4));
			this.enemies.add(new WaveEnemy(EnemyType.TURRET_VOLLEY, 8, waveLimit, 5));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 9, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 9, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 12, waveLimit, 1, 2, 3));
			this.enemies.add(new WaveEnemy(EnemyType.MISCFISH, 16, waveLimit, 1, 2, 3));
		}
	}	
	;
	
	
	protected ArrayList<WaveEnemy> enemies = new ArrayList<WaveEnemy>();
	protected ArrayList<WaveTag> tags = new ArrayList<WaveTag>();
	
	private WaveType(WaveTag... tags) {
		for (WaveTag tag: tags) {
			this.tags.add(tag);
		}
	}
	
	public void spawnWave(SpawnerWave spawner, int waveNum, int extraField) {

		for (WaveEnemy enemy : enemies) {
			enemy.createEnemy(spawner, waveNum, extraField);
		}
	}
	
	private static int lastWave;
	private static WaveType currentWave = WaveType.WAVE1;
	private static final int waveLimit = 100;
	public static WaveType getWave(ArrayList<WaveTag> tags, int waveNum) {
		
		if (lastWave != waveNum) {
			lastWave = waveNum;
			Array<WaveType> waves = new Array<WaveType>();
			
			for (WaveType wave: WaveType.values()) {
				
				boolean get = false;
				
				for (WaveTag tag: tags) {
					if (wave.tags.contains(tag)) {
						get = true;
					}
				}
				if (get) {
					waves.add(wave);
				}
			}

			if (!waves.isEmpty()) {
				currentWave = waves.get(GameStateManager.generator.nextInt(waves.size));
			}
		}
		
		return currentWave;
	}
	
	public class WaveEnemy {
		
		private int lastWave;
		private int[] pointId;
		private int thisId;
		private int minWave, maxWave;
		private EnemyType type;
		
		public WaveEnemy(EnemyType type, int minWave, int maxWave, int... pointId) {
			this.minWave = minWave;
			this.maxWave = maxWave;
			this.pointId = pointId;
			this.type = type;
		}
		
		public void createEnemy(SpawnerWave spawner, int waveNum, float extraField) {
			
			if (waveNum < minWave || (waveNum > maxWave && maxWave != waveLimit)) {
				return;
			}
			
			if (lastWave != waveNum) {
				lastWave = waveNum;
				
				if (pointId.length == 0) {
					thisId = 1;
				} else {
					thisId = pointId[GameStateManager.generator.nextInt(pointId.length)];
				}
			}

			if (thisId == spawner.getPointId()) {
				type.generateEnemy(spawner.getState(), spawner.getPixelPosition(), Constants.ENEMY_HITBOX, extraField, null);
			}
		}
	}
	
	public enum WaveTag {
		STANDARD,
	}
}
