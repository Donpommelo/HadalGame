package com.mygdx.hadal.schmucks.bodies.enemies;

public class BossAction {

	protected Boss1 boss;
	private float duration;
	
	public BossAction(Boss1 boss, float duration) {
		this.boss = boss;
		this.duration = duration;
	}
	
	
	public void execute() {
		
	}

	public float getDuration() {
		return duration;
	}	
}
