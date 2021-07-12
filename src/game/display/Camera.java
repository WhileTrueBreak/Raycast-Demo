package game.display;

import game.Handler;
import game.entity.Entity;

public class Camera {
	
	private Handler handler;
	
	//relative to top left (0,0)
	private float xoff, yoff;
	private float rot;
	private float scale;
	
	private float PoFx, PoFy;
	
	public Camera(Handler handler, float xoff, float yoff) {
		this.handler = handler;
		this.xoff = xoff;
		this.yoff = yoff;
		this.scale = 1;
		this.rot = 0;
	}
	
	public void focusOnEntity(Entity e, float spring) {
		float cameraSpring = spring;
		PoFx = (e.getX()+(float)e.getHitbox().getBoundingRect().getWidth()/2)*scale - handler.getWidth()/2;
		PoFy = (e.getY()+(float)e.getHitbox().getBoundingRect().getHeight()/2)*scale - handler.getHeight()/2;
		if(cameraSpring <= 0)
			move((PoFx-xoff), (PoFy-yoff));
		else {
			move(((PoFx-xoff)/cameraSpring)/(float)handler.getCurrentFps(), 
					((PoFy-yoff)/cameraSpring)/(float)handler.getCurrentFps());
		}
	}
	
	public void focusOnPoint(float x, float y, float spring) {
		float cameraSpring = spring;
		PoFx = x*scale - handler.getWidth()/2;
		PoFy = y*scale - handler.getHeight()/2;
		if(cameraSpring <= 0)
			move((PoFx-xoff), (PoFy-yoff));
		else
			move(((PoFx-xoff)/cameraSpring)/(float)handler.getCurrentFps(), 
					((PoFy-yoff)/cameraSpring)/(float)handler.getCurrentFps());
	}
	
	public void move(float amtx, float amty) {
		xoff += amtx;
		yoff += amty;
	}

	public float getXoff() {
		float dx = xoff-PoFx;
		float dy = yoff-PoFy;
		return (float)(dx*Math.cos(rot)-dy*Math.sin(rot))+PoFx;
	}

	public void setXoff(int xoff) {
		this.xoff = xoff;
	}

	public float getYoff() {
		float dx = xoff-PoFx;
		float dy = yoff-PoFy;
		return (float)(dx*Math.sin(rot)+dy*Math.cos(rot))+PoFy;
	}

	public void setYoff(int yoff) {
		this.yoff = yoff;
	}
	
	public float getScale() {
		return scale;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
	}

	public float getRot() {
		return rot;
	}

	public void setRot(float rot) {
		this.rot = rot;
	}
	
}
