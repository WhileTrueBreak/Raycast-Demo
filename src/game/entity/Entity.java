package game.entity;

import java.awt.Graphics;
import java.util.ArrayList;

import game.Handler;
import utils.Polygon;

public abstract class Entity {
	
	protected Handler handler;
	
	protected Polygon hitbox;
	protected int hitboxRotation = 0;
	
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
	
	public Polygon getHitbox() {
		return hitbox;
	}
	
}
