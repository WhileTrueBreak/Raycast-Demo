package utils;

public class Func {
	public static float dist(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
	public static float radians(float deg) {
		return (float) (deg*Math.PI/180);
	}
	public static float degrees(float rad) {
		return (float) (rad*180/Math.PI);
	}
	public static float map(float in, float in_min, float in_max, float min, float max) {
		if(in <= in_min)
			return in_min;
		if(in >= in_max)
			return in_max;
		return in_min + ((in_max - in_min) / (max - min)) * (in - min);
	}
}
