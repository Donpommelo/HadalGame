package com.mygdx.hadal.event.hub;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.actors.Text;
import com.mygdx.hadal.save.UnlockEquip;
import com.mygdx.hadal.states.PlayState;

import box2dLight.RayHandler;

public class Quartermaster extends HubEvent {

	private static final String name = "Quartermaster";
	private static final String title = "SPEND SCRIP";

	public Quartermaster(PlayState state, World world, OrthographicCamera camera, RayHandler rays, int width, int height,
			int x, int y) {
		super(state, world, camera, rays, name, width, height, x, y, title);
	
	}
	
	public void enter() {

		super.enter();
		
		for (UnlockEquip c: UnlockEquip.values()) {
			
			if (!c.isUnlocked()) {
				final UnlockEquip selected = c;
				Text itemChoose = new Text(HadalGame.assetManager, selected.getName() + " Cost: " + selected.getCost(), 0, 0);
				
				itemChoose.addListener(new ClickListener() {
			        public void clicked(InputEvent e, float x, float y) {
			        	if (state.getGsm().getRecord().getScrip() >= selected.getCost()) {
				        	state.getGsm().getRecord().incrementScrip( -selected.getCost());
				        	selected.setUnlocked(true);
				        	leave();
			        	}
			        }
			    });
				itemChoose.setScale(0.50f);
				tableInner.add(itemChoose).width(optionsWidth).height(optionsHeight);
				tableInner.row();
			}
		}
		tableInner.add(new Text(HadalGame.assetManager, "", 0, 0)).width(optionsWidth).height(optionsHeight);
		tableInner.row();
	}
}
