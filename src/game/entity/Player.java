package game.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import game.Handler;
import game.inputs.Binds;
import game.raycasting.RayEmitter;
import game.raycasting.object.RayObject;
import game.raycasting.object.RayPortal;
import utils.Func;
import utils.Logging;
import utils.Polygon;
import utils.Vector;
import utils.collision.Collisions;
import utils.tuple.Tuple2;
import utils.tuple.Tuple3;

public class Player extends Entity{

	private final static float PLAYER_WIDTH = 0.4f;
	private final static float PLAYER_HEIGHT = 0.4f;

	private final static float PLAYER_MOVE_SPEED = 3.7f;

	private RayEmitter rayEmitter;

	public Player(float x, float y, Handler handler) {
		super(x, y, handler);
		Vector[] hitboxVertex = {new Vector(0,0), new Vector(PLAYER_WIDTH, 0), new Vector(PLAYER_WIDTH, PLAYER_HEIGHT), new Vector(0, PLAYER_HEIGHT)};
		hitbox = new Polygon(hitboxVertex);
		hitboxRotation = (int) Math.signum(getHitboxRotation());
		//hitbox = new Rectangle2D.Float(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT);
		rayEmitter = new RayEmitter(handler, x+PLAYER_WIDTH/2, y+PLAYER_HEIGHT/2);
	}

	@Override
	public void update() {
		
		long st = System.nanoTime();
		
		move();
		
		long est = System.nanoTime();
		Logging.addLog("Move time: "+(est-st)/1000+"us");
		
		rayEmitter.setX(x+PLAYER_WIDTH/2);
		rayEmitter.setY(y+PLAYER_HEIGHT/2);
		rayEmitter.setRayObjects(handler.getWorld().getRayObjects());
		rayEmitter.updateRays();
		//handler.getCamera().focusOnEntity(this, 1.1f);
		handler.getCamera().focusOnPoint(x+PLAYER_WIDTH/2, y+PLAYER_HEIGHT/2, 0.1f);
		
		//logging
		long et = System.nanoTime();
		Logging.addLog("RT time: "+(et-est)/1000+"us");
		long time = (et-st)/1000;
		if(time>10000) {
			System.out.println("[Player]\tUpdate time: "+time+"us");
			Logging.dumpLog();
		}
		Logging.clearLog();
	}

	@Override
	public void render(Graphics g) {
		
		long st = System.nanoTime();
		
		rayEmitter.render(g);
		
		for(Tuple2<Polygon, Vector> poly:cutPolygon(getHitboxNewPos(hitbox, new Vector(0, 0)), new Vector(0, 0))) {
			poly.getFirst().render(handler, g);
		}
		
//		g.setColor(new Color(0, 255, 0));
//		g.fillRect((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
//				(int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
//				(int)(PLAYER_WIDTH*handler.getCamera().getScale()), 
//				(int)(PLAYER_HEIGHT*handler.getCamera().getScale()));
		
		//logging
		long et = System.nanoTime();
		long time = (et-st)/1000;
		if(time>15000) {
			System.out.println("[Player]\tRender time: "+time+"us");
			Logging.dumpLog();
		}
		Logging.clearLog();
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
		//collisions
		vel = collisions(vel);
		//portal collisions
		vel = portalCollisions(vel);

		inWall();
		x+=vel.getX();
		y+=vel.getY();
	}

	private Vector collisions(Vector vel) {
		//get polygon points
		//Vector[] points = getHitboxPolygon(vel);
		//cut polygon with portal
		ArrayList<Tuple2<Polygon, Vector>>polygons = cutPolygon(getHitboxSweep(hitbox, vel), vel);
		Tuple3<RayObject, Vector, Float> collisionData = closestCollisionPoint(polygons, vel);
		ArrayList<RayObject> done = new ArrayList<RayObject>();
		while(collisionData != null) {
			RayObject obj = collisionData.getFirst();
			if(done.contains(obj)) {
				vel.mult(0);
				break;
			}
			//move dist
			//change vel
			Vector objV = collisionData.getSecond();
			objV.normalise();
			float dot = objV.getX()*vel.getX()+objV.getY()*vel.getY();
			vel = Vector.mult(objV, dot);
			done.add(obj);
			polygons = cutPolygon(getHitboxSweep(hitbox, vel), vel);
			collisionData = closestCollisionPoint(polygons, vel);
		}
		return vel;
	}

