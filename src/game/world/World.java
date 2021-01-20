package game.world;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import game.Handler;
import game.entity.Player;
import game.raycasting.object.RayMirror;
import game.raycasting.object.RayObject;
import game.raycasting.object.RayWall;

public class World {

	private Handler handler;

	private ArrayList<RayObject>rayObjects;
	
	private Player player;
	
	public World(Handler handler) {
		this.handler = handler;
		handler.setWorld(this);
		rayObjects = new ArrayList<RayObject>();
		player = new Player(5, 1, handler);

		//rayObjects.add(new RayWall(handler, 1, 1, 5, 5, new Color(255, 0, 0)));
		rayObjects.add(new RayMirror(handler, 1, 1, 5, 5));
		
	}

	public void update() {
		for(RayObject obj:rayObjects) {
			obj.update();
		}
		player.update();
	}

	public void render(Graphics g) {
		for(RayObject obj:rayObjects) {
			obj.render(g);
		}
		player.render(g);
	}

	public ArrayList<RayObject> getRayObjects() {
		return rayObjects;
	}
	
}
