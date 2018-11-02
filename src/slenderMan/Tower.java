package slenderMan;

import java.util.ArrayList;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.TickerBehaviour;

public class Tower extends Agent {
	static final int MAX_DEVICE_TIME = 10;
	static final int NUMBER_OF_DEVICES = 8;
	private Device[] dev = new Device[NUMBER_OF_DEVICES];
	private Player[] players;
	private ArrayList<Integer> playersReady = new ArrayList<Integer>();

	public Tower(ContinuousSpace<Object> space, Grid<Object> grid, Context<Object> context, Player[] players) {
		this.players = players;

		//Create static map elements (Devices and Rechargers)
		for (int i = 0; i < dev.length; i++) {
			dev[i] = new Device(space, grid, i);
			context.add(dev[i]);
			Recharge r = new Recharge(space, grid, i);
			context.add(r);
		}
	}

	@Override
	public void setup() {
		addBehaviour(new CheckDevices(this));
		addBehaviour(new ListeningBehaviour(this));
	}

	private class CheckDevices extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		private Tower agent;

		public CheckDevices(Agent a) {
			super(a);
			agent = (Tower)a;
		}
		@Override
		public void action() {

			agent.reducingDevicesTimeOn();

			boolean endGameLost = agent.isEndGameLost();
			boolean endGameWin = agent.isEndGameWin();

			if (endGameLost || endGameWin) {
				System.out.println("End Of The Game");
				if(endGameLost) {
					System.out.println("Slender Won... :(");
				} else {
					System.out.println("Player Won!!! :D");
				}
				RunEnvironment.getInstance().endRun();
			}
		}

	}

	private class ListeningBehaviour extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		public Tower agent;
		public ListeningBehaviour(Agent a) {
			super(a);
			this.agent = (Tower)a;
		}

		public void action() {
			if(agent.areAllPlayersReady()) {
				System.out.println("Tower: ALL PLAYERS ARE READY!!!");
			}
			boolean next = true;
			while(next) {
				ACLMessage msg = receive(mt);
				if(msg != null) {
					String contentID = msg.getConversationId();
					if(contentID == "device_found") {
						int deviceID = Integer.parseInt(msg.getContent());
						System.out.println(agent.getName() + " received msg from " + msg.getSender());
						System.out.println("        Device " + deviceID);
					} else if(contentID == "knows_all_devices") {
						int agentID = Integer.parseInt(msg.getContent());
						agent.acknowledgePlayer(agentID);
					}
				} else {
					block();
					next = false;
				}
			}
		}
	}

	public Player[] getPlayers() {
		return this.players;
	}

	public boolean areAllPlayersReady() {
		for(int i = 0; i < players.length; i++) {
			if(players[i].isAlive() && !playersReady.contains((Integer)players[i].getID())) {
				return false;
			}
		}
		return true;
	}

	public void acknowledgePlayer(int id) {
		this.playersReady.add(id);
	}

	public void reducingDevicesTimeOn() {
		for (int i = 0; i < dev.length; i++) {
			if (!dev[i].isOn()) {
				dev[i].decreaseTimer();
				if (dev[i].getTime() == 0) {
					dev[i].setOn(true);
					dev[i].setTime(Tower.MAX_DEVICE_TIME);
				}

			}
		}
	}

	public boolean isEndGameLost() {
		for (int i = 0; i < players.length; i++) {
			if (players[i].isAlive()) {
				return false;
			}
		}
		return true;
	}

	public boolean isEndGameWin() {
		for (int i = 0; i < dev.length; i++) {
			if (dev[i].isOn()) {
				return false;
			}
		}
		return true;
	}
}
