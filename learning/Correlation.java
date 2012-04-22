package learning;
import persistence.DBHandler;
import state.Board;
import valuation.Evaluator;

public class Correlation {
	double prescribedMax = Math.pow(2, 20);//made this up, see Samuels p. 219, powers of 2
	Board myBoard = null;
	Evaluator myEvaluator = null;
	double[] correlations={0,0,0,0,0,0,0,0,0,0,
			               0,0,0,0,0,0,0,0,0,0,
			               0,0,0,0,0,0,0,0,0,0,
			               0,0,0,0,0,0,0,0,0,0};
	
	
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
	public void adjustCorrelationCoefficients(){//Samuels p. 219
		//set largest to max
		//set others proportionately
		System.err.println("Correlation::adjustCorrCoeff:");
		int which = largestCorrelationCoefficient();
		double previous = myEvaluator.getWeight(which);
		myEvaluator.setWeight(which,prescribedMax);
		double scale = prescribedMax/previous;
		for(int i=0; i<DBHandler.NUMPARAMS; i++){
			if(i != which){myEvaluator.scaleWeights(i,scale);}
		}
		
		
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
