package game.raycasting.object;

import java.awt.Color;
import java.awt.Graphics;

import game.Handler;

public class RayPortal extends RayObject{
	
	private RayPortal linkedPortal;
	
	public RayPortal(Handler handler, float x1, float y1, float x2, float y2) {
		super(handler, x1, y1, x2, y2);
		transposeRay = true;
	}
	
	public RayPortal(Handler handler, float x1, float y1, float x2, float y2, RayPortal linkedPortal) {
		super(handler, x1, y1, x2, y2);
		transposeRay = true;
		this.linkedPortal = linkedPortal;
		linkedPortal.setLinkedPortal(this);
	}

	@Override
	public void render(Graphics g) {
		g.setColor(new Color(255, 0, 255));
		g.drawLine((int)(x1*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				   (int)(y1*handler.getCamera().getScale()-handler.getCamera().getYoff()), 
				   (int)(x2*handler.getCamera().getScale()-handler.getCamera().getXoff()), 
				   (int)(y2*handler.getCamera().getScale()-handler.getCamera().getYoff()));
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	public RayPortal getLinkedPortal() {
		return linkedPortal;
	}

	public void setLinkedPortal(RayPortal linkedPortal) {
		this.linkedPortal = linkedPortal;
	}

}
