package game.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import game.Handler;
import game.inputs.Binds;
import game.raycasting.RayEmitter;
import game.raycasting.object.RayMirror;
import game.raycasting.object.RayObject;
import game.raycasting.object.RayPortal;
import game.raycasting.object.RayWall;
import utils.Func;
import utils.Logging;
import utils.Polygon;
import utils.Vector;
import utils.collision.Collisions;

public class Player extends Entity{

	private final static float PLAYER_WIDTH = 0.4f;
	private final static float PLAYER_HEIGHT = 0.4f;

	private final static float PLAYER_MOVE_SPEED = 3.7f;

	private RayEmitter rayEmitter;

	public Player(float x, float y, Handler handler) {
		super(x, y, handler);
		Vector[] hitboxVertex = {new Vector(0,0), new Vector(PLAYER_WIDTH, 0), new Vector(PLAYER_WIDTH, PLAYER_HEIGHT), new Vector(0, PLAYER_HEIGHT)};
		hitbox = new Polygon(hitboxVertex, 4);
		hitboxRotation = (int) Math.signum(getHitboxRotation());
		//hitbox = new Rectangle2D.Float(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT);
		rayEmitter = new RayEmitter(handler, x+PLAYER_WIDTH/2, y+PLAYER_HEIGHT/2);
	}

	@Override
	public void update() {
		move();
		rayEmitter.setX(x+PLAYER_WIDTH/2);
		rayEmitter.setY(y+PLAYER_HEIGHT/2);
		rayEmitter.setRayObjects(handler.getWorld().getRayObjects());
		rayEmitter.updateRays();
		//handler.getCamera().focusOnEntity(this, 1.1f);
		handler.getCamera().focusOnPoint(x+PLAYER_WIDTH/2, y+PLAYER_HEIGHT/2, 0.1f);
	}

