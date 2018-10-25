package slenderMan;

import java.util.Iterator;
import java.util.List;

import bsh.This;
import slenderMan.Slender;
import repast.simphony.engine.watcher.DefaultWatchData;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.Watcher2;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.query.space.grid.VNQuery;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;

public class Player extends Agent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int energy, startingEnergy;
	VNQuery<Object> nearSlender;
	public Player(ContinuousSpace<Object> space, Grid<Object> grid, int energy) {
		this.space = space;
		this.grid = grid;
		this.energy = startingEnergy = energy;
	}

	@Override
	public void setup() {
		addBehaviour(new RunAround(this, 1));
	}

	private class RunAround extends TickerBehaviour {

		public RunAround(Agent a, long period) {
			super(a, period);
			nearSlender = new VNQuery<Object>(grid, this.myAgent, 2,2);
		}

		@Override
		protected void onTick() {
			//@Watch(watcheeClassName = "slenderMan.Slender", watcheeFieldNames = "moved", query = "within_vn 1", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
			boolean s = false;
			Iterable<Object> objs = nearSlender.query();
			Iterator<Object> iter = objs.iterator();
			while(iter.hasNext()){
				if(iter.next().getClass() == Slender.class) {
					s = true;
					System.out.println("NearSlender");
					continue;
				}
			}
			if(!s) {
				return;
			}
			// get the grid location of this Human
			GridPoint pt = grid.getLocation(this.getAgent());

			// use the GridCellNgh class to create GridCells for
			// the surrounding neighborhood.
			GridCellNgh<Slender> nghCreator = new GridCellNgh<Slender>(grid, pt, Slender.class, 1, 1);
			List<GridCell<Slender>> gridCells = nghCreator.getNeighborhood(true);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

			GridPoint pointWithLeastSlenders = null;
			int minCount = Integer.MAX_VALUE;
			for (GridCell<Slender> cell : gridCells) {
				if (cell.size() < minCount) {
					pointWithLeastSlenders = cell.getPoint();
					minCount = cell.size();
				}
			}

			if (energy > 0) {
				moveTowards(pointWithLeastSlenders);
			} else {
				energy = startingEnergy;
			}
		}
	}

	public void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 2, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			// energy--;
		}
	}
}
