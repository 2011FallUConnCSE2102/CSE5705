package decisionmaking;

import actions.Move;

public class CheckersTreeEdge {
	
	CheckersTreeNode parent = null;
	Move mv = null;
	
	public CheckersTreeEdge(){
		
	}
	public CheckersTreeEdge(CheckersTreeNode p){
		parent = p;
		
	}
	public CheckersTreeEdge(Move m){
		mv = m;
		
	}
	public CheckersTreeEdge(CheckersTreeNode p, Move m){
		parent = p;
		mv = m;
		
	}
	

}
