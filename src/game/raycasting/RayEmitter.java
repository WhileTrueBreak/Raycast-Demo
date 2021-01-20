package game.raycasting;

import java.awt.Graphics;
import java.util.ArrayList;

import game.Handler;
import game.raycasting.object.RayObject;

public class RayEmitter {

	private ArrayList<Ray>rays;
	private ArrayList<RayObject>rayObjects;
	
	private Handler handler;
	private float x, y;
	
	public RayEmitter(Handler handler, float x, float y) {
		rayObjects = new ArrayList<RayObject>();
		this.handler = handler;
		this.x = x;
		this.y = y;
	}
	
	public void updateRays() {
		rays = new ArrayList<Ray>();
		for(int i = 0;i < 360;i++) {
			Ray ray = new Ray(handler, x, y, (float) (i*Math.PI/180), 100);
			ray.setRayObjects(rayObjects);
			ray.update();
			rays.add(ray);
		}
	}
	
	public void render(Graphics g) {
		for(Ray r:rays) {
			r.render(g);
		}
	}
	
	public void setRayObjects(ArrayList<RayObject> rayObjects) {
		this.rayObjects = rayObjects;
	}
	
}
