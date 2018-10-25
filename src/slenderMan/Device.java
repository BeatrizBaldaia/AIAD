/**
 * 
 */
package slenderMan;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Device {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int id;
	private boolean on = true;
	private int time = Tower.MAX_DEVICE_TIME;

	public Device(ContinuousSpace<Object> space, Grid<Object> grid, int id) {
		this.space = space;
		this.grid = grid;
		this.id = id;
	}

	public void turnOff(Player p) {
		GridPoint pt = grid.getLocation(this);
		GridPoint p_pt = grid.getLocation(p);

		if (pt == p_pt) {
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
}
