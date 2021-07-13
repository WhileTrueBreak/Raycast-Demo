package game.raycasting;

import game.raycasting.object.RayObject;
import utils.Vector;

public class RayEndpointInfo {

	private RayObject obj;
	private RayObject last;
	private int id;
	private int depth;
	private Vector pos;
	
	public RayEndpointInfo(RayObject obj, Vector pos, int depth, int id, RayObject last) {
		this.obj = obj;
		this.pos = pos;
		this.depth = depth;
		this.id = id;
		this.last = last;
	}

	public RayObject getLast() {
		return last;
	}

	public RayObject getObj() {
		return obj;
	}

	public int getId() {
		return id;
	}

	public int getDepth() {
		return depth;
	}

	public Vector getPos() {
		return pos;
	}
	
}