	private ArrayList<Tuple2<Polygon, Vector>> cutPolygon(Polygon polygon, Vector vel){
		long st = System.nanoTime();
		//ArrayList<Vector> poly1 = new ArrayList<Vector>();
		//ArrayList<Vector> poly2 = new ArrayList<Vector>();
		ArrayList<Tuple2<Polygon, Vector>> polygons = new ArrayList<Tuple2<Polygon, Vector>>();
		RayPortal portalObject = null;
		//TODO cut polygon with portal
		for(RayObject obj:handler.getWorld().getRayObjects()) {
			if(obj instanceof RayPortal) {
				if(Collisions.polyLine(polygon.getVertices(), obj.getX1(), obj.getY1(), obj.getX2(), obj.getY2()))
					portalObject = (RayPortal)obj;
			}
		}
		if(portalObject != null) {
			Vector tail = polygon.getVertices()[polygon.getVertexCount()-1];
			
			boolean onRealSide = !Collisions.lineLine(this.x+PLAYER_WIDTH/2, this.y+PLAYER_HEIGHT/2, tail.getX(), tail.getY(), 
					portalObject.getX1(), portalObject.getY1(), portalObject.getX2(), portalObject.getY2());
			
			ArrayList<Vector> realSeg = new ArrayList<Vector>();
			Vector[] realSegArr = new Vector[0];
			ArrayList<Vector> imgSeg = new ArrayList<Vector>();
			Vector[] imgSegArr = new Vector[0];
			//check is last element is on the real side
			Vector portalEx = new Vector(portalObject.getX2()-portalObject.getX1(), portalObject.getY2()-portalObject.getY1());
			portalEx.mult(10);
			//loop all edges
			for(Vector head:polygon.getVertices()) {
				if(onRealSide) realSeg.add(tail);
				else imgSeg.add(tail);
				Vector collisionPoint = Collisions.lineLineVector(tail.getX(), tail.getY(), head.getX(), head.getY(), 
						portalObject.getX1()-portalEx.getX(), portalObject.getY1()-portalEx.getY(), portalObject.getX2()+portalEx.getX(), portalObject.getY2()+portalEx.getY());
				if(collisionPoint != null) {
					realSeg.add(collisionPoint);
					imgSeg.add(collisionPoint);
					onRealSide = !onRealSide;
				}
				tail = head;
			}
			//portal translation
			ArrayList<Vector> NimgSeg = new ArrayList<Vector>();
			Vector portalPos1 = new Vector(portalObject.getX1(),  portalObject.getY1());
			Vector portalPos2 = new Vector(portalObject.getX2(),  portalObject.getY2());
			Vector portalVel = vel.copy();
			
			RayPortal linkedPortal = portalObject.getLinkedPortal();
			float enteranceAngle = (float) Math.atan2(portalObject.getY2()-portalObject.getY1(), portalObject.getX2()-portalObject.getX1());
			float exitAngle = (float) Math.atan2(linkedPortal.getY2()-linkedPortal.getY1(), linkedPortal.getX2()-linkedPortal.getX1());
			float deltaAngle = exitAngle-enteranceAngle;
			float portalLength = Func.dist(portalObject.getX1(), portalObject.getY1(), portalObject.getX2(), portalObject.getY2());
			
			for(Vector point:imgSeg) {
				Vector portalVector = Vector.sub(portalPos2, portalPos1);
				Vector branchVector = Vector.sub(point, portalPos1);
				portalVector.normalise();
				float dot = Vector.dot(portalVector, branchVector);
				Vector resolute = Vector.mult(portalVector, dot);
				Vector mappedPoint = Vector.add(portalPos1, resolute);
				Vector perp = Vector.sub(branchVector, resolute);
				
				float fromCollision = Func.dist(portalObject.getX1(), portalObject.getY1(), mappedPoint.getX(), mappedPoint.getY());
				float collisionPercent = fromCollision/portalLength;
				
				float exitdx = linkedPortal.getX2()-linkedPortal.getX1();
				float exitdy = linkedPortal.getY2()-linkedPortal.getY1();
				Vector exitPoint = new Vector(linkedPortal.getX1()+exitdx*collisionPercent, linkedPortal.getY1()+exitdy*collisionPercent);
				
				perp.rotate(deltaAngle);
				exitPoint.add(perp);
				NimgSeg.add(exitPoint);
			}
			portalVel.rotate(deltaAngle);
			
			//convert to arr
			realSegArr = realSeg.toArray(realSegArr);
			imgSegArr = NimgSeg.toArray(imgSegArr);
			polygons.add(new Tuple2<Polygon, Vector>(new Polygon(realSegArr), vel));
			polygons.add(new Tuple2<Polygon, Vector>(new Polygon(imgSegArr), portalVel));
			
			//logging
			long et = System.nanoTime();
			Logging.addLog("\tcut polygon: "+(et-st)/1000+"us");
			
			return polygons;
		}
		
		//move cut portion to other side of portal
		polygons.add(new Tuple2<Polygon, Vector>(polygon, vel));
		//logging
		long et = System.nanoTime();
		Logging.addLog("\tcut polygon: "+(et-st)/1000+"us");
		
		return polygons;
	}

