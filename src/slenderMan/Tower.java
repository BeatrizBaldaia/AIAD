package slenderMan;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;

public class Tower extends Agent {
	static final int MAX_DEVICE_TIME = 200;
	static final int NUMBER_OF_DEVICES = 8;
	private Device[] dev = new Device[NUMBER_OF_DEVICES];
	private Player[] players;

	public Tower(ContinuousSpace<Object> space, Grid<Object> grid, Context<Object> context, Player[] players) {
		this.players = players;
		for (int i = 0; i < dev.length; i++) {
			dev[i] = new Device(space, grid, i);
			context.add(dev[i]);
			Recharge r = new Recharge(space, grid, i);
			context.add(r);
		}
		
////		TODO: delete test de know all devices
//		for (int i = 0; i < players.length; i++) {
//			Device[] dev_test = new Device[NUMBER_OF_DEVICES];
//			for(int j=0;j<dev.length;j++)
//			dev_test[j] = new Device(space.getLocation(dev[j]), grid);
//			players[i].setDev(dev_test);
//		}
	}

	@Override
	public void setup() {
		addBehaviour(new CheckDevices(this, 1));
	}

	private class CheckDevices extends TickerBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CheckDevices(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			for (int i = 0; i < dev.length; i++) {
				if (!dev[i].isOn()) {
					dev[i].decreaseTimer();
					if (dev[i].getTime() == 0) {
						dev[i].setOn(true);
						dev[i].setTime(Tower.MAX_DEVICE_TIME);
						System.out.println(dev[i].getTime());
					}

				}
			}
			boolean endGame = true;
			for (int i = 0; i < players.length; i++) {
				if (players[i].isAlive()) {
					endGame = false;
					continue;
				}
			}
			boolean endGameWin = true;
			for (int i = 0; i < dev.length; i++) {
				if (dev[i].isOn()) {
					endGameWin = false;
					continue;
				}
			}
			if (endGame || endGameWin) {
				System.out.println("EndGame");
				RunEnvironment.getInstance().endRun();
			}
		}

	}
}
