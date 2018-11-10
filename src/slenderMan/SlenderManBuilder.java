package slenderMan;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import sajas.core.AID;
import sajas.core.Runtime;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;

public class SlenderManBuilder extends RepastSLauncher implements ContextBuilder<Object> {

	private  ContainerController mainContainer;
	private  ContainerController agentContainer;
	private Context<Object> context;
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	@Override
	public Context<?> build(Context<Object> context) {
		this.context = context;
		context.setId("SlenderMan");

		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>(
				"infection network", context, true);
		netBuilder.buildNetwork();

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), 50,
				50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 50, 50));

		return super.build(context);
	}

	@Override
	public String getName() {
		return "NOME LAUCHER";
	}

	@Override
	protected void launchJADE() {
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		agentContainer = mainContainer;

		launchAgents();
	}
	private void launchAgents() {

		try {
			Parameters params = RunEnvironment.getInstance().getParameters();

			int slender_radius = (Integer) params.getValue("slender_radius");
			double slender_running_speed = (Double) params.getValue("slender_running_speed");
			double slender_walking_speed = (Double) params.getValue("slender_walking_speed");

			Slender s = new Slender(space, grid, slender_radius, slender_running_speed, slender_walking_speed);
			agentContainer.acceptNewAgent("Slender" + 1, s).start();
			context.add(s);

			int player_count = (Integer) params.getValue("player_count");
			int player_big_radius = (Integer) params.getValue("player_big_radius");
			int player_small_radius = (Integer) params.getValue("player_small_radius");
			int player_speed = (Integer) params.getValue("player_speed");
			Player[] players = new Player[player_count];
			for (int i = 0; i < player_count; i++) {
				Player p = new Player(space, grid, i, player_count, player_big_radius, player_small_radius, player_speed);
				agentContainer.acceptNewAgent("Player" + i, p).start();
				context.add(p);
				players[i] = p;
			}
			
			int device_timeout = (Integer) params.getValue("device_timeout");
			Tower t = new Tower(space, grid, context, players, device_timeout);
			agentContainer.acceptNewAgent("Tower", t).start();

			for (Object obj : context) {
				NdPoint pt = space.getLocation(obj);
				grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
			}

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

	}
}
