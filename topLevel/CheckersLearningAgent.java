package topLevel;

import state.Board;
import actions.Move;
import actions.Step;
import actions.Move.Side;

public class CheckersLearningAgent {
	Board bd = null;
	Move mv = null;
	String col = null;
	Side s = null;
	
	public CheckersLearningAgent(){
		//here's the constructor
		bd = new Board();
		
	}
	
	public StringBuffer init(String color ){
		//returns either a move or an ack, depending on color
		s = convertString2Side(color);
		mv = new Move(s);
		StringBuffer answer = new StringBuffer(color);
		return answer;
	}
	
	private Side convertString2Side(String color){
		//no defense
		Side result=Side.BLACK;
		char[] letters= new char[1];
		color.getChars(0,1, letters,0);
		char firstLetter = letters[0];
		switch (firstLetter){
		case 'B':
			result= Side.BLACK;
		case 'W':
			result= Side.WHITE;
	}
		return result;}
	
	public StringBuffer acceptMoveAndRespond(StringBuffer move){
		//to make a move, need to choose a piece that can move, and
		//figure out what to do with that piece
		//get all possible next moves(board), samuel p. 212
		java.util.List <Step> poss = bd.getAllPossibleNextSteps();
		//
		StringBuffer answer = new StringBuffer("(2:4):(3:5)");
		return answer;}
	
	
	
	

}
