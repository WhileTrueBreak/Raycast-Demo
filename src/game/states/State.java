package game.states;

import java.awt.Graphics;

import game.Handler;

public abstract class State {
	
	private static State current_state = null;
	
	public static State getState() {
		return current_state;
	}

	public static void setState(State current_state) {
		State.current_state = current_state;
	}

	protected Handler handler;
	
	public State(Handler handler) {
		this.handler = handler;
	}
	
	public abstract void update();
	
	public abstract void render(Graphics g);
	
}
