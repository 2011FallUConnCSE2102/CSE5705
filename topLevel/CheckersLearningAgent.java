package topLevel;

import state.Board;
import actions.Move;
import actions.Move.Side;

public class CheckersLearningAgent {
	Board bd = null;
	Move mv = null;
	Move.Side col = null;
	Side s = null;
	
	public CheckersLearningAgent(){
		//here's the constructor
		bd = new Board();
		
	}
	
	public StringBuffer init(String color ){
		//returns either a move or an ack, depending on color
		s = convertString2Side(color);
		StringBuffer answer=new StringBuffer("");
		char cr = color.charAt(0);
		switch (cr){
		case 'B':
			col = Move.Side.BLACK;
			answer = bd.initiateMove(col);
			break;
		default:
			col = Move.Side.WHITE;
			answer.append("OK");
		}
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
	
	public StringBuffer acceptMoveAndRespond(Move sm){
		//to make a move, need to choose a piece that can move, and
		//figure out what to do with that piece
		//get all possible next moves(board), samuel p. 212
		StringBuffer boardAnswer = bd.acceptMove(sm);
		//
		StringBuffer answer = new StringBuffer("(2:4):(3:5)");
		return answer;
		}
	
	
	
	

}
