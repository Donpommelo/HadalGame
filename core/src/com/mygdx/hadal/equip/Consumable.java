package com.mygdx.hadal.equip;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.hadal.schmucks.bodies.Schmuck;
import com.mygdx.hadal.schmucks.userdata.BodyData;
import com.mygdx.hadal.schmucks.userdata.PlayerBodyData;
import com.mygdx.hadal.states.PlayState;

public class Consumable extends Equipable {

	private int chargesMax;

	private int chargesLeft;
	
	protected Vector2 velo;
	protected int x, y;
	protected short faction;
	
	public Consumable(Schmuck user, String name, float useCd, float useDelay, int charges) {
		super(user, name, useCd, useDelay);
		this.chargesMax = charges;
		this.chargesLeft = chargesMax;
	}

	@Override
	public void mouseClicked(float delta, PlayState state, BodyData bodyData, short faction, int x, int y) {
		//Convert body coordinates into screen coordinates to calc a starting velocity for the projectile.
		Vector3 bodyScreenPosition = new Vector3(
				bodyData.getSchmuck().getPosition().x,
				bodyData.getSchmuck().getPosition().y, 0);
		state.camera.project(bodyScreenPosition);
		
		float powerDiv = bodyScreenPosition.dst(x, y, 0);
		
		float xImpulse = -(bodyScreenPosition.x - x) / powerDiv;
		float yImpulse = -(bodyScreenPosition.y - y) / powerDiv;
		this.velo = new Vector2(xImpulse, yImpulse);
		
		//Also store the recoil vector and filter.
		this.faction = faction;
		this.x = x;
		this.y = y;
		
	}

	@Override
	public void execute(PlayState state, BodyData bodyData) {
		chargesLeft--;
		
		if (chargesLeft <= 0) {
			if (bodyData instanceof PlayerBodyData) {
				((PlayerBodyData)bodyData).emptySlot(((PlayerBodyData)bodyData).getCurrentSlot());
				((PlayerBodyData)bodyData).switchDown();
			}
		}
		
	}

	@Override
	public void release(PlayState state, BodyData bodyData) {}

	@Override
	public void reload(float delta) {}

	@Override
	public String getText() {
		return chargesLeft + "";
	}
	
	public void gainClip(int gained) {
		chargesLeft += gained;
		if (chargesLeft > chargesMax) {
			chargesLeft = chargesMax;
		}
	}

}
