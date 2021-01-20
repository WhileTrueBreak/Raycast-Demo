package game.raycasting.object;

import java.awt.Color;
import java.awt.Graphics;

import game.Handler;

public class RayWall extends RayObject{

	private Color color;
	
	public RayWall(Handler handler, float x1, float y1, float x2, float y2, Color color) {
		super(handler, x1, y1, x2, y2);
		this.color = color;
	}

	@Override
	public void render(Graphics g) {
		g.setColor(color);
		g.drawLine((int)(x1*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				   (int)(y1*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
				   (int)(x2*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				   (int)(y2*handler.getCamera().getScale()-handler.getCamera().getYoff()));
	}

	@Override
	public void update() {
		
	}

}
