package game.display;

import game.Handler;
import game.entity.Entity;

public class Camera {
	
	private Handler handler;
	
	private float xoff, yoff;
	private float scale;
	
	public Camera(Handler handler, float xoff, float yoff) {
		this.handler = handler;
		this.xoff = xoff;
		this.yoff = yoff;
		this.scale = 1;
	}
	
	public void focusOnEntity(Entity e, int spring) {
		float cameraSpring = spring;
		float setX = (e.getX()+(float)e.getHitbox().getWidth()/2)*scale - handler.getWidth()/2;
		float setY = (e.getY()+(float)e.getHitbox().getHeight()/2)*scale - handler.getHeight()/2;
		if(cameraSpring <= 0)
			move((setX-xoff), (setY-yoff));
		else
			move((setX-xoff)/cameraSpring, (setY-yoff)/cameraSpring);
	}
	
	public void focusOnPoint(int x, int y, int spring) {
		float cameraSpring = spring;
		float setX = x*scale - handler.getWidth()/2;
		float setY = y*scale - handler.getHeight()/2;
		if(cameraSpring <= 0)
			move((setX-xoff), (setY-yoff));
		else
			move((setX-xoff)/cameraSpring, (setY-yoff)/cameraSpring);
	}
	
	public void move(float amtx, float amty) {
		xoff += amtx;
		yoff += amty;
	}

	public float getXoff() {
		return xoff;
	}

	public void setXoff(int xoff) {
		this.xoff = xoff;
	}

	public float getYoff() {
		return yoff;
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
	
}
