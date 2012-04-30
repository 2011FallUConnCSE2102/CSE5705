package learning;
import valuation.Evaluator;

public class GeneticAlgorithmStrategy extends Strategy {
	boolean [] upHyperplane;
	boolean [] downHyperplane;
	Evaluator myEvaluator=null;
	boolean applySimulatedAnnealing = true;
	long gameEndHorizon = 0;
	
	public GeneticAlgorithmStrategy(boolean usingSimAnneal){
		this.applySimulatedAnnealing = usingSimAnneal;
	}
	public boolean[] getUpHyperplane(){
		return this.upHyperplane;
	}
	public boolean[] getDownHyperplane(){
		return this.downHyperplane;
	}
	public double[] getProposedWeights(){
		double[] proposed;
		proposed = myEvaluator.getWeightValues();//these are the starting weight values, what are we going to do with them?
		return proposed;
	}
	public void updateUpHyperplane(boolean[] update ){
		this.upHyperplane = update;
		
	}
	public void updateDownHyperplane(boolean[] update ){
		this.downHyperplane = update;
		
	}

}
