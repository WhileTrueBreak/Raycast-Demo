package game.raycasting;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import game.Handler;
import game.raycasting.object.RayMirror;
import game.raycasting.object.RayObject;
import game.raycasting.object.RayPass;
import game.raycasting.object.RayPortal;
import game.raycasting.object.RayWall;
import utils.Vector;

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

		float preceivedWidth = handler.getWidth()/handler.getCamera().getScale();
		float preceivedHeight = handler.getHeight()/handler.getCamera().getScale();

		int MAX_T = 4;

		ExecutorService pool = Executors.newFixedThreadPool(MAX_T); 

		ArrayList<RayThread> rts = new ArrayList<RayThread>();
		rays = new ArrayList<Ray>();
		for(float i = 0;i < 360;i+=1) {
			Ray ray = new Ray(handler, x, y, (float) (i*Math.PI/180), (float)Math.sqrt(preceivedWidth*preceivedWidth+preceivedHeight*preceivedHeight));
			ray.setRayObjects(rayObjects);
			RayThread rt = new RayThread(ray);
			rts.add(rt);
			pool.execute(rt);
		}
		pool.shutdown();
		while(!pool.isTerminated()) {}
		for(RayThread rt:rts) {
			rays.add(rt.getRay());
		}
		pool = Executors.newFixedThreadPool(MAX_T); 
		rts = new ArrayList<RayThread>();
		//second pass
		ArrayList<ArrayList<RayObject>> prevChain = segChain(rays.get(rays.size()-1).getRayEndChain());
		ArrayList<ArrayList<RayObject>> currentChain = new ArrayList<ArrayList<RayObject>>();
		for(int i = 0;i < rays.size();i++) {
			boolean notMatching = false;
			currentChain = segChain(rays.get(i).getRayEndChain());
			if(prevChain.size()!=currentChain.size()) {
				notMatching = true;
			}else {
				for(int j = 0;j < prevChain.size();j++) {
					if(!(currentChain.get(j).containsAll(prevChain.get(j)) && prevChain.get(j).containsAll(currentChain.get(j)))) {
						notMatching = true;
						break;
					}
				}
			}
			if(notMatching) {
				float inc = (float) (Math.PI/180)/10;
				float startAngle = rays.get(i==0?rays.size()-1:i-1).getTheta();
				float endAngle = rays.get(i).getTheta();
				for(float j = startAngle+inc;j < endAngle;j+=inc) {
					Ray ray = new Ray(handler, x, y, j, (float)Math.sqrt(preceivedWidth*preceivedWidth+preceivedHeight*preceivedHeight));
					ray.setRayObjects(rayObjects);
					RayThread rt = new RayThread(ray);
					rts.add(rt);
					pool.execute(rt);
				}
			}
			prevChain = segChain(rays.get(i).getRayEndChain());
		}
		pool.shutdown();
		while(!pool.isTerminated()) {}
		for(RayThread rt:rts) {
			rays.add(rt.getRay());
		}
		//sort by angle
		rays.sort(new AngleSorter());
		//		rays = new ArrayList<Ray>();
		//		for(float i = 0;i < 360;i+=1f) {
		//			Ray ray = new Ray(handler, x, y, (float) (i*Math.PI/180), 100);
		//			ray.setRayObjects(rayObjects);
		//			ray.update();
		//			rays.add(ray);
		//		}

		//		Ray ray = new Ray(handler, x, y, (float) (100*Math.PI/180), 100);
		//		ray.setRayObjects(rayObjects);
		//		ray.update();
		//		rays.add(ray);
	}

	public void render(Graphics g) {
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

		ArrayList<RayEndpointInfo> activeEP = new ArrayList<RayEndpointInfo>();
		ArrayList<RayEndpointInfo> currentEP = new ArrayList<RayEndpointInfo>();

		int depth = 1;
		//first pass
		for(int i = 0;i < rayChains[rayChains.length-1].length;i++) {
			for(int j = 0;j < rayChains[rayChains.length-1][i].length;j++) {
				if(rayChains[rayChains.length-1][i][j]!=null) {
					float dist = rays.get(rayChains.length-1).getDistance(depth);
					float angle = rays.get(rayChains.length-1).getTheta()+handler.getWorld().getRotation();
					Vector pos = new Vector((float)(Math.cos(angle)*dist+this.x),(float)(Math.sin(angle)*dist+this.y));
					activeEP.add(new RayEndpointInfo(rayChains[rayChains.length-1][i][j], pos, depth, i));
					currentEP.add(new RayEndpointInfo(rayChains[rayChains.length-1][i][j], pos, depth, i));
					depth++;
				}
			}
		}
		//looped pass
		for(int i = 0;i < rayChains.length;i++) {
			depth = 1;
			ArrayList<RayEndpointInfo> toAdd = new ArrayList<RayEndpointInfo>();
			ArrayList<RayEndpointInfo> tempCurrent = new ArrayList<RayEndpointInfo>();
			for(int j = 0;j < rayChains[i].length;j++) {
				for(int k = 0;k < rayChains[i][j].length;k++) {
					if(rayChains[i][j][k]!=null) {
						float dist = rays.get(i).getDistance(depth);
						float angle = rays.get(i).getTheta()-handler.getWorld().getRotation();
						Vector pos = new Vector((float)(Math.cos(angle)*dist+this.x),(float)(Math.sin(angle)*dist+this.y));
						RayEndpointInfo newInfo = new RayEndpointInfo(rayChains[i][j][k], pos, depth, j);
						tempCurrent.add(newInfo);
						boolean inActive = false;
						for(RayEndpointInfo info:activeEP) {
							if(info.getObj()==rayChains[i][j][k]&&info.getId()==j) {
								inActive = true;
								break;
							}
						}
						depth++;
						//add to active
						if(!inActive) {
							toAdd.add(newInfo);
						}
					}
				}
			}
			//check for diffs
			ArrayList<RayEndpointInfo> toRemove = new ArrayList<RayEndpointInfo>();
			for(RayEndpointInfo activeInfo:activeEP) {
				boolean inCurrent = false;
				for(RayEndpointInfo currentInfo:tempCurrent) {
					if(activeInfo.getObj()==currentInfo.getObj()&&activeInfo.getId()==currentInfo.getId()) {
						inCurrent = true;
					}
				}
				if(!inCurrent||i==rayChains.length-1) {
					toRemove.add(activeInfo);
					for(RayEndpointInfo currentInfo:currentEP) {
						if(activeInfo.getObj()==currentInfo.getObj()&&activeInfo.getId()==currentInfo.getId()) {
							if(activeInfo.getObj() instanceof RayMirror) {
								
							}else if(activeInfo.getObj() instanceof RayWall){
								renderLine(g,activeInfo.getPos().getX(),activeInfo.getPos().getY(), currentInfo.getPos().getX(), currentInfo.getPos().getY(), new Color(0,0,0));
							}else if(activeInfo.getObj() instanceof RayPass){
								renderLine(g,activeInfo.getPos().getX(),activeInfo.getPos().getY(), currentInfo.getPos().getX(), currentInfo.getPos().getY(), new Color(0,0,0));
							}else if(activeInfo.getObj() instanceof RayPortal){
								
							}
						}
					}

				}
			}
			activeEP.removeAll(toRemove);
			activeEP.addAll(toAdd);
			currentEP = tempCurrent;
		}

//		for(Ray r:rays) {
//			r.render(g);
//		}
	}

	//		for(int i = 0;i < rayChains.length;i++) {
	//			RayObject[][] chain1;
	//			chain1 = i==0?rayChains[rayChains.length-1]:rayChains[i-1];
	//			RayObject[][] chain2 = rayChains[i];
	//			int j = 0;
	//			while(j<chain1.length && j<chain2.length && chain1[j][0]==chain2[j][0]) {
	//				RayObject[] chainSeg1 = Arrays.copyOfRange(chain1[j], 1, chain1[j].length);
	//				RayObject[] chainSeg2 = Arrays.copyOfRange(chain2[j], 1, chain2[j].length);
	//				if(chainSeg1.length == 0||chainSeg2.length == 0) {
	//					j++;
	//					continue;
	//				}
	//				for(RayObject obj2:chainSeg2) {
	//					for(RayObject obj1:chainSeg1) {
	//						if(obj1==obj2) {
	//							int depth1 = depthCounter(obj1, rays.get(i==0?rayChains.length-1:i-1).getRayEndChain(), j);
	//							int depth2 = depthCounter(obj2, rays.get(i).getRayEndChain(), j);
	//							
	//							float dist1 = rays.get(i==0?rayChains.length-1:i-1).getDistance(depth1);
	//							float dist2 = rays.get(i).getDistance(depth2);
	//							float angle1 = rays.get(i==0?rayChains.length-1:i-1).getTheta();
	//							float angle2 = rays.get(i).getTheta();
	//							float x1, y1, x2, y2;
	//							
	//							x1 = (float) (Math.cos(angle1)*dist1+this.x);
	//							y1 = (float) (Math.sin(angle1)*dist1+this.y);
	//							x2 = (float) (Math.cos(angle2)*dist2+this.x);
	//							y2 = (float) (Math.sin(angle2)*dist2+this.y);
	//							
	//							if(obj1 instanceof RayMirror) {
	//								g.setColor(new Color(0, 255, 0));
	//							}else if(obj1 instanceof RayWall){
	//								g.setColor(new Color(255, 0, 0));
	//							}else if(obj1 instanceof RayPass){
	//								g.setColor(new Color(0, 0, 255));
	//							}else if(obj1 instanceof RayPortal){
	//								g.setColor(new Color(255, 0, 255));
	//							}
	//							
	//							boolean inFrame = Collisions.lineRect(x1, y1, x2, y2, 
	//									handler.getCamera().getXoff()/handler.getCamera().getScale(),
	//									handler.getCamera().getYoff()/handler.getCamera().getScale(),
	//									handler.getWidth()/handler.getCamera().getScale(),
	//									handler.getHeight()/handler.getCamera().getScale()); 
	//							if(inFrame) {
	//								g.drawLine((int)(x1*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
	//										   (int)(y1*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
	//										   (int)(x2*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
	//										   (int)(y2*handler.getCamera().getScale()-handler.getCamera().getYoff()));
	//							}
	//						}
	//					}
	//				}
	//				j++;
	//			}
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
	//	}
	
	private ArrayList<ArrayList<RayObject>> segChain(ArrayList<RayObject> linkedChain){
		ArrayList<ArrayList<RayObject>> segChain = new ArrayList<ArrayList<RayObject>>();
		ArrayList<RayObject> seg = new ArrayList<RayObject>();
		seg.add(null);
		for(RayObject obj:linkedChain) {
			if(obj.canTransposeRay()) {
				segChain.add(seg);
				seg = new ArrayList<RayObject>();
			}
			seg.add(obj);
		}
		segChain.add(seg);
		return segChain;
	}

	private void renderLine(Graphics g, float x1, float y1, float x2, float y2, Color color) {
		g.setColor(color);
		g.drawLine(
				(int)(x1*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				(int)(y1*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
				(int)(x2*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				(int)(y2*handler.getCamera().getScale()-handler.getCamera().getYoff()));
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
