package valuation;

import persistence.DBHandler;
import actions.Move;
import state.Board;

public class SetOfBoardsWithPolynomial {
	private java.util.List <Board> theHistoryOfBoards;
	private java.util.List <int[]> theHistoryOfFeatureValues; //ADV is a feature, 7 is a featureValue
	private java.util.List <double[]> theHistoryOfWeightValues; //3/2 is a weight value, or we might choose powers of 2, as Samuel did
	
	public SetOfBoardsWithPolynomial(){
		theHistoryOfBoards = new java.util.ArrayList<Board>();
		theHistoryOfFeatureValues = new java.util.ArrayList<int[]>();
		theHistoryOfWeightValues = new java.util.ArrayList<double[]>();
		
		
	}
	public void addBoardPolynomial(Board bd, int[] featureValues, double[]weightValues){
		theHistoryOfBoards.add(bd);
		theHistoryOfFeatureValues.add(featureValues);
		theHistoryOfWeightValues.add(weightValues);
		//System.err.println("SetOfBoardsWithPolynomial::addBoardPolynomial"+weightValues[0]+" "+featureValues[DBHandler.PADV]);
		
	}
	public double getMostRecentScore(){
		double retval =0;
		int last = theHistoryOfFeatureValues.size()-1;
		if (last>=0){
		   for(int i = 0; i< DBHandler.NUMPARAMS; i++)
			retval += theHistoryOfFeatureValues.get(last)[i] * theHistoryOfWeightValues.get(last)[i]; 
		}
		else retval = 0;
		return retval;
	}

}
