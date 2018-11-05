package slenderMan;

import java.util.ArrayList;
import java.util.Arrays;
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
	private Grid<Object> grid;
	private List<List<Node>> routes = new ArrayList<List<Node>>();
	private Player[] assignedPlayers = {};
	private ArrayList<Player> remainingPlayers = new ArrayList<Player>();

	public Tower(ContinuousSpace<Object> space, Grid<Object> grid, Context<Object> context, Player[] players) {
		this.players = players;
		this.space = space;
		this.grid = grid;
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
		addBehaviour(new WatchGame(this));
		addBehaviour(new ListeningBehaviour(this));
	}

	private class WatchGame extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		private Tower agent;

		public WatchGame(Agent a) {
			super(a);
			agent = (Tower) a;
		}

		@Override
		public void action() {

			agent.reducingDevicesTimeOn();

			boolean endGameLost = agent.isEndGameLost();
			boolean endGameWin = agent.isEndGameWin();
			
			if(!agent.getRoutes().isEmpty()) {
				if(!agent.assignNewPlayer()) {
					endGameLost = true;
				}
			} else {
				if (agent.areAllPlayersReady()) {
					System.out.println("--- ALL PLAYERS ARE READY ---");
					if(!agent.doAlgothirtm()) {
						endGameLost = true;
					}
				}
			}

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
			boolean next = true;
			while (next) {
				ACLMessage msg = receive(mt);
				if (msg != null) {
					String contentID = msg.getConversationId();
					if (contentID == "device_found") {
						int deviceID = Integer.parseInt(msg.getContent());
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
	
	public List<List<Node>> getRoutes() {
		return this.routes;
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

	

	/**
	 * Gets a list with all players that are still alive
	 * @return
	 */
	private Set<Player> getPlayersAlive() {
		Set<Player> set = new HashSet<>();
		for (int i = 0; i < players.length; i++) {
			if (players[i].isAlive())
				set.add(players[i]);
		}
		return set;
	}

	/**
	 * Creates a list with nodes that represent the devices
	 * @return
	 */
	private List<Node> getNodesDevices() {
		List<Node> set = new ArrayList<>();
		for (int i = 0; i < dev.length; i++) {
			Node node = new Node(space.getLocation(dev[i]), grid.getLocation(dev[i]), space, dev[i].getID());
			set.add(node);
		}
		return set;
	}
	
	/**
	 * Creates a list of closed routes
	 * @return
	 */
	public List<List<Node>> deviceAllocation() {

		List<Node> nodes_devices = getNodesDevices();
		List<List<Node>> players_routes = new ArrayList<List<Node>>();
		List<Node> nodes_to_search = new ArrayList<Node>();
		nodes_to_search.addAll(nodes_devices);
		while(!nodes_to_search.isEmpty()) {
			List<Node> route = new ArrayList<Node>();
			Node node = nodes_to_search.remove(0);
			route.add(node);
			Node possible = node.getPossible(route, nodes_to_search);
			while(possible != null) {
				nodes_to_search.remove(possible);
				route.add(possible);
				possible = node.getPossible(route, nodes_to_search);
			}
			players_routes.add(route);
		}
		return players_routes;
	}

	/**
	 * Strategy that distributes the routes through all the players
	 * @return true if every thing is ok and players can still
	 * win the game
	 */
	public boolean doAlgothirtm() {
		List<List<Node>> routes = deviceAllocation();
		Set<Player> players_alive = getPlayersAlive();
		List<List<Node>> routes_tmp = new ArrayList<List<Node>>();
		routes_tmp.addAll(routes);
		this.routes.addAll(routes);
		List<Player> remaining_players = new ArrayList<Player>();
		for (Player p : players_alive) {
			NdPoint d = p.findNearestDevice(routes_tmp);
			List<Node> route = findNearestRoute(d, routes_tmp);
			if(route == null) {
				remaining_players.add(p);
			} else {
				int index = routes.indexOf(route);
				if(index >= this.assignedPlayers.length) {
					this.assignedPlayers = Arrays.copyOf(this.assignedPlayers, index + 1);
				}
				this.assignedPlayers[index] = p;
				sendRouteToPlayer(p, route);
				routes_tmp.remove(route);
				if(routes_tmp.isEmpty()) {
					break;
				}
			}
		}
		this.remainingPlayers.addAll(remaining_players);
		if(!routes_tmp.isEmpty()) {
			return handleRemainingRoutes(routes_tmp, remaining_players);
		}
		return true;
		
	}

	/**
	 * Finds the route where d is included
	 * @param d
	 * @param routes
	 * @return
	 */
	private List<Node> findNearestRoute(NdPoint d, List<List<Node>> routes) {
		for(List<Node> route: routes) {
			for(Node n: route) {
				if(n.getPoint().getX()== d.getX() && n.getPoint().getY()== d.getY()) {
					return route;
				}
			}
		}
		return null;
	}
	
	/**
	 * Assign the remaining players to the remaining routes.
	 * If there aren't  players, players will never make so
	 * they won't turn off all the devices and then lose
	 * @param routes
	 * @param players
	 * @return
	 */
	public boolean handleRemainingRoutes(List<List<Node>> routes, List<Player> players) {
		if(routes.size() > players.size()) {
			return false;
		}
		for(List<Node> route: routes) {
			Player p = players.remove(0);
			sendRouteToPlayer(p, route);
		}
		return true;
	}

	/**
	 * Message sent to the players proposing routes to turn off devices
	 * @param p : Player Receiver
	 * @param route : The route that p will be assigned to
	 */
	private void sendRouteToPlayer(Player p, List<Node> route) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		String content = generateMsg(route);
		msg.setContent(content);
		msg.setConversationId("target_route");
		AID aid = new AID("Player" + p.getID(), AID.ISLOCALNAME);
		msg.addReceiver(aid);
		send(msg);
	}
	
	public String generateMsg(List<Node> route) {
		String str = route.get(0).toString();
		for(int i = 1; i < route.size(); i++) {
			str += " " + route.get(i).id;
		}
		return str;
	}

	/**
	 * Check if all remaining players already know all devices' positions
	 * @return
	 */
	public boolean areAllPlayersReady() {
		for (int i = 0; i < players.length; i++) {
			if (players[i].isAlive() && !playersReady.contains((Integer) players[i].getID())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check that one specific player knows all devices' positions
	 * @param id : Player's id
	 */
	public void acknowledgePlayer(int id) {
		this.playersReady.add(id);
	}

	/**
	 * Devices' inactivity count down
	 */
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
	
	/**
	 * If a Player assigned to a certain route die, 
	 * another one will replace him. If it is not possible,
	 * then it's a lost game
	 * @return false if it's a list game
	 */
	public boolean assignNewPlayer() {
		for(int i = 0; i< this.assignedPlayers.length; i++) {
			if(!this.assignedPlayers[i].isAlive()) {
				if(this.remainingPlayers.isEmpty()) {
					return false;
				}
				Player p = this.remainingPlayers.remove(0);
				this.assignedPlayers[i] = p;
				List<Node> route = this.routes.get(i);
				sendRouteToPlayer(p, route);
			}
		}
		return true;
	}

	/**
	 * End of game with players' lost
	 * @return
	 */
	public boolean isEndGameLost() {
		for (int i = 0; i < players.length; i++) {
			if (players[i].isAlive()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * End of game with players' victory
	 * @return
	 */
	public boolean isEndGameWin() {
		for (int i = 0; i < dev.length; i++) {
			if (dev[i].isOn()) {
				return false;
			}
		}
		return true;
	}
}
