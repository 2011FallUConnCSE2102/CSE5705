package learning;

import persistence.DBHandler;
import topLevel.CheckersLearningAgent;
import actions.Move;

public class CombinationOfStrategies {//here we find whether the separate strategies agree on anything, and implement what we agree on
	int numberOfStrategies = 0;
	java.util.ArrayList <Strategy> theStrategies = null;
	boolean[][] upHyperplanes;
	boolean[][] downHyperplanes;
	double[] proposedWeights;
	boolean[] agreementUp;
	boolean[] agreementDown;
	
	public CombinationOfStrategies(){
		theStrategies = new java.util.ArrayList <Strategy>(); 
		
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
				agreementUp[whichCoefficient] = agreementUp[whichCoefficient] && upHyperplanes[duJour][whichCoefficient];
				agreementDown[whichCoefficient] = agreementDown[whichCoefficient] && downHyperplanes[duJour][whichCoefficient];
			}
			
		}
	}
	public double[] decideCoefficients(){
		double[] coefficients = null;  
		for(int whichCoefficient=1; whichCoefficient< DBHandler.NUMPARAMS; whichCoefficient++){// initialize to true, no one disagrees yet
			coefficients[whichCoefficient]=0;
		}
		//get the information
		for(int duJour=1; duJour< numberOfStrategies; duJour++){
			boolean[] tempUpHyperPlane=theStrategies.get(duJour).getUpHyperplane();
			boolean[] tempDownHyperPlane=theStrategies.get(duJour).getDownHyperplane();
			for(int feature = 0; feature<DBHandler.NUMPARAMS; feature++){
			upHyperplanes[duJour][feature]=tempUpHyperPlane[feature];
			downHyperplanes[duJour][feature]=tempDownHyperPlane[feature];
			agreementUp[feature] = true;//no one has disagreed yet, set at least often enough, not too many strategies, not too much waste
			agreementDown[feature] = true;
			}
			
		}
		//find where we agree
		
		for(int duJour=1; duJour< numberOfStrategies; duJour++){	
			for(int feature = 0; feature<DBHandler.NUMPARAMS; feature++){
				if(!upHyperplanes[duJour][feature]){
					agreementUp[feature]=false;
				}
				if(!downHyperplanes[duJour][feature]){
					agreementDown[feature]=false;
				}
				proposedWeights[feature]=0; //looking ahead to averaging
			}
			
		}
		//now that we know where we agree, what to do? go back to strategies and get info
		//we're going to average the recommendations we agree on
		
		for(int duJour=1; duJour< numberOfStrategies; duJour++){
			double[] stratWeights=theStrategies.get(duJour).getProposedWeights();;
			for(int feature = 0; feature<DBHandler.NUMPARAMS; feature++){
				if(upHyperplanes[duJour][feature] || downHyperplanes[duJour][feature])
				proposedWeights[feature]+=stratWeights[feature]/DBHandler.NUMPARAMS;
			}
			
		}
		return proposedWeights;
	}
	

}
