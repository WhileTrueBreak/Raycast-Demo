package game.raycasting;

public class RayThread implements Runnable {

	private boolean isRunning = true;

	private Ray ray;
	private boolean done = false;

	public RayThread(Ray ray) {
		this.ray = ray;
	}
	
	@Override
	public void run() {
		//System.out.println("start");
		ray.update();
		done = true;
		//System.out.println("stop");
	}

	public void kill() {
		isRunning = false;
	}

	public Ray getRay() {
		return ray;
	}

	public boolean isDone() {
		return done;
	}

}