package stori;

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
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class MyContextBuilder implements ContextBuilder<Object> {

	public static int initialMaxStoriLimit;
	public static int LiquidToken;
	GrowthEval growth;
//	Schedule schedule;
	
	@Override
	public Context build(Context<Object> context) {
		context.setId("stori");
		
		LiquidToken = 0;
		//movedRange = 5;

		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("staking network", context, true);
		netBuilder.buildNetwork();

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), 50,50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 50, 50));

		Parameters params = RunEnvironment.getInstance().getParameters();
		initialMaxStoriLimit = (Integer) params.getValue("max_stori");
		int initialZombieCount = (Integer) params.getValue("zombie_count");
		LiquidToken = (Integer) params.getValue("saled_token");
		int randomCoin = 0;
		for (int i = 0; i < initialZombieCount; i++) {
			randomCoin = RandomHelper.nextIntFromTo(1, 10);
			LiquidToken -= randomCoin;
			if(LiquidToken < 0) {
				LiquidToken = randomCoin = 0;				
			}				
			context.add(new PD(space, grid, i, randomCoin));
		}

		int initialHumanCount = (Integer) params.getValue("human_count");
		
		for (int i = 0; i < initialHumanCount; i++) {
			int energy = RandomHelper.nextIntFromTo(4, 10);
			context.add(new ST(space, grid, energy, i, initialMaxStoriLimit));
		}
		
		// 모든 Obj을  space 에  넣는 부분
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}
		
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(20);
		}
		
		
		// 초기에 스토리 1개 생성해 놓음
		String tempstr = "제네시스 스토리";
		System.out.println("tempstr : "+ tempstr);
		StoriBoard storiB = new StoriBoard(space, grid, tempstr);
		context.add(storiB);
		space.moveTo(storiB, 0, 0);
		grid.moveTo(storiB, 0, 0);
		Network<Object> net = (Network<Object>)context.getProjection("staking network");
		net.addEdge(storiB, storiB);

		System.out.println("check Print Console on Context Build");
		System.out.println("check context name : " + context.toString());
		
		System.out.println("check total collections : " + context.size());		
		System.out.println("check total human collections : " + context.getObjects(ST.class).size());
		System.out.println("check total zombi collections : " + context.getObjects(PD.class).size());

		
		/*
		growth = new GrowthEval();		
		schedule = new Schedule();		
		System.out.println(schedule.schedule(growth));
		schedule.execute();
		*/
		// 이렇게 해야 Scheduler 에 포함되고, Annotation 조건에 따라서 실행딤
		context.add(new GrowthEval());
		
		return context;
	}
}
