package slenderMan;

import java.util.ArrayList;
import java.util.List;

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

public class Slender extends Agent {
	private static final double SLENDER_SPEED = 1;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	static final int BIG_RADIUS = 7;
	static final int SMALL_RADIUS = 3;

	public Slender(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}

	@Override
	public void setup() {
		addBehaviour(new RunAround(this, 1));
	}

	private class RunAround extends TickerBehaviour {

		private static final long serialVersionUID = 1L;

		public RunAround(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			GridPoint pt = grid.getLocation(this.getAgent());
			GridPoint pointWithMostPlayers = getPointWithMostPlayerWithPhone(pt);
			if(pointWithMostPlayers == null) {
			}
			moveTowards(pointWithMostPlayers);
			infect();
		}

	}
	public GridPoint getPointWithMostPlayerWithPhone(GridPoint pt) {
		GridPoint pt_res = null;
		GridCellNgh<Player> nghCreator = new GridCellNgh<Player>(grid, pt, Player.class, BIG_RADIUS, BIG_RADIUS);
		List<GridCell<Player>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

		gridCells = takeout_players_without_phone(gridCells);

		return pt_res;
	}
	public GridPoint getPointWithMostPlayerWithoutPhone() {
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
		GridPoint pointWithMostPlayers = null;
		int maxCount = -1;
		for (GridCell<Player> cell : gridCells) {
			if (cell.size() > maxCount) {
				pointWithMostPlayers = cell.getPoint();
				maxCount = cell.size();
			}
		}
		return null;
	}

	private List<GridCell<Player>> takeout_players_without_phone(List<GridCell<Player>> gridCells) {
		// TODO Auto-generated method stub
		return null;
	}

	public void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			double dist = space.getDistance(myPoint, otherPoint);
			dist = (dist < SLENDER_SPEED) ? dist : SLENDER_SPEED;
			space.moveByVector(this, 1, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
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
			System.out.println("Killing player!");
			((Player) obj).killPlayer();
		}
	}
}
