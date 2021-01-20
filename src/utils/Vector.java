package utils;

public class Vector {
	
	private float x, y;
	
	public Vector(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	public Vector mult(float n) {
		return new Vector(this.x*n, this.y*n);
	}
	
	public Vector sub(Vector v) {
		return new Vector(this.x-v.getX(), this.y*v.getY());
	}
	
}
