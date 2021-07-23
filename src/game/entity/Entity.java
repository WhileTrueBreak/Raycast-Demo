package game.entity;

import java.awt.Graphics;

import game.Handler;
import utils.Polygon;
import utils.Vector;

public abstract class Entity {
	
	protected Handler handler;
	
	protected Polygon hitbox;
	protected int hitboxRotation = 0;

	protected float x, y;
	protected float width, height;
	protected float entityRelativeAngle = 0;
	
	public Entity(float x, float y, float width, float height,  Handler handler) {
		this.handler = handler;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
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
	
	protected Polygon getRelativeHitbox() {
		Vector[] vertices = this.hitbox.copyVertices();
		Vector[] newVertices = new Vector[vertices.length];
		for(int i = 0;i < vertices.length;i++) {
			vertices[i].sub(new Vector(width/2, height/2));
			vertices[i].rotate(entityRelativeAngle);
			vertices[i].add(new Vector(width/2, height/2));
			newVertices[i] = vertices[i];
		}
		return new Polygon(newVertices);
	}
	
}
