package game.world;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import game.Handler;
import game.entity.Player;
import game.raycasting.object.RayMirror;
import game.raycasting.object.RayObject;
import game.raycasting.object.RayPass;
import game.raycasting.object.RayPortal;
import game.raycasting.object.RayWall;

public class World {

	private Handler handler;

	private ArrayList<RayObject>rayObjects;
	
	private Player player;
	
	private float t = 0;
	
	public World(Handler handler) {
		this.handler = handler;
		handler.setWorld(this);
		rayObjects = new ArrayList<RayObject>();
		player = new Player(5, 1, handler);

//		RayPortal rp1 = new RayPortal(handler, 0, 0, 0, 4);
//		rayObjects.add(rp1);
//		rayObjects.add(new RayPortal(handler, 0, 0, 4, 0, rp1));

		float s = 4;
		
		RayPortal rp1 = new RayPortal(handler, 0, 0, 0, s);
		rayObjects.add(rp1);
		rayObjects.add(new RayPortal(handler, s, 0, s, s, rp1));
		RayPortal rp2 = new RayPortal(handler, 0, 0, s, 0);
		rayObjects.add(rp2);
		rayObjects.add(new RayPortal(handler, 0, s, s, s, rp2));

		rayObjects.add(new RayPass(handler, 1, 1, 3, 3, new Color(0, 0, 255)));
		rayObjects.add(new RayWall(handler, 1, 1, 3, 3, new Color(255, 0, 0)));
		
//		rayObjects.add(new RayWall(handler, 6, 4, -1, 4, new Color(255, 0, 0)));
//		rayObjects.add(new RayWall(handler, -1, 0, -1, 4, new Color(255, 0, 0)));
//		
//		RayPortal rp1 = new RayPortal(handler, 6, 0, 6, 4);
//		rayObjects.add(rp1);
//		rayObjects.add(new RayPortal(handler, 6, 0, -1, 0, rp1));
//		
//		rayObjects.add(new RayPass(handler, 0, 1.5f, 2, 2.5f, new Color(0, 0, 255)));
//		rayObjects.add(new RayPass(handler, 3, 1.5f, 5, 2.5f, new Color(0, 0, 255)));
//		
//		rayObjects.add(new RayMirror(handler, 0, 3, 5, 3));
//		rayObjects.add(new RayMirror(handler, 0, 1, 5, 1));
		
	}

	public void update() {
		for(RayObject obj:rayObjects) {
			obj.update();
		}
		player.update();
		rayObjects.get(4).setX1((float) Math.cos(t)+2);
		rayObjects.get(4).setY1((float) Math.sin(t)+2);
		rayObjects.get(4).setX2((float) Math.cos(Math.PI+t)+2);
		rayObjects.get(4).setY2((float) Math.sin(Math.PI+t)+2);
		rayObjects.get(5).setX1((float) Math.cos(t+Math.PI/2)+2);
		rayObjects.get(5).setY1((float) Math.sin(t+Math.PI/2)+2);
		rayObjects.get(5).setX2((float) Math.cos(Math.PI*3/2+t)+2);
		rayObjects.get(5).setY2((float) Math.sin(Math.PI*3/2+t)+2);
		t+=2/handler.getCurrentFps();
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
