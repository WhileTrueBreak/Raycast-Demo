package game.entity;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import game.Handler;

public abstract class Entity {
	
	protected Handler handler;
	
	protected Rectangle2D hitbox;
	
	protected float x, y;
	
	public Entity(float x, float y, Handler handler) {
		this.handler = handler;
		this.x = x;
		this.y = y;
	}
	
	public abstract void update();
	
	public abstract void render(Graphics g);

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
	
	public Rectangle2D getHitbox() {
		return hitbox;
	}
	
}
