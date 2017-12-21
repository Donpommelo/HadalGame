package com.mygdx.hadal.equip;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.hadal.schmucks.bodies.HadalEntity;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.states.PlayState;

import box2dLight.RayHandler;

public abstract class Equipable {	
	
	public HadalEntity user;
	public String name;
	public float useCd;
	public float useDelay;

	public boolean reloading;
	public float reloadCd;
	
	
	public Equipable(HadalEntity user, String name, float swingcd, float shootDelay) {
		this.user = user;
		this.name = name;
		this.useCd = swingcd;
		this.useDelay = shootDelay;
		this.reloading = false;
		this.reloadCd = 0;
	}
	
	public abstract void mouseClicked(PlayState state, BodyData shooter, short faction, int x, int y, World world, OrthographicCamera camera, RayHandler rays);
	
	public abstract void execute(PlayState state, BodyData bodyData, World world, OrthographicCamera camera, RayHandler rays);
	
	public abstract void reload(float delta);
	
	public abstract String getText();
	
	public void release(PlayState state, BodyData bodyData, World world, OrthographicCamera camera, RayHandler rays) {
		
	}

	public boolean charging() {
		return false;
	}
}
