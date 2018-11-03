package slenderMan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.TickerBehaviour;

public class Tower extends Agent {
	static final int MAX_DEVICE_TIME = 10;
	static final int NUMBER_OF_DEVICES = 8;
	private Device[] dev = new Device[NUMBER_OF_DEVICES];
	private Player[] players;
	private ArrayList<Integer> playersReady = new ArrayList<Integer>();
	private ContinuousSpace<Object> space;

	public Tower(ContinuousSpace<Object> space, Grid<Object> grid, Context<Object> context, Player[] players) {
		this.players = players;
		this.space = space;
		// Create static map elements (Devices and Rechargers)
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
			agent = (Tower) a;
		}

		@Override
		public void action() {

			agent.reducingDevicesTimeOn();

			boolean endGameLost = agent.isEndGameLost();
			boolean endGameWin = agent.isEndGameWin();

			if (endGameLost || endGameWin) {
				System.out.println("End Of The Game");
				if (endGameLost) {
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
			this.agent = (Tower) a;
		}

		public void action() {
			if (agent.areAllPlayersReady()) {
				System.out.println("Tower: ALL PLAYERS ARE READY!!!");
				agent.doAlgothirtm();
			}
			boolean next = true;
			while (next) {
				ACLMessage msg = receive(mt);
				if (msg != null) {
					String contentID = msg.getConversationId();
					if (contentID == "device_found") {
						int deviceID = Integer.parseInt(msg.getContent());
						System.out.println(agent.getName() + " received msg from " + msg.getSender());
						System.out.println("        Device " + deviceID);
					} else if (contentID == "knows_all_devices") {
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

	public void doAlgothirtm() {

		Set<Node> settledNodes = new HashSet<>();
		Set<Node> unsettledNodes = new HashSet<>();
		Set<Node> allNodes = getNodes(unsettledNodes);

		while (unsettledNodes.size() != 0) {
			Node currentNode = getLowestDistanceNode(unsettledNodes);
			unsettledNodes.remove(currentNode);
			for (Node adjacentNode : allNodes) {
				Double edgeWeight = space.getDistance(currentNode.getPoint(), adjacentNode.getPoint());
				if (!settledNodes.contains(adjacentNode)) {
					calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
					unsettledNodes.add(adjacentNode);
				}
			}
			settledNodes.add(currentNode);
		}
		sendMessages(allNodes);
		return;
	}

	private void sendMessages(Set<Node> allNodes) {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
		String string = "";
		msg.setContent(string);
		msg.setConversationId("target");

		msg.addReceiver(new AID("Tower", AID.ISLOCALNAME));
		for (int i = 0; i < players.length; i++) {
			AID aid = new AID("Player" + i, AID.ISLOCALNAME);
			msg.addReceiver(aid);
		}
		send(msg);
	}

	private Set<Node> getNodes(Set<Node> unsettledNodes) {
		Set<Node> set = new HashSet<>();
		for (int i = 0; i < dev.length; i++) {
			Node node = new Node(space.getLocation(dev[i]));
			set.add(node);
		}
		for (int i = 0; i < players.length; i++) {
			if (players[i].isAlive()) {
				Node node = new Node(space.getLocation(players[i]));
				node.setDistance(0);
				set.add(node);
				unsettledNodes.add(node);
			}
		}
		return set;
	}

	private static Node getLowestDistanceNode(Set<Node> unsettledNodes) {
		Node lowestDistanceNode = null;
		double lowestDistance = Double.MAX_VALUE;
		for (Node node : unsettledNodes) {
			double nodeDistance = node.getDistance();
			if (nodeDistance < lowestDistance) {
				lowestDistance = nodeDistance;
				lowestDistanceNode = node;
			}
		}
		return lowestDistanceNode;
	}

	private static void calculateMinimumDistance(Node evaluationNode, Double edgeWeigh, Node sourceNode) {
		Double sourceDistance = sourceNode.getDistance();
		if (sourceDistance + edgeWeigh < evaluationNode.getDistance()) {
			evaluationNode.setDistance(sourceDistance + edgeWeigh);
			LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
			shortestPath.add(sourceNode);
			evaluationNode.setShortestPath(shortestPath);
		}
	}

	public boolean areAllPlayersReady() {
		for (int i = 0; i < players.length; i++) {
			if (players[i].isAlive() && !playersReady.contains((Integer) players[i].getID())) {
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
