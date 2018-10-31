package slenderMan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.query.space.grid.MooreQuery;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public class Player extends Agent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int energy;
	private int startingEnergy;
	private int distBattery;

	private int lightPeriod = 0;
	private int darknessPeriod = 0;
	private GridPoint targetPoint = null;
	private boolean mobileOn = false;
	private int mobileBattery = 100;
	private ArrayList<Device> knownDevices = new ArrayList<Device>();
	private boolean knowAllDevices = false;

	static final int BIG_RADIUS = 7;
	static final int SMALL_RADIUS = 3;
	static final int PLAYER_SPEED = 1;

	MooreQuery<Object> nearSlender;
	private boolean alive;
	private ArrayList<Device> claimedDevices = new ArrayList<Device>();;

	public Player(ContinuousSpace<Object> space, Grid<Object> grid, int energy) {
		this.space = space;
		this.grid = grid;
		this.setEnergy(startingEnergy = energy);
	}

	@Override
	public void setup() {
		addBehaviour(new Exploring(this));
		alive = true;
	}
	public int getLightPeriod() {
		return this.lightPeriod;
	}

	public int getDarknessPeriod() {
		return this.darknessPeriod;
	}

	public void setLightPeriod(int p) {
		this.lightPeriod = p;
	}

	public void setDarknessPeriod(int p) {
		this.darknessPeriod = p;
	}

	public void turnMobileOff() {
		this.mobileOn = true;
	}

	public void turnMobileOn() {
		this.mobileOn = false;
	}

	public int getMobileBattery() {
		return this.mobileBattery;
	}

	public void setMobileBattery(int b) {
		this.mobileBattery = b;
	}

	/**
	 * Verifies if slender is in the neighborhood
	 * 
	 * @return slender point or null if slender is not in the neighborhood
	 */
	public GridPoint getSlenderPosition() {
		int radius = mobileOn ? BIG_RADIUS : SMALL_RADIUS;

		GridPoint pt = grid.getLocation(this);

		GridCellNgh<Slender> nghCreator = new GridCellNgh<Slender>(grid, pt, Slender.class, radius, radius);

		List<GridCell<Slender>> gridCells = nghCreator.getNeighborhood(true);

		GridPoint slenderPoint = null;
		for (GridCell<Slender> cell : gridCells) {
			if (cell.size() != 0) {
				slenderPoint = cell.getPoint();
				return slenderPoint;
			}
		}

		return slenderPoint;
	}

	/**
	 * Moves in the slender opposite direction
	 * 
	 * @param slenderPoint:
	 *            slender position
	 */
	public void escape(GridPoint slenderPoint) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(slenderPoint.getX(), slenderPoint.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint) + Math.PI; // mover no sentido
																									// oposto ao slender
																									// (+PI)
		space.moveByVector(this, PLAYER_SPEED, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
	}

	/**
	 * Normal player move
	 */
	public void move() {
		if (this.targetPoint == null) {
			findTarget();
		}
		moveForward();
		checkDestiny();
	}

	/**
	 * Take a step
	 */
	public void moveForward() {
		GridPoint myPt = grid.getLocation(this);

		if (this.targetPoint == null) {// random move
			GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, myPt, Object.class, PLAYER_SPEED,
					PLAYER_SPEED);
			List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(false);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
			GridPoint otherPoint = gridCells.get(0).getPoint();
			spacialMove(otherPoint);
		} else {
			spacialMove(this.targetPoint);
		}
	}

	/**
	 * See if player reached his destiny (target point)
	 */
	public void checkDestiny() {
		if (this.targetPoint == null) {
			return;
		}
		GridPoint myPt = grid.getLocation(this);
		if (myPt.getX() == targetPoint.getX() && myPt.getY() == targetPoint.getY()) {
			Iterator<Object> iter = grid.getObjectsAt(myPt.getX(), myPt.getY()).iterator();
			while (iter.hasNext()) {
				Object element = iter.next();
				if (element.getClass() == Device.class) {
					((Device) element).turnOff(this);
					//TODO: desclaim device
					if (!knownDevices.contains(element)) {
						knownDevices.add((Device) element);
						knowAllDevices = (knownDevices.size() == Tower.NUMBER_OF_DEVICES);
						// TODO: mandar mensagem aos outros com a localizacao do dispositivo encontrado
					}
				} // TODO: else para quando a class e Battery
			}
			this.targetPoint = null;
		}
	}

	/**
	 * Place the player in the space, updating his position
	 * 
	 * @param pt
	 *            : player's final position
	 */
	public void spacialMove(GridPoint pt) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
		double dist = grid.getDistance(grid.getLocation(this), pt);
		dist = dist < PLAYER_SPEED ? dist : PLAYER_SPEED;
		space.moveByVector(this, dist, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());

	}

	/**
	 * Try to find a target (device or battery) in the neighborhood
	 */
	public void findTarget() {
		if (knowAllDevices) {
//			System.out.println("I know all devices");
			findDeviceToTurnOff();
			return;
		}
		int radius = mobileOn ? BIG_RADIUS : SMALL_RADIUS;

		GridPoint myPt = grid.getLocation(this);

		GridCellNgh<Device> nghCreator = new GridCellNgh<Device>(grid, myPt, Device.class, radius, radius);

		List<GridCell<Device>> gridCells = nghCreator.getNeighborhood(true);

		double nearestDeviceDist = Double.MAX_VALUE;
		GridPoint nearestDevice = null;
		for (GridCell<Device> cell : gridCells) {
			if (cell.size() != 0) {
				GridPoint otherPt = cell.getPoint();
				double dist = grid.getDistance(myPt, otherPt);
				if (dist < nearestDeviceDist) {
					Iterator<Device> iter = cell.items().iterator();
					Device device = iter.next();
					if (knownDevices.indexOf(device) == -1) {// ainda nao conhece este dispositivo
						nearestDevice = otherPt;
						nearestDeviceDist = dist;
					}
				}
			}
		}
		this.targetPoint = nearestDevice;
	}

	private void findDeviceToTurnOff() {
		NdPoint betterDevice = null;
//		System.out.println("Know all");
		// GOTO nearest not claimed
		NdPoint pt = space.getLocation(this);
		double min_dist = Double.MAX_VALUE;
		for (int i = 0; i < knownDevices.size(); i++) {
			if (knownDevices.get(i).isOn() && (!claimedDevices.contains(knownDevices.get(i)))) { 
				NdPoint pt_dev = space.getLocation(knownDevices.get(i));
//				knownDevices.get(i).getPt_space();
//				System.out.println("Finding");
				double dist = space.getDistance(pt, pt_dev);
				if (min_dist > dist) {
					min_dist = dist;
					betterDevice = pt_dev;
				}
			}
		}
		if (betterDevice != null)
			this.targetPoint = new GridPoint((int)betterDevice.getX(),(int)betterDevice.getY());
//TODO:		Claim Device
	}

	private class Exploring extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		public Player agent;
		public Random rand;

		public Exploring(Agent a) {
			super(a);
			agent = (Player) this.myAgent;
			rand = new Random();
		}

		@Override
		public void action() {

			int lightPeriod = agent.getLightPeriod();
			int darknessPeriod = agent.getDarknessPeriod();

			GridPoint slenderPoint = agent.getSlenderPosition();
			if (slenderPoint != null) {
				agent.turnMobileOff();
				targetPoint = null;
				agent.escape(slenderPoint);
			} else {
				if (lightPeriod > 0) {
					agent.turnMobileOn(); // Anabela ADDED
					agent.move();
					agent.setLightPeriod(lightPeriod - 1);
					agent.setMobileBattery(agent.getMobileBattery() - 1);
				} else if (darknessPeriod > 0) {
					agent.turnMobileOff();
					agent.move();
					agent.setDarknessPeriod(darknessPeriod - 1);
				} else {
					int newLightPeriod = rand.nextInt(5);
					int newDarknessPeriod = rand.nextInt(5);
					agent.setLightPeriod(newLightPeriod);
					agent.setDarknessPeriod(newDarknessPeriod);
				}
			}

		}

	}

	public boolean isAlive() {
		return alive;
	}

	@Override
	public void doDelete() {
		super.doDelete();
		alive = false;
	}

	public void killPlayer() {
		Context<?> context = ContextUtils.getContext(this);
		context.remove(this);
		doDelete();
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

}
