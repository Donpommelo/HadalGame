package com.mygdx.hadal.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.hadal.HadalGame;
import com.mygdx.hadal.actors.MenuWindow;
import com.mygdx.hadal.actors.Text;
import com.mygdx.hadal.audio.SoundEffect;
import com.mygdx.hadal.effects.Shader;
import com.mygdx.hadal.managers.AssetList;
import com.mygdx.hadal.managers.GameStateManager;

/**
 * The AboutState is selected from the title screen and gives information about the game
 * @author Snujube Spambly
 */
public class AboutState extends GameState {

	//This table contains the ui elements of the pause screen
	private Table options, details;
	
	//options that the player can view
	private Text aboutOption, miscOption, tipsOption, creditsOption, exitOption;
	
	//Dimensions of the setting menu
	private static final int optionsX = 25;
	private static final int optionsY = 100;
	private static final int optionsWidth = 300;
	private static final int optionsHeight = 600;
	
	private static final int detailsX = 320;
	private static final int detailsY = 100;
	private static final int detailsWidth = 800;
	private static final int detailsHeight = 600;
	
	private static final float optionsScale = 0.5f;
	private static final float optionHeight = 35.0f;
	private static final float optionPad = 15.0f;
	private static final float detailsScale = 0.3f;
	
	private static final float titlePad = 25.0f;
	private static final int detailsTextWidth = 750;

	//this state's background shader
	private final Shader shaderBackground;
	private final TextureRegion bg;
	
	/**
	 * Constructor will be called when the player enters the about state from the title menu.
	 */
	public AboutState(final GameStateManager gsm) {
		super(gsm);
		
		shaderBackground = Shader.SPLASH;
		shaderBackground.loadDefaultShader();
		this.bg = new TextureRegion((Texture) HadalGame.assetManager.get(AssetList.BACKGROUND2.toString()));
	}
	
	@Override
	public void show() {
		
		stage = new Stage() {
			{
				addActor(new MenuWindow(optionsX, optionsY, optionsWidth, optionsHeight));
				addActor(new MenuWindow(detailsX, detailsY, detailsWidth, detailsHeight));
				
				options = new Table();
				options.setPosition(optionsX, optionsY);
				options.setSize(optionsWidth, optionsHeight);
				options.top();
				addActor(options);
				
				details = new Table();
				details.setPosition(detailsX, detailsY);
				details.setSize(detailsWidth, detailsHeight);
				details.top();
				addActor(details);
				
				aboutOption = new Text("ABOUT", 0, 0, true);
				aboutOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) {
						SoundEffect.UISWITCH1.play(gsm, 1.0f, false);
						aboutSelected();
			        }
			    });
				aboutOption.setScale(optionsScale);
				
