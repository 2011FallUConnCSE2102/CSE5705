package state;


import exceptions.IllegalMoveException;
import actions.*;
import valuation.*;


public class Board {
	private enum Occupant {
		PAWN, NOTHING, KING, INVALID
	}
	
	//could we just have one word for pawn, one for king, for each side is 4 words, because we'll be saving boards
	//never look in invalid places
	

	private int[] samloc=
		{0,1,2,3,4,5,6,7,8,10,11,12,13,14,15,16,17,19,20,21,22,23,24,25,26,28,29,30,31,32,33,34,35};
	

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

	int starting; //this is in board square numbers as in the protocol from Prof. McCartney
	
	public Board(){
		long FAw = 0L;                      // white pieces that move "forward" will only be kings, and there are none at start
		long FAb =  (long) (Math.pow(2,13)+ //forward active when black's turn: pawns in black's front row
				    (long) Math.pow(2,12)+
				    (long) Math.pow(2,11)+
				    (long) Math.pow(2,10));
		long BAw =  (long) Math.pow(2,26)+ //backward active on white's turn: pawns in white's front row
				    (long) Math.pow(2,25)+
				    (long) Math.pow(2,24)+
				    (long) Math.pow(2,23);
		long BAb =  0L;                    //backward active on black's turn: nobody, until kings				
		long FPw =  (long) Math.pow(2,35)+ //white pieces forward passive is all of them, because none are kings
				    (long) Math.pow(2,34)+
				    (long) Math.pow(2,33)+
				    (long) Math.pow(2,32)+
				    (long) Math.pow(2,31)+
				    (long) Math.pow(2,30)+
				    (long) Math.pow(2,29)+
				    (long) Math.pow(2,28)+
				    (long) Math.pow(2,26)+
				    (long) Math.pow(2,25)+
				    (long) Math.pow(2,24)+
				    (long) Math.pow(2,23);
		long FPb =  (long) Math.pow(2,1)+ //black pieces forward passive: only the back two rows, b/c cannot jump own pieces
				    (long) Math.pow(2,2)+
				    (long) Math.pow(2,3)+
				    (long) Math.pow(2,4)+
				    (long) Math.pow(2,5)+
				    (long) Math.pow(2,6)+
				    (long) Math.pow(2,7)+
				    (long) Math.pow(2,8);
		long BPw =  (long) Math.pow(2,35)+ //white pieces backward passive is back two rows
			        (long) Math.pow(2,34)+
			        (long) Math.pow(2,33)+
			        (long) Math.pow(2,32)+
			        (long) Math.pow(2,31)+
			        (long) Math.pow(2,30)+
			        (long) Math.pow(2,29)+
			        (long) Math.pow(2,28);
	    long BPb =  (long) Math.pow(2,1)+ //black pieces backward passive: all b/c no kings
			        (long) Math.pow(2,2)+
			        (long) Math.pow(2,3)+
			        (long) Math.pow(2,4)+
			        (long) Math.pow(2,5)+
			        (long) Math.pow(2,6)+
			        (long) Math.pow(2,7)+
			        (long) Math.pow(2,8)+
			        (long) Math.pow(2,10)+
			        (long) Math.pow(2,11)+
			        (long) Math.pow(2,12)+
			        (long) Math.pow(2,13);
	}
	
	public Board(Board bd){
		//all the components of board copied into new board
		blackPieces = bd.blackPieces;
		whitePieces = bd.whitePieces;
		antagonist = bd.antagonist;
		protagonist = bd.protagonist;
		protSide = bd.protSide;
		antSide = bd.antSide;
	}
	public Board(long faw,
			long fab,
			long baw,
			long bab,
			long fpw,
			long fpb,
			long bpw,
			long bpb){
	FAw = faw;
	FAb = fab;
	BAw = baw;
	BAb = bab;
	FPw = fpw;
	FPb = fpb;
	BPw = bpw;
	BPb = bpb;}
	
	
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
/*	public java.util.List <Step> getAllPossibleNextSteps(){
		java.util.List <Step> possibleSteps= null;
		//TODO are any?
		if(true){Step step = new Step();
		possibleSteps.add(step);}
		return possibleSteps;
	}*/
    public int score(ScoreExpression se){
    	int myScore = 0;
    	//TODO Samuel says, p. 212 that there is a scoring polynomial, of course it can change with time
    	//the score comes from the score expression and the piece arrangments on the board
    	//score expression is a weighted sum of features, so contains a list of weights
    	return myScore;
    }
    
