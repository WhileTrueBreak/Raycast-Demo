package game.world;

import java.awt.Graphics;

import game.Handler;
import game.entity.Player;
import game.raycasting.RayEmitter;

public class World {

	private Player player;
	private RayEmitter rayEmitter;
	
	private Handler handler;

	public World(Handler handler) {
		this.handler = handler;
		handler.setWorld(this);
		rayEmitter = new RayEmitter(handler, 5, 5);
		
		//player = new Player(5, 1, handler);
	}

	public void update() {
		rayEmitter.updateRays();
		//player.update();
	}

	public void render(Graphics g) {
		rayEmitter.render(g);
		//player.render(g);
	}
}
