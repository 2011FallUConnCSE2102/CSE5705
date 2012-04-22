package learning;

import actions.Move;

public class CombinationOfStrategies {//here we find whether the separate strategies agree on anything, and implement what we agree on
	int numberOfStrategies = 0;
	java.util.ArrayList <Strategy> theStrategies = null;
	boolean[][] hyperplane;
	
	public CombinationOfStrategies(){
		
	}
	public void addStrategy(Strategy s){
		this.theStrategies.add(s);
		
	}
	public void removeStrategy(int i){
		this.theStrategies.remove(i);
	}
	public void removeStrategy(Strategy s){
		this.theStrategies.remove(s);
	}
	public double[] decideCoefficients(){
		double[] coefficients; //eek, uninitialized
		
		for(int duJour=1; duJour< numberOfStrategies; duJour++){
			hyperplane[duJour][]=theStrategies.get(duJour).getHyperplane();
		}
		return coefficients;
	}
	

}
