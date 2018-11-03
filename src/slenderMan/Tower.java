package slenderMan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public class Tower extends Agent {
	static final int MAX_DEVICE_TIME = 50;
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

	class State {
		int cover, head;

		State(int c, int h) {
			cover = c;
			head = h;
		}
	}

	public List<List<Node>> deviceAllocation() {

		List<Node> nodes_devices = getNodesDevices();
		List<List<Node>> players_routes = new ArrayList<List<Node>>();
		List<Node> to_shearch = new ArrayList<Node>();
		to_shearch.addAll(nodes_devices);
		while (!to_shearch.isEmpty()) {
			List<Node> route = new ArrayList<Node>();
			Node node = to_shearch.get(0);
			to_shearch.remove(node);
			route.add(node);
			Node possible = node.getPossible(route, to_shearch);
			while (possible != null) {
				to_shearch.remove(possible);
				route.add(possible);
				possible = node.getPossible(route, to_shearch);
			}
			players_routes.add(route);
		}
		System.out.println(players_routes);
		return players_routes;
	}

	private Set<Player> getPlayersAlive() {
		Set<Player> set = new HashSet<>();
		for (int i = 0; i < players.length; i++) {
			if (players[i].isAlive())
				set.add(players[i]);
		}
		return set;
	}

	private List<Node> getNodesDevices() {
		List<Node> set = new ArrayList<>();
		for (int i = 0; i < dev.length; i++) {
			Node node = new Node(space.getLocation(dev[i]), space, dev[i].getID());
			set.add(node);
		}
		return set;
	}

	public void doAlgothirtm() {
		List<List<Node>> routes = deviceAllocation();
		Set<Player> players_alive = getPlayersAlive();
		List<List<Node>> using = new ArrayList<List<Node>>();
		using.addAll(routes);
		for (Player p : players_alive) {
			NdPoint d = p.findNearestDevice(using);
			List<Node> route = findList(d,using);
			sendRouteToPlayer(p, route);
			using.remove(route);
			if(using.isEmpty()) {
				using.addAll(routes);
			}
		}
	}

	private List<Node> findList(NdPoint d, List<List<Node>> routes) {
		for(List<Node> list: routes) {
			for(Node n:list) {
				if(n.getPoint().getX()== d.getX() && n.getPoint().getY()== d.getY()) {
					return list;
				}
			}
		}
		return null;
	}

	private void sendRouteToPlayer(Player p, List<Node> route) {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
		String string = route.toString();
		msg.setContent(string);
		msg.setConversationId("target");
		AID aid = new AID("Player" + p.getID(), AID.ISLOCALNAME);
		msg.addReceiver(aid);
		send(msg);
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

	// private Set<Node> getNodes(Set<Node> unsettledNodes) {
	// Set<Node> set = new HashSet<>();
	// for (int i = 0; i < dev.length; i++) {
	// Node node = new Node(space.getLocation(dev[i]));
	// set.add(node);
	// }
	// for (int i = 0; i < players.length; i++) {
	// if (players[i].isAlive()) {
	// Node node = new Node(space.getLocation(players[i]));
	// node.setDistance(0);
	// set.add(node);
	// unsettledNodes.add(node);
	// }
	// }
	// return set;
	// }
	//
	// private static Node getLowestDistanceNode(Set<Node> unsettledNodes) {
	// Node lowestDistanceNode = null;
	// double lowestDistance = Double.MAX_VALUE;
	// for (Node node : unsettledNodes) {
	// double nodeDistance = node.getDistance();
	// if (nodeDistance < lowestDistance) {
	// lowestDistance = nodeDistance;
	// lowestDistanceNode = node;
	// }
	// }
	// return lowestDistanceNode;
	// }
	//
	// private static void calculateMinimumDistance(Node evaluationNode, Double
	// edgeWeigh, Node sourceNode) {
	// Double sourceDistance = sourceNode.getDistance();
	// if (sourceDistance + edgeWeigh < evaluationNode.getDistance()) {
	// evaluationNode.setDistance(sourceDistance + edgeWeigh);
	// LinkedList<Node> shortestPath = new
	// LinkedList<>(sourceNode.getShortestPath());
	// shortestPath.add(sourceNode);
	// evaluationNode.setShortestPath(shortestPath);
	// }
	// }

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
