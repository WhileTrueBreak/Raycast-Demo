package game.raycasting;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import game.Handler;
import game.raycasting.object.RayMirror;
import game.raycasting.object.RayObject;
import game.raycasting.object.RayPass;
import game.raycasting.object.RayWall;

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
//		Ray ray = new Ray(handler, x, y, (float) (100*Math.PI/180), 100);
//		ray.setRayObjects(rayObjects);
//		ray.update();
//		rays.add(ray);
	}
	
	public void render(Graphics g) {
		
		for(Ray ray:rays) {
			ArrayList<RayObject>chain = ray.getRayEndChain();
			for(int i = 0;i < chain.size();i++) {
				double x, y;
				x = Math.cos(ray.getTheta())*ray.getDistance(i+1)+this.x;
				y = Math.sin(ray.getTheta())*ray.getDistance(i+1)+this.y;
				if(chain.get(i) instanceof RayMirror) {
					g.setColor(new Color(0, 255, 0));
				}else if(chain.get(i) instanceof RayWall){
					g.setColor(new Color(255, 0, 0));
				}else if(chain.get(i) instanceof RayPass){
					g.setColor(new Color(0, 0, 255));
				}
				g.drawOval((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff())-2, 
						   (int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff())-2, 4, 4);
			}
		}
		
//		//add first to end
//		rays.add(rays.get(0));
//		for(int i = 0;i < rays.size()-1;i++) {
//			ArrayList<RayObject>history = new ArrayList<RayObject>();
//			ArrayList<RayObject>chain1 = rays.get(i).getRayEndChain();
//			ArrayList<RayObject>chain2 = rays.get(i+1).getRayEndChain();
//			for(RayObject obj1:chain1) {
//				if(obj1 instanceof RayMirror) {
//					history.add(obj1);
//					continue;
//				}
//				int seq = 0;
//				for(RayObject obj2:chain2) {
//					if(obj2 instanceof RayMirror) {
//						if(history.size() <= seq) break;
//						if(obj2 != history.get(seq))break;
//						seq++;
//					}
//					if(obj1 == obj2) {
//						
//						double x, y;
//						x = Math.cos(rays.get(i).getTheta())*rays.get(i).getTotalDistance()+this.x;
//						y = Math.sin(rays.get(i).getTheta())*rays.get(i).getTotalDistance()+this.y;
//						System.out.print(x);
//						System.out.print(" ");
//						System.out.println(y);
//						g.setColor(new Color(255, 0, 0));
//						g.drawOval((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
//								   (int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff()), 1, 1);
//						
//					}
//				}
//			}
//		}
//		//remove end
//		rays.remove(rays.size()-1);
		for(Ray r:rays) {
			//r.render(g);
		}
	}
	
	public void setRayObjects(ArrayList<RayObject> rayObjects) {
		this.rayObjects = rayObjects;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	
}
