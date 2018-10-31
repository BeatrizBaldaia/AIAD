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
	private Device[] dev = new Device[8];

	private int lightPeriod = 0;
	private int darknessPeriod = 0;
	private GridPoint targetPoint = null;
	private GridPoint rechargePoint = null;
	private boolean mobileOn = false;
	private int mobileBattery = 100;
	private boolean goingToRecharge = false;
	private boolean recharging = false;
	private ArrayList<Device> knownDevices = new ArrayList<Device>();

	static final int BIG_RADIUS = 7;
	static final int SMALL_RADIUS = 3;
	static final int PLAYER_SPEED = 1;
	static final int BATTERY_PER_TICK = 20;

	private boolean alive;
	private ArrayList<Device> claimedDevices = new ArrayList<Device>();;

	public Player(ContinuousSpace<Object> space, Grid<Object> grid, int energy) {
		this.space = space;
		this.grid = grid;
	}

	@Override
	public void setup() {
		addBehaviour(new Exploring(this));
		alive = true;
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
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint) + Math.PI; // mover no sentido oposto ao slender (+PI)
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
						System.out.println(this.getName() + " encontrou um novo dispositivo " + ((Device)element).getID());
						knownDevices.add((Device) element);
						// TODO: mandar mensagem aos outros com a localizacao do dispositivo encontrado
					}
				} else if(element.getClass() == Recharge.class) {
					if(goingToRecharge) {
						goingToRecharge = false;
						recharging = true;
					}
				}
			}
			this.targetPoint = null;
		}
	}

	/**
	 * Player is next to a recharger and so is going to recharge his mobile
	 */
	public void rechargeMobile() {
		System.out.println(this.getName() + " a carregar...");
		mobileBattery += BATTERY_PER_TICK;
		if(mobileBattery >= 100) {
			mobileBattery = 100;
			recharging = false;
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
		if(dist <= PLAYER_SPEED) {
			space.moveTo(this, otherPoint.getX(), otherPoint.getY());
		} else {
			space.moveByVector(this, PLAYER_SPEED, angle, 0);
		}
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());

	}

	/**
	 * Try to find a target (device or battery) in the neighborhood
	 */
	public void findTarget() {		
		if(goingToRecharge) {
			if(findRechargerInTheDark()) {
				return;
			}
		}
		if (knownDevices.size() == 8) {
			System.out.println("I know all devices");
			findDeviceToTurnOff();
			return;
		}

		GridPoint myPt = grid.getLocation(this);
		int radius = mobileOn ? BIG_RADIUS : SMALL_RADIUS;

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

	public boolean findRechargerInTheDark() {
		GridPoint myPt = grid.getLocation(this);
		GridCellNgh<Recharge> nghCreator = new GridCellNgh<Recharge>(grid, myPt, Recharge.class, SMALL_RADIUS, SMALL_RADIUS);
		List<GridCell<Recharge>> gridCells = nghCreator.getNeighborhood(true);

		double nearestRechargeDist = Double.MAX_VALUE;
		GridPoint nearestRecharge = null;

		for(GridCell<Recharge> cell : gridCells) {
			if(cell.size() != 0) {
				GridPoint otherPt = cell.getPoint();
				double dist = grid.getDistance(myPt, otherPt);
				if(dist < nearestRechargeDist) {
					nearestRecharge = otherPt;
					nearestRechargeDist = dist;
				}
			}
		}
		this.targetPoint = nearestRecharge;

		return nearestRecharge != null;
	}

	/**
	 * check if needs to recharge his mobile.
	 * @return true if the battery capacity is equal or lower than the big radius 
	 */
	public boolean needsToRecharge() {
		if(mobileBattery <= BIG_RADIUS) {
			goingToRecharge = true;
			System.out.println(this.getName() + " precisa de recarregar! Bateria = " + mobileBattery);
			return true;
		}
		return false;
	}

	/**
	 * Save the current nearest recharge point
	 */
	public void chooseNearestRechargePoint() {
		double nearestRechargePointDist = Double.MAX_VALUE;
		GridPoint nearestRechargePoint = null;
		GridPoint myPt = grid.getLocation(this);

		Iterator<Object> iter = grid.getObjects().iterator();
		while(iter.hasNext()) {
			Object element = iter.next();
			if(element.getClass() == Recharge.class) {
				GridPoint otherPt = grid.getLocation((Recharge)element);
				double dist = grid.getDistance(myPt, otherPt);
				if(dist < nearestRechargePointDist) {
					nearestRechargePointDist = dist;
					nearestRechargePoint = otherPt;
				}
			}
		}

		this.rechargePoint = nearestRechargePoint;
	}

	/**
	 * Player knows the position of the recharge point
	 * but uses the mobile to light the way since he cannot
	 * see the recharger yet
	 */
	public void moveTowardsRechargePoint() {
		if(this.rechargePoint == null) {
			chooseNearestRechargePoint();
		}
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(rechargePoint.getX(), rechargePoint.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
		space.moveByVector(this, PLAYER_SPEED, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());

		canSeeInTheDark();
	}

	/**
	 * Check if the player can already see the recharger
	 * in the dark
	 * @return
	 */
	public boolean canSeeInTheDark() {
		GridPoint myPt = grid.getLocation(this);

		GridCellNgh<Recharge> nghCreator = new GridCellNgh<Recharge>(grid, myPt, Recharge.class, SMALL_RADIUS, SMALL_RADIUS);

		List<GridCell<Recharge>> gridCells = nghCreator.getNeighborhood(true);

		for(GridCell<Recharge> cell : gridCells) {
			if(cell.size() != 0) {
				this.targetPoint = this.rechargePoint;
				return true;
			}
		}
		return false;
	}

	private void findDeviceToTurnOff() {
		NdPoint betterDevice = null;
		System.out.println("Know all");
		// GOTO nearest not claimed
		NdPoint pt = space.getLocation(this);
		double min_dist = Double.MAX_VALUE;
		for (int i = 0; i < knownDevices.size(); i++) {
			if (knownDevices.get(i).isOn() && (!claimedDevices.contains(knownDevices.get(i)))) { 
				NdPoint pt_dev = space.getLocation(knownDevices.get(i));
				//				knownDevices.get(i).getPt_space();
				System.out.println("Finding");
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
			if(knownDevices.size() == 8) {
				System.out.println(agent.getName() + " ENCONTROU TODOS OS DISPOSITIVOS");
			}

			GridPoint slenderPoint = agent.getSlenderPosition();
			if (slenderPoint != null) {
				resetCurrentState();
				agent.escape(slenderPoint);
			} else {
				if(recharging) {
					agent.rechargeMobile();
				} else {
					if(goingToRecharge || needsToRecharge()) {
						rechargeMobileMove();
					} else {
						normalMove(lightPeriod, darknessPeriod);
					}
				}
			}

		}
		
		public void resetCurrentState() {
			agent.turnMobileOff();
			targetPoint = null;
			rechargePoint = null;
			goingToRecharge = recharging;
			recharging = false;
		}

		public void rechargeMobileMove() {
			if(targetPoint != null) {//ja avistou o carregador
				agent.turnMobileOff();
				agent.move();
			} else {
				if(agent.mobileBattery != 0){
					moveTowardsRechargePoint();
					agent.setMobileBattery(agent.getMobileBattery() - 1);
				} else {
					agent.move();
				}
			}
		}

		public void normalMove(int lightPeriod, int darknessPeriod) {
			if(lightPeriod > 0) {
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

	public Device[] getDev() {
		return dev;
	}

	public void setDev(Device[] dev) {
		this.dev = dev;
	}

}
