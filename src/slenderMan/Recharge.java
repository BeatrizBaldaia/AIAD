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

			if (pt.getX() == p_pt.getX() && pt.getY() == p_pt.getY()) { 
				if(p.getMobileBattery() + RECHARGE_ENERGY < MAX_ENERGY)
				p.setMobileBattery(p.getMobileBattery() + RECHARGE_ENERGY);
				else
					p.setMobileBattery(MAX_ENERGY);
			}
		}
}
