package game.raycasting;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.concurrent.Callable;
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

		//multitreading variables
		int MAX_T = 4;
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T); 
		ArrayList<Callable<Object>> todo = new ArrayList<Callable<Object>>(1);
		ArrayList<RayThread> rts = new ArrayList<RayThread>();
		
		rays = new ArrayList<Ray>();
		
		for(float i = 0;i < 360;i+=0.5f) {
			Ray ray = new Ray(handler, x, y, (float) (i*Math.PI/180), (float)Math.sqrt(preceivedWidth*preceivedWidth+preceivedHeight*preceivedHeight));
			ray.setRayObjects(rayObjects);
			RayThread rt = new RayThread(ray);
			rts.add(rt);
			todo.add(Executors.callable(rt));
		}
		try {
			pool.invokeAll(todo);
		} catch (InterruptedException e) {}
		for(RayThread rt:rts) {
			rays.add(rt.getRay());
		}
		
		//reset for next pass
		todo = new ArrayList<Callable<Object>>(1);
		rts = new ArrayList<RayThread>();
		
		//second pass
		ArrayList<ArrayList<RayObject>> prevChain = segChain(rays.get(rays.size()-1).getRayEndChain());
		ArrayList<ArrayList<RayObject>> currentChain = new ArrayList<ArrayList<RayObject>>();
		for(int i = 0;i < rays.size();i++) {
			currentChain = segChain(rays.get(i).getRayEndChain());
			if(checkIfRayChainMatch(prevChain, currentChain)) {
				float inc = (float) (Math.PI/180)/10;
				float startAngle = rays.get(i==0?rays.size()-1:i-1).getTheta();
				float endAngle = rays.get(i).getTheta();
				for(float j = startAngle+inc;j < endAngle;j+=inc) {
					Ray ray = new Ray(handler, x, y, j, (float)Math.sqrt(preceivedWidth*preceivedWidth+preceivedHeight*preceivedHeight));
					ray.setRayObjects(rayObjects);
					RayThread rt = new RayThread(ray);
					rts.add(rt);
					todo.add(Executors.callable(rt));
				}
			}
			prevChain = segChain(rays.get(i).getRayEndChain());
		}
		try {
			pool.invokeAll(todo);
		} catch (InterruptedException e) {}
		for(RayThread rt:rts) {
			rays.add(rt.getRay());
		}
		pool.shutdown();
		//sort by angle
		rays.sort(new AngleSorter());
	}
	
	public boolean checkIfRayChainMatch(ArrayList<ArrayList<RayObject>> chain1, ArrayList<ArrayList<RayObject>> chain2) {
		if(chain1.size()!=chain2.size()) {
			return true;
		}else {
			for(int j = 0;j < chain1.size();j++) {
				if(!(chain2.get(j).containsAll(chain1.get(j)) && chain1.get(j).containsAll(chain2.get(j)))) {
					return true;
				}
			}
		}
		return false;
	}

	public void render(Graphics g) {
		//format in to array
		
		//0-rays
		//1-chains(pass, pass, wall), (mirror, wall)
		//2-links
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
		int lastIndex = rayChains.length-1;
		for(int i = 0;i < rayChains[lastIndex].length;i++) {
			for(int j = 0;j < rayChains[lastIndex][i].length;j++) {
				if(rayChains[lastIndex][i][j]!=null) {
					float dist = rays.get(lastIndex).getDistance(depth);
					float angle = rays.get(lastIndex).getTheta()-handler.getWorld().getRotation();
					Vector pos = new Vector((float)(Math.cos(angle)*dist+this.x),(float)(Math.sin(angle)*dist+this.y));
					RayEndpointInfo info = new RayEndpointInfo(rayChains[lastIndex][i][j], pos, depth, i, rayChains[lastIndex][i][0]);
					if(i == 0) info = new RayEndpointInfo(rayChains[lastIndex][i][j], pos, depth, i, null);
					activeEP.add(info);
					currentEP.add(info);
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
						RayEndpointInfo newInfo = new RayEndpointInfo(rayChains[i][j][k], pos, depth, j, rayChains[i][j][0]);
						if(j == 0) newInfo = new RayEndpointInfo(rayChains[i][j][k], pos, depth, j, null);
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
						if(activeInfo.getObj()==currentInfo.getObj()&&activeInfo.getId()==currentInfo.getId()&&activeInfo.getLast()==currentInfo.getLast()) {
							if(activeInfo.getObj() instanceof RayMirror) {
								renderLine(g,activeInfo.getPos().getX(),activeInfo.getPos().getY(), currentInfo.getPos().getX(), currentInfo.getPos().getY(), 
										new Color(0,255,255));
							}else if(activeInfo.getObj() instanceof RayWall){
								renderLine(g,activeInfo.getPos().getX(),activeInfo.getPos().getY(), currentInfo.getPos().getX(), currentInfo.getPos().getY(), 
										new Color(0,0,0));
							}else if(activeInfo.getObj() instanceof RayPass){
								renderLine(g,activeInfo.getPos().getX(),activeInfo.getPos().getY(), currentInfo.getPos().getX(), currentInfo.getPos().getY(), 
										new Color(0,0,0));
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
