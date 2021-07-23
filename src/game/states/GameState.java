package game.states;

import java.awt.Graphics;

import game.Handler;
import game.world.World;

public class GameState extends State{
	
	private World world;
	
	public GameState(Handler handler) {
		super(handler);
		handler.getCamera().setScale(handler.getWidth()/4);
		world = new World(handler);
	}
	
	@Override
	public void update() {
		world.update();
	}

	@Override
	public void render(Graphics g) {
		world.render(g);
	}

}
