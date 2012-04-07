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
			
	 private int[][] samloc ={
			 {32, -1, 33, -1, 34, -1, 35, -1},//row 0
			 {-1, 28, -1, 29, -1, 30, -1, 31},//row 1
			 {23, -1, 24, -1, 25, -1, 26, -1},//row 2
			 {-1, 19, -1, 20, -1, 21, -1, 22},//row 3
			 {14, -1, 15, -1, 16, -1, 17, -1},//row 4
			 {-1, 10, -1, 11, -1, 12, -1, 13},//row 5
			 { 5, -1,  6, -1,  7, -1,  8, -1},//row 6
			 {-1,  1, -1,  2, -1,  3, -1,  4}//row 7
	 };
	 int sam_loc = 0;
	 boolean startNotWritten = true;
	public Move(){
		theSteps = new java.util.ArrayList <Step>(); 	
	}
	
	public Move(Side s){
		theSteps = new java.util.ArrayList <Step>(); 
		this.whoseTurn = s;
	}
	public Move(StringBuffer s){
		//strings are of the form Move:<color>:<row-col>:<row-col>^+
		//<row-col> is of the form (<row>:<col>)
		//System.err.println("Move::constructorFromString: with "+s);
		theSteps = new java.util.ArrayList <Step>();
		int nchars = s.length();
		char[] theChars = s.toString().toCharArray();
		char theChar;
		int howManyColons = 0;
		int theRow = -1;
		int theColumn = -1;
		whoseTurn = Move.Side.BLACK;
		StringBuffer assembledNumber_sb= new StringBuffer("");
		int stepIndex = -1;
		for (int i = 0; i<nchars; i++){
			theChar=theChars[i];
			switch(theChar){
				case 'M': case'o': case'v': case'e':
						break;
				case ':':
					howManyColons++;
					if ( howManyColons ==2){
						Step step = new Step();
						stepIndex++;
						addStep(step);
					}
					if ((howManyColons ==3)){//have a row, and it is only a starting point
							int assembledNumber_int=-1;
							
							try{
								Integer assembledNumber_Int = Integer.valueOf(assembledNumber_sb.toString());
								assembledNumber_int = assembledNumber_Int.shortValue();
							}
						    catch (Exception ex){System.err.println("Move::constructor(String)excptn: number assembly "+assembledNumber_sb);}
		                    theRow = assembledNumber_int;
		                    //need to clear out assembledNumber_sb
		                    assembledNumber_sb.replace(0, assembledNumber_sb.length(),"");
		                    //stepStart = false;
		                    //System.err.println("Move::the assembled"+assembledNumber_sb);
						}//end of odd colon handling
					else if  (howManyColons>3 && howManyColons%2 ==1){//between row, column, so have a row, and it is an end,
						//but could be a beginning if a later colon
						int assembledNumber_int=-1;
						
						try{
							Integer assembledNumber_Int = Integer.valueOf(assembledNumber_sb.toString());
							assembledNumber_int = assembledNumber_Int.shortValue();
						}
					    catch (Exception ex){System.err.println("Move::constructor(String)excptn: number assembly "+assembledNumber_sb);}
	                    theRow = assembledNumber_int;
	                    //need to clear out assembledNumber_sb
	                    assembledNumber_sb.replace(0, assembledNumber_sb.length(),"");
	                    //stepStart = false;
						}//end of odd colon handling, state is: finished collecting a row
					else if (howManyColons>5 && howManyColons%2 ==0){//a new step, start is previous (r:c), ends is coming(r:c)
						//stepStart = true;
						Step step = new Step();
						stepIndex++;
						addStep(step);
						step.setStartLocation(sam_loc);//start is previous (r:c)
					}//end of even colon handling, state is: collecting a new step
						break;
				case 'W':
						whoseTurn = Move.Side.WHITE;
						break;
				case'h': case'i': case 't': /*case'e':*/ case'B': case'l': case'a': case 'c': case'k':
						break;
				case '(':
					//state is, beginning to collect a row
						if(assembledNumber_sb.length()==0){;}
						else {assembledNumber_sb.replace(0,assembledNumber_sb.length()-1,"");} //clear out the assembled number
						break;
				case ')':
					//state is: end of (row:column), first one goes in start location of move, and first step
                        int assembledNumber_int=-1;		
					try{
						Integer assembledNumber_Int = Integer.valueOf(assembledNumber_sb.toString());
						assembledNumber_int = assembledNumber_Int.shortValue();
						//System.out.println("Move::constructor(String): number assembly "+assembledNumber_int);
					}
				    catch (Exception ex){System.err.println("Move::constructor(String): number assembly "+assembledNumber_sb);}
					//now having row and column
					theColumn = assembledNumber_int;
					assembledNumber_sb.replace(0, assembledNumber_sb.length(),"");
					//System.err.println("Move::constructor(String): with row " +theRow+" and col "+theColumn);
					sam_loc = samloc[theRow][theColumn];
                    //convert the row and column into sam notation
				    if(stepIndex==0 && startNotWritten){ //start of move only gets written once, start of step once here, later in some :'s
				    	getStep(0).setStartLocation(sam_loc);
				    	startingLocation=sam_loc;
				    	startNotWritten = false;
				    }
				    getStep(stepIndex).setEndLocation(sam_loc); //the end of step 0
					//if all characters are used up, then we have a move.
					if(i==(nchars-1)){
						System.err.println("Move::constructorFromString: with move "+this.toString());
						this.endingLocation = sam_loc;
					}
					   //state is, we are expecting a :, because we have not had last character
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
	public void setSide(Side s){
		this.whoseTurn =s;
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
		System.err.println("Move::toString: having "+numSteps+" steps");
		for(int i=0; i<numSteps;i++){
			move_sb.append(theSteps.get(i).toString());
			//System.err.println("Move::toString:  "+move_sb);
			if (i<numSteps-1) move_sb.append(':');//syntax is steps separated by colons
		}
		return move_sb.toString();
	}
	public StringBuffer toStringBuffer(){
		StringBuffer move_sb = new StringBuffer("");
		int numSteps = theSteps.size();
		System.err.println("Move::toString: having "+numSteps+" steps");
		for(int i=0; i<numSteps;i++){
			move_sb.append(theSteps.get(i).toString());
			if (i<numSteps-1) move_sb.append(':');//syntax is steps separated by colons
		}
		return move_sb;
	}
	
}
