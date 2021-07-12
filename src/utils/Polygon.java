package utils;

import java.awt.geom.Rectangle2D;

public class Polygon {
	
	Vector[] vertices;
	Rectangle2D.Float boundingRect;
	
	public Polygon(Vector[] vertices, int vertexCount) {
		this.vertices = vertices;
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
	
}
