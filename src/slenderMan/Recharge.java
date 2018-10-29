package slenderMan;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Recharge {

		private static final int RECHARGE_ENERGY = 20;
		private static final int MAX_ENERGY = 100;
		private ContinuousSpace<Object> space;
		private Grid<Object> grid;
		private int id;

		public Recharge(ContinuousSpace<Object> space, Grid<Object> grid, int id) {
			this.space = space;
			this.grid = grid;
			this.id = id;
		}
		public void recharge(Player p) {
			GridPoint pt = grid.getLocation(this);
			GridPoint p_pt = grid.getLocation(p);

			if (pt == p_pt) { // TODO: teste if the if is like this, or not
				System.out.println("Recharging energy");
				if(p.getEnergy() + RECHARGE_ENERGY < MAX_ENERGY)
				p.setEnergy(p.getEnergy() + RECHARGE_ENERGY);
				else
					p.setEnergy(MAX_ENERGY);
			}
		}
}
