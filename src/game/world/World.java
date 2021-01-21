package game.world;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import game.Handler;
import game.entity.Player;
import game.raycasting.object.RayMirror;
import game.raycasting.object.RayObject;
import game.raycasting.object.RayPass;
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

		rayObjects.add(new RayWall(handler, -1, 0, -1, 4, new Color(255, 0, 0)));
		rayObjects.add(new RayWall(handler, -1, 0, 6, 0, new Color(255, 0, 0)));
		rayObjects.add(new RayWall(handler, 6, 0, 6, 4, new Color(255, 0, 0)));
		rayObjects.add(new RayWall(handler, 6, 4, -1, 4, new Color(255, 0, 0)));
		
		rayObjects.add(new RayPass(handler, 0, 1.5f, 2, 2.5f, new Color(0, 0, 255)));
		rayObjects.add(new RayPass(handler, 3, 2.5f, 5, 1.5f, new Color(0, 0, 255)));
		
		rayObjects.add(new RayMirror(handler, 0, 3, 5, 3));
		rayObjects.add(new RayMirror(handler, 0, 1, 5, 1));
		
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
