package utils.collision;

import utils.Vector;

public class Collisions {
	public static Boolean lineLine(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
		float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
		if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
			return true;
		}
		return false;
	}
	public static Vector lineLineVector(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
		float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
		if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
			float intersectionX = x1 + (uA * (x2-x1));
		    float intersectionY = y1 + (uA * (y2-y1));
		    return new Vector(intersectionX, intersectionY);
		}
		return null;
	}
	public static Boolean lineCircle(float x1, float y1, float x2, float y2, float cx, float cy, float r) {

		// is either end INSIDE the circle?
		boolean inside1 = pointCircle(x1,y1, cx,cy,r);
		boolean inside2 = pointCircle(x2,y2, cx,cy,r);
		// get length of the line
		float distX = x1 - x2;
		float distY = y1 - y2;
		float len = (float) Math.sqrt( (distX*distX) + (distY*distY) );

		// get dot product of the line and circle
		float dot = (float) ((((cx-x1)*(x2-x1)) + ((cy-y1)*(y2-y1))) / Math.pow(len,2));

		// find the closest point on the line
		float closestX = x1 + (dot * (x2-x1));
		float closestY = y1 + (dot * (y2-y1));
		// is this point actually on the line segment?
		boolean onSegment = linePoint(x1,y1,x2,y2, closestX,closestY);
		if (!onSegment) {
			if(inside1) {
				return true;
			}
			if(inside2) {
				return true;
			}
			return false;
		}

		// get distance to closest point
		distX = closestX - cx;
		distY = closestY - cy;
		float distance = (float) Math.sqrt( (distX*distX) + (distY*distY) );

		if (distance <= r) {
			return true;
		}
		return false;
	}
	public static boolean pointCircle(float px, float py, float cx, float cy, float r) {

		// get distance between the point and circle's center
		// using the Pythagorean Theorem
		float distX = px - cx;
		float distY = py - cy;
		float distance = (float) Math.sqrt( (distX*distX) + (distY*distY) );

		// if the distance is less than the circle's
		// radius the point is inside!
		if (distance <= r) {
			return true;
		}
		return false;
	}
	public static boolean linePoint(float x1, float y1, float x2, float y2, float px, float py) {

		// get distance from the point to the two ends of the line
		float d1 = (float) Math.hypot(px-x1, py-y1);
		float d2 = (float) Math.hypot(px-x2, py-y2);

		// get the length of the line
		float lineLen = (float) Math.hypot(x1-x2, y1-y2);

		// since floats are so minutely accurate, add
		// a little buffer zone that will give collision
		float buffer = 0.1f;    // higher # = less accurate

		// if the two distances are equal to the line's
		// length, the point is on the line!
		// note we use the buffer here to give a range,
		// rather than one #
		if (d1+d2 >= lineLen-buffer && d1+d2 <= lineLen+buffer) {
			return true;
		}
		return false;
	}

	public static boolean lineRect(float x1, float y1, float x2, float y2, float rx, float ry, float rw, float rh) {
		if(pointRect(x1, y1, rx, ry, rw, rh))return true;
		if(pointRect(x2, y2, rx, ry, rw, rh))return true;
		boolean left =   lineLine(x1,y1,x2,y2, rx,ry,rx, ry+rh);
		boolean right =  lineLine(x1,y1,x2,y2, rx+rw,ry, rx+rw,ry+rh);
		boolean top =    lineLine(x1,y1,x2,y2, rx,ry, rx+rw,ry);
		boolean bottom = lineLine(x1,y1,x2,y2, rx,ry+rh, rx+rw,ry+rh);
		if (left || right || top || bottom) return true;
		return false;
	}

	public static boolean pointRect(float x, float y, float rx, float ry, float rw, float rh) {
		if(x>rx&&x<rx+rw&&y>ry&&y<ry+rh) return true;
		return false;
	}

	public static boolean circleCircle(float c1x, float c1y, float c1r, float c2x, float c2y, float c2r) {
		float distX = c1x - c2x;
		float distY = c1y - c2y;
		double distance = Math.sqrt( (distX*distX) + (distY*distY) );
		if (distance <= c1r+c2r) {
			return true;
		}
		return false;
	}
	public static boolean polyRect(Vector[] vertices, float rx, float ry, float rw, float rh) {

		// go through each of the vertices, plus the next
		// vertex in the list
		int next = 0;
		for (int current=0; current<vertices.length; current++) {

			// get next vertex in list
			// if we've hit the end, wrap around to 0
			next = current+1;
			if (next == vertices.length) next = 0;

			// get the PVectors at our current position
			// this makes our if statement a little cleaner
			Vector vc = vertices[current];    // c for "current"
			Vector vn = vertices[next];       // n for "next"

			// check against all four sides of the rectangle
			boolean collision = lineRect(vc.getX(),vc.getY(),vn.getX(),vn.getY(), rx,ry,rw,rh);
			if (collision) return true;

			// optional: test if the rectangle is INSIDE the polygon
			// note that this iterates all sides of the polygon
			// again, so only use this if you need to
			boolean inside = polygonPoint(vertices, rx,ry);
			if (inside) return true;
		}
		return false;
	}
	public static boolean polygonPoint(Vector[] vertices, float px, float py) {
		boolean collision = false;

		// go through each of the vertices, plus the next
		// vertex in the list
		int next = 0;
		for (int current=0; current<vertices.length; current++) {

			// get next vertex in list
			// if we've hit the end, wrap around to 0
			next = current+1;
			if (next == vertices.length) next = 0;

			// get the PVectors at our current position
			// this makes our if statement a little cleaner
			Vector vc = vertices[current];    // c for "current"
			Vector vn = vertices[next];       // n for "next"

			// compare position, flip 'collision' variable
			// back and forth
			if (((vc.getY() > py && vn.getY() < py) || (vc.getY() < py && vn.getY() > py)) &&
					(px < (vn.getX()-vc.getX())*(py-vc.getY()) / (vn.getY()-vc.getY())+vc.getX())) {
				collision = !collision;
			}
		}
		return collision;
	}
}
