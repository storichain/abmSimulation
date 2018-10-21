package stori;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

public class GrowthEval {

	int checkInterval;
	
	
	public GrowthEval() {		
		checkInterval = 0;			
	}
	
	// 7 ticks 간격으로 리워드파이 평가 시행
	@ScheduledMethod(start = 1, interval = 7)
	public void stepGrowth() {

		checkInterval++;
		//System.out.println("checkInterval : " + checkInterval);
		
		Context<Object> contextForEval = ContextUtils.getContext(this);
		//System.out.println(contextForEval.getObjects(StoriBoard.class));
		System.out.println("Storiboard Count : " + contextForEval.getObjects(StoriBoard.class).size());
		
		IndexedIterable<Object> storiIter  = contextForEval.getObjects(StoriBoard.class);
		
		System.out.println("현재 Tick : " + RepastEssentials.GetTickCount());
		
		
	
		// 임시로 여기서 스토리들에  GrowthIndex 점수를 랜덤으로 주고(전체 스토리에 10%),  바로 평가하자
		StoriBoard storiTmp = null;
		for(int x = 0; x < storiIter.size() * 0.1; x++) {
			storiTmp = (StoriBoard) storiIter.get(RandomHelper.nextIntFromTo(0, storiIter.size()-1));
			storiTmp.growthIndex = RandomHelper.nextIntFromTo(1, 7);  // 주당 1점에서 7점 사이의 램덤값 할당
		}
		
		// 여기서 2가지 처리 List 를 만드는 것과 Mint 처리 하는 부분
		// Mint 처리 하는 부분, 매주 한 번씩 Staking 정도에 따라서 인플레이션 처리
		List<StoriBoard> storiBoardListTmp = new ArrayList<>();	
	//	int index = 0;
		int tmpVal = 0;
		for(Object obj : storiIter) {
			// 정렬을 위한 List 만드는 부분
			storiBoardListTmp.add((StoriBoard) obj);
			// Mint 처리 부분
			tmpVal = ((StoriBoard)obj).pi.stakingThisWeek;
			if(tmpVal > 0){
				MyContextBuilder.LiquidToken += tmpVal;
				((StoriBoard)obj).pi.totalStaking += tmpVal;
				((StoriBoard)obj).pi.stakingThisWeek = 0;
			}
			//index++;
		}
		// 바로 주당 평가 시행 (Staking 금액과 growthIndex 가 가장 높은 스토리에 리워드 파이 순위 매김
		Collections.sort(storiBoardListTmp, new CompareGrowthIndex());
		

		for (int z=0 ; z < storiBoardListTmp.size() * 0.2; z++) {
			storiTmp =  storiBoardListTmp.get(z);
			storiTmp.pi.totalStaking += 1;
			// 20% 에 안에 안들지만, growthIndex가 있는 스토리는 다음에 재평가시 반영됨
			storiTmp.growthIndex = 0; 
		}
		
//		for (Object orderedList : storiBoardListTmp) {
//            System.out.println((((StoriBoard)orderedList).growthIndex));
//        }
		
	}
	
	
	// 내림차순(Desc) 정렬
	static class CompareGrowthIndex implements Comparator<StoriBoard>{
		 
        @Override
        public int compare(StoriBoard o1, StoriBoard o2) {
        	return o1.growthIndex + o1.pi.totalStaking > o2.growthIndex + o2.pi.totalStaking 
        		? -1 : o1.growthIndex + o1.pi.totalStaking < o2.growthIndex + o2.pi.totalStaking
        		? 1 : 0;
        }
    }

}
