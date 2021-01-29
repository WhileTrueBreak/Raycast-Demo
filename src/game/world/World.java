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
	
	private float rotation = (float) (0*Math.PI/180);
	private float t = 0;
	
	public World(Handler handler) {
		this.handler = handler;
		handler.setWorld(this);
		rayObjects = new ArrayList<RayObject>();
		player = new Player(2, 2, handler);

//		RayPortal rp1 = new RayPortal(handler, 0, 0, 0, 4);
//		rayObjects.add(rp1);
//		rayObjects.add(new RayPortal(handler, 0, 0, 4, 0, rp1));

//		float s = 4;
		
		rayObjects.add(new RayWall(handler, 0, 0, 5, 0, new Color(0, 0, 0)));
		rayObjects.add(new RayWall(handler, 0, 0, 0, 5, new Color(0, 0, 0)));
		rayObjects.add(new RayWall(handler, 3, 3, 3, 10, new Color(0, 0, 0)));
		//rayObjects.add(new RayWall(handler, 3, 3, 5, 3, new Color(0, 0, 0)));
		

		rayObjects.add(new RayWall(handler, 5, 0, 5, -10, new Color(0, 0, 0)));
		rayObjects.add(new RayWall(handler, 7, 0, 7, -10, new Color(0, 0, 0)));
		
		rayObjects.add(new RayWall(handler, 7, 0, 8, 5, new Color(0, 0, 0)));
		rayObjects.add(new RayWall(handler, 5, 3, 5, 10, new Color(0, 0, 0)));
		
		RayPortal rp1 = new RayPortal(handler, 0, 5, 3, 5);
		rayObjects.add(rp1);
		rayObjects.add(new RayPortal(handler, 8, 5, 5, 5, rp1));
		
		RayPortal rp2 = new RayPortal(handler, 5, -10, 7, -10);
		rayObjects.add(rp2);
		rayObjects.add(new RayPortal(handler, 3, 10, 5, 10, rp2));
		
//		RayPortal rp1 = new RayPortal(handler, 0, s, 0, 0);
//		rayObjects.add(rp1);
//		rayObjects.add(new RayPortal(handler, s, 0, s, s, rp1));
//		RayPortal rp2 = new RayPortal(handler, 0, 0, s, 0);
//		rayObjects.add(rp2);
//		rayObjects.add(new RayPortal(handler, 0, s, s, s, rp1));
//		
//		rayObjects.add(new RayWall(handler, 0, 0, s, 0, new Color(0, 0, 0)));
//		rayObjects.add(new RayWall(handler, s, 0, s, s, new Color(255, 0, 0)));
		
//		rayObjects.add(new RayPass(handler, 1, 1, 3, 3, new Color(255, 0, 0)));
//		rayObjects.add(new RayPass(handler, 1, 3, 3, 1, new Color(255, 0, 0)));
		
	}

	public void update() {
		for(RayObject obj:rayObjects) {
			obj.update();
		}
		player.update();
//		rayObjects.get(4).setX1((float) Math.cos(t)*2		   +2);
//		rayObjects.get(4).setY1((float) Math.sin(t)*2		   +2);
//		rayObjects.get(4).setX2((float) Math.cos(Math.PI+t)*2  +2);
//		rayObjects.get(4).setY2((float) Math.sin(Math.PI+t)*2  +2);
//		rayObjects.get(5).setX1((float) Math.cos(t+Math.PI/2)  +2);
//		rayObjects.get(5).setY1((float) Math.sin(t+Math.PI/2)  +2);
//		rayObjects.get(5).setX2((float) Math.cos(Math.PI*3/2+t)+2);
//		rayObjects.get(5).setY2((float) Math.sin(Math.PI*3/2+t)+2);
//		t+=1/handler.getCurrentFps();
//		rotation+=0.1/handler.getCurrentFps();
	}

	public void render(Graphics g) {
//		for(RayObject obj:rayObjects) {
//			obj.render(g);
//		}
		player.render(g);
	}

	public ArrayList<RayObject> getRayObjects() {
		return rayObjects;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	
}
