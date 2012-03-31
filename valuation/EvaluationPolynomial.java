package valuation;

import state.*;

public class EvaluationPolynomial extends ScoreExpression {
	
	java.util.List<WeightedFeature> theWeightedFeatures = null;
	
	public EvaluationPolynomial(){
		
	}
	public void addWeightedFeature(WeightedFeature wf){
		theWeightedFeatures.add(wf);
	}
	public void removeWeightedFeatureByIndex (int i){
		theWeightedFeatures.remove(i);
	}
	public void removeThisWeightedFeature (WeightedFeature wf){
		theWeightedFeatures.remove(wf);
	}
	public void clearWeightedFeatures(){
		theWeightedFeatures.clear();
	}
	public int score(Board bd){
		//add up the weighted values of the different features on the board
		int numWeightedFeatures = theWeightedFeatures.size();
		int sumSoFar = 0;
		for(int i=0; i<numWeightedFeatures; i++){
			sumSoFar += theWeightedFeatures.get(i).getWeight().getWeight() 
					*
					theWeightedFeatures.get(i).getFeature().evalFeature(bd);
		}//end of loop
		return sumSoFar;
	}

}
