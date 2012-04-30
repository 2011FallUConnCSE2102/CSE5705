package learning;
import persistence.DBHandler;
import state.Board;
import valuation.Evaluator;

public class Correlation {
	double prescribedMax = 1L<<20;//made this up, see Samuels p. 219, powers of 2
	Board myBoard = null;
	Evaluator myEvaluator = null;
	double[] correlations= new double[DBHandler.NUMPARAMS];
	
	
	
	public Correlation(Board bd){
		myBoard = bd;
		myEvaluator = bd.myEvaluator;
		
		
	}
	public void correlateSignsFeaturesDelta(double delta, Board bd){//Samuels p. 219
		System.err.println("Correlation::correlatesSigns: delta "+delta);
		double[] weight = myBoard.myEvaluator.getWeightValues();
		for (int i = 0; i<DBHandler.NUMPARAMS; i++){
			if((weight[i]>0 && delta>0) || (weight[i]<0)&& delta<0){
				correlations[i]++;
			}
		}
	}
	public void adjustWeights(){//Samuels p. 219
		//set largest to max
		//set others proportionately
		System.err.println("Correlation::adjustCorrCoeff:");
		int which = largestCorrelationCoefficient();
		double previous = myEvaluator.getWeight(which);
		myEvaluator.setWeight(which,prescribedMax);
		double scale = prescribedMax/previous;
		if(myBoard.mySamuelStrategy.applySimulatedAnnealing){
			long now = System.currentTimeMillis();
			scale = scale *  (myBoard.mySamuelStrategy.gameEndHorizon - now);
		}
		myEvaluator.scaleWeights(which,scale);	
	}
	public int largestCorrelationCoefficient(){
		int which = 0;
		double currentMax = -99;
		for  (int i = 0; i<DBHandler.NUMPARAMS; i++){
			if(correlations[i]>currentMax){
				which=i;
				currentMax = correlations[i];
			}
		}
		
		return which;
	}

}
