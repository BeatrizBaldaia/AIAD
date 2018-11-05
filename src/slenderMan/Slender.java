package slenderMan;

import java.util.ArrayList;
import java.util.Iterator;
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
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.TickerBehaviour;

public class Slender extends Agent {
	private static double RUNNING_SPEED = 1.3;
	private static double WALKING_SPEED = 1;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	static int RADIUS = 3;

	public Slender(ContinuousSpace<Object> space, Grid<Object> grid, int radius, double running, double walking) {
		this.space = space;
		this.grid = grid;
		RADIUS = radius;
		RUNNING_SPEED = running;
		WALKING_SPEED = walking;
	}

	@Override
	public void setup() {
		addBehaviour(new Hunting(this));
	}

	private class Hunting extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		public Hunting(Agent a) {
			super(a);
		}

			@Override
			public void action() {
				Slender agent = (Slender)this.getAgent();
				GridPoint myPt = grid.getLocation(agent);
				
				Player player = agent.getNearestPlayer(myPt);
			Player playerWithPhone = agent.getNearestPlayerWithPhone(myPt);
			Player prey = agent.choosePrey(player, playerWithPhone, myPt);
			
			if(prey == null) {
				agent.randomMove(myPt);
			} else {
				agent.moveTowards(myPt, grid.getLocation(prey), RUNNING_SPEED);
			}
			
			kill();
		}



	}	
	
	public Player getNearestPlayer(GridPoint myPt) {
		GridCellNgh<Player> nghCreator = new GridCellNgh<Player>(grid, myPt, Player.class, RADIUS, RADIUS);
		List<GridCell<Player>> gridCells = nghCreator.getNeighborhood(true);

		double nearestPlayerDist = Double.MAX_VALUE;
		Player nearestPlayer = null;
		for (GridCell<Player> cell : gridCells) {
			if (cell.size() != 0) {
				GridPoint otherPt = cell.getPoint();
				double dist = grid.getDistance(myPt, otherPt);
				if(dist < nearestPlayerDist) {
					nearestPlayerDist = dist;
					nearestPlayer = cell.items().iterator().next();
				}
			}
		}
		return nearestPlayer;
	}

	public Player getNearestPlayerWithPhone(GridPoint myPt) {
		double nearestPlayerWithPhoneDist = Double.MAX_VALUE;
		Player nearestPlayerWithPhone = null;

		Iterator<Object> iter = grid.getObjects().iterator();
		while(iter.hasNext()) {
			Object element = iter.next();
			if(element.getClass() == Player.class) {
				if(((Player)element).isMobileOn()) {
					GridPoint otherPt = grid.getLocation((Player)element);
					double dist = grid.getDistance(myPt, otherPt);
					if(dist < nearestPlayerWithPhoneDist) {
						nearestPlayerWithPhoneDist = dist;
						nearestPlayerWithPhone = (Player)element;
					}
				}
			}
		}
		
		return nearestPlayerWithPhone;
	}
	
	public Player choosePrey(Player nearestPlayer, Player nearestPlayerWithMobile, GridPoint myPt) {
		if(nearestPlayer == null) {
			if(nearestPlayerWithMobile == null) {
				return null;
			} else {
				return nearestPlayerWithMobile;
			} 
		} else if (nearestPlayerWithMobile == null) {
			return nearestPlayer;
		}
		
		double dist1 = grid.getDistance(myPt, grid.getLocation((Player)nearestPlayer));
		double dist2 = grid.getDistance(myPt, grid.getLocation((Player)nearestPlayerWithMobile));
		
		if(dist1 < dist2/2) {
			return nearestPlayer;
		}
		return nearestPlayer;
		
	}
	
	public void randomMove(GridPoint myPt) {
		GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, myPt, Object.class, RADIUS,
				RADIUS);
		List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(false);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		GridPoint otherPoint = gridCells.get(0).getPoint();
		moveTowards(myPt, otherPoint, WALKING_SPEED);
	}
	
	
	public void moveTowards(GridPoint src, GridPoint dest, double speed) {
		// only move if we are not already in this grid location
		if (!dest.equals(src)) {
			NdPoint myPt = new NdPoint(src.getX(), src.getY());
			NdPoint otherPt = new NdPoint(dest.getX(), dest.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPt, otherPt);
			double dist = space.getDistance(myPt, otherPt);
			if(dist < speed) {
				space.moveTo(this, otherPt.getX(), otherPt.getY());
			} else {
				space.moveByVector(this, speed, angle, 0);
			}
			
			myPt = space.getLocation(this);
			grid.moveTo(this, (int) myPt.getX(), (int) myPt.getY());
		}
	}

	public void kill() {
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
			((Player) obj).die();
		}
	}
}
