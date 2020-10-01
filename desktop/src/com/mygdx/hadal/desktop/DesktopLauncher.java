package com.mygdx.hadal.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.hadal.HadalGame;

public class DesktopLauncher {
	
	private static final String TITLE = "Hadal Calm";
	
	public static void main (String[] arg) {
		final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.addIcon("128.png", FileType.Internal);
		config.addIcon("32.png", FileType.Internal);
		config.addIcon("16.png", FileType.Internal);
		config.title = TITLE;
		config.resizable = false;
		config.pauseWhenMinimized = false;
		config.audioDeviceSimultaneousSources = 192;
		new LwjglApplication(new HadalGame() {
			
			@Override
			public void setFrameRate(int framerate) {
				
				//This exposes config to the app to change fps during runtime.
				config.foregroundFPS = framerate;
			}
			
		}, config);
	}
}
