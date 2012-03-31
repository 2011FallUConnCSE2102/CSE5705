package actions;
//holds a move, which is a sequence of steps

public class Move {
	//a move occupies a turn. A move has one or more steps.
	public enum Side {
		BLACK,WHITE
	};
	int startingLocation = -1;
	int endingLocation =  -1;
	boolean becomeKing = false;
	int captures = -1;
	Side whoseTurn = null;
	java.util.ArrayList <Step> theSteps = null;
	
	public Move(){
		theSteps = new java.util.ArrayList <Step>(); 
		
	}
	
	public Move(Side s){
		theSteps = new java.util.ArrayList <Step>(); 
		this.whoseTurn = s;
	}
	public void init() {
		theSteps.clear();
		startingLocation = -1;
		endingLocation = -1;
	}
	public void addStep (Step s){
	    Step lastStep = theSteps.get(theSteps.size()-1);
	    if (lastStep.getEndLocation() == s.getStartLocation()){
	    	 theSteps.add(s);
	    	 endingLocation = s.getEndLocation();
	    }
	}
	public void revokeStep (){
		theSteps.remove(theSteps.size()-1);
		if(theSteps.size() == 0) {
			this.startingLocation = -1;
			this.endingLocation = -1;}
		else this.endingLocation = theSteps.get(theSteps.size()-1).getEndLocation();
		
	}
	public int getHowManySteps(){
		return theSteps.size();
	}
	public Step getStep(int i){
		return theSteps.get(i);
	}
	public Side getSide(){
		return this.whoseTurn;
	}
	public int getStartLocation(){
		return this.startingLocation;
	}
	public int getEndLocation(){
		return this.endingLocation;
	}
	
}
