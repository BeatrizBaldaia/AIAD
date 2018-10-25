package slenderMan;

import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;

public class Tower extends Agent {
	static final int MAX_DEVICE_TIME = 200;
	private Device[] dev = new Device[8];
	private Player[] players;
	
	public Tower(ContinuousSpace<Object> space, Grid<Object> grid, Context<Object> context, Player[] players) {
		this.players = players;
		for(int i = 0; i < dev.length; i++) {
			dev[i] = new Device(space, grid, i);
			context.add(dev[i]);
		}
	}

	@Override
	public void setup() {
		addBehaviour(new CheckDevices(this, 1));
	}
	
	private class CheckDevices extends TickerBehaviour {

		public CheckDevices(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			for(int i = 0; i < dev.length; i++) {
				if(!dev[i].isOn()) {
					dev[i].decreaseTimer();
					if(dev[i].getTime() == 0) {
						dev[i].setOn(false);
						dev[i].setTime(MAX_DEVICE_TIME);
					}

				}
			}
			boolean endGame = true;
			for(int i = 0; i < players.length; i++) {
				if(players[i].isAlive()) {
					endGame = false;
					continue;
				}
			}
			if(endGame) {
				System.out.println("EndGame");
				RunEnvironment.getInstance().endRun();
			}
		}

	}
}
