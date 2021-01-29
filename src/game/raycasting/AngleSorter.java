package game.raycasting;

import java.util.Comparator;

public class AngleSorter implements Comparator<Ray>{

	@Override
	public int compare(Ray r1, Ray r2) {
		return r2.getTheta()>r1.getTheta()?1:0;
	}

}