    private long emptyLoc;
    private long FAw; //forward active, forward is toward high number where white starts, so on black move pawns and kings. on white move only kings
    private long BAw; //backward active, on black move, kings, on white move, pawns and kings
    private long FPw; //forward passive,  
    private long BPw; //backward passive
    private long FAb; //forward active, forward is toward high number where white starts, so on black move pawns and kings. on white move only kings
    private long BAb; //backward active, on black move, kings, on white move, pawns and kings
    private long FPb; //forward passive,  
    private long BPb; //backward passive
    private long isJump;
    private long RFw; //right forward, these are locations, corresponding to steps taken
    private long LFw; //left forward
    private long RBw; //right backward
    private long LBw; //left backward
    private long RFb; //right forward, these are locations, corresponding to steps taken
    private long LFb; //left forward
    private long RBb; //right backward
    private long LBb; //left backward
    private long empty;
    private int firstBreakPly = 3;
    private int secondBreakPly = 4;
    private int thirdBreakPly = 5;
    private int fourthBreakPly = 11;
    private int fifthBreakPly = 20;
    private int numberPiecesOnBoardThreshold = 8;
    
    public void setEmpty(){
    	long invertedempty = (FAw | BAw | FPw | BPw | FAb | BAb | FPb | BPb );//want bitwise or's
    	empty = (long) Math.pow(2, 37)-1;// start with all 0's, put 1 at 37 position, subtract 1 makes 36 1's in a row (bit 0 to bit 35)
    	//adding this to invertedempty will invert it, with a carry into the 37th bit.
    	//if we care about the upper order bits we can do something, like empty % Math.pow(2, 37)
    }
    public long getEmpty(){
    	return emptyLoc;
    }
    public long calcRFw(){
    	long nextRFw = RFw*16 //this is the shift by 4 
    			            & empty; //this is the logical and with empty
    	return nextRFw;
    	
    }
    public long calcRFb(){
    	long nextRFb = RFb*16 //this is the shift by 4 
    			            & empty; //this is the logical and with empty
    	return nextRFb;
    	
    }
    public long calcLFw(){
    	long nextLFw = LFw*32 //this is the shift by 4 
    			            & empty; //this is the logical and with empty
    	return nextLFw;
    	
    }
    public long calcLFb(){
    	long nextLFb = LFb*32 //this is the shift by 4 
    			            & empty; //this is the logical and with empty
    	return nextLFb;
    	
    }
    public long calcRBw(){
    	long nextRBw = RBw/16 //this is the shift by 4 
    			            & empty; //this is the logical and with empty
    	return nextRBw;
    	
    }
    public long calcRBb(){
    	long nextRBb = RBb/16 //this is the shift by 4 
    			            & empty; //this is the logical and with empty
    	return nextRBb;
    	
    }
    public long calcLBw(){
    	long nextLBw = LBw/32 //this is the shift by 4 
    			            & empty; //this is the logical and with empty
    	return nextLBw;
    	
    }
    public long calcLBb(){
    	long nextLBb = LBb/32 //this is the shift by 4 
    			            & empty; //this is the logical and with empty
    	return nextLBb;
    	
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    public long calcjRFw(){
    	long nextRFw = RFw*16*16 //this is the shift by 4 
    			            & empty //this is the logical and with empty
    	                    &  RFw*16 //this is the intermediate location
    	                    &  (FAb | BAb | FPb | BPb);     //these are any opponent piece, occupying the spot over which jumping
    	return nextRFw;
    	
    }
    public long calcjRFb(){
    	long nextRFb = RFb*16*16 //this is the shift by 4 
    			            & empty //this is the logical and with empty
    	                    &  RFb*16 //this is the intermediate location
    	                    &  (FAw | BAw | FPw | BPw);     //these are any opponent piece; //this is the logical and with occupied
    	return nextRFb;
    	
    }
    public long calcjLFw(){
    	long nextLFw = LFw*32*32 //this is the shift by 4 
    			            & empty //this is the logical and with empty
    	                    &  LFw*32 //this is the intermediate location
    	                    &  (FAb | BAb | FPb |BPb);     //these are any opponent piece; //this is the logical and with occupied
    	return nextLFw;
    	
    }
    public long calcjLFb(){
    	long nextLFb = LFb*32*32 //this is the shift by 4 
    			            & empty //this is the logical and with empty
    			            &  LFb*32 //this is the intermediate location
    	                    &  (FAw | BAw | FPw | BPw);     //these are any opponent piece; //this is the logical and with occupied
    	return nextLFb;
    	
    }
    public long calcjRBw(){
    	long nextRBw = (RBw/16)/16 //this is the shift by 4 
    			            & empty //this is the logical and with empty
    			            &  RBw/16 //this is the intermediate location
    	                    &  (FAb | BAb | FPb | BPb);     //these are any opponent piece; //this is the logical and withoccupied
    	return nextRBw;
    	
    }
    public long calcjRBb(){
    	long nextRBb = (RBb/16)/16 //this is the shift by 4 
    			            & empty //this is the logical and with empty
    			            & RBb/16 //this is the intermediate location
    	                    &  (FAw | BAw | FPw | BPw);     //these are any opponent piece; //this is the logical and with occupied
    	return nextRBb;
    	
    }
    public long calcjLBw(){
    	long nextLBw = (LBw/32)/32 //this is the shift by 4 
    			            & empty //this is the logical and with empty
    			            &  LBw/32 //this is the intermediate location
    	                    &  (FAb | BAb | FPb | BPb);     //these are any opponent piece; //this is the logical and with occupied
    	return nextLBw;
    	
    }
    public long calcjLBb(){
    	long nextLBb = (LBb/32)/32 //this is the shift by 4 
    			            & empty //this is the logical and with empty
    			            &  LBb/32 //this is the intermediate location
    	                    &  (FAw | BAw | FPw | BPw);     //these are any opponent piece; //this is the logical and with occupied
    	return nextLBb;
    	
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    public java.util.List <Step> getAllPossibleNextSteps(Move.Side activeSide){//depends on whose turn it is
		java.util.List <Step> possibleSteps= null;
		//TODO are any?
		//look first for steps containing a jump, if find, don't look for non-jump steps,  
		//what color is active?
		if(activeSide == Move.Side.BLACK){
		    	
		}
		else {
			
		}
		if(true){Step step = new Step();
		possibleSteps.add(step);}
		return possibleSteps;
    }
    public void setFA(long FA, Move.Side side){ 
    	switch(side){
    	case BLACK:
    		FAb = FA;
    		break;
    	case WHITE:
    		FAw = FA;
    		break;
    	}
    }
    public Board changeFA(long FA, Move.Side side){
        Board result = new Board(this);
        result.setFA(FA, side);
        return result;
    }
    
    
    public void evalUsingMiniMax(long oldFA, long newFA,
    		                     long oldBA, long newBA, Move.Side side, int ply){ //whatever the side is, maybe don't need to know
    	//what do we want to come back from evals? ultimately, the choice of move
    	//grow the tree forward some number of plies
    	//generate all possible moves in parallel, explore then one by one p.212 top
    	//we're taking in results from a step
    	if (ply > firstBreakPly)
    	for (int i=1; i<35; i++){
    	   if (i%9 != 0){ //not looking at squares that are divided evenly by 9	
    	      if(oldFA%i != newFA%i){ //this is a step that took place
    	    	  //so, figure out the board from this one step, and evaluate it, or go another ply from it
    	    	  //these boards get saved. What for data structure?
    	    	  long testFA = oldFA;
                  long impulse = (long) Math.pow(2,i);
                  long oldbit = oldFA & impulse;
                  if (oldbit == 0){ testFA = oldFA + impulse;} else {testFA = oldFA - impulse;}
    	    	  Board nbd =  changeFA(testFA, side);
    	    	  nbd.evalUsingMiniMax(testFA, newFA, oldBA, newBA, side, ply+1); //recursive call, need base case
    	      }
    	}
    	
    	//first go the minimum number of plies, then check
    	//1 is any next move(step) a jump?
    	//2 was the last move(step) a jump?
    	//3 is an exchange offer available?
    	//if any of these is true, keep looking forward, i.e., generating new board prospectively
    	//after the fourth ply, 
    	//if any next move(step) is a jump?
    	//if an exchange was available?
    	//if both are true, keep going
    	//after 5th ply
    	//if there is a jump possibility, keep going
    	//after the 11th ply,
    	//if one side is ahead by more than 2 kings, stop and evaluate
    	//after 20th ply, stop for reasons of memory (obviously machine dependent)
    	//perform eval of board starting at leaves
    	//figure out the preferred choices, working back upwards
    	//take it
    }
    }
    public StringBuffer acceptMove(Move mv){
    	//TODO figure out
    	StringBuffer result = new StringBuffer("(2:4):(3:5)");
    	return result;
    }
    public StringBuffer initiateMove(Move.Side side){
    	//TODO figure out
    	StringBuffer result = new StringBuffer("(5:5):(4:4)");//this is a legal first move
    	return result;
    }

}
