package topLevel;

import state.Board;
import actions.Move;
import actions.Move.Side;
import persistence.DBHandler;

public class CheckersLearningAgent {
	Board bd = null;
	Move mv = null;
	Move.Side col = null;
	Side s = null;
	
	public CheckersLearningAgent(DBHandler db, boolean alphaBeta){
		//here's the constructor
		
		 
		bd = new Board(db, alphaBeta);
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
		Move mv = new Move(mv_sb);
		String boardAnswer = bd.acceptMoveAndRespond(mv);
		//String answer = new String("(2:4):(3:5)");//temporary
		return boardAnswer;
		}
	
	
	
	

}
