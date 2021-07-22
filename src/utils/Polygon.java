package utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import game.Handler;

public class Polygon {
	
	private Vector[] vertices;
	private Rectangle2D.Float boundingRect;
	
	private int vertexCount;
	
	public Polygon(Vector[] vertices) {
		this.vertices = vertices;
		this.vertexCount = vertices.length;
		float minx = Float.POSITIVE_INFINITY, miny = Float.POSITIVE_INFINITY;
		float maxx = Float.NEGATIVE_INFINITY, maxy = Float.NEGATIVE_INFINITY;
		for(Vector vertex:vertices) {
			float vx = vertex.getX();
			float vy = vertex.getY();
			if(minx>vx) {
				minx = vx;
			}if(maxx<vx) {
				maxx = vx;
			}if(miny>vy) {
				miny = vy;
			}if(maxy<vy) {
				maxy = vy;
			}
		}
		boundingRect = new Rectangle2D.Float(minx, miny, maxx-minx, maxy-miny);
	}
	
	public void render(Handler handler, Graphics g) {
		Vector tail = vertices[vertexCount-1];
		for(Vector head:vertices) {
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.drawLine((int)(tail.getX()*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
					   (int)(tail.getY()*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
					   (int)(head.getX()*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
					   (int)(head.getY()*handler.getCamera().getScale()-handler.getCamera().getYoff()));
			tail = head;
		}
	}

	public Vector[] getVertices() {
		return vertices;
	}
	
	public Vector[] copyVertices() {
		Vector[] copy = new Vector[vertices.length];
		for(int i = 0;i < vertices.length;i++) {
			copy[i] = new Vector(vertices[i].getX(), vertices[i].getY());
		}
		return copy;
	}
	
	public Rectangle2D.Float getBoundingRect(){
		return boundingRect;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
}
