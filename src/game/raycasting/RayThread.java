package game.raycasting;

public class RayThread implements Runnable {

	private Ray ray;
	private boolean done = false;

	public RayThread(Ray ray) {
		this.ray = ray;
	}
	
	@Override
	public void run() {
		ray.update();
		done = true;
	}

	public Ray getRay() {
		return ray;
	}

	public boolean isDone() {
		return done;
	}

}