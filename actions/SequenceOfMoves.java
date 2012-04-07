package actions;

public class SequenceOfMoves {
  //a sequence of moves is intended to useful for looking ahead, which we plan to do max/min wise
	java.util.List <Move> theMoves;
	
	public void initSequence() {
		theMoves.clear();
	}
	public void addMove (Move mv){
	    Move lastMove = theMoves.get(theMoves.size()-1);
	    if ((lastMove.getEndLocation()) == (mv.getStartLocation()))
	    	 {theMoves.add(mv);}
	}
	public void revokeMove (){
		theMoves.remove(theMoves.size()-1);
	}
}
