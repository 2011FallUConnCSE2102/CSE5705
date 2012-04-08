package actions;

public class SetOfMoves {
	 //a set of moves is intended to useful for examining possible next moves
		java.util.ArrayList <Move> theMoves = new java.util.ArrayList<Move>();
		public SetOfMoves(){
			
			
		}
		
		public void initSequence() {
			theMoves.clear();
		}
		public void addMove (Move mv){
		    	 theMoves.add(mv);
		}
		public void removeLastMove (){
			theMoves.remove(theMoves.size()-1);
		}
		public void addMove (Move mv, int i){
	    	 {theMoves.set(i,mv);}
	}
	public Move getMove (int i){
		return theMoves.get(i);
	}
	public boolean isEmpty(){
		return theMoves.isEmpty();
	}

	public int howMany() {
		
		return theMoves.size();
	}

}