				tipsOption = new Text("TIPS", 0, 0, true);
				tipsOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) {
						SoundEffect.UISWITCH1.play(gsm, 1.0f, false);
						tipsSelected();
			        }
			    });
				tipsOption.setScale(optionsScale);
				
				miscOption = new Text("MISC", 0, 0, true);
				miscOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) {
						SoundEffect.UISWITCH1.play(gsm, 1.0f, false);
						miscSelected();
			        }
			    });
				miscOption.setScale(optionsScale);
				
				creditsOption = new Text("CREDITS", 0, 0, true);
				creditsOption.addListener(new ClickListener() {
			        
					@Override
					public void clicked(InputEvent e, float x, float y) {
						SoundEffect.UISWITCH1.play(gsm, 1.0f, false);
						creditsSelected();
			        }
			    });
				creditsOption.setScale(optionsScale);
				
				exitOption = new Text("EXIT?", 0, 0, true);
				exitOption.addListener(new ClickListener() {
					
					@Override
					public void clicked(InputEvent e, float x, float y) {
						SoundEffect.NEGATIVE.play(gsm, 1.0f, false);
						gsm.getApp().fadeOut();
						gsm.getApp().setRunAfterTransition(() -> gsm.removeState(AboutState.class));
			        }
			    });
				exitOption.setScale(optionsScale);
				
				options.add(aboutOption).height(optionHeight).pad(optionPad).row();
				options.add(tipsOption).height(optionHeight).pad(optionPad).row();
				options.add(miscOption).height(optionHeight).pad(optionPad).row();
				options.add(creditsOption).height(optionHeight).pad(optionPad).row();
				options.add(exitOption).height(optionHeight).pad(optionPad).expand().row();
			}
		};
		app.newMenu(stage);
		gsm.getApp().fadeIn();
		
		//start off with about selected
		aboutSelected();
	}

	/**
	 * This is called whenever the player selects the ABOUT tab
	 */
	private void aboutSelected() {
		details.clear();
		
		details.add(new Text("ABOUT", 0, 0, false)).colspan(2).pad(titlePad).row();
		
		Text about = new Text(GameStateManager.miscText.getString("about"), 0, 0, false, true, detailsTextWidth);
		about.setScale(detailsScale);
		
		details.add(about);
	}
	
	/**
	 * This is called whenever the player selects the TIPS tab
	 */
	private void tipsSelected() {
		details.clear();
		
		details.add(new Text("TIPS", 0, 0, false)).colspan(2).pad(titlePad).row();
		
		Text tips = new Text(GameStateManager.miscText.getString("tips"), 0, 0, false, true, detailsTextWidth);
		tips.setScale(detailsScale);
		
		details.add(tips);
	}
	
	/**
	 * This is called whenever the player selects the MISC tab
	 */
	private void miscSelected() {
		details.clear();
		
		details.add(new Text("MISC", 0, 0, false)).colspan(2).pad(titlePad).row();
		
		Text misc = new Text(GameStateManager.miscText.getString("misc"), 0, 0, false, true, detailsTextWidth);
		misc.setScale(detailsScale);
		
		details.add(misc);
	}

	/**
	 * This is called whenever the player selects the CREDITS tab
	 */
	private void creditsSelected() {
		details.clear();
		
		details.add(new Text("CREDITS", 0, 0, false)).colspan(2).pad(titlePad).row();
		
		//dev and art options have url links
		Text dev = new Text("DONPOMMELO", 0, 0, true);
		dev.setScale(detailsScale);
		dev.setColor(Color.RED);
		
		dev.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent e, float x, float y) {
				SoundEffect.UISWITCH1.play(gsm, 1.0f, false);
				Gdx.net.openURI("https://donpommelo.itch.io/");
	        }
	    });
		
		Text art = new Text("SHOEBANFOO", 0, 0, true);
		art.setScale(detailsScale);
		art.setColor(Color.RED);
		
		art.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent e, float x, float y) {
				SoundEffect.UISWITCH1.play(gsm, 1.0f, false);
				Gdx.net.openURI("https://www.instagram.com/shoebanfoo/");
	        }
	    });
		
		Text sfx = new Text(GameStateManager.miscText.getString("credits"), 0, 0, false, true, detailsTextWidth);
		sfx.setScale(detailsScale);
		
		details.add(dev).height(optionHeight).row();
		details.add(art).height(optionHeight).row();
		details.add(sfx).height(optionHeight);
	}
	
	@Override
	public void update(float delta) {}

	private float timer;
	@Override
	public void render(float delta) {
		timer += delta;
		
		batch.begin();
		
		shaderBackground.getShaderProgram().bind();
		shaderBackground.shaderDefaultUpdate(timer);
		batch.setShader(shaderBackground.getShaderProgram());
		
		batch.draw(bg, 0, 0, HadalGame.CONFIG_WIDTH, HadalGame.CONFIG_HEIGHT);
		
		batch.setShader(null);
		
		batch.end();
	}
	
	@Override
	public void dispose() {	
		stage.dispose(); 
	}
}
