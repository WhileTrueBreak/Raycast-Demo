package game.raycasting;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import game.Handler;
import game.raycasting.object.RayMirror;
import game.raycasting.object.RayObject;
import game.raycasting.object.RayPass;
import utils.Func;
import utils.Vector;
import utils.collision.Collisions;

public class Ray {
	public static int REFLECTION_LIMIT = 10;

	private Handler handler;
	
	//the object the ray ends on
	private RayObject endRayObject;
	//the next ray if required
	private Ray nextRay;
	//distance of the ray
	private float dist;
	//previous object the ray collided with
	private RayObject prevCollision;
	//all ray objects
	private ArrayList<RayObject>rayObjects;


	float x1, y1, x2, y2, theta;
	//current iteration of the ray
	int layer;
	//first ray initialization of the ray
	public Ray(Handler handler, float x,  float y, float theta, float dist){
		this.handler = handler;
		rayObjects = new ArrayList<RayObject>();
		this.dist = dist;
		this.x1 = x;
		this.y1 = y;
		this.x2 = (float) (x+dist*Math.cos(theta));
		this.y2 = (float) (y+dist*Math.sin(theta));
		this.theta = theta;
		this.nextRay = null;
		this.layer = 1;
	}

	public Ray(Handler handler, float x,  float y, float theta, float dist, int layer){
		this.handler = handler;
		rayObjects = new ArrayList<RayObject>();
		this.dist = dist;
		this.x1 = x;
		this.y1 = y;
		this.x2 = (float) (x+dist*Math.cos(theta));
		this.y2 = (float) (y+dist*Math.sin(theta));
		this.theta = theta;
		this.nextRay = null;
		this.layer = layer;
	}

	public void render(Graphics g){
		g.setColor(new Color(0, 0, 0));
		g.drawLine((int)(x1*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				   (int)(y1*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
				   (int)(x2*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				   (int)(y2*handler.getCamera().getScale()-handler.getCamera().getYoff()));
		if(nextRay!=null) nextRay.render(g);
	}

	public void update(){
		//get collision
		RayObject collisionObject = getClosestCollision();
		float pointDistance = dist;
		//check if collision exists
		if(collisionObject != null){
			//get distance to collision
			Vector collisionPoint = Collisions.lineLineVector(collisionObject.getX1(), collisionObject.getY1(), collisionObject.getX2(), collisionObject.getY2(), x1, y1, x2, y2);
			pointDistance = Func.dist(x1, y1, collisionPoint.getX(), collisionPoint.getY());
			//if collision is mirror and not at iteration limit create reflected ray
			if(layer < REFLECTION_LIMIT){
				
				if(collisionObject instanceof RayMirror){
					nextRay = getReflectedRay(collisionObject, dist-pointDistance);
				}else if(collisionObject instanceof RayPass){
					nextRay = getPassedRay(collisionObject, dist-pointDistance);
				}
			}
		}
		//set new distance
		dist = pointDistance;
		//update x2 and y2
		x2 = (float) (x1+pointDistance*Math.cos(theta));
		y2 = (float) (y1+pointDistance*Math.sin(theta));
		//update next ray if exist
		if(nextRay!=null) nextRay.update();
	}

	private Ray getPassedRay(RayObject collisionObject, float remainingDistance) {
		Vector collisionPoint = Collisions.lineLineVector(collisionObject.getX1(), collisionObject.getY1(), collisionObject.getX2(), collisionObject.getY2(), x1, y1, x2, y2);
		//create new ray
		Ray newRay = new Ray(handler, collisionPoint.getX(), collisionPoint.getY(), theta, remainingDistance, layer);
		//initialise variables
		newRay.setRayObjects(rayObjects);
		newRay.setPrevCollision(collisionObject);
		return newRay;
	}

	private Ray getReflectedRay(RayObject reflectObject, float remainingDistance){
		//get normal of reflected object
		float gradient = (reflectObject.getY2()-reflectObject.getY1())/(reflectObject.getX2()-reflectObject.getX1());
		float normalGradient = -1/gradient;
		float angle = (float) Math.atan(normalGradient);
		Vector normal = new Vector((float) Math.cos(angle), (float) Math.sin(angle));

		//render normal
		Vector collisionPoint = Collisions.lineLineVector(reflectObject.getX1(), reflectObject.getY1(), reflectObject.getX2(), reflectObject.getY2(), x1, y1, x2, y2);

		//get reflected ray
		Vector normalizedRay = new Vector((float) Math.cos(theta), (float) Math.sin(theta));
		float dot = normalizedRay.getX()*normal.getX()+normalizedRay.getY()*normal.getY();
		normal = normal.mult(dot*2);
		normal = normalizedRay.sub(normal);

		//get angle of reflected vector
		angle = (float) Math.atan2(normal.getY(), normal.getX());

		//create new ray
		Ray newRay = new Ray(handler, collisionPoint.getX(), collisionPoint.getY(), angle, remainingDistance, layer+1);
		//initialise variables
		newRay.setRayObjects(rayObjects);
		newRay.setPrevCollision(reflectObject);
		return newRay;
	}

	public RayObject getClosestCollision(){
		RayObject closest = null;
		float recordDist = (float) Double.POSITIVE_INFINITY;
		for(RayObject ro:rayObjects){
			if(ro == prevCollision) continue;
			Vector collisionPoint = Collisions.lineLineVector(ro.getX1(), ro.getY1(), ro.getX2(), ro.getY2(), x1, y1, x2, y2);
			if(collisionPoint == null) continue;
			float pointDistance = Func.dist(x1, y1, collisionPoint.getX(), collisionPoint.getY());
			if(pointDistance < recordDist){
				closest = ro;
				recordDist = pointDistance;
			}
		}
		return closest;
	}

	public void setRayObjects(ArrayList<RayObject>rayObjects){
		this.rayObjects = rayObjects;
	}

	public void setPrevCollision(RayObject prevCollision){
		this.prevCollision = prevCollision;
	}

	//GETTERS
	public RayObject getEndRayObject() {
		return endRayObject;
	}

	public Ray getRay() {
		return nextRay;
	}

}
