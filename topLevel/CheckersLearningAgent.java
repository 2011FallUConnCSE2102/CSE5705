package topLevel;

import learning.CombinationOfStrategies;
import learning.GeneticAlgorithmStrategy;
import learning.SamuelStrategy;
import state.Board;
import state.Piece;
import actions.Move;
import actions.Step;
import actions.Move.Side;
import persistence.DBHandler;

public class CheckersLearningAgent {
	Board bd = null;
	DBHandler db;
	Move mv = null;
	Move.Side col = null;
	Side s = null;
	boolean alphaBeta = false;
	CombinationOfStrategies comb = null;
	SamuelStrategy ss = null;
	GeneticAlgorithmStrategy gen = null;
	public enum Strategy {
		SAMUEL, GENETIC, ENTROPY
	};
	int TTG=-1;
	
	public CheckersLearningAgent(DBHandler db, boolean alphaBeta, Board bd, boolean usingSimulatedAnnealing){
		//here's the constructor
		this.bd = bd;
		this.db = db;
		this.alphaBeta = alphaBeta;
		if(alphaBeta){//do what strategies need at beginning of game
			comb = new CombinationOfStrategies();
			ss = new SamuelStrategy(usingSimulatedAnnealing);
			comb.addStrategy(ss);
			bd.setSamuelStrategy(ss);
			gen = new GeneticAlgorithmStrategy(usingSimulatedAnnealing);
			comb.addStrategy(gen);	
		}	
	}
	
	public String init(String color ){
		//returns either a move or an ack, depending on color
		//System.err.println("CLA::init: with color "+color);
		s = convertString2Side(color);
		//System.err.println("CLA::init: with s "+s);
		StringBuffer answer=new StringBuffer("");
		char cr = color.charAt(0);
		switch (cr){
		case 'B':
			col = Move.Side.BLACK;
			answer = bd.initiateMove(col);
			bd.vb.addState(bd.FAw, bd.FAb, bd.BAw, bd.BAb);
			break;
		default:
			col = Move.Side.WHITE;
			answer.append("OK");
		}
		return answer.toString();
	}
	
	private Side convertString2Side(String color){
		//no defense
		Side result=Side.BLACK;
		char[] letters= new char[1];
		color.getChars(0,1, letters,0);
		char firstLetter = letters[0];
		//System.err.println("CLA::convertString2Side: with "+firstLetter);
		switch (firstLetter){
		case 'B':
			result= Side.BLACK;
			break;
		case 'W':
			result= Side.WHITE;
	}
		return result;}
	
	public String acceptMoveAndRespond(StringBuffer mv_sb){
		//to make a move, need to choose a piece that can move, and
		//figure out what to do with that piece
		//get all possible next moves(board), samuel p. 212
		Move mv = new Move(mv_sb, bd);
		String boardAnswer = bd.acceptMoveAndRespond(mv, TTG);
		//String answer = new String("(2:4):(3:5)");//temporary
		return boardAnswer;
		}
	
  public void  learnFromExperience(StringBuffer sb, String col){//do what strategies do at end of game
		//was it a win or a lose Result:B or Result:W
	  if(alphaBeta){
		switch(sb.charAt(7)){
			case 'D'://draw
				break;
			case 'W'://White
				if(col.equals("White")){db.finishGame(1, bd.myEvaluator.getWeightValues());}//we won again!
				else{db.finishGame(0, bd.myEvaluator.getWeightValues());};
				break;
			case 'B':
				if(col.equals("Black")){db.finishGame(1, bd.myEvaluator.getWeightValues());}//we won again!
				else{db.finishGame(0, bd.myEvaluator.getWeightValues());}
				break;
				default: System.err.println("CheckersLearningAgent:: learnFromExperience: unexected result message"+sb);
		}
	  
	  }
  }
	
	public void howMuchTTG(StringBuffer sb){
 
		int nchars = sb.length();
		char[] theChars = sb.toString().toCharArray();
		char theChar;
		StringBuffer assembledNumber_sb = new StringBuffer();
		//System.err.println("CLA::howMuchTTG "+sb);
		for (int i = 5; i<nchars-1; i++){
			theChar=theChars[i];
			switch(theChar){
			//case '?': case 'M': case'o': case'v': case'e': case ':':
			//			break;
				case '(':
					//state is, beginning to collect a time
						if(assembledNumber_sb.length()==0){;}
						else {assembledNumber_sb.replace(0,assembledNumber_sb.length()-1,"");} //clear out the assembled number
						break;
				case ')':
					//now we know the number is finished
					int assembledNumber_int=-1;
					
					try{
						Integer assembledNumber_Int = Integer.valueOf(assembledNumber_sb.toString());
						assembledNumber_int = assembledNumber_Int.shortValue();
					}
				    catch (Exception ex){System.err.println("CLA::howMuchTTG excptn: number assembly "+assembledNumber_sb);}
                    this.TTG = assembledNumber_int;
					
						break;
				default:
						//it's a digit, so assemble digits into numbers
						assembledNumber_sb.append(theChar);
						
						}	
				}//loop on characters
	}

}
