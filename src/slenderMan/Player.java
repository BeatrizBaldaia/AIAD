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
import sajas.core.behaviours.TickerBehaviour;

public class Player extends Agent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int energy;
	private int startingEnergy;
	private Device[] dev = new Device[8];
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

	private static final double MOVE_DIST = 2;
	MooreQuery<Object> nearSlender;
	private boolean alive;

	public Player(ContinuousSpace<Object> space, Grid<Object> grid, int energy) {
		this.space = space;
		this.grid = grid;
		this.setEnergy(startingEnergy = energy);
	}

	@Override
	public void setup() {
		//addBehaviour(new RunAround(this, 1));
		addBehaviour(new Exploring(this));
		alive = true;
	}
/*
	private class RunAround extends TickerBehaviour {
		private static final long serialVersionUID = 1L;

		public RunAround(Agent a, long period) {
			super(a, period);
			nearSlender = new MooreQuery<Object>(grid, this.myAgent, 2, 2);
		}

		@Override
		protected void onTick() {
			updateWorld();
			boolean s = false;
			Iterable<Object> objs = nearSlender.query();
			nearSlender.reset(this.myAgent, 2, 2);
			Iterator<Object> iter = objs.iterator();
			while (iter.hasNext()) {
				Object k = iter.next();
				if (k.getClass() == Slender.class) {
					s = true;
					continue;
				}
			}
			GridPoint pt = grid.getLocation(this.getAgent());
			objs = grid.getObjectsAt(pt.getX(), pt.getY());
			iter = objs.iterator();
			while (iter.hasNext()) {
				Object with_me = iter.next();
				if (this.myAgent.equals(with_me)) {
					break;
				}
//				System.out.println("SOMETHING IS HERE WITH ME");
				if (with_me.getClass() == Slender.class) {
					s = true;
//					System.err.println("NearSlender - " + this.myAgent.getName());
				}
				if (with_me.getClass() == Device.class) {
//					System.err.println("IN A CELL WITH THE DEVICE");
					((Device) with_me).turnOff((Player) this.myAgent);
				}
				if (with_me.getClass() == Recharge.class) {
//					System.err.println("IN A CELL WITH THE RECHARGE");
					((Recharge) with_me).recharge((Player) this.myAgent);
				}
			}
			if (!s) {
				if (know_all_devices()) {
					go_To_device();
				}
				return;
			}

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
			moveTowards(pointWithLeastSlenders);
		}

	}

	private void go_To_device() {
		NdPoint pt = space.getLocation(this);
		double min_dist = Double.MAX_VALUE;
		NdPoint nearestDevice = null;
		for (int i = 0; i < dev.length; i++) {
			if (dev[i].isOn()) {
//				NdPoint pt_dev = space.getLocation(dev[i]);
				NdPoint pt_dev = dev[i].getPt_space();
				double dist = space.getDistance(pt, pt_dev);
				if (min_dist > dist) {
					min_dist = dist;
					nearestDevice = pt_dev;
				}
			}
		}
		if(nearestDevice != null)
		moveTowards(new GridPoint((int) nearestDevice.getX(), (int) nearestDevice.getY()));
		else
			System.err.println("Does not exist!!!");
	}

	public void updateWorld() {
		for (int i = 0; i < dev.length; i++) {
			if (dev[i] != null && !dev[i].isOn()) {
				dev[i].decreaseTimer();
				if (dev[i].getTime() == 0) {
					dev[i].setOn(true);
					dev[i].setTime(Tower.MAX_DEVICE_TIME);
				}

			}
		}

	}

	private boolean know_all_devices() {
		if (knowAllDevices) {
			return true;
		}
		for (int i = 0; i < dev.length; i++) {
			if (dev[i] == null) {
				return false;
			}
		}
		knowAllDevices = true;
		return knowAllDevices;
	}

	public void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			double dist = space.getDistance(myPoint, otherPoint);
			dist = (dist < MOVE_DIST )? dist : MOVE_DIST;
			space.moveByVector(this, dist, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
		}
	}

	*/
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
	 * @return slender point or null if slender is not in the neighborhood
	 */
	public GridPoint getSlenderPosition() {
		int radius = mobileOn ? BIG_RADIUS : SMALL_RADIUS;

		GridPoint pt = grid.getLocation(this);

		GridCellNgh<Slender> nghCreator = new GridCellNgh<Slender>(grid, pt, Slender.class, radius, radius);

		List<GridCell<Slender>> gridCells = nghCreator.getNeighborhood(true);

		GridPoint slenderPoint = null;
		for(GridCell<Slender> cell : gridCells) {
			if(cell.size() != 0) {
				slenderPoint = cell.getPoint();
				return slenderPoint;
			}
		}

		return slenderPoint;
	}

	/**
	 * Moves in the slender opposite direction
	 * @param slenderPoint: slender position
	 */
	public void escape(GridPoint slenderPoint) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(slenderPoint.getX(), slenderPoint.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint) + Math.PI; //mover no sentido oposto ao slender (+PI)
		space.moveByVector(this, PLAYER_SPEED, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
	}

	/**
	 * Normal player move
	 */
	public void move() {
		if(this.targetPoint == null) {
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

		if(this.targetPoint == null) {//random move
			GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, myPt, Object.class, PLAYER_SPEED, PLAYER_SPEED);
			List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(false);
			SimUtilities.shuffle(gridCells,  RandomHelper.getUniform());
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
		if(this.targetPoint == null) {
			return;
		}
		GridPoint myPt = grid.getLocation(this);
		if(myPt.getX() == targetPoint.getX() && myPt.getY() == targetPoint.getY()) {
			Iterator<Object> iter = grid.getObjectsAt(myPt.getX(), myPt.getY()).iterator();
			while(iter.hasNext()) {
				Object element = iter.next();
				if(element.getClass() == Device.class) {
					if(!knownDevices.contains(element)) {
						knownDevices.add((Device)element);
						//TODO: mandar mensagem aos outros com a localizacao do dispositivo encontrado
					}
				} //TODO: else para quando a class e Battery
			}
			this.targetPoint = null;
		}
	}

	/**
	 * Place the player in the space, updating his position
	 * @param pt : player's final position
	 */
	public void spacialMove(GridPoint pt) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
		double dist = grid.getDistance(grid.getLocation(this), pt);
		dist = dist < PLAYER_SPEED ? dist : PLAYER_SPEED;
		space.moveByVector(this, dist, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());

	}

	/**
	 * Try to find a target (device or battery) in the neighborhood
	 */
	public void findTarget() {
		int radius = mobileOn ? BIG_RADIUS : SMALL_RADIUS;

		GridPoint myPt = grid.getLocation(this);

		GridCellNgh<Device> nghCreator = new GridCellNgh<Device>(grid, myPt, Device.class, radius, radius);

		List<GridCell<Device>> gridCells = nghCreator.getNeighborhood(true);

		double nearestDeviceDist = Double.MAX_VALUE;
		GridPoint nearestDevice = null;
		for(GridCell<Device> cell : gridCells) {
			if(cell.size() != 0) {
				GridPoint otherPt = cell.getPoint();
				double dist = grid.getDistance(myPt, otherPt);
				if(dist < nearestDeviceDist) {
					Iterator<Device> iter = cell.items().iterator();
					Device device = iter.next();
					if(knownDevices.indexOf(device) == -1) {//ainda nao conhece este dispositivo
						nearestDevice = otherPt;
						nearestDeviceDist = dist;
					}
				}
			}
		}
		this.targetPoint = nearestDevice;
	}

	private class Exploring extends CyclicBehaviour {

		public Player agent;
		public Random rand;

		public Exploring(Agent a) {
			super(a);
			agent = (Player)this.myAgent;
			rand = new Random();
		}

		@Override
		public void action() {

			int lightPeriod = agent.getLightPeriod();
			int darknessPeriod = agent.getDarknessPeriod();

			GridPoint slenderPoint = agent.getSlenderPosition();
			if(slenderPoint != null) {
				agent.turnMobileOff();
				targetPoint = null;
				agent.escape(slenderPoint);
			} else {
				if(lightPeriod > 0) {
					agent.move();
					agent.setLightPeriod(lightPeriod - 1);
					agent.setMobileBattery(agent.getMobileBattery() - 1);
				} else if(darknessPeriod > 0) {
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

	public Device[] getDev() {
		return dev;
	}

	public void setDev(Device[] dev) {
		this.dev = dev;
	}

}
