package learning;

import persistence.DBHandler;
import actions.Move;

public class CombinationOfStrategies {//here we find whether the separate strategies agree on anything, and implement what we agree on
	int numberOfStrategies = 0;
	java.util.ArrayList <Strategy> theStrategies = null;
	boolean[][] hyperplane;
	boolean[] agreementUp;
	boolean[] agreementDown;
	
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
	public void findAgreement(){
		for(int whichCoefficient=1; whichCoefficient< DBHandler.NUMPARAMS; whichCoefficient++){// initialize to true, no one disagrees yet
			agreementUp[whichCoefficient] = true;
			agreementDown[whichCoefficient] = true;
		}
		for(int duJour=1; duJour< numberOfStrategies; duJour++){
			//TODO hyperplane[][duJour]=theStrategies.get(duJour).getUpHyperplane();
			for(int whichCoefficient=1; whichCoefficient< DBHandler.NUMPARAMS; whichCoefficient++){// initialize to true, no one disagrees yet
				agreementUp[whichCoefficient] = agreementUp[whichCoefficient] && hyperplane[duJour][whichCoefficient];
				agreementDown[whichCoefficient] = agreementDown[whichCoefficient] && hyperplane[duJour][whichCoefficient];
			}
			
		}
	}
	public double[] decideCoefficients(){
		double[] coefficients = null;  
		for(int whichCoefficient=1; whichCoefficient< DBHandler.NUMPARAMS; whichCoefficient++){// initialize to true, no one disagrees yet
			coefficients[whichCoefficient]=0;
		}
		
		for(int duJour=1; duJour< numberOfStrategies; duJour++){
		//TODO	hyperplane[duJour][]=theStrategies.get(duJour).getHyperplane();
		}
		return coefficients;
	}
	

}
