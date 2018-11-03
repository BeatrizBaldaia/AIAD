package slenderMan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import repast.simphony.space.continuous.NdPoint;

public class Node {
    
    private String name;
     
    private List<Node> shortestPath = new LinkedList<>();
     
    private Double distance = Double.MAX_VALUE;
    public Double getDistance() {
		return distance;
	}
    public void setDistance(double distance) {
		this.distance = distance;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Node> getShortestPath() {
		return shortestPath;
	}

	public void setShortestPath(List<Node> shortestPath) {
		this.shortestPath = shortestPath;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	private NdPoint point;
    public Node(NdPoint ndPoint) {
        this.point = ndPoint;
    }

	public NdPoint getPoint() {
		return point;
	}

	public void setPoint(NdPoint point) {
		this.point = point;
	}
     
}