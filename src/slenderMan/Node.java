package slenderMan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;

public class Node {
    
	private static final double MAX_DIST_ROUTE = Tower.MAX_DEVICE_TIME * Player.PLAYER_SPEED;
	Set<Player> players = new HashSet<Player>();
	public NdPoint point;
	private ContinuousSpace<Object> space;
	private int id;
    public Node(NdPoint ndPoint, ContinuousSpace<Object> space, int id) {
        this.point = ndPoint;
        this.space = space;
        this.id = id;
    }

	public Node getPossible(List<Node> route, List<Node> to_shearch) {
		double dist = getDistance(route);
		Node res = null;
		double min = Double.MAX_VALUE;
		for(Node pos : to_shearch) {
			double step = space.getDistance(route.get(route.size()-1).point, pos.point);
			if(dist + step <= MAX_DIST_ROUTE && dist + step <= min ) {
				res = pos;
				min = dist + step;
			}
		}
		return res;
	}

	@Override
	public String toString() {
		return "Node " + id + " ";
	}

	private double getDistance(List<Node> route) {
		double res = 0;
		for(int i = 0; i < route.size()-1; i++) {
			Node n1 = route.get(i);
			Node n2 = route.get(i+1);
			res += space.getDistance(n1.point, n2.point);
		}
		return res;
	}
    
//  private String name;
//    private List<Node> shortestPath = new LinkedList<>();
//     
//    private Double distance = Double.MAX_VALUE;
//    public Double getDistance() {
//		return distance;
//	}
//    public void setDistance(double distance) {
//		this.distance = distance;
//	}
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public List<Node> getShortestPath() {
//		return shortestPath;
//	}
//
//	public void setShortestPath(List<Node> shortestPath) {
//		this.shortestPath = shortestPath;
//	}
//
//	public void setDistance(Double distance) {
//		this.distance = distance;
//	}
//
//
//	public NdPoint getPoint() {
//		return point;
//	}
//
//	public void setPoint(NdPoint point) {
//		this.point = point;
//	}
     
}