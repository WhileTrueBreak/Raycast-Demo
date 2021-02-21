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
import utils.Vector;
import utils.collision.Collisions;

public class Player extends Entity{

	private final static float PLAYER_WIDTH = 0.4f;
	private final static float PLAYER_HEIGHT = 0.4f;

	private final static float PLAYER_MOVE_SPEED = 3.7f;

	private RayEmitter rayEmitter;

	public Player(float x, float y, Handler handler) {
		super(x, y, handler);
		hitbox = new Rectangle2D.Float(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT);
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
		//collisions
		vel = collisions(vel);
		//portal collisions
		vel = portalCollisions(vel);
		x+=vel.getX();
		y+=vel.getY();
	}
	
	private Vector collisions(Vector vel) {
		
		return vel;
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
