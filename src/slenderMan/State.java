package slenderMan;

public enum State { 
	EXPLORING, //Player is tring to find all devices
	WAITING, //Player already knows all devices' location, but is waiting for a strategy
	ACTING, //Player is executing the strategy
	RUNNING, //Game is running
	LOST, //End of Game (Players lost)
	VICTORY //End of Game (Players victory)
}
