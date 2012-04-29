package decisionmaking;

import actions.*;
import state.*;
import persistence.*;

public class CheckersTreeNode {
	Board theStartingPosition = null;
	java.util.List<CheckersTreeEdge> thePossibleEdges = null; 
	Board theOnePlyResult = null; //one for each possible edge? calculate one at a time?
	VisualBoard vb = null;
	
	public CheckersTreeNode(VisualBoard vb){
		this.vb = vb;
		
	}
public CheckersTreeNode(Board bd, Move.Side side){
		theStartingPosition = bd;
		//TODO jump moves, and if not, other moves java.util.List<Step> thePossibleSteps = bd.getAllPossibleNextSteps(side);
		//want all the moves (of some length of some nature, see p. 212. A journey of a 1000 li begins with one step.
		//todo int howManyFirstSteps = thePossibleSteps.size();
		//one first step at a time
		//for(int firstStepIndex = 0; firstStepIndex< howManyFirstSteps; firstStepIndex++){
			//TODO think about how to extend these steps into moves, add 4, is it occupied, add 5, is it occupied,
			//is it a king? already did this someplace, valid move checking
		//}
	}//end of construct tree node from board
public void addEdge(CheckersTreeEdge e){
	thePossibleEdges.add(e);
}
public void removeEdgeByIndex(int i){
	thePossibleEdges.remove(i);
}
public void removeEdge(CheckersTreeEdge e){
	thePossibleEdges.remove(e);
}
public java.util.List<CheckersTreeEdge> getAllPossibleNextMoves(){
	//all possible moves: one step at a time
	//consider breadth first search? space? This is searching. Do we want to do limiting?
	//p.212 bottom, produce all possible moves from given board
	//explore each move in turn, producing new board position records, corresponding to result of move
	//old boards are saved
	return thePossibleEdges;
}
public CheckersTreeEdge getEdgeByIndex(int i){
	//if (i<thePossibleEdges.size()) probably we don't want to be defensive because we need to be fast
		return thePossibleEdges.get(i);
	//else return thePossibleEdges.get(0); or try catch or something
}
    
public Board effectMove(Move m){
	//a move is the result of steps, but board will perform a whole move
	boolean alphaBeta = true;//TODO
	DBHandler db = null;//TODO
	Board resultBoard = new Board(this.theStartingPosition, db, alphaBeta, vb);//initializing the result board
		try{
			//resultBoard.updateState(m);
		}
		catch (Exception ex){
			System.err.println("CheckersTreeNode::effectMove: could not do move"+m.toString());
		}
	return resultBoard;
}

	

}
