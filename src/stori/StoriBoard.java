package stori;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class StoriBoard {

	public int storiNum;
	public RewardPi pi;
	String storiTitle;
	public int growthIndex;

	public StoriBoard (ContinuousSpace<Object> space, Grid<Object> grid, String title) {
		//this.energy = startingEnergy = energy;
		//this.storiList = new List(storiName);
		this.pi = new RewardPi();
		this.storiTitle = title;
		growthIndex = 0;
	}
	
	public String getTitle() {
		return this.storiTitle;
	}
	
	// Need to make Chart on Ω««‡√¢
	public int getTotalStaking() {
		return pi.getTotalStaking();
	}
	
	public void setTotalStaking(int staking) {
		pi.setTotalStaking(staking);
	}
}
