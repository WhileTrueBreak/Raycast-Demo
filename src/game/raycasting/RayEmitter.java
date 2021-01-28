package game.raycasting;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;

import game.Handler;
import game.raycasting.object.RayMirror;
import game.raycasting.object.RayObject;
import game.raycasting.object.RayPass;
import game.raycasting.object.RayPortal;
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
		for(float i = 0;i < 360;i+=1f) {
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

//		for(Ray r:rays) {
//			r.render(g);
//		}
//		for(Ray ray:rays) {
//			ArrayList<RayObject>chain = ray.getRayEndChain();
//			for(int i = 0;i < chain.size();i++) {
//				double x, y;
//				x = Math.cos(ray.getTheta())*ray.getDistance(i+1)+this.x;
//				y = Math.sin(ray.getTheta())*ray.getDistance(i+1)+this.y;
//				if(chain.get(i) instanceof RayMirror) {
//					g.setColor(new Color(0, 255, 0));
//					g.drawOval((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff())-2, 
//							   (int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff())-2, 4, 4);
//				}else if(chain.get(i) instanceof RayWall){
//					g.setColor(new Color(255, 0, 0));
//					g.drawOval((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff())-2, 
//							   (int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff())-2, 4, 4);
//				}else if(chain.get(i) instanceof RayPass){
//					g.setColor(new Color(0, 0, 255));
//					g.drawOval((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff())-2, 
//							   (int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff())-2, 4, 4);
//				}else if(chain.get(i) instanceof RayPortal){
//					g.setColor(new Color(255, 0, 255));
//					g.drawOval((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff())-2, 
//							   (int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff())-2, 4, 4);
//				}
//			}
//		}
		//format in to array
		RayObject[][][] rayChains = new RayObject[rays.size()][0][0];
		for(int i = 0;i < rayChains.length;i++) {
			ArrayList<RayObject> arrlistChain = rays.get(i).getRayEndChain();
			ArrayList<ArrayList<RayObject>> splitChain = new ArrayList<ArrayList<RayObject>>();
			ArrayList<RayObject> chainSeg = new ArrayList<RayObject>();
			chainSeg.add(null);
			for(RayObject obj:arrlistChain) {
				if(obj.canTransposeRay()){
					splitChain.add(chainSeg);
					chainSeg = new ArrayList<RayObject>();
				}
				chainSeg.add(obj);
			}
			splitChain.add(chainSeg);
			RayObject[][] arrChain = new RayObject[splitChain.size()][0];
			for(int j = 0;j < arrChain.length;j++) {
				arrChain[j] = new RayObject[splitChain.get(j).size()];
				arrChain[j] = splitChain.get(j).toArray(arrChain[j]);
			}
			rayChains[i] = arrChain;
		}
		//
		for(int i = 0;i < rayChains.length;i++) {
			RayObject[][] chain1;
			chain1 = i==0?rayChains[rayChains.length-1]:rayChains[i-1];
			RayObject[][] chain2 = rayChains[i];
			int j = 0;
			while(j<chain1.length && j<chain2.length && chain1[j][0]==chain2[j][0]) {
				RayObject[] chainSeg1 = Arrays.copyOfRange(chain1[j], 1, chain1[j].length);
				RayObject[] chainSeg2 = Arrays.copyOfRange(chain2[j], 1, chain2[j].length);
				if(chainSeg1.length == 0||chainSeg2.length == 0) {
					j++;
					continue;
				}
				for(RayObject obj2:chainSeg2) {
					for(RayObject obj1:chainSeg1) {
						if(obj1==obj2) {
							int depth1 = depthCounter(obj1, rays.get(i==0?rayChains.length-1:i-1).getRayEndChain(), j);
							int depth2 = depthCounter(obj2, rays.get(i).getRayEndChain(), j);
							
							float dist1 = rays.get(i==0?rayChains.length-1:i-1).getDistance(depth1);
							float dist2 = rays.get(i).getDistance(depth2);
							float angle1 = rays.get(i==0?rayChains.length-1:i-1).getTheta();
							float angle2 = rays.get(i).getTheta();
							float x1, y1, x2, y2;
							
							x1 = (float) (Math.cos(angle1)*dist1+this.x);
							y1 = (float) (Math.sin(angle1)*dist1+this.y);
							x2 = (float) (Math.cos(angle2)*dist2+this.x);
							y2 = (float) (Math.sin(angle2)*dist2+this.y);
							
							if(obj1 instanceof RayMirror) {
								g.setColor(new Color(0, 255, 0));
							}else if(obj1 instanceof RayWall){
								g.setColor(new Color(255, 0, 0));
							}else if(obj1 instanceof RayPass){
								g.setColor(new Color(0, 0, 255));
							}else if(obj1 instanceof RayPortal){
								g.setColor(new Color(255, 0, 255));
							}
							
							g.drawLine((int)(x1*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
									   (int)(y1*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
									   (int)(x2*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
									   (int)(y2*handler.getCamera().getScale()-handler.getCamera().getYoff()));
						}
					}
				}
				j++;
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
	}
	
	private int depthCounter(RayObject obj, ArrayList<RayObject> chain, int j) {
		int counter = 0;
		int jc = 0;
		for(RayObject ro:chain) {
			counter++;
			if(ro == obj && jc == j) break;
			if(ro.canTransposeRay()) jc++;
		}
		return counter;
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
