package slenderMan;

import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.List;

import slenderMan.Player;
import repast.simphony.context.Context;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Slender extends Agent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private boolean moved;

	public Slender(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}

	@Override
	public void setup() {
		addBehaviour(new RunAround(this, 1));
	}

	private class RunAround extends TickerBehaviour {

		public RunAround(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			GridPoint pt = grid.getLocation(this.getAgent());
			
			GridCellNgh<Player> nghCreator = new GridCellNgh<Player>(grid, pt, Player.class, 1, 1);
			List<GridCell<Player>> gridCells = nghCreator.getNeighborhood(true);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

			GridPoint pointWithMostPlayers = null;
			int maxCount = -1;
			for (GridCell<Player> cell : gridCells) {
				if (cell.size() > maxCount) {
					pointWithMostPlayers = cell.getPoint();
					maxCount = cell.size();
				}
			}
			moveTowards(pointWithMostPlayers);
			infect();
		}

	}

	public void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 1, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			moved = true;
		}
	}

	public void infect() {
		GridPoint pt = grid.getLocation(this);
		List<Object> humans = new ArrayList<Object>();
		for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {
			if (obj instanceof Player) {
				humans.add(obj);
			}
		}
		if (humans.size() > 0) {
			int index = RandomHelper.nextIntFromTo(0, humans.size() - 1);
			Object obj = humans.get(index);
			((Player)obj).killPlayer();
		}
	}
}
