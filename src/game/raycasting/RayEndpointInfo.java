package game.raycasting;

import game.raycasting.object.RayObject;
import utils.Vector;

public class RayEndpointInfo {

	private RayObject obj;
	private int id;
	private int depth;
	private Vector pos;
	
	public RayEndpointInfo(RayObject obj, Vector pos, int depth, int id) {
		this.obj = obj;
		this.pos = pos;
		this.depth = depth;
		this.id = id;
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
