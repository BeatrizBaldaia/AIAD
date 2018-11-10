package slenderMan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;

public class Node {
    
	private static final double MAX_DIST_ROUTE = Tower.MAX_DEVICE_TIME * Player.PLAYER_SPEED;
	Set<Player> players = new HashSet<Player>();
	public GridPoint gridPoint;
	public NdPoint point;
	private ContinuousSpace<Object> space;
	public int id;
    public Node(NdPoint ndPoint, GridPoint gridPoint, ContinuousSpace<Object> space, int id) {
        this.setPoint(ndPoint);
        this.space = space;
        this.id = id;
    }

    /**
     * Gets a node from the to_search list that can make part of route list.
     * The cost of route has to be less than MAX_DIST_ROUTE, it is, the period
     * of devices' inactivity
     * @param route
     * @param to_search
     * @return
     */
	public Node getPossible(List<Node> route, List<Node> to_search) {
		double dist = getDistance(route);
		Node res = null;
		double min = Double.MAX_VALUE;
		for(Node pos : to_search) {
			double step = space.getDistance(route.get(route.size()-1).getPoint(), pos.getPoint());
			if(dist + step <= MAX_DIST_ROUTE && dist + step <= min ) {
				res = pos;
				min = dist + step;
			}
		}
		return res;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}

	/**
	 * Gets the route cost
	 * @param route
	 * @return
	 */
	private double getDistance(List<Node> route) {
		double res = 0;
		for(int i = 0; i < route.size()-1; i++) {
			Node n1 = route.get(i);
			Node n2 = route.get(i+1);
			res += space.getDistance(n1.getPoint(), n2.getPoint());
		}
		return res;
	}

	public NdPoint getPoint() {
		return point;
	}

	public void setPoint(NdPoint point) {
		this.point = point;
	}     
}