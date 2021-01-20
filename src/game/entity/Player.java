package game.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import game.Handler;
import game.inputs.Binds;
import game.raycasting.RayEmitter;
import utils.Vector;

public class Player extends Entity{
	
	private final static float PLAYER_WIDTH = 0.4f;
	private final static float PLAYER_HEIGHT = 0.4f;
	
	private final static float PLAYER_MOVE_SPEED = 3.7f;

	private RayEmitter rayEmitter;
	
	public Player(float x, float y, Handler handler) {
		super(x, y, handler);
		hitbox = new Rectangle2D.Float(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT);
		rayEmitter = new RayEmitter(handler, x+PLAYER_WIDTH/2, y+PLAYER_HEIGHT/2);
	}

	@Override
	public void update() {
		rayEmitter.setX(x+PLAYER_WIDTH/2);
		rayEmitter.setY(y+PLAYER_HEIGHT/2);
		rayEmitter.setRayObjects(handler.getWorld().getRayObjects());
		rayEmitter.updateRays();
		move();
	}
	
	@Override
	public void render(Graphics g) {
		rayEmitter.render(g);
		g.setColor(new Color(0, 255, 0));
		g.fillRect((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				   (int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
				   (int)(PLAYER_WIDTH*handler.getCamera().getScale()), 
				   (int)(PLAYER_HEIGHT*handler.getCamera().getScale()));
	}
	
	private void move() {
		Vector vel = new Vector(0, 0);
		if(handler.getKeyManager().isKeyPressed(Binds.LEFT)) {
			vel.add(new Vector(-1, 0));
		}if(handler.getKeyManager().isKeyPressed(Binds.RIGHT)) {
			vel.add(new Vector(1, 0));
		}if(handler.getKeyManager().isKeyPressed(Binds.UP)) {
			vel.add(new Vector(0, -1));
		}if(handler.getKeyManager().isKeyPressed(Binds.DOWN)) {
			vel.add(new Vector(0, 1));
		}
		vel.normalise();
		vel.mult((float) (PLAYER_MOVE_SPEED/handler.getCurrentFps()));
		x+=vel.getX();
		y+=vel.getY();
	}
	
}
