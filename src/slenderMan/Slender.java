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
	private static final double SLENDER_SPEED = 3;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	static final int BIG_RADIUS = 10;
	static final int SMALL_RADIUS = 5;

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
			GridPoint pointWithNearestPlayer = null;
			GridPoint pt = grid.getLocation(this.getAgent());
			List<Player> players = getPlayerWithPhone(pt);
			if (players.size() == 0) {
				players = getPlayerWithoutPhone(pt);
			}
			if(players.size() != 0) {
				pointWithNearestPlayer = getPointWithNearestPlayer(players);
			} else {
				GridCellNgh<Player> nghCreator = new GridCellNgh<Player>(grid, pt, Player.class, SMALL_RADIUS, SMALL_RADIUS);
				List<GridCell<Player>> gridCells = nghCreator.getNeighborhood(true);
				pointWithNearestPlayer = gridCells.get(RandomHelper.nextIntFromTo(0, gridCells.size()-1)).getPoint();
			}
			moveTowards(pointWithNearestPlayer);
			infect();
		}



	}
	private List<Player> getPlayerWithoutPhone(GridPoint pt) {
		GridCellNgh<Player> nghCreator = new GridCellNgh<Player>(grid, pt, Player.class, SMALL_RADIUS, SMALL_RADIUS);
		List<GridCell<Player>> gridCells = nghCreator.getNeighborhood(true);

		List<Player> players = new ArrayList<Player>();
		for (GridCell<Player> cell : gridCells) {
			for (Player p : cell.items()) {
					players.add(p);
			}
		}
		return players;
	}
	public GridPoint getPointWithNearestPlayer(List<Player> players) {
		double nearestPlayer = Double.MAX_VALUE;
		NdPoint pt_res = null;
		NdPoint pt_me = space.getLocation(this);
		for (Player p : players) {
			NdPoint pt_p = space.getLocation(p);
			double dist = space.getDistance(pt_me, pt_p);
			if(dist < nearestPlayer) {
				nearestPlayer = dist;
				pt_res = pt_p;
			}
		}
		return new GridPoint((int)pt_res.getX(),(int)pt_res.getY());
	}

	public List<Player> getPlayerWithPhone(GridPoint pt) {
		GridCellNgh<Player> nghCreator = new GridCellNgh<Player>(grid, pt, Player.class, BIG_RADIUS, BIG_RADIUS);
		List<GridCell<Player>> gridCells = nghCreator.getNeighborhood(true);
		List<Player> players = new ArrayList<Player>();
		for (GridCell<Player> cell : gridCells) {
			for (Player p : cell.items()) {
				if (p.isMobileOn()) {
					players.add(p);
				}
			}
		}
		return players;
	}
	
	public void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			double dist = space.getDistance(myPoint, otherPoint);
			dist = (dist < SLENDER_SPEED) ? dist : SLENDER_SPEED;
			space.moveByVector(this, dist, angle, 0);
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
