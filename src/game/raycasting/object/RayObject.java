package game.raycasting.object;

import java.awt.Graphics;

import game.Handler;

public abstract class RayObject {
	
	protected Handler handler;
	protected float x1, y1, x2, y2;
	  
	  public RayObject(Handler handle, float x1, float y1, float x2, float y2){
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
}