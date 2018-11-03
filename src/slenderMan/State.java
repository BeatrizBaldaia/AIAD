package slenderMan;

public enum State { 
	EXPLORING, //Player is tring to find all devices
	WAITING, //Player already knows all devices' location, but is waiting for a strategy
	ACTING //Player is executing the strategy
}
