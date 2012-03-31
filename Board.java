package state;

import exceptions.IllegalMoveException;
import actions.*;


public class Board {
	private enum Occupant {
		PAWN, NOTHING, KING, INVALID
	}
//	private enum Side {
//		BLACK, WHITE
//	}
/*	private int[] row=
		{0,7,7,7,7,6,6,6,6,5,5,5,5,4,4,4,4,3,3,3,3,2,2,2,2,1,1,1,1,0,0,0,0};//ours are 33, to save the -1
	private int[] col=
			{0,1,3,5,7,0,2,4,6,1,3,5,7,0,2,4,6,1,3,5,7,0,2,4,6,1,3,5,7,0,2,4,6};*/
	private int[] samloc=
		{0,1,2,3,4,5,6,7,8,10,11,12,13,14,15,16,17,19,20,21,22,23,24,25,26,28,29,30,31,32,33,34,35};
	
/*	private Occupant[][] whitePieces ={
		{Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING},
		{Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,   Occupant.NOTHING, Occupant.PAWN},
		{Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING},
		{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING},
		{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING},
		{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING},
		{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING},
		{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING}	
	};
	private Occupant[][] blackPieces ={
			{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING},
			{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING},
			{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING},
			{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING},
			{Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING, Occupant.NOTHING},
			{Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING},
			{Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN},
			{Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING, Occupant.PAWN,    Occupant.NOTHING}	
		};
	private Occupant[][] protagonist;
	private Occupant[][] antagonist;*/
	private Occupant[] blackPieces ={Occupant.INVALID,
			                           Occupant.PAWN,      Occupant.PAWN,      Occupant.PAWN,     Occupant.PAWN,
			                                    Occupant.PAWN,         Occupant.PAWN,     Occupant.PAWN,     Occupant.PAWN,
			                           Occupant.INVALID,//9 
			                           Occupant.PAWN,      Occupant.PAWN,      Occupant.PAWN,     Occupant.PAWN,
			                                   Occupant.NOTHING,       Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING, 
			                           Occupant.INVALID,//18
			                           Occupant.NOTHING,    Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING, 
	                                           Occupant.NOTHING,       Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING, 
	                                   Occupant.INVALID,//27
	                                   Occupant.NOTHING,    Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING, 
	                                           Occupant.NOTHING,       Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING}; 
	private Occupant[] whitePieces ={Occupant.INVALID,
			                           Occupant.NOTHING,    Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING, 
	                                            Occupant.NOTHING,       Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING,
                                       Occupant.INVALID,//9 
                                       Occupant.NOTHING,       Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING,
                                                Occupant.NOTHING,       Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING, 
                                       Occupant.INVALID,//18
                                       Occupant.NOTHING,    Occupant.NOTHING,  Occupant.NOTHING,   Occupant.NOTHING, 
                                                Occupant.PAWN,       Occupant.PAWN,        Occupant.PAWN,     Occupant.PAWN, 
                                       Occupant.INVALID,//27
                                       Occupant.PAWN,    Occupant.PAWN,  Occupant.PAWN,   Occupant.PAWN, 
                                                Occupant.PAWN,      Occupant.PAWN,      Occupant.PAWN,     Occupant.PAWN};
	private Occupant[] protagonist;
	private Occupant[] antagonist;
	
	Move.Side protSide;
	Move.Side antSide;
	/*int startRow; //this is in board square numbers as in the protocol from Prof. McCartney
	int startCol; //this is in board square numbers as in the protocol from Prof. McCartney */
	int starting; //this is in board square numbers as in the protocol from Prof. McCartney
	
	
	public void  updateState(Move mv) throws IllegalMoveException{
	//if move is invalid, board is not updated.
	if(!validMove (mv))
			throw new IllegalMoveException();
		else reviseBoard(mv);
	}
	
	private void reviseBoard(Move mv) {
		protSide = mv.getSide();
		switch (protSide){
		case WHITE:
		     protagonist = whitePieces;
		     antagonist = blackPieces;
		     //antSide = BLACK;
		     break;
		case BLACK:
		     protagonist = blackPieces;
		     antagonist = whitePieces;
		     //antSide = WHITE;
		     break;  }  
			//work with the protagonistPieces array 
			//take piece from starting point
			//check about capturing, removing if that happens
			//leave piece at ending point
			//not forgetting to promote to king
		    	 int moveStart = mv.getStartLocation();
		    	 int moveEnd = mv.getEndLocation();
		    	 Occupant pieceType = protagonist[moveStart];
		    	 protagonist[moveStart]=Occupant.NOTHING;
		    	 protagonist[moveEnd]=pieceType;	
	}


    //a reachable place is reached by some sequence of reachable steps
	public boolean validMove(Move mv){
		boolean validMove = true; //only if each step is valid is the move valid
	     for(int stepIndex = 0; (stepIndex < mv.getHowManySteps() && validMove); stepIndex++){
	    	 Step s = mv.getStep(stepIndex);
	          if (!validStep(s)) validMove = false;
	     }
	     return validMove;
	}
	
	
	public boolean validStep(Step s){
		int sam_start = samloc[s.getStartLocation()];
		int sam_end = samloc[s.getEndLocation()];
		//if we canonicalize at the server input/output, everything else is in samloc
		//valid moves having no captures, landing on unoccupied squares, differ by 4 or 5 in samloc
		if (sam_start-sam_end   == 4 ||
		    sam_start-sam_end   == 5 ||
		    sam_end  -sam_start == 4 ||
		    sam_end  -sam_start == 5) {
			Occupant theSwitchCase = antagonist[sam_end];
			switch (theSwitchCase){
			case NOTHING:
			 return true;}			
		}
		//if there is one capture, w step by 8 or 10
		if ((sam_start-sam_end   == 8  && antagonist[4]!=Occupant.NOTHING)||
			(sam_start-sam_end   == 10 && antagonist[5]!=Occupant.NOTHING)||
			(sam_end  -sam_start == 8  && antagonist[4]!=Occupant.NOTHING)||
			(sam_end  -sam_start == 10 && antagonist[5]!=Occupant.NOTHING)) if (antagonist[sam_end]==Occupant.NOTHING){
				//capture = 1;
				return true;
			}
		return false;
	}
 

}
