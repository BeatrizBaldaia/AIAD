/**
 * 
 */
package slenderMan;

import java.io.Serializable;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Device {

	private static final long serialVersionUID = 6688350845207187287L;
	private ContinuousSpace<Object> space;
	transient private Grid<Object> grid;
	private int id;
	private boolean on = true;
	private int time = Tower.MAX_DEVICE_TIME;
	private NdPoint pt_space = null;

	public Device(ContinuousSpace<Object> space, Grid<Object> grid, int id) {
		this.space = space;
		this.grid = grid;
		this.id = id;
	}

	public Device(NdPoint pt, Grid<Object> grid2) {
		this.setPt_space(pt);
//		on = false;
		grid = grid2;
	}

	public void turnOff(Player p) {
//		System.out.println("IN Turn off");
		if(getPt_space() == null)
			setPt_space(space.getLocation(this));
		GridPoint pt = new GridPoint((int)getPt_space().getX(), (int)getPt_space().getY());
		GridPoint p_pt = grid.getLocation(p);

		if (pt.getX() == p_pt.getX() && pt.getY() == p_pt.getY()) {
//			System.out.println("Turning off energy");
//			p.getDev()[this.id] = new Device(getPt_space(), grid);
//			p.getDev()[this.id].on=false;
			setOn(false);
		}

	}

	public void decreaseTimer() {
		setTime(getTime() - 1);
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public NdPoint getPt_space() {
		return pt_space;
	}

	public void setPt_space(NdPoint pt_space) {
		this.pt_space = pt_space;
	}
	
	public int getID() {
		return this.id;
	}
}
