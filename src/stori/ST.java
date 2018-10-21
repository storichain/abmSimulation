package stori;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class ST {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int energy;//, startingEnergy;

	public List<Object> storiboardList;
	public int maxStoriLimit;
	
	public ST(ContinuousSpace<Object> space, Grid<Object> grid, int energy, int count, int maxStori) {
		this.space = space;
		this.grid = grid;
		this.energy = energy;
//		this.ownStoriBoardName = "";	// isEmpty() works after setting this ""
		this.storiboardList  = new ArrayList<Object>();
		//this.countNumber = count;
		this.maxStoriLimit = maxStori;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public void setEnergy(int ener) {
		this.energy = ener;
	}
	
	@Watch(watcheeClassName = "stori.PD", watcheeFieldNames = "moved", query = "within_vn 5", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void run() {	
		//this.wait();
						
		// get the grid location of this Human
		GridPoint pt = grid.getLocation(this);

		// use the GridCellNgh class to create GridCells for the surrounding neighborhood.
		GridCellNgh<StoriBoard> nghCreator = new GridCellNgh<StoriBoard>(grid, pt,StoriBoard.class, 1, 3);
		//GridCellNgh<StoriBoard> nghCreator = new GridCellNgh<StoriBoard>(grid, pt,StoriBoard.class, 1, 1);
		List<GridCell<StoriBoard>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

		GridPoint pointWithLeastStoriboard = null;
		int minCount = Integer.MAX_VALUE;
		for (GridCell<StoriBoard> cell : gridCells) {
			if (cell.size() < minCount) {
				pointWithLeastStoriboard = cell.getPoint();
				minCount = cell.size();
			}
		}
		
        if (energy > 0) {
        	// Max number of storiBoard = 3 and energy level >= 7 can make storiboard
        	if(energy > 7 && storiboardList.size() < maxStoriLimit) {
				Context<Object> context = ContextUtils.getContext(this);
				NdPoint spacePt = space.getLocation(this);
				GridPoint ptBoard = grid.getLocation(this);
				
				//this.ownStoriBoardName = "스토리네임";
				String tempstr = "스토리"+storiboardList.size();
	//			System.out.println("tempstr : "+ tempstr);
				StoriBoard storiB = new StoriBoard(space, grid, tempstr);
				storiboardList.add(storiB);
				context.add(storiB);
				space.moveTo(storiB, spacePt.getX(), spacePt.getY());
				grid.moveTo(storiB, ptBoard.getX(), ptBoard.getY());
				Network<Object> net = (Network<Object>)context.getProjection("staking network");
				net.addEdge(this, storiB);
        	}
			moveTowards(pointWithLeastStoriboard);
			
		} else {
			//energy = startingEnergy;
			energy = RandomHelper.nextIntFromTo(4, 10);
			//System.out.println("random energy : " + energy);
		}
   //     System.out.println("check energy : " + energy);
        //Parameters params = RunEnvironment.getInstance().getParameters();
        //System.out.println("param check : " + (Integer) params.getValue("human_count"));
	}	
	
	
	public void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 2, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
			energy--;
		}
	}

}
