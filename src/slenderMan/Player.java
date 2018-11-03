package slenderMan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.context.Context;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public class Player extends Agent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int id;
	private int playerNum;

	private int lightPeriod = 0;
	private int darknessPeriod = 0;
	private GridPoint targetPoint = null;
	private GridPoint rechargePoint = null;
	private boolean mobileOn = false;
	private int mobileBattery = 100;
	private boolean goingToRecharge = false;
	private boolean recharging = false;
	private boolean updateStyle = false;
	private boolean sendingMsg = false;
	private boolean startsRunnig = false;
	private State state = State.EXPLORING;

	private ArrayList<Device> knownDevices = new ArrayList<Device>();

	static int BIG_RADIUS = 7;
	static int SMALL_RADIUS = 3;
	static int PLAYER_SPEED = 1;

	static final int BATTERY_PER_STEP = 1;
	static final int BATTERY_PER_MSG = 3;
	static final int BATTERY_PER_TICK = 20;

	private boolean alive;
	private ArrayList<Device> claimedDevices = new ArrayList<Device>();

	public Player(ContinuousSpace<Object> space, Grid<Object> grid, int id, int playerNum, int big_radius,
			int small_radius, int speed) {
		this.space = space;
		this.grid = grid;
		this.id = id;
		this.playerNum = playerNum;

		BIG_RADIUS = big_radius;
		SMALL_RADIUS = small_radius;
		PLAYER_SPEED = speed;
	}

	@Override
	public void setup() {
		addBehaviour(new Exploring(this));
		addBehaviour(new ListeningBehaviour(this));
		alive = true;
	}

	public int getID() {
		return this.id;
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

	public boolean isMobileOn() {
		return this.mobileOn;
	}

	public boolean isSendingMsg() {
		return this.sendingMsg;
	}

	public void setSendingMsg(boolean s) {
		this.sendingMsg = s;
	}

	public boolean isStartingRunning() {
		return this.startsRunnig;
	}

	public void setStartsRunning(boolean s) {
		this.startsRunnig = s;
	}

	public boolean needsToUpdateStyle() {
		return this.updateStyle;
	}

	public void setUpdateStyle(boolean s) {
		this.updateStyle = s;
	}

	public void turnMobileOff() {
		this.mobileOn = false;
	}

	public void turnMobileOn() {
		this.mobileOn = true;
	}

	public int getMobileBattery() {
		return this.mobileBattery;
	}

	public void setMobileBattery(int b) {
		this.mobileBattery = b;
	}

	public ArrayList<Device> getKnownDevices() {
		return this.knownDevices;
	}

	public State getState() {
		return this.state;
	}

	public void setState(State s) {
		this.state = s;
	}

	/**
	 * Verifies if slender is in the neighborhood
	 * 
	 * @return slender point or null if slender is not in the neighborhood
	 */
	public GridPoint getSlenderPosition() {
		int radius = this.mobileOn ? BIG_RADIUS : SMALL_RADIUS;

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
			spatialMove(otherPoint);
		} else {
			spatialMove(this.targetPoint);
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
					if (!knownDevices.contains((Device) element)) {
						// System.out.println(this.getName() + " found device num " +
						// ((Device)element).getID());
						knownDevices.add((Device) element);
						this.sendingMsg = true;
						setUpdateStyle(true);
						shareDevicePosition(((Device) element).getID());
						this.setMobileBattery(this.getMobileBattery() - BATTERY_PER_MSG);
					}
				} else if (element.getClass() == Recharge.class) {
					if (goingToRecharge) {
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
		mobileBattery += BATTERY_PER_TICK;
		if (mobileBattery >= 100) {
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
	public void spatialMove(GridPoint pt) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
		double dist = grid.getDistance(grid.getLocation(this), pt);
		if (dist <= PLAYER_SPEED) {
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
		if (goingToRecharge) {
			if (findRechargerInTheDark()) {
				return;
			}
		}

		GridPoint myPt = grid.getLocation(this);
		int radius = this.mobileOn ? BIG_RADIUS : SMALL_RADIUS;

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

	/**
	 * Player needs to recharge but doesn't know the recharger location so he going
	 * to search in the dark
	 * 
	 * @return
	 */
	public boolean findRechargerInTheDark() {
		GridPoint myPt = grid.getLocation(this);
		GridCellNgh<Recharge> nghCreator = new GridCellNgh<Recharge>(grid, myPt, Recharge.class, SMALL_RADIUS,
				SMALL_RADIUS);
		List<GridCell<Recharge>> gridCells = nghCreator.getNeighborhood(true);

		double nearestRechargeDist = Double.MAX_VALUE;
		GridPoint nearestRecharge = null;

		for (GridCell<Recharge> cell : gridCells) {
			if (cell.size() != 0) {
				GridPoint otherPt = cell.getPoint();
				double dist = grid.getDistance(myPt, otherPt);
				if (dist < nearestRechargeDist) {
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
	 * 
	 * @return true if the battery capacity is equal or lower than the big radius
	 */
	public boolean needsToRecharge() {
		if (mobileBattery <= BIG_RADIUS) {
			goingToRecharge = true;
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
		while (iter.hasNext()) {
			Object element = iter.next();
			if (element.getClass() == Recharge.class) {
				GridPoint otherPt = grid.getLocation((Recharge) element);
				double dist = grid.getDistance(myPt, otherPt);
				if (dist < nearestRechargePointDist) {
					nearestRechargePointDist = dist;
					nearestRechargePoint = otherPt;
				}
			}
		}

		this.rechargePoint = nearestRechargePoint;
	}

	/**
	 * Player knows the position of the recharge point but uses the mobile to light
	 * the way since he cannot see the recharger yet
	 */
	public void moveTowardsRechargePoint() {
		if (this.rechargePoint == null) {
			chooseNearestRechargePoint();
		}
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(rechargePoint.getX(), rechargePoint.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
		space.moveByVector(this, PLAYER_SPEED, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());

		canSeeRechargerInTheDark();
	}

	/**
	 * Check if the player can already see the recharger in the dark
	 * 
	 * @return
	 */
	public boolean canSeeRechargerInTheDark() {
		GridPoint myPt = grid.getLocation(this);

		GridCellNgh<Recharge> nghCreator = new GridCellNgh<Recharge>(grid, myPt, Recharge.class, SMALL_RADIUS,
				SMALL_RADIUS);

		List<GridCell<Recharge>> gridCells = nghCreator.getNeighborhood(true);

		for (GridCell<Recharge> cell : gridCells) {
			if (cell.size() != 0) {
				this.targetPoint = this.rechargePoint;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if the player can already see the device in the dark
	 * @return
	 */
	public boolean canSeeDeviceInTheDark() {
		double dist = grid.getDistance(grid.getLocation(this), this.targetPoint);
		if(dist <= BIG_RADIUS) {
			return true;
		}
		return false;
	}

	private class Exploring extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		public Player agent;
		public Random rand;
		public boolean update = false;

		public Exploring(Agent a) {
			super(a);
			agent = (Player) this.myAgent;
			rand = new Random();
		}

		@Override
		public void action() {
			int lightPeriod = agent.getLightPeriod();
			int darknessPeriod = agent.getDarknessPeriod();

			if (agent.getKnownDevices().size() == 8 && agent.getState() == State.EXPLORING) {
				noticeTower();
				agent.setState(State.WAITING);
			}
			GridPoint slenderPoint = agent.getSlenderPosition();
			if (slenderPoint != null) {
				if (!agent.isStartingRunning()) {
					agent.setStartsRunning(true);
					agent.setUpdateStyle(true);
					resetCurrentState();
				}
				agent.escape(slenderPoint);
			} else {
				if (agent.isStartingRunning()) {
					agent.setStartsRunning(false);
					agent.setUpdateStyle(true);
					update = true;
				}
				if (recharging) {
					agent.rechargeMobile();
				} else {
					if (goingToRecharge || needsToRecharge()) {
						rechargeMobileMove();
					} else {
						if(agent.getState() != State.ACTING) {
							exploringMove(lightPeriod, darknessPeriod);
						} else {
							
						}
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
			if (targetPoint != null) {// ja avistou o carregador
				agent.turnMobileOff();
				agent.move();
			} else {
				if (agent.mobileBattery != 0) {
					moveTowardsRechargePoint();
					agent.setMobileBattery(agent.getMobileBattery() - BATTERY_PER_STEP);
				} else {
					agent.move();
				}
			}
		}

		public void exploringMove(int lightPeriod, int darknessPeriod) {
			if (lightPeriod > 0) {
				agent.turnMobileOn();
				if (update) {
					agent.setUpdateStyle(true);
					update = false;
				}
				agent.move();
				agent.setLightPeriod(lightPeriod - 1);
				agent.setMobileBattery(agent.getMobileBattery() - BATTERY_PER_STEP);
				if ((lightPeriod - 1) == 0) {
					update = true;
				}
			} else if (darknessPeriod > 0) {
				agent.turnMobileOff();
				if (update) {
					agent.setUpdateStyle(true);
					update = false;
				}
				agent.move();
				agent.setDarknessPeriod(darknessPeriod - 1);
			} else {
				update = true;
				int newLightPeriod = rand.nextInt(5);
				int newDarknessPeriod = rand.nextInt(5);
				agent.setLightPeriod(newLightPeriod);
				agent.setDarknessPeriod(newDarknessPeriod);
			}
		}
	}

	private class ListeningBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		public Player agent;

		public ListeningBehaviour(Agent a) {
			super(a);
			this.agent = (Player) a;
		}

		public void action() {
			boolean next = true;
			if (agent.isMobileOn()) {
				while (next) {
					ACLMessage msg = receive(mt);
					if (msg != null) {
						int deviceID = Integer.parseInt(msg.getContent());
						if (msg.getConversationId() == "device_found") {
							agent.acknowledgeNewDevice(deviceID);
						} /*else if (msg.getConversationId() == "claim") {
							Device dev = null;
							for(Device dev_iter: knownDevices) {
								if(dev_iter.getID() == deviceID) {
									dev = dev_iter;
									break;
								}
							}
							agent.claimedDevices.add(dev);
						}*/
					} else {
						block();
						next = false;
					}
				}
			} else {
				if (isRunnable()) {
					restart();
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

	public void die() {
		Context<?> context = ContextUtils.getContext(this);
		context.remove(this);
		doDelete();
	}

	public void acknowledgeNewDevice(int id) {
		Iterator<Object> iter = grid.getObjects().iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			if (element.getClass() == Device.class) {
				if (((Device) element).getID() == id && !knownDevices.contains((Device) element)) {
					knownDevices.add((Device) element);
				}
			}
		}
	}

	public void shareDevicePosition(int id) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(Integer.toString(id));
		msg.setConversationId("device_found");

		msg.addReceiver(new AID("Tower", AID.ISLOCALNAME));
		for (int i = 0; i < playerNum; i++) {
			if (i != this.id) {
				AID aid = new AID("Player" + i, AID.ISLOCALNAME);
				msg.addReceiver(aid);
			}
		}
		send(msg);
	}

	public void noticeTower() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(Integer.toString(this.id));
		msg.setConversationId("knows_all_devices");
		msg.addReceiver(new AID("Tower", AID.ISLOCALNAME));
		send(msg);
	}

}
