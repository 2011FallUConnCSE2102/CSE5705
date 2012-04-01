package actions;
//holds a move, which is a sequence of steps
//if there are moves with captures (jumps) must capture
//free to choose which move with capturing, but in that move must capture all that can be captured in that sequence


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
	private int[][] samloc={{32, 33, 34, 35},
			   {28,29,30,31},
				{23,24,25,26},
				{19,20,21,22},
				{14,15,16,17},
				{10,11,12,13},
				{5,6,7,8},
				{1,2,3,4}};
			
	
	public Move(){
		theSteps = new java.util.ArrayList <Step>(); 	
	}
	
	public Move(Side s){
		theSteps = new java.util.ArrayList <Step>(); 
		this.whoseTurn = s;
	}
	public Move(String s){
		//strings are of the form Move:<color>:<row-col>:<row-col>^+
		//<row-col> is of the form (<row>:<col>)
		theSteps = new java.util.ArrayList <Step>();
		int nchars = s.length();
		char[] theChars = s.toCharArray();
		char theChar;
		int howManyColons = 0;
		int theRow = -1;
		int theColumn = -1;
		boolean stepStart = false; //first and other odd '(' makes this true, even '(' makes it false
		Move.Side side = Move.Side.BLACK;
		StringBuffer assembledNumber_sb= new StringBuffer("");
		int stepIndex = -1;
		for (int i = 0; i<nchars; i++){
			theChar=theChars[i];
			switch(theChar){
				case 'M': case'o': case'v': case'e':
						break;
				case ':':
					howManyColons++;
					if ((howManyColons>2)&&(howManyColons%2==1)){//end of start number
							int assembledNumber_int=-1;
							
							try{
								Integer assembledNumber_Int = Integer.valueOf(assembledNumber_sb.toString());
								assembledNumber_int = assembledNumber_Int.shortValue();
							}
						    catch (Exception ex){System.err.println("Move::constructor(String): number assembly "+assembledNumber_sb);}
		                    theRow = assembledNumber_int;
		                    //need to clear out assembledNumber_sb
		                    assembledNumber_sb.replace(0, assembledNumber_sb.length(),"");
		                    //System.err.println("Move::the assmbled"+assembledNumber_sb);
						}//end of odd colon handling
					else if  ((howManyColons>2)&&(howManyColons%2==0)){//end of end number
		                          int assembledNumber_int=-1;		
							try{
								Integer assembledNumber_Int = Integer.valueOf(assembledNumber_sb.toString());
								assembledNumber_int = assembledNumber_Int.shortValue();
							}
						    catch (Exception ex){System.err.println("Move::constructor(String): number assembly "+assembledNumber_sb);}
							//now having row and column
							theColumn = assembledNumber_int;
							assembledNumber_sb.replace(0, assembledNumber_sb.length(),"");
							int sam_loc = samloc[theRow][theColumn];
		                    //convert the row and column into sam notation
							if (stepStart){getStep(stepIndex).setStartLocation(sam_loc);}
							else {getStep(stepIndex).setEndLocation(sam_loc);}
						}//end of even colon handling
						break;
				case 'W':
						side = Move.Side.WHITE;
						break;
				case'h': case'i': case 't': /*case'e':*/ case'B': case'l': case'a': case 'c': case'k':
						break;
				case '(':
						//beginning of step
						Step step = new Step();
						stepIndex++;
						addStep(step);
						if(assembledNumber_sb.length()==0){;}
						else {assembledNumber_sb.replace(0,assembledNumber_sb.length()-1,"");} //clear out the assembled number
						stepStart = ! stepStart;
						break;
				case ')':
						break;
				default:
						//it's a digit, so assemble digits into numbers
						assembledNumber_sb.append(theChar);
						
						}	
				}//loop on characters
		
	}
	public void init() {
		theSteps.clear();
		startingLocation = -1;
		endingLocation = -1;
	}
	public void addStep (Step s){
		if(theSteps.size() >0){
	       Step lastStep = theSteps.get(theSteps.size()-1);
	       if (lastStep.getEndLocation() == s.getStartLocation()){
	    	 theSteps.add(s);
	    	 endingLocation = s.getEndLocation();
	    }}
		else{
			theSteps.add(s);
		    startingLocation=s.getStartLocation();
		     endingLocation = s.getEndLocation();}
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
	public String toString(){
		StringBuffer move_sb = new StringBuffer("");
		int numSteps = theSteps.size();
		for(int i=0; i<numSteps;i++){
			move_sb.append(theSteps.get(i).toString());
			if (i<numSteps-1) move_sb.append(':');//syntax is steps separated by colons
		}
		return move_sb.toString();
	}
	
}
