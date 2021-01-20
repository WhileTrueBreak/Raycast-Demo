package game.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import game.Handler;

public class Player extends Entity{
	
	private final static float PLAYER_WIDTH = 0.4f;
	private final static float PLAYER_HEIGHT = 0.4f;
	
	private final static float PLAYER_MOVE_SPEED = 3.7f;
	
	public Player(float x, float y, Handler handler) {
		super(x, y, handler);
		hitbox = new Rectangle2D.Float(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT);
	}

	@Override
	public void update() {
	}
	
	@Override
	public void render(Graphics g) {
		g.setColor(new Color(0, 255, 0));
		g.fillRect((int)(x*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				(int)(y*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
				(int)(PLAYER_WIDTH*handler.getCamera().getScale()), (int)(PLAYER_HEIGHT*handler.getCamera().getScale()));
	}
}
