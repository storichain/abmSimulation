package stori;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
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
import repast.simphony.util.collections.IndexedIterable;

public class PD {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int coinAsset;
	public int intervalStep;
	public StoriBoard intervalStori = null;
	GridPoint pointWithMostStoriBoard = null;
	//public JZombiesBuilder globalEnv;
//	int maxStoriLimit;
	private boolean moved;

	public PD(ContinuousSpace<Object> space, Grid<Object> grid, int count, int coin) {
		this.space = space;
		this.grid = grid;
		//recordOfmoving = new ArrayList<Object>();
		intervalStep = 0;
		this.coinAsset = coin;
		
//		Parameters params = RunEnvironment.getInstance().getParameters();
//		maxStoriLimit = (Integer) params.getValue("max_stori");
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		
		int randomCoin = 0;
		
		if( !checkMaxLimitStaking() ) {
//			System.out.println("스테이킹할 돈이 없습니다.");
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			System.out.println("static evn : " + JZombiesBuilder.LiquidToken);
			if(MyContextBuilder.LiquidToken > 0) {
				randomCoin = RandomHelper.nextIntFromTo(1, 5);
				if(randomCoin <= MyContextBuilder.LiquidToken) {
					MyContextBuilder.LiquidToken -= randomCoin;
					this.coinAsset = randomCoin;
					System.out.println("randomCoin : " + randomCoin);
				}					
			}
			
			return;
		}
		
		if(intervalStep == 0){		
		
			intervalStep = 3;
			intervalStori = null;
			pointWithMostStoriBoard = null;
			
			// get the grid location of this Zombie
			GridPoint pt = grid.getLocation(this);
						
			Context<Object> contextTmp = ContextUtils.getContext(this);
			//System.out.println("Storiboard Count : " + contextTmp.getObjects(StoriBoard.class).size());
			
			IndexedIterable<Object> storiIter  = contextTmp.getObjects(StoriBoard.class);
			
			//Context<Object> context = ContextUtils.getContext(obj);
			Network<Object> net = (Network<Object>)contextTmp.getProjection("staking network");
			
			StoriBoard storiTmp = null;	
						
//			System.out.println("스토리갯수 : " + storiIter.size());
			
//			if(storiIter.size() > 19 && storiIter.size() <= 20) {				
//			
//				for (int n=0; n < storiIter.size(); n++) {
//					System.out.println(" text : "+ ((StoriBoard)storiIter.get(n)).pi.getTotalStaking());
//				}
//			}
			
			
			if(storiIter.size() > 3) { // 전체적으로 최소 3개 이상의 스토리가 만들어지면 이 방식으로 투자자 움직임

				storiTmp = (StoriBoard) storiIter.get(RandomHelper.nextIntFromTo(0, storiIter.size()-1));
				for(int y=0; (y < 10 && net.isAdjacent(this, storiTmp)); y++) {
					storiTmp = (StoriBoard) storiIter.get(RandomHelper.nextIntFromTo(0, storiIter.size()-1));
				}
				
				pointWithMostStoriBoard = grid.getLocation(storiTmp);
				intervalStori = storiTmp;
				
			}else { // 기존에 좀비가 움직이는 방식으로 작동
				// use the GridCellNgh class to create GridCells for the surrounding neighborhood.		
				// 가까운 작가들이 모여 있는 곳으로 이동한다
				GridCellNgh<StoriBoard> nghCreator = new GridCellNgh<StoriBoard>(grid, pt, StoriBoard.class, 1, 1);
				List<GridCell<StoriBoard>> gridCells = nghCreator.getNeighborhood(true);
				
				// 이웃의 8 셀을 램덤으로 섞는다
				SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		 
				//GridPoint pointWithMostStoriBoard = null;
				int maxCount = -1;
				for (GridCell<StoriBoard> cell : gridCells) {
					if (cell.size() > maxCount) {
						pointWithMostStoriBoard = cell.getPoint();
						maxCount = cell.size();
					}
				}
			}		
			
		}else {
			pointWithMostStoriBoard = grid.getLocation(intervalStori);
		}
		// 스토리보드가 몰려 있는 곳으로 움직인다.
		moveTowards(pointWithMostStoriBoard);
		staking();
		
		intervalStep--;
	}

	public void moveTowards(GridPoint pt) {
		//System.out.println("Storiboard 좌표 : " + pt);
		if(pt != null) {
			// only move if we are not already in this grid location
			if (!pt.equals(grid.getLocation(this))) {
			
				NdPoint myPoint = space.getLocation(this);
				NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
				double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,otherPoint);
				space.moveByVector(this, 1, angle, 0);
				myPoint = space.getLocation(this);
				grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
				
				// watchFieldName by Human run(), I think that false allocation is auto??
				moved = true;
			}
		}else {
			System.out.println("투자자 안 움직임");
		}
	}

	public void staking() {
		GridPoint pt = grid.getLocation(this);
		List<Object> storiBoardListTmp = new ArrayList<Object>();
		
		// 같은 위치에 스토리가 있으면
		for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {
			if (obj instanceof StoriBoard) {
				storiBoardListTmp.add(obj);
			}
		}
		
		//System.out.println("storiBoardListTmp.size() : "+storiBoardListTmp.size());
		if (storiBoardListTmp.size() > 0) {
			// If there are plural stories, only pick one story by random
			int index = RandomHelper.nextIntFromTo(0, storiBoardListTmp.size() - 1);
			//System.out.println("index : "+index);
			
			StoriBoard obj = (StoriBoard)storiBoardListTmp.get(index);
			Context<Object> context = ContextUtils.getContext(obj);

	//		@SuppressWarnings("unchecked")
			Network<Object> net = (Network<Object>) context.getProjection("staking network");
			
			
			//int tmp = this.coinAsset-1;
			//System.out.println("tmp : " + tmp);
			if(this.coinAsset > 0) {
				//staking 이 할때는 무조건 1코인만 함.
				this.coinAsset -= 1;			
				obj.pi.doStaking(1);
				
				//System.out.println("obj 이름  first  : " + obj);
				if(net.isAdjacent(this, obj)) {
					System.out.println("already staking");					
				}else {
					net.addEdge(this,obj);					
					//System.out.println("Title : " + obj.getTitle());									
				}
				//System.out.println("obj 이름 net : " + net.getAdjacent(obj));   
				Iterable<Object> iterNet = net.getAdjacent(obj);
		
				for(Object cell : iterNet) {
					//System.out.println(cell.toString());
//					System.out.println(" :: " + cell.getClass());
//					System.out.println(" :: " + cell.getClass().getName());
//					System.out.println(" :: " + cell.getClass().getName().compareTo("jzombies.Human"));
					if(cell.getClass().getName().compareTo("jzombies.Human")==0) {
						((ST)cell).setEnergy(this.coinAsset);				
					}						
				}
			}			
		}
	}
	
	public boolean checkMaxLimitStaking() {
		//System.out.println("투자자 자산 : " + this.coinAsset);
		if(this.coinAsset > 0) {
			return true;
		}
		return false;	
	}
}
