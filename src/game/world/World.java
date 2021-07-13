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
		player = new Player(1, 5, handler);
		
		/////////////////////////////////MAP/////////////////////////////////

		rayObjects.add(new RayWall(handler, 0, 4, 4, 0, new Color(0, 0, 0)));
		rayObjects.add(new RayWall(handler, 2, 4, 4, 2, new Color(0, 0, 0)));
		rayObjects.add(new RayWall(handler, -1, 4, 0, 4, new Color(0, 0, 0)));
		rayObjects.add(new RayWall(handler, -1, 6, 0, 6, new Color(0, 0, 0)));
		
		RayPortal rp1 = new RayPortal(handler, 0, 6, 2, 6);
		rayObjects.add(rp1);
		rayObjects.add(new RayPortal(handler, 4, 0, 4, 2, rp1));
		
		RayPortal rp2 = new RayPortal(handler, -1, 4, -1, 6);
		rayObjects.add(rp2);
		rayObjects.add(new RayPortal(handler, 2, 4, 2, 6, rp2));
		
		/////////////////////////////////////////////////////////////////////
		
	}

	public void update() {
		for(RayObject obj:rayObjects) {
			obj.update();
		}
		player.update();
	}

	public void render(Graphics g) {
		
		for(RayObject obj:rayObjects) {
			//obj.render(g);
		}
		player.render(g);
		//render center
		g.setColor(new Color(0,0,0));
		g.drawOval(handler.getWidth()/2-2, handler.getHeight()/2-2, 4, 4);
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
