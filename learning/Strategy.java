package learning;

import persistence.DBHandler;
import valuation.Evaluator;

public class Strategy {
	boolean [] upHyperplane;
	boolean [] downHyperplane;
	Evaluator myEvaluator=null;
	double[] proposedWeights;
	double[] proposedWeightChanges;
	boolean applySimulatedAnnealing = true;
	long gameEndHorizon = 0;
	public Strategy(){
		
	}
	public boolean[] getUpHyperplane(){
		return this.upHyperplane;
	}
	public void setUpHyperplane(boolean[] update ){
		this.upHyperplane = update;	
	}
	public boolean[] getDownHyperplane(){
		return this.downHyperplane;
	}
	public void setDownHyperplane(boolean[] update ){
		this.downHyperplane = update;		
	}
	public double[] getProposedWeights(){
		double[] proposed;
		proposed = myEvaluator.getWeightValues();//these are the starting weight values, what are we going to do with them?
		return this.proposedWeights;
	}
	public void setProposedWeights(double[] weights){
		this.proposedWeights  = weights;
	}
	public void calculateProposedWeights(){
		//first figure out the changes to weights
		//then, if simulated annealing is being used, scale down the changes
		if(applySimulatedAnnealing){
			long now = System.currentTimeMillis();
			for(int i = 0; i<DBHandler.NUMPARAMS; i++){
				proposedWeightChanges[i]=proposedWeightChanges[i]* (gameEndHorizon - now);
			}
		}
		//apply the changes
		for(int i = 0; i<DBHandler.NUMPARAMS; i++){
			proposedWeights[i]+=proposedWeightChanges[i];
		}
	}
	public void setGameEndHorizon(long horizon){
		this.gameEndHorizon = horizon;
	}
	public void setApplySimulatedAnnealing(boolean using){
		this.applySimulatedAnnealing = using;
	}

}
