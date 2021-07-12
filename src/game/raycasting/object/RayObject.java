package game.raycasting.object;

import java.awt.Graphics;

import game.Handler;

public abstract class RayObject {
	
	public boolean isSolid = false;

	protected Handler handler;
	protected float x1, y1, x2, y2;
	
	protected boolean transposeRay = false;

	public RayObject(Handler handler, float x1, float y1, float x2, float y2){
		this.handler = handler;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public abstract void render(Graphics g);
	public abstract void update();

	public float getX1(){
		return x1;
	}

	public float getY1(){
		return y1;
	}

	public float getX2(){
		return x2;
	}

	public float getY2(){
		return y2;
	}

	public void setX1(float x1) {
		this.x1 = x1;
	}

	public void setY1(float y1) {
		this.y1 = y1;
	}

	public void setX2(float x2) {
		this.x2 = x2;
	}

	public void setY2(float y2) {
		this.y2 = y2;
	}

	public boolean canTransposeRay() {
		return transposeRay;
	}
	
}