	@Override
	public void render(Graphics g) {
		rayEmitter.render(g);
		g.setColor(new Color(0, 255, 0));
		g.fillRect((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				(int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
				(int)(PLAYER_WIDTH*handler.getCamera().getScale()), 
				(int)(PLAYER_HEIGHT*handler.getCamera().getScale()));
	}

	private void move() {
		Vector vel = new Vector(0, 0);
		if(handler.getKeyManager().isKeyPressed(Binds.LEFT)) {
			vel.add(new Vector(-1, 0));
		}if(handler.getKeyManager().isKeyPressed(Binds.RIGHT)) {
			vel.add(new Vector(1, 0));
		}if(handler.getKeyManager().isKeyPressed(Binds.UP)) {
			vel.add(new Vector(0, -1));
		}if(handler.getKeyManager().isKeyPressed(Binds.DOWN)) {
			vel.add(new Vector(0, 1));
		}
		vel.normalise();
		vel.mult((float) (PLAYER_MOVE_SPEED/handler.getCurrentFps()));
		vel = new Vector((float) (vel.getX()*Math.cos(handler.getWorld().getRotation())-vel.getY()*Math.sin(handler.getWorld().getRotation())),
				(float) (vel.getX()*Math.sin(handler.getWorld().getRotation())+vel.getY()*Math.cos(handler.getWorld().getRotation())));
		
		long st = System.nanoTime();
		//collisions
		vel = collisions(vel);
		//portal collisions
		vel = portalCollisions(vel);
		long et = System.nanoTime();
		long time = (et-st)/1000;
		if(time>100) {
			System.out.println("[Player]\tCollision time: "+time+"us");
			Logging.dumpLog();
		}
		Logging.clearLog();
		
		inWall();
		x+=vel.getX();
		y+=vel.getY();
	}

	private Vector collisions(Vector vel) {
		//get polygon points
		//Vector[] points = getHitboxPolygon(vel);
		//cut polygon with portal
		Polygon[] polygons = cutPolygon(getHitboxPolygon(vel));
		HashMap<RayObject, Vector> collisions = allCollisions(polygons);
		ArrayList<RayObject> done = new ArrayList<RayObject>();
		//System.out.println(collisions.size());
		while(collisions.size()!=0) {
			//System.out.println(collisions);
			RayObject obj = collisions.keySet().iterator().next();
			if(done.contains(obj)) {
				vel.mult(0);
				break;
			}
			Vector objV = collisions.get(obj);
			objV.normalise();
			float dot = objV.getX()*vel.getX()+objV.getY()*vel.getY();
			vel = Vector.mult(objV, dot);
			done.add(obj);
			polygons = cutPolygon(getHitboxPolygon(vel));
			collisions.clear();
			collisions = allCollisions(polygons);
		}
		return vel;
	}

	private Polygon[] cutPolygon(Polygon polygon){
		//TODO cut polygon with portal
		//move cut portion to other side of portal
		Polygon[] polygons = {polygon};
		return polygons;
	}

	private HashMap<RayObject, Vector> allCollisions(Polygon[] polygons){
		long st = System.nanoTime();
		HashMap<RayObject, Vector>out = new HashMap<RayObject, Vector>();
		ArrayList<RayObject>rayObjects = handler.getWorld().getRayObjects();
		for(RayObject obj:rayObjects) {
			for(int i = 0;i < polygons.length;i++) {
				if(obj.isSolid) {
					Rectangle2D.Float bound = polygons[i].getBoundingRect();
					if(Collisions.lineRect(obj.getX1(), obj.getY1(), obj.getX2(), obj.getY2(), 
							bound.x, bound.y, bound.width, bound.height)) {
						if(Collisions.polyLine(polygons[i].getVertices(), obj.getX1(), obj.getY1(), obj.getX2(), obj.getY2())) {
							Vector para = new Vector(obj.getX2()-obj.getX1(), obj.getY2()-obj.getY1());
							Vector normal = new Vector(1, -para.getX()/para.getY());
							normal.normalise();
							out.put(obj, para);
						}
					}
				}
			}
		}
		long et = System.nanoTime();
		Logging.addLog("all collisions: "+(et-st)/1000+"us");
		//TODO: sort by type of collision
		//collisions on line end handled last
		//or fix corner collisions (will do anyways)
		return out;
	}

	private void inWall(){
		ArrayList<RayObject>rayObjects = handler.getWorld().getRayObjects();
		for(RayObject obj:rayObjects) {
			if(obj.isSolid) {
				Rectangle2D.Float boundingRect = hitbox.getBoundingRect();
				if(Collisions.lineRect(obj.getX1(), obj.getY1(), obj.getX2(), obj.getY2(), 
						(float)boundingRect.getX(), (float)boundingRect.getY(), 
						(float)boundingRect.getWidth(), (float)boundingRect.getHeight())) {
					if(Collisions.polyLine(hitbox.getVertices(), obj.getX1(), obj.getY1(), obj.getX2(), obj.getY2())) {
						//System.out.println("in wall");
					}
				}
			}
		}
	}
	
	private float getHitboxRotation() {
		//finds the direction which the vertices follow
		//+ : anticlockwise
		//- : clockwise
		
		Vector[] points = hitbox.getVertices();
		if(points.length<3) System.out.println("[Player]\t\tHitbox does not have enough vertices: "+Integer.toString(points.length));
		Vector avgPoint = new Vector(0, 0);
		for(Vector point:points) {
			avgPoint.add(point);
		}
		avgPoint.mult(1/(float)points.length);

		Vector v1 = Vector.sub(points[0], avgPoint);
		Vector v2 = Vector.sub(points[1], avgPoint);
		
		float cross = v1.getX()*v2.getY()-v1.getY()*v2.getX();
		return cross;
	}

	private Polygon getHitboxPolygon(Vector vel) {

		long st = System.nanoTime();
		//creates polygon stretched in the direction of vel for better collisions
		
		//loop through vertices
		Vector[] points = hitbox.copyVertices();
		int[] entryRots = new int[points.length];
		Vector tail = points[points.length-1];
		for(int i = 0;i < points.length;i++) {
			Vector segment = Vector.sub(points[i], tail);
			entryRots[i] = (int) Math.signum(vel.getX()*segment.getY()-vel.getY()*segment.getX());
			if(entryRots[i] == 0) {
				entryRots[i] = -hitboxRotation;
			}
			tail = points[i];
		}
		
		//create polygon
		ArrayList<Vector> newPoints = new ArrayList<Vector>();
		for(int i = 0;i < entryRots.length;i++) {
			int lastIndex = i-1;
			if(lastIndex < 0) lastIndex = points.length+lastIndex;
			if(entryRots[i] != hitboxRotation) {
				if(entryRots[i] != entryRots[lastIndex]) {
					newPoints.add(points[lastIndex]);
				}
				newPoints.add(points[i]);
			}else {
				if(entryRots[i] != entryRots[lastIndex]) {
					newPoints.add(Vector.add(points[lastIndex], vel));
				}
				newPoints.add(Vector.add(points[i], vel));
			}
		}
		Vector[] newPointsArr = new Vector[newPoints.size()];
		newPointsArr = newPoints.toArray(newPointsArr);
		for(int i = 0;i < newPointsArr.length;i++) {
			newPointsArr[i].add(new Vector(this.x, this.y));
		}
		long et = System.nanoTime();
		Logging.addLog("polygon sweep: "+(et-st)/1000+"us");
		return new Polygon(newPointsArr, newPointsArr.length);
		
//		//get diagonals
//		Vector diagT = new Vector((float)(hitbox.getMaxX()-hitbox.getMinX()),(float)(hitbox.getMaxY()-hitbox.getMinY()));
//		Vector diagB = new Vector((float)(hitbox.getMaxX()-hitbox.getMinX()),(float)(hitbox.getMinY()-hitbox.getMaxY()));
//		//get polygon points
//		Vector[] points = new Vector[6];
//		//get dot products
//		float dotT = diagT.getX()*vel.getX()+diagT.getY()*vel.getY();
//		float dotB = diagB.getX()*vel.getX()+diagB.getY()*vel.getY();
//		//get other two points
//		if(dotT<dotB) {
//			if(dotB>0) {
//				points[1] = new Vector((float)hitbox.getMinX(),(float)hitbox.getMinY());
//				points[2] = new Vector((float)hitbox.getMinX()+vel.getX(),(float)hitbox.getMinY()+vel.getY());
//				points[4] = new Vector((float)hitbox.getMaxX()+vel.getX(),(float)hitbox.getMaxY()+vel.getY());
//				points[5] = new Vector((float)hitbox.getMaxX(),(float)hitbox.getMaxY());
//
//				points[0] = new Vector((float)hitbox.getMinX(),(float)hitbox.getMaxY());
//				points[3] = new Vector((float)hitbox.getMaxX()+vel.getX(),(float)hitbox.getMinY()+vel.getY());
//			}else {
//				points[5] = new Vector((float)hitbox.getMinX(),(float)hitbox.getMinY());
//				points[4] = new Vector((float)hitbox.getMinX()+vel.getX(),(float)hitbox.getMinY()+vel.getY());
//				points[2] = new Vector((float)hitbox.getMaxX()+vel.getX(),(float)hitbox.getMaxY()+vel.getY());
//				points[1] = new Vector((float)hitbox.getMaxX(),(float)hitbox.getMaxY());
//
//				points[0] = new Vector((float)hitbox.getMaxX(),(float)hitbox.getMinY());
//				points[3] = new Vector((float)hitbox.getMinX()+vel.getX(),(float)hitbox.getMaxY()+vel.getY());
//			}
//		}else {
//			if(dotT>0) {
//				points[1] = new Vector((float)hitbox.getMaxX(),(float)hitbox.getMinY());
//				points[2] = new Vector((float)hitbox.getMaxX()+vel.getX(),(float)hitbox.getMinY()+vel.getY());
//				points[4] = new Vector((float)hitbox.getMinX()+vel.getX(),(float)hitbox.getMaxY()+vel.getY());
//				points[5] = new Vector((float)hitbox.getMinX(),(float)hitbox.getMaxY());
//
//				points[0] = new Vector((float)hitbox.getMinX(),(float)hitbox.getMinY());
//				points[3] = new Vector((float)hitbox.getMaxX()+vel.getX(),(float)hitbox.getMaxY()+vel.getY());
//			}else {
//				points[5] = new Vector((float)hitbox.getMaxX(),(float)hitbox.getMinY());
//				points[4] = new Vector((float)hitbox.getMaxX()+vel.getX(),(float)hitbox.getMinY()+vel.getY());
//				points[2] = new Vector((float)hitbox.getMinX()+vel.getX(),(float)hitbox.getMaxY()+vel.getY());
//				points[1] = new Vector((float)hitbox.getMinX(),(float)hitbox.getMaxY());
//
//				points[0] = new Vector((float)hitbox.getMaxX(),(float)hitbox.getMaxY());
//				points[3] = new Vector((float)hitbox.getMinX()+vel.getX(),(float)hitbox.getMinY()+vel.getY());
//			}
//		}
//		for(int i = 0;i < points.length;i++) {
//			points[i].add(new Vector(this.x, this.y));
//		}
	}

	private Vector portalCollisions(Vector vel) {
		ArrayList<RayObject>rayObjects = handler.getWorld().getRayObjects();
		float tx = 0, ty = 0;
		float portalAngle = 0;
		boolean collided = false;
		for(RayObject obj:rayObjects) {
			if(obj instanceof RayPortal) {
				RayPortal portalObject = (RayPortal) obj;
				Vector collisionPoint = Collisions.lineLineVector(portalObject.getX1(), portalObject.getY1(), portalObject.getX2(), portalObject.getY2(), 
						x+PLAYER_WIDTH/2, y+PLAYER_HEIGHT/2, x+PLAYER_WIDTH/2+vel.getX(), y+PLAYER_HEIGHT/2+vel.getY());
				if(collisionPoint != null){
					RayPortal linkedPortal = portalObject.getLinkedPortal();
					float enteranceAngle = (float) Math.atan2(portalObject.getY2()-portalObject.getY1(), portalObject.getX2()-portalObject.getX1());
					float exitAngle = (float) Math.atan2(linkedPortal.getY2()-linkedPortal.getY1(), linkedPortal.getX2()-linkedPortal.getX1());
					float deltaAngle = exitAngle-enteranceAngle;
					float portalLength = Func.dist(portalObject.getX1(), portalObject.getY1(), portalObject.getX2(), portalObject.getY2());
					float fromCollision = Func.dist(portalObject.getX1(), portalObject.getY1(), collisionPoint.getX(), collisionPoint.getY());
					float collisionPercent = fromCollision/portalLength;

					float exitdx = linkedPortal.getX2()-linkedPortal.getX1();
					float exitdy = linkedPortal.getY2()-linkedPortal.getY1();
					Vector exitPoint = new Vector(linkedPortal.getX1()+exitdx*collisionPercent, linkedPortal.getY1()+exitdy*collisionPercent);

					vel = new Vector((float) (vel.getX()*Math.cos(deltaAngle)-vel.getY()*Math.sin(deltaAngle)),
							(float) (vel.getX()*Math.sin(deltaAngle)+vel.getY()*Math.cos(deltaAngle)));

					tx = exitPoint.getX()-PLAYER_WIDTH/2;
					ty = exitPoint.getY()-PLAYER_HEIGHT/2;
					collided = true;
					portalAngle = deltaAngle;
					handler.getWorld().setRotation(handler.getWorld().getRotation()+deltaAngle);
				}
			}else {
				continue;
			}
		}
		if(collided) {
			//move camera to player teleported location relative to previous location
			float camPlayerXoff, camPlayerYoff;
			float relPlayerX, relPlayerY;
			relPlayerX = (x+(float)PLAYER_WIDTH/2)*handler.getCamera().getScale() - (float)handler.getWidth ()/2;
			relPlayerY = (y+(float)PLAYER_HEIGHT/2)*handler.getCamera().getScale() - (float)handler.getHeight()/2;
			camPlayerXoff = handler.getCamera().getXoff() - relPlayerX;
			camPlayerYoff = handler.getCamera().getYoff() - relPlayerY;
			//rotate for portal
			float theta = handler.getWorld().getRotation();
			//			System.out.println("[Player]\tangle||"+String.valueOf(theta));
			//			System.out.println("[Player]\tcam-ply1||"+String.valueOf(camPlayerXoff)+","+String.valueOf(camPlayerYoff));
			camPlayerXoff = (float)(camPlayerXoff*Math.cos(theta)-camPlayerYoff*Math.sin(theta));
			camPlayerYoff = (float)(camPlayerXoff*Math.sin(theta)+camPlayerYoff*Math.cos(theta));

			//			System.out.println("[Player]\tcam-ply2||"+String.valueOf(camPlayerXoff)+","+String.valueOf(camPlayerYoff));


			handler.getCamera().focusOnPoint(tx+PLAYER_WIDTH/2, ty+PLAYER_HEIGHT/2, 0);
			handler.getCamera().move(camPlayerXoff, camPlayerYoff);

			handler.getCamera().setRot(handler.getWorld().getRotation());

			this.x = tx;
			this.y = ty;

		}
		return vel;
	}
}
