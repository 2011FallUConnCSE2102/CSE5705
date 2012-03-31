package parsing;

import actions.*;


public class parser {
	private int[] samloc={0,1,2,3,4,5,6,7,8,10,11,12,13,14,15,16,17,19,20,21,22,23,24,25,26,28,29,30,31,32,33,34,35};
	public Move convertString2Move(String s){
		//strings are of the form Move:<color>:<row-col>:<row-col>^+
		//<row-col> is of the form (<row>:<col>)
		Move mv = new Move();
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
				    catch (Exception ex){System.err.println("parser::convertString2Move: number assembly "+assembledNumber_sb);}
                    theRow = assembledNumber_int;
				}
				else if  ((howManyColons>2)&&(howManyColons%2==0)){//end of end number
                          int assembledNumber_int=-1;		
					try{
						Integer assembledNumber_Int = Integer.valueOf(assembledNumber_sb.toString());
						assembledNumber_int = assembledNumber_Int.shortValue();
					}
				    catch (Exception ex){System.err.println("parser::convertString2Move: number assembly "+assembledNumber_sb);}
					//now having row and column
					theColumn = assembledNumber_int;
					int sam_loc = samloc[assembledNumber_int];
                    //convert the row and column into sam notation
					if (stepStart){mv.getStep(stepIndex).setStartLocation(sam_loc);}
					else {mv.getStep(stepIndex).setEndLocation(sam_loc);}
				}
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
				mv.addStep(step);
				assembledNumber_sb.replace(0,assembledNumber_sb.length()-1,""); //clear out the assembled number
				stepStart = ! stepStart;
				break;
			case ')':
				break;
			default:
				//it's a digit, so assemble digits into numbers
				assembledNumber_sb.append(theChar);
				}	
			}//loop on characters
			
		return mv;
		
	}//end of convertString2Move
	public String convertMove2String(Move mv){
		StringBuffer sb = new StringBuffer();
		return sb.toString();
	}

}