	private Tuple3<RayObject, Vector, Float> closestCollisionPoint(ArrayList<Tuple2<Polygon, Vector>> polygons, Vector vel){
		long st = System.nanoTime();
		int HBC = 0, EPC = 0;
		ArrayList<Tuple3<RayObject, Vector, Float>>allCollisions = new ArrayList<Tuple3<RayObject, Vector, Float>>();
		
		for(RayObject obj:handler.getWorld().getRayObjects()) {
			if(obj.isSolid) {
				for(int i = 0;i < polygons.size();i++) {
					
					float realAngle = (float) Math.atan2(vel.getY(), vel.getX());
					float portalAngle = (float) Math.atan2(polygons.get(i).getSecond().getY(), polygons.get(i).getSecond().getX());
					float deltaAngle = portalAngle-realAngle;
					
					Vector[] vertices = polygons.get(i).getFirst().getVertices();
					Rectangle2D.Float bound = polygons.get(i).getFirst().getBoundingRect();
					if(Collisions.lineRect(obj.getX1(), obj.getY1(), obj.getX2(), obj.getY2(), 
							bound.x, bound.y, bound.width, bound.height)) {
						//System.out.println(deltaAngle);
						//checking hitbox vertex collisions
						for(Vector vertex:vertices) {
							Vector collisionPoint = Collisions.lineLineVector(vertex.getX(), vertex.getY(), vertex.getX()-polygons.get(i).getSecond().getX(), vertex.getY()-polygons.get(i).getSecond().getY(), 
									obj.getX1(), obj.getY1(), obj.getX2(), obj.getY2());
							HBC++;
							if(collisionPoint != null) {
								float dist = Func.dist(collisionPoint.getX(), collisionPoint.getY(), vertex.getX(), vertex.getY());
								Vector slide = new Vector(obj.getX2()-obj.getX1(), obj.getY2()-obj.getY1());
								slide.rotate(deltaAngle);
								allCollisions.add(new Tuple3<RayObject, Vector, Float>(obj, slide, dist));
							}
						}
						//check endpoint collisions
						Vector tail = vertices[vertices.length-1];
						for(int j = 0;j < vertices.length;j++) {
							//check endpoint 1
							Vector collisionPoint1 = Collisions.lineLineVector(tail.getX(), tail.getY(), vertices[j].getX(), vertices[j].getY(), 
									obj.getX1(), obj.getY1(), obj.getX1()+polygons.get(i).getSecond().getX(), obj.getY1()+polygons.get(i).getSecond().getY());
							EPC++;
							if(collisionPoint1 != null) {
								float dist = Func.dist(collisionPoint1.getX(), collisionPoint1.getY(), obj.getX1(), obj.getY1());
								Vector slide = new Vector(vertices[j].getX()-tail.getX(), vertices[j].getY()-tail.getY());
								slide.rotate(deltaAngle);
								allCollisions.add(new Tuple3<RayObject, Vector, Float>(obj, slide, dist));
							}
							//check endpoint 2
							Vector collisionPoint2 = Collisions.lineLineVector(tail.getX(), tail.getY(), vertices[j].getX(), vertices[j].getY(), 
									obj.getX2(), obj.getY2(), obj.getX2()+polygons.get(i).getSecond().getX(), obj.getY2()+polygons.get(i).getSecond().getY());
							EPC++;
							if(collisionPoint2 != null) {
								float dist = Func.dist(collisionPoint2.getX(), collisionPoint2.getY(), obj.getX2(), obj.getY2());
								Vector slide = new Vector(vertices[j].getX()-tail.getX(), vertices[j].getY()-tail.getY());
								slide.rotate(deltaAngle);
								allCollisions.add(new Tuple3<RayObject, Vector, Float>(obj, slide, dist));
							}
							tail = vertices[j];
						}
					}
				}
			}
		}
		
		float maxDist = 0;
		Tuple3<RayObject, Vector, Float> closestCollision = null;
		
		for(Tuple3<RayObject, Vector, Float> col:allCollisions) {
			if(col.getThird()>maxDist) {
				closestCollision = col;
				maxDist = col.getThird();
			}
		}
		
		long et = System.nanoTime();
		Logging.addLog("\tHBC/EPC Checks: " + HBC + "/" + EPC);
		Logging.addLog("\tall collisions: "+(et-st)/1000+"us");
		return closestCollision;
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
						//TODO: Add Stuff
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

	private Polygon getHitboxNewPos(Polygon hitbox, Vector vel) {
		long st = System.nanoTime();
		Vector[] hitboxVertices = hitbox.getVertices();
		Vector[] newPoints = new Vector[hitboxVertices.length];
		for(int i = 0;i < hitboxVertices.length;i++) {
			newPoints[i] = Vector.add(hitboxVertices[i], vel);
			newPoints[i].add(new Vector(this.x, this.y));
		}
		long et = System.nanoTime();
		Logging.addLog("\tpolygon move: "+(et-st)/1000+"us");
		return new Polygon(newPoints);
	}

	private Polygon getHitboxSweep(Polygon hitbox, Vector vel) {

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
		Logging.addLog("\tpolygon sweep: "+(et-st)/1000+"us");
		return new Polygon(newPointsArr);

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
			
			float camPlayerXoffT = (float)(camPlayerXoff*Math.cos(theta)-camPlayerYoff*Math.sin(theta));
			camPlayerYoff = (float)(camPlayerXoff*Math.sin(theta)+camPlayerYoff*Math.cos(theta));
			camPlayerXoff = camPlayerXoffT;

			handler.getCamera().focusOnPoint(tx+PLAYER_WIDTH/2, ty+PLAYER_HEIGHT/2, 0);
			handler.getCamera().move(camPlayerXoff, camPlayerYoff);

			handler.getCamera().setRot(-handler.getWorld().getRotation());

			this.x = tx;
			this.y = ty;

		}
		return vel;
	}
}
