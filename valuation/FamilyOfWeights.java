package valuation;

public class FamilyOfWeights {
	java.util.ArrayList <SetOfWeights> theSetsOfWeights = new java.util.ArrayList<SetOfWeights>();
	public FamilyOfWeights(){
		
		
	}
	
	public void initFamily() {
		theSetsOfWeights.clear();
	}
	public void addSetOfWeights (SetOfWeights mv){
	    	 theSetsOfWeights.add(mv);
	}
	public void removeLastSetOfWeights (){
		theSetsOfWeights.remove(theSetsOfWeights.size()-1);
	}
	public void addSetOfWeights (SetOfWeights s, int i){
    	 theSetsOfWeights.set(i,s);}

}
