package game;

import game.display.Camera;
import game.display.Display;
import game.inputs.KeyManager;
import game.inputs.MouseManager;
import game.world.World;

public class Handler {
	
	private Main main;
	private World world;
	
	private double currentFps;
	
	public Handler(Main main) {
		this.main = main;
	}

	public Main getMain() {
		return main;
	}

	public int getWidth() {
		return main.getWidth();
	}

	public int getHeight() {
		return main.getHeight();
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
	
	public KeyManager getKeyManager() {
		return main.getKeyManager();
	}
	
	public MouseManager getMouseManager() {
		return main.getMouseManager();
	}
	
	public Camera getCamera() {
		return main.getCamera();
	}
	
	//https://pastebin.com/CGukyGrd
	public String getDir() {
		return System.getProperty("user.dir");
	}
	
	public Display getDisplay() {
		return main.getDisplay();
	}

	public double getCurrentFps() {
		return currentFps;
	}

	public void setCurrentFps(double currentFps) {
		this.currentFps = currentFps;
	}
	
	public double getSpeedMult() {
		return 1/currentFps;
	}
	
}
