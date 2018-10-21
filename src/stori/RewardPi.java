package stori;

public class RewardPi {

	public int totalStaking;
	public int stakingThisWeek;
	public int orderOfWeek;
	
	public RewardPi() {
		totalStaking = 0;
		orderOfWeek = 0;	// 해당 주에 순위
		stakingThisWeek = 0;
	}
	
	public void doStaking(int staking) {
		stakingThisWeek += staking;
		
		//System.out.println("totalStaking : " + totalStaking);
	}
	
	public int getStakingThisWeek() {
		return stakingThisWeek;
	}
	
	public int getTotalStaking() {
		return totalStaking;
	}
	
	public void setTotalStaking(int staking) {
		this.totalStaking = staking;
	}
	
}
