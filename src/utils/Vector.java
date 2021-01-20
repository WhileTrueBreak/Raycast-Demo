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
	
	public void mult(float n) {
		this.x*=n;
		this.y*=n;
	}
	
	public void sub(Vector v) {
		this.x-=v.getX();
		this.y-=v.getY();
	}
	
	public void add(Vector v) {
		this.x+=v.getX();
		this.y+=v.getY();
	}
	
	public void normalise() {
		double mag = Math.sqrt(x*x+y*y);
		if(mag!=0) {
			this.x/=mag;
			this.y/=mag;
		}
	}
	
	public static Vector sub(Vector v1, Vector v2) {
		return new Vector(v1.getX()-v2.getX(), v1.getY()-v2.getY());
	}
	
	public static Vector add(Vector v1, Vector v2) {
		return new Vector(v1.getX()+v2.getX(), v1.getY()+v2.getY());
	}
	
	public static Vector mult(Vector v1, float n) {
		return new Vector(v1.getX()*n, v1.getY()*n);
	}
	
	
}
