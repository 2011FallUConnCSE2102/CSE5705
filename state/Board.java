package state;


import exceptions.IllegalMoveException;
import actions.*;
import valuation.*;
import persistence.DBHandler;


public class Board {

	private Move.Side whoseTurn = null;
	private Move.Side whoAmI = null;
	private Piece[] theWhitePieces = null;
	private Piece[] theBlackPieces = null;
	private DBHandler myPersistence = null;
	private Evaluator myEvaluator = null;
	
	long FAw = 0L;                      // white pieces that move "forward" will only be kings, and there are none at start
	long FAb =  (long) Math.pow(2,1)+ //forward active when black's turn: pawns  
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
	long BAw =  (long) Math.pow(2,23)+ //backward active on white's turn: pawns   
		    	(long) Math.pow(2,24)+
		    	(long) Math.pow(2,25)+
		    	(long) Math.pow(2,26)+
		    	(long) Math.pow(2,28)+
		    	(long) Math.pow(2,29)+
		    	(long) Math.pow(2,30)+ 
		    	(long) Math.pow(2,31)+
		    	(long) Math.pow(2,32)+
		    	(long) Math.pow(2,33)+  
			    (long) Math.pow(2,34)+
			    (long) Math.pow(2,35);
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
    boolean firstMove = true;
    private long emptyLoc;
      //forward active, forward is toward high number where white starts, so on black move pawns and kings. on white move only kings
      //backward active, on black move, kings, on white move, pawns and kings
      //forward passive,  
     //backward passive
     //forward active, forward is toward high number where white starts, so on black move pawns and kings. on white move only kings
      //backward active, on black move, kings, on white move, pawns and kings
      //forward passive,  
     //backward passive
    private long isJump;
    private long RFw; //right forward, these are locations, corresponding to steps taken
    private long LFw; //left forward
    private long RBw; //right backward
    private long LBw; //left backward
    private long RFb; //right forward, these are locations, corresponding to steps taken
    private long LFb; //left forward
    private long RBb; //right backward
    private long LBb; //left backward
    private int firstBreakPly = 3;
    private int secondBreakPly = 4;
    private int thirdBreakPly = 5;
    private int fourthBreakPly = 11;
    private int fifthBreakPly = 20;
    private int numberPiecesOnBoardThreshold = 8;
    private boolean haveBoardValue = false;
    private BoardValue boardValue = null;
	
	public Board(DBHandler db){
		 myPersistence = db;
		 myEvaluator = new Evaluator(this);
		 theWhitePieces = new Piece[12];
		 theBlackPieces = new Piece[12];
		 for(int i = 0; i < 12; i++){
		 theWhitePieces[i] = new Piece();//born as pawns
		 theBlackPieces[i] = new Piece();
		 }
		theWhitePieces[0].setSamLoc(35);
		theWhitePieces[1].setSamLoc(34);
		theWhitePieces[2].setSamLoc(33);
		theWhitePieces[3].setSamLoc(32);
		theWhitePieces[4].setSamLoc(31);
		theWhitePieces[5].setSamLoc(30);
		theWhitePieces[6].setSamLoc(29);
		theWhitePieces[7].setSamLoc(28);
		theWhitePieces[8].setSamLoc(26);
		theWhitePieces[9].setSamLoc(25);
		theWhitePieces[10].setSamLoc(24);
		theWhitePieces[11].setSamLoc(23);
		
		theBlackPieces[0].setSamLoc(1);
		theBlackPieces[1].setSamLoc(2);
		theBlackPieces[2].setSamLoc(3);
		theBlackPieces[3].setSamLoc(4);
		theBlackPieces[4].setSamLoc(5);
		theBlackPieces[5].setSamLoc(6);
		theBlackPieces[6].setSamLoc(7);
		theBlackPieces[7].setSamLoc(8);
		theBlackPieces[8].setSamLoc(9);
		theBlackPieces[9].setSamLoc(10);
		theBlackPieces[10].setSamLoc(11);
		theBlackPieces[11].setSamLoc(12);
		
		
		
	}
	
	public Board(Board bd){
		//all the components of board copied into new board
		

		firstMove = bd.firstMove;
	}
	public Board(long faw,
			long fab,
			long baw,
			long bab,
			long fpw,
			long fpb,
			long bpw,
			long bpb){
	this.FAw = faw;
	this.FAb = fab;
	this.BAw = baw;
	this.BAb = bab;
	this.FPw = fpw;
	this.FPb = fpb;
	this.BPw = bpw;
	this.BPb = bpb;}
	
	

    public int score(ScoreExpression se){
    	int myScore = 0;
    	//TODO Samuel says, p. 212 that there is a scoring polynomial, of course it can change with time
    	//the score comes from the score expression and the piece arrangments on the board
    	//score expression is a weighted sum of features, so contains a list of weights
    	return myScore;
    }
    

    
    public void setEmpty(){
    	long invertedEmpty = (FAw | BAw | FPw | BPw | FAb | BAb | FPb | BPb );//want bitwise or's
    	invertedEmpty = invertedEmpty |( (long)Math.pow(2, 9)) |( (long)Math.pow(2, 18)) | ((long)Math.pow(2, 27)) | ((long)Math.pow(2, 0)) ;
    	emptyLoc = (long) Math.pow(2, 36)-1;// start with all 0's, put 1 at 36 position, subtract 1 makes 36 1's in a row (bit 0 to bit 35)
    	//xor'ing this to invertedempty will invert it 
    	emptyLoc = emptyLoc ^ invertedEmpty;
    	//System.err.println("Board::setEmpty");
    	//showBitz(emptyLoc);
    }
    public long getEmpty(){
    	return emptyLoc;
    }
    public long calcRFw(){
    	long nextRFw = RFw*16 //this is the shift by 4 
    			            & emptyLoc; //this is the logical and with empty
    	return nextRFw;
    	
    }
    public long calcRFb(){
    	long nextRFb = RFb*16 //this is the shift by 4 
    			            & emptyLoc; //this is the logical and with empty
    	return nextRFb;
    	
    }
    public long calcLFw(){
    	long nextLFw = LFw*32 //this is the shift by 4 
    			            & emptyLoc; //this is the logical and with empty
    	return nextLFw;
    	
    }
    public long calcLFb(){
    	long nextLFb = LFb*32 //this is the shift by 4 
    			            & emptyLoc; //this is the logical and with empty
    	return nextLFb;
    	
    }
    public long calcRBw(){
    	long nextRBw = RBw/16 //this is the shift by 4 
    			            & emptyLoc; //this is the logical and with empty
    	return nextRBw;
    	
    }
    public long calcRBb(){
    	long nextRBb = RBb/16 //this is the shift by 4 
    			            & emptyLoc; //this is the logical and with empty
    	return nextRBb;
    	
    }
    public long calcLBw(){
    	long nextLBw = LBw/32 //this is the shift by 4 
    			            & emptyLoc; //this is the logical and with empty
    	return nextLBw;
    	
    }
    public long calcLBb(){
    	long nextLBb = LBb/32 //this is the shift by 4 
    			            & emptyLoc; //this is the logical and with empty
    	return nextLBb;
    	
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    public long calcjRFw(){
    	long nextRFw = RFw*16*16 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    	                    &  RFw*16 //this is the intermediate location
    	                    &  (FAb | BAb | FPb | BPb);     //these are any opponent piece, occupying the spot over which jumping
    	return nextRFw;
    	
    }
    public long calcjRFb(){
    	long nextRFb = RFb*16*16 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    	                    &  RFb*16 //this is the intermediate location
    	                    &  (FAw | BAw | FPw | BPw);     //these are any opponent piece; //this is the logical and with occupied
    	return nextRFb;
    	
    }
    public long calcjLFw(){
    	long nextLFw = LFw*32*32 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    	                    &  LFw*32 //this is the intermediate location
    	                    &  (FAb | BAb | FPb |BPb);     //these are any opponent piece; //this is the logical and with occupied
    	return nextLFw;
    	
    }
    public long calcjLFb(){
    	long nextLFb = LFb*32*32 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            &  LFb*32 //this is the intermediate location
    	                    &  (FAw | BAw | FPw | BPw);     //these are any opponent piece; //this is the logical and with occupied
    	return nextLFb;
    	
    }
    public long calcjRBw(){
    	long nextRBw = (RBw/16)/16 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            &  RBw/16 //this is the intermediate location
    	                    &  (FAb | BAb | FPb | BPb);     //these are any opponent piece; //this is the logical and withoccupied
    	return nextRBw;
    	
    }
    public long calcjRBb(){
    	long nextRBb = (RBb/16)/16 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            & RBb/16 //this is the intermediate location
    	                    &  (FAw | BAw | FPw | BPw);     //these are any opponent piece; //this is the logical and with occupied
    	return nextRBb;
    	
    }
    public long calcjLBw(){
    	long nextLBw = (LBw/32)/32 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            &  LBw/32 //this is the intermediate location
    	                    &  (FAb | BAb | FPb | BPb);     //these are any opponent piece; //this is the logical and with occupied
    	return nextLBw;
    	
    }
    public long calcjLBb(){
    	long nextLBb = (LBb/32)/32 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            &  LBb/32 //this is the intermediate location
    	                    &  (FAw | BAw | FPw | BPw);     //these are any opponent piece; //this is the logical and with occupied
    	return nextLBb;
    	
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    
   /* public java.util.List <Step> getAllPossibleNextSteps(Move.Side activeSide){//depends on whose turn it is
		java.util.List <Step> possibleSteps= null;
		
		//look first for steps containing a jump, if find, don't look for non-jump steps,  
		//what color is active?
		if(activeSide == Move.Side.BLACK){
		    	
		}
		else {
			
		}
		if(true){Step step = new Step();
		possibleSteps.add(step);}
		return possibleSteps;
    }*/
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
    
    
    public BoardValue evalUsingMiniMax(long oldFA, long newFA,
    		                     long oldBA, long newBA, Move.Side side, int ply){ //whatever the side is, maybe don't need to know
    	//what do we want to come back from evals? the value of the board
    	//let's keep all the features scores separately, a vector of scores
    	//to be used to determine, ultimately, the choice of move
    	//grow the tree forward some number of plies
    	//generate all possible moves in parallel, explore then one by one p.212 top
    	//we're taking in results from a step
    	BoardValue result = new BoardValue();//have to know the right number
    	//Do I know this board already?
    	result = lookUpValue(FAw, BAw, FAb, BAb, side);
    	if (result != null){
    		return result;
    	}
    	//else //now we're going to figure it out    		
    	//TEST FOR BASE CASE
    	if (ply > firstBreakPly){
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
    	}
    	   //
    	
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
    	//RECURSION--need all next moves, check if any is jump, flip whose turn it is
    	//check for any jumps (because if there is a jump must take a jump
    	
		return result;
    	
    }
    public String acceptMoveAndRespond(Move mv){
    	StringBuffer bestMove_sb = new StringBuffer("(2:4):(3:5)");
    	Move bestMove = null;
    	switch( mv.getSide()){
    	case BLACK://they are BLACK
    		this.whoAmI = Move.Side.WHITE;
    		this.whoseTurn =  Move.Side.BLACK;
    		break;
    	case WHITE:
    		this.whoAmI = Move.Side.BLACK;
    		this.whoseTurn = Move.Side.WHITE;
    		break;
    	default:
    		//System.err.println("Board::acceptMoveAndRespond: no side set in Move");
    	}
    	//update the board
    		//find the piece at the start part of the move
    	    //capture any pieces that get jumped
    	    //update the board
    	//consider the possibilities for response
    	
    	//update the board
    	   //find the piece as the start part of the move, remove it
    	   updateBoard(mv);
    	//System.err.println("Board::acceptAndRespond showing updated board empty");
    	//showBitz(emptyLoc);
    	   //figure out if it captured anyone, if so remove them
    	 
    	SetOfMoves som = null;
    	if (anyJumps()){
    		//System.err.println("Board::acceptAndRespond: there are jumps");
    		som = getJumpMoves();
        	if (som.howMany()>0){   				
        		bestMove =chooseBestMove(som);
        		 updateBoard(bestMove);
        		return(bestMove.toString());
        	}
    	}
    	else{
    		//System.err.println("Board::acceptAndRespond: there are no jumps");
    		som = getNonJumpMoves();
    		//System.err.println("Board::acceptAndRespond: there are "+som.howMany()+" moves");
    	}
    	if (som.howMany()>0){
    		bestMove = chooseBestMove(som);
    		//now update board with this chosen move    		
    		 updateBoard(bestMove);
    		//System.err.println("Board:: acceptRespond recording my owm move");
    		//showBitz(emptyLoc);
    		return(bestMove.toString());
    	}   	
    	//if we got to here, there is no move 
    	return "NO MOVE: CONCEDE";
    }
    public StringBuffer initiateMove(Move.Side side){
    	StringBuffer result = new StringBuffer("(5:5):(4:4)");//this is a legal first move
        setEmpty();
        //showBitz(emptyLoc);
    	placePiece(16, Move.Side.BLACK, Piece.Rank.PAWN ); //TODO later, when promoting, will want to fix
        //showBitz(emptyLoc);
    	removePiece(12);//this takes away the piece from its start
    	//showBitz(emptyLoc);
    	if(this.firstMove){this.firstMove=false;}
    	else{
    		//only happens when we start out as black, otherwise will be accept and respond
    	}
    	return result;
    }
    
    public boolean anyJumps(){
    	setEmpty();
    	//check all possible jumps. If any change in variables, there was a jump
    	//kings can jump any thing, so what's not empty?
    	//forward active, try going forward, backward active try going backward. 
    	long opponents = 0L;
		long jumpers = 0L;//they move differently so we check them separately
		long successful = 0L;
    	//System.err.println("Board::anyJumps: with "+whoAmI);
    	switch(this.whoAmI){
    	case BLACK:
    		//black pawns are never backward active
    		//get the empty 
    		opponents = FAw | FPw | BAw | BPw;
    		jumpers = FAb;//they move differently so we check them separately
    		successful =  jumpers & (opponents>>4) & (emptyLoc>>8);
    		//System.err.println("Board::anyJumps: showing successful");
    		//showBitz(successful);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		successful =  jumpers & (opponents>>5) & (emptyLoc>>10);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		jumpers = BAb;
    		successful =   jumpers & (opponents<<5) & (emptyLoc<<10);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		successful =  jumpers & (opponents<<4) & (emptyLoc<<8);
    		if (successful != 0){//there are jumps
    			return true;
    		}
            break;
    		
    	case WHITE:
    		//white pawns are never forward active
    		opponents = FAb | FPb | BAb | BPb;
    		jumpers = FAw;
    		successful =   jumpers & (opponents>>4) & (emptyLoc>>8);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		successful =   jumpers & (opponents>>5) & (emptyLoc>>10);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		jumpers = BAw;
    		successful =  jumpers & (opponents<<5) & (emptyLoc<<10);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		successful = jumpers & (opponents<<4) & (emptyLoc<<8);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		break;
    		default: System.err.println("Board::anyJumps: default"); return false; 
    	}
    	return false;
    }

    public SetOfMoves getJumpMoves(){
    	setEmpty();
    	SetOfMoves som = new SetOfMoves();
    	long mover;
    	//do foward active and backward active
    	//do right and left
    	//ought to be recursive, because at the conclusion of a step that is a jump, need to know whether there is another step that is a jump
        setEmpty();
    	
    	switch(whoAmI){
    	  case BLACK:
    		//black pawns are never backward active
    		//get the empty 
    		long movers = FAb;
    		long place = 2;//2 raised to the bit position
    		//look through FAb, testing one at a time
    		for (int i = 1; i<32; i++){//32 and up cannot move forward 4
    			if((i%9)==0){i++; movers = movers/2; place = place*2;}//this assures we do not test movers at places that are 9ish
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	//and there is an opponent at +4
    		    	if ((i+4)%9 != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i+4, whoAmI)){
    		    	//and there is space at +8
    		    	if ((emptyLoc & place*16*16)!=0){//can move forward right
    		    		if ((place*16*16)%9!=0){//valid destination
    		    			Move mv = new Move(i);   		    			
    		    			mv.setSide(Move.Side.BLACK);
    		    			som.addMove(mv);
    		    			Step step = new Step();    		    			
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+8);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = copyBoard();
    		    			//System.err.println("Board::getJumpMoves: made new board");
    		    			if (nextBd == null){System.err.println("Board::getJumpMoves: new board null");}
    		    			nextBd.updateBoard(mv); //update removes captureds, does not change sides
    		    			//System.err.println("Board::getJumpMoves: called update new board");
    		    			//update does not change sides nextBd.changeSides(); //revert to unchanged side
    		    			if(nextBd.anyJumps(i+8, Move.Side.BLACK, mv.getRankAtStart())){//only want jumps by this one mover, uniquely identified by starting position, need which side
    		    				//need recognize that I have found more than one move, if indeed have
    		    				//if at least one, extend move with one step, might make two moves
    		    				//check if can move -- two possibilities if pawn, 3 if king, because don't undo the way I got here, i.e., previous step, can't, update has removed captured
    		    				//canStepForwardLeft
    		    				//
    		    				//should be doing recursive call, getJumpMoves on the resulting board, and expect a vector of moves
    		    				SetOfMoves jumpExtensions = nextBd.getJumpMoves(); //needs to know side, which nextBd has 
    		    				//update has removed captured
    		    				int howManyExtensions = jumpExtensions.howMany();
    		    				if(howManyExtensions>1){
    		    					for(int extensionIndex = 1; extensionIndex<howManyExtensions; extensionIndex++){//only make new moves for extensions beyond first
    		    						Move theExtension = new Move(mv); 
    		    						//now, for this move, copy the steps in it onto the the move that started with mv
    		    						
    		    						int howManyStepsThisMove = jumpExtensions.getMove(extensionIndex).getHowManySteps();
    		    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    		    							Step extStep = jumpExtensions.getMove(extensionIndex).getStep(stepIndex);//here, need first step to have good start location
    		    							Step theStep = new Step(extStep);
    		    							theExtension.addStep(theStep);//adding the first step also sets the start of the move
    		    						}
    		    						//the end location of the move is set when the step is added
    		    						System.err.println("Board::getJumpMoves: adding Extension Move, start "+theExtension.getStartLocation());
    		    						som.addMove(theExtension);
    		    					}
    		    				}
    		    				//for the first move, we extend mv, but for more than one, we make a copy of mv and extend it that specific way

	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}
    		    			
    		    			}//end of if there are any next jumps
    		    			
    		    			//System.err.println("Board:: getJumpMoves: FAb "+FAb+"finding from "+i+" to "+(i+8));
    		    		}
    		    }
    		    }}}
    		    place = place *2;
    		}
    		movers = FAb;
    		place = 2;//2 raised to the bit position
    		//System.err.println("Board::getNonJumpMoves, showing FAb before +5 moves");
    		//showBitz(FAb);
    		//System.err.println("Board::getNonJumpMoves, showing empty");
    		//showBitz(emptyLoc);
    		//look through FAb, testing one at a time
    		for (int i = 1; i<31; i++){//31 and up cannot move forward 5
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	//and there is an opponent at +5
    		    	if ((i+5)%9 != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i+5, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & place*32*32)!=0){//can move forward left
    		    		if ((place*32*32)%9!=0){
    		    			Move mv = new Move(i);		    			
    		    			mv.setSide(Move.Side.BLACK);
    		    			som.addMove(mv);
    		    			Step step = new Step();   		    			
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+10);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = copyBoard();
    		    			nextBd.updateBoard(mv); //update does not change sides
    		    			//nextBd.changeSides(); 
    		    			if(nextBd.anyJumps(i+10, Move.Side.BLACK, mv.getRankAtStart())){//only want jumps by this one mover, uniquely identified by starting position, need which side
    		    				//need recognize that I have found more than one move, if indeed have
    		    				//if at least one, extend move with one step, might make two moves
    		    				//check if can move -- two possibilities if pawn, 3 if king, because don't undo the way I got here, i.e., previous step
    		    				//canStepForwardLeft
    		    				//
    		    				//should be doing recursive call, getJumpMoves on the resulting board, and expect a vector of moves
    		    				SetOfMoves jumpExtensions = nextBd.getJumpMoves(); //needs to know side, which nextBd has
    		    				//can't, update has removed captured
    		    				int howManyExtensions = jumpExtensions.howMany();
    		    				if(howManyExtensions>1){
    		    					for(int extensionIndex = 1; extensionIndex<howManyExtensions; extensionIndex++){//only make new moves for extensions beyond first
    		    						Move theExtension = new Move(mv);
    		    						//now, for this move, copy the steps in it onto the the move that started with mv
    		    						
    		    						int howManyStepsThisMove = jumpExtensions.getMove(extensionIndex).getHowManySteps();
    		    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    		    							Step extStep = jumpExtensions.getMove(extensionIndex).getStep(stepIndex);
    		    							Step theStep = new Step(extStep);
    		    							theExtension.addStep(theStep);
    		    						}
    		    						som.addMove(theExtension);
    		    					}
    		    				}
    		    				//for the first move, we extend mv, but for more than one, we make a copy of mv and extend it that specific way

	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}
    		    			
    		    			}//end of if there are any next jumps
    		    			//System.err.println("Board:: getJumpMoves: FAb "+FAb+"finding from "+i+" to "+(i+10));
    		    		}
    		    	}
    		    }}}
    		    place=place*2;
    		}
    		movers = BAb;
    		//System.err.println("Board::getNonJumpMoves, showing BAb before -4 moves");
    		//showBitz(BAb);
    		//System.err.println("Board::getNonJumpMoves, showing empty");
    		//showBitz(emptyLoc);
    		place=32;
    		//look through BAb, testing one at a time
    		movers = movers/16; //skip checking places 1-4, they cannot possibly move backwards
    		for (int i = 5; i<36; i++){
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	//and there is an opponent at -4
    		    	if ((i-4)%9 != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i-4, whoAmI)){
    		    	//and there is space at -8
    		    	if ((emptyLoc & place/(16*16))!=0){//can move backward left
    		    		if ((place/(16*16))%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.BLACK);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-8);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = copyBoard();
    		    			nextBd.updateBoard(mv); //update does not change sides
    		    			//nextBd.changeSides(); //revert to unchanged side
    		    			if(nextBd.anyJumps(i-8, Move.Side.BLACK, mv.getRankAtStart())){//only want jumps by this one mover, uniquely identified by starting position, need which side
    		    				//need recognize that I have found more than one move, if indeed have
    		    				//if at least one, extend move with one step, might make two moves
    		    				//check if can move -- two possibilities if pawn, 3 if king, because don't undo the way I got here, i.e., previous step
    		    				//canStepForwardLeft
    		    				//
    		    				//should be doing recursive call, getJumpMoves on the resulting board, and expect a vector of moves
    		    				SetOfMoves jumpExtensions = nextBd.getJumpMoves(); //needs to know side, which nextBd has
    		    				//can't, update has removed captured
    		    				int howManyExtensions = jumpExtensions.howMany();
    		    				if(howManyExtensions>1){
    		    					for(int extensionIndex = 1; extensionIndex<howManyExtensions; extensionIndex++){//only make new moves for extensions beyond first
    		    						Move theExtension = new Move(mv);
    		    						//now, for this move, copy the steps in it onto the the move that started with mv
    		    						
    		    						int howManyStepsThisMove = jumpExtensions.getMove(extensionIndex).getHowManySteps();
    		    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    		    							Step extStep = jumpExtensions.getMove(extensionIndex).getStep(stepIndex);
    		    							Step theStep = new Step(extStep);
    		    							theExtension.addStep(theStep);
    		    						}
    		    						som.addMove(theExtension);
    		    					}
    		    				}
    		    				//for the first move, we extend mv, but for more than one, we make a copy of mv and extend it that specific way

	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}
    		    			
    		    			}//end of if there are any next jumps
    		    			//System.err.println("Board:: getJumpMoves: BAb"+BAb+" finding from "+i+" to "+(i-8));
    		    		}
    		    	}
    		    }}}
    		    place=place*2;
    		}
    		//look through BAb, testing one at a time
    		movers = BAb;
    		place = 64;
    		for (int i = 6; i<36; i++){
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	//and there is an opponent at -5
    		    	if ((i-5)%9 != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i-5, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & place/(32*32))!=0){//can move backward right
    		    		if ((place/(32*32))%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.BLACK);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-10);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = copyBoard();
    		    			nextBd.updateBoard(mv); //update does not change sides
    		    			//nextBd.changeSides(); //revert to unchanged side
    		    			if(nextBd.anyJumps(i-10, Move.Side.BLACK, mv.getRankAtStart())){//only want jumps by this one mover, uniquely identified by starting position, need which side
    		    				//need recognize that I have found more than one move, if indeed have
    		    				//if at least one, extend move with one step, might make two moves
    		    				//check if can move -- two possibilities if pawn, 3 if king, because don't undo the way I got here, i.e., previous step
    		    				//canStepForwardLeft
    		    				//
    		    				//should be doing recursive call, getJumpMoves on the resulting board, and expect a vector of moves
    		    				SetOfMoves jumpExtensions = nextBd.getJumpMoves(); //needs to know side, which nextBd has
    		    				//can't, update has removed captured
    		    				int howManyExtensions = jumpExtensions.howMany();
    		    				if(howManyExtensions>1){
    		    					for(int extensionIndex = 1; extensionIndex<howManyExtensions; extensionIndex++){//only make new moves for extensions beyond first
    		    						Move theExtension = new Move(mv);
    		    						//now, for this move, copy the steps in it onto the the move that started with mv
    		    						
    		    						int howManyStepsThisMove = jumpExtensions.getMove(extensionIndex).getHowManySteps();
    		    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    		    							Step extStep = jumpExtensions.getMove(extensionIndex).getStep(stepIndex);
    		    							Step theStep = new Step(extStep);
    		    							theExtension.addStep(theStep);
    		    						}
    		    						som.addMove(theExtension);
    		    					}
    		    				}
    		    				//for the first move, we extend mv, but for more than one, we make a copy of mv and extend it that specific way

	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}
    		    			
    		    			}//end of if there are any next jumps
    		    			//System.err.println("Board:: getJumpMoves: BAb "+BAb+" finding from "+i+" to "+(i-10));
    		    		}
    		    	}
    		    }}}
    		    place=place*2;
    		}
    		break;
    		
    	case WHITE:
    		//white pawns are never forward active
    		//black pawns are never backward active
    		//get the empty 
    		 movers = FAw;
    		place = 1;
    		setEmpty();
    		//look through FA, testing one at a time
    		if(movers!=0L)
    			{for (int i = 1; i<31; i++){//32 and up cannot move forward
    			if((i%9)==0){i++; movers = movers/2; place = place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	//and there is an opponent at +5
    		    	if ((i+4)%9 != 0){//don't want 9ish intermediary locations 
    		    	if (opponentAt(i+4, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & place*16*16)!=0){//can move forward right
    		    		if ((place*16*16)%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+8);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: FAw created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = copyBoard();
    		    			nextBd.updateBoard(mv); //update does not change sides
    		    			//nextBd.changeSides(); //revert to unchanged side
    		    			if(nextBd.anyJumps(i+8, Move.Side.WHITE, mv.getRankAtStart())){//only want jumps by this one mover, uniquely identified by starting position, need which side
    		    				//need recognize that I have found more than one move, if indeed have
    		    				//if at least one, extend move with one step, might make two moves
    		    				//check if can move -- two possibilities if pawn, 3 if king, because don't undo the way I got here, i.e., previous step
    		    				//canStepForwardLeft
    		    				//
    		    				//should be doing recursive call, getJumpMoves on the resulting board, and expect a vector of moves
    		    				SetOfMoves jumpExtensions = nextBd.getJumpMoves(); //needs to know side, which nextBd has
    		    				//can't, update has removed captured
    		    				int howManyExtensions = jumpExtensions.howMany();
    		    				if(howManyExtensions>1){
    		    					for(int extensionIndex = 1; extensionIndex<howManyExtensions; extensionIndex++){//only make new moves for extensions beyond first
    		    						Move theExtension = new Move(mv);
    		    						//now, for this move, copy the steps in it onto the the move that started with mv
    		    						
    		    						int howManyStepsThisMove = jumpExtensions.getMove(extensionIndex).getHowManySteps();
    		    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    		    							Step extStep = jumpExtensions.getMove(extensionIndex).getStep(stepIndex);
    		    							Step theStep = new Step(extStep);
    		    							theExtension.addStep(theStep);
    		    						}
    		    						som.addMove(theExtension);
    		    					}
    		    				}
    		    				//for the first move, we extend mv, but for more than one, we make a copy of mv and extend it that specific way

	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}
    		    			
    		    			}//end of if there are any next jumps
    		    			//System.err.println("Board:: getJumpMoves: finding from FAw "+i+" to "+(i+8));
    		    		}
    		    	}
    		    }}}
    		    place = place *2;
    		}
    		movers = FAw;
    		setEmpty();
    		place = 1;
    		//look through FAb, testing one at a time
    		for (int i = 1; i<30; i++){
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	//and there is an opponent at +5
    		    	if ((i+5)%9 != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i+5, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & place*32*32)!=0){//can move forward left
    		    		if ((place*32*32)%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+10);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: FAw created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = copyBoard();
    		    			nextBd.updateBoard(mv); //update does not change sides
    		    			//nextBd.changeSides(); //revert to unchanged side
    		    			if(nextBd.anyJumps(i+10, Move.Side.WHITE, mv.getRankAtStart())){//only want jumps by this one mover, uniquely identified by starting position, need which side
    		    				//need recognize that I have found more than one move, if indeed have
    		    				//if at least one, extend move with one step, might make two moves
    		    				//check if can move -- two possibilities if pawn, 3 if king, because don't undo the way I got here, i.e., previous step
    		    				//canStepForwardLeft
    		    				//
    		    				//should be doing recursive call, getJumpMoves on the resulting board, and expect a vector of moves
    		    				SetOfMoves jumpExtensions = nextBd.getJumpMoves(); //needs to know side, which nextBd has
    		    				//can't, update has removed captured
    		    				int howManyExtensions = jumpExtensions.howMany();
    		    				if(howManyExtensions>1){
    		    					for(int extensionIndex = 1; extensionIndex<howManyExtensions; extensionIndex++){//only make new moves for extensions beyond first
    		    						Move theExtension = new Move(mv);
    		    						//now, for this move, copy the steps in it onto the the move that started with mv
    		    						
    		    						int howManyStepsThisMove = jumpExtensions.getMove(extensionIndex).getHowManySteps();
    		    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    		    							Step extStep = jumpExtensions.getMove(extensionIndex).getStep(stepIndex);
    		    							Step theStep = new Step(extStep);
    		    							theExtension.addStep(theStep);
    		    						}
    		    						som.addMove(theExtension);
    		    					}
    		    				}
    		    				//for the first move, we extend mv, but for more than one, we make a copy of mv and extend it that specific way

	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}
    		    			
    		    			}//end of if there are any next jumps
    		    			//System.err.println("Board:: getJumpMoves: finding from FAw "+i+" to "+(i+10));
    		    		}
    		    	}
    		    }}}
    		    place=place*2;
    		}}
    		movers = BAw;
    		setEmpty();
    		place=32;
    		//look through BAb, testing one at a time
    		movers = movers/16; //skip checking places 1-4, they cannot possibly move backwards
    		for (int i = 5; i<36; i++){//
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	//and there is an opponent at -4
    		    	if ((i-4)%9 != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i-4, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & place/(16*16))!=0){//can move backward left
    		    		if ((place/(16*16))%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-8);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: BAw created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = copyBoard();   			 
    		    			nextBd.updateBoard(mv); //update does not change sides
    		    			//nextBd.changeSides(); //revert to unchanged side
    		    			if(nextBd.anyJumps(i-8, Move.Side.WHITE, mv.getRankAtStart())){//only want jumps by this one mover, uniquely identified by starting position, need which side
    		    				//need recognize that I have found more than one move, if indeed have
    		    				//if at least one, extend move with one step, might make two moves
    		    				//check if can move -- two possibilities if pawn, 3 if king, because don't undo the way I got here, i.e., previous step
    		    				//canStepForwardLeft
    		    				//
    		    				//should be doing recursive call, getJumpMoves on the resulting board, and expect a vector of moves
    		    				SetOfMoves jumpExtensions = nextBd.getJumpMoves(); //needs to know side, which nextBd has
    		    				//can't, update has removed captured
    		    				int howManyExtensions = jumpExtensions.howMany();
    		    				if(howManyExtensions>1){
    		    					for(int extensionIndex = 1; extensionIndex<howManyExtensions; extensionIndex++){//only make new moves for extensions beyond first
    		    						Move theExtension = new Move(mv);
    		    						//now, for this move, copy the steps in it onto the the move that started with mv
    		    						
    		    						int howManyStepsThisMove = jumpExtensions.getMove(extensionIndex).getHowManySteps();
    		    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    		    							Step extStep = jumpExtensions.getMove(extensionIndex).getStep(stepIndex);
    		    							Step theStep = new Step(extStep);
    		    							theExtension.addStep(theStep);
    		    						}
    		    						som.addMove(theExtension);
    		    					}
    		    				}
    		    				//for the first move, we extend mv, but for more than one, we make a copy of mv and extend it that specific way

	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}
    		    			
    		    			}//end of if there are any next jumps
    		    			//System.err.println("Board:: getJumpMoves: finding from BAw "+i+" to "+(i-8));
    		    		}
    		    	}
    		    }}}
    		    place=place*2;
    		}
    		//look through BAw, testing one at a time
    		movers = BAw;
    		place = 64;
    		movers=movers/32; //skip checking places 1-5, they cann possibly move backwards
    		for (int i = 6; i<36; i++){
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	//and there is an opponent at -5
    		    	if ((i-5)%9 != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i-5, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & place/(32*32))!=0){//can move backward right
    		    		if ((place/(32*32))%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-10);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: BAw created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = copyBoard();
    		    			nextBd.updateBoard(mv); //update does not   change sides
    		    			//nextBd.changeSides(); //revert to unchanged side
    		    			if(nextBd.anyJumps(i-10, Move.Side.WHITE, mv.getRankAtStart())){//only want jumps by this one mover, uniquely identified by starting position, need which side
    		    				//need recognize that I have found more than one move, if indeed have
    		    				//if at least one, extend move with one step, might make two moves
    		    				//check if can move -- two possibilities if pawn, 3 if king, because don't undo the way I got here, i.e., previous step
    		    				//canStepForwardLeft
    		    				//
    		    				//should be doing recursive call, getJumpMoves on the resulting board, and expect a vector of moves
    		    				SetOfMoves jumpExtensions = nextBd.getJumpMoves(); //needs to know side, which nextBd has
    		    				//can't, update has removed captured
    		    				int howManyExtensions = jumpExtensions.howMany();
    		    				if(howManyExtensions>1){
    		    					for(int extensionIndex = 1; extensionIndex<howManyExtensions; extensionIndex++){//only make new moves for extensions beyond first
    		    						Move theExtension = new Move(mv);
    		    						//now, for this move, copy the steps in it onto the the move that started with mv
    		    						
    		    						int howManyStepsThisMove = jumpExtensions.getMove(extensionIndex).getHowManySteps();
    		    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    		    							Step extStep = jumpExtensions.getMove(extensionIndex).getStep(stepIndex);
    		    							Step theStep = new Step(extStep);
    		    							theExtension.addStep(theStep);
    		    						}
    		    						som.addMove(theExtension);
    		    					}
    		    				}
    		    				//for the first move, we extend mv, but for more than one, we make a copy of mv and extend it that specific way

	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}
    		    			
    		    			}//end of if there are any next jumps
    		    			//System.err.println("Board:: getJumpMoves: finding from BAw "+i+" to "+(i-10));
    		    		}
    		    	}
    		    }}}
    		    place=place*2;
    		}
    		break;
    		default: System.err.println("Board::allJumps: default"); 
    		break;
    	}
    	    	
    	  
    	return som;
    }
    
    public SetOfMoves getNonJumpMoves(){
    	setEmpty();
    	SetOfMoves som = new SetOfMoves();
    	long mover;
    	//do foward active and backward active
    	//do right and left
        setEmpty();
    	
    	switch(whoAmI){
    	  case BLACK:
    		//black pawns are never backward active
    		//get the empty 
    		long movers = FAb;
    		//System.err.println("Board::getNonJumpMoves, showing FAb before +4 moves");
    		//showBitz(FAb);
    		//System.err.println("Board::getNonJumpMoves, showing empty");
    		//showBitz(emptyLoc);
    		long place = 2;//2 raised to the bit position
    		//look through FAb, testing one at a time
    		for (int i = 1; i<32; i++){//32 and up cannot move forward 4
    			if((i%9)==0){i++; movers = movers/2; place = place*2;}//this assures we do not test movers at places that are 9ish
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	if ((emptyLoc & place*16)!=0){//can move forward right
    		    		if ((place*16)%9!=0){//valid destination
    		    			Move mv = new Move(i);   		    			
    		    			mv.setSide(Move.Side.BLACK);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+4);
    		    			mv.setStartLocation(i);
    		    			mv.setEndLocation(i+4);
    		    			//System.err.println("Board:: getNonJumpMoves: FAb "+FAb+"finding from "+i+" to "+(i+4));
    		    		}
    		    }
    		    }
    		    place = place *2;
    		}
    		movers = FAb;
    		place = 2;//2 raised to the bit position
    		//System.err.println("Board::getNonJumpMoves, showing FAb before +5 moves");
    		//showBitz(FAb);
    		//System.err.println("Board::getNonJumpMoves, showing empty");
    		//showBitz(emptyLoc);
    		//look through FAb, testing one at a time
    		for (int i = 1; i<31; i++){//31 and up cannot move forward 5
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	if ((emptyLoc & place*32)!=0){//can move forward left
    		    		if ((place*32)%9!=0){
    		    			Move mv = new Move(i);		    			
    		    			mv.setSide(Move.Side.BLACK);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+5);
    		    			mv.setStartLocation(i);
    		    			mv.setEndLocation(i+5);
    		    			//System.err.println("Board:: getNonJumpMoves: FAb "+FAb+"finding from "+i+" to "+(i+5));
    		    		}
    		    	}
    		    }
    		    place=place*2;
    		}
    		movers = BAb;
    		//System.err.println("Board::getNonJumpMoves, showing BAb before -4 moves");
    		//showBitz(BAb);
    		//System.err.println("Board::getNonJumpMoves, showing empty");
    		//showBitz(emptyLoc);
    		place=32;
    		//look through BAb, testing one at a time
    		movers = movers/16; //skip checking places 1-4, they cannot possibly move backwards
    		for (int i = 5; i<36; i++){
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	if ((emptyLoc & place/16)!=0){//can move backward left
    		    		if ((place/16)%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.BLACK);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-4);
      		    			mv.setStartLocation(i);
    		    			mv.setEndLocation(i-4);
    		    			//System.err.println("Board:: getNonJumpMoves: BAb"+BAb+" finding from "+i+" to "+(i-4));
    		    		}
    		    	}
    		    }
    		    place=place*2;
    		}
    		//look through BAb, testing one at a time
    		movers = BAb;
    		//System.err.println("Board::getNonJumpMoves, showing BAb before -5 moves");
    		//showBitz(BAb);
    		//System.err.println("Board::getNonJumpMoves, showing empty");
    		//showBitz(emptyLoc);
    		place = 64;
    		for (int i = 6; i<36; i++){
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	if ((emptyLoc & place/32)!=0){//can move backward right
    		    		if ((place/32)%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.BLACK);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-5);
    		    			mv.setStartLocation(i);
    		    			mv.setEndLocation(i-5);
    		    			//System.err.println("Board:: getNonJumpMoves: BAb "+BAb+" finding from "+i+" to "+(i-5));
    		    		}
    		    	}
    		    }
    		    place=place*2;
    		}
    		break;
    		
    	case WHITE:
    		//white pawns are never forward active
    		//black pawns are never backward active
    		//get the empty 
    		 movers = FAw;
    		place = 1;
    		setEmpty();
    		//look through FA, testing one at a time
    		for (int i = 1; i<31; i++){//32 and up cannot move forward
    			if((i%9)==0){i++; movers = movers/2; place = place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	if ((emptyLoc & place*16)!=0){//can move forward right
    		    		if ((place*16)%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+4);
    		    			mv.setStartLocation(i);
    		    			mv.setEndLocation(i+4);
    		    			//System.err.println("Board:: getNonJumpMoves: finding from FAw "+i+" to "+(i+4));
    		    		}
    		    	}
    		    }
    		    place = place *2;
    		}
    		movers = FAw;
    		setEmpty();
    		place = 1;
    		//look through FAb, testing one at a time
    		for (int i = 1; i<30; i++){
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	if ((emptyLoc & place*32)!=0){//can move forward left
    		    		if ((place*32)%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+5);
    		    			mv.setStartLocation(i);
    		    			mv.setEndLocation(i+5);
    		    			//System.err.println("Board:: getNonJumpMoves: finding from FAw "+i+" to "+(i+5));
    		    		}
    		    	}
    		    }
    		    place=place*2;
    		}
    		movers = BAw;
    		setEmpty();
    		place=32;
    		//look through BAb, testing one at a time
    		movers = movers/16; //skip checking places 1-4, they cannot possibly move backwards
    		for (int i = 5; i<36; i++){//
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	if ((emptyLoc & place/16)!=0){//can move backward left
    		    		if ((place/16)%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-4);
    		    			mv.setStartLocation(i);
    		    			mv.setEndLocation(i-4);
    		    			//System.err.println("Board:: getNonJumpMoves: finding from BAw "+i+" to "+(i-4));
    		    		}
    		    	}
    		    }
    		    place=place*2;
    		}
    		//look through BAw, testing one at a time
    		movers = BAw;
    		//System.err.println("Board::getNonJumpMoves, showing BAw before -5 moves");
    		//showBitz(BAw);
    		//System.err.println("Board::getNonJumpMoves, showing empty");
    		//showBitz(emptyLoc);
    		place = 64;
    		movers=movers/32; //skip checking places 1-5, they cann possibly move backwards
    		for (int i = 6; i<36; i++){
    			if((i%9)==0){i++; movers = movers/2; place=place*2;}
    			movers = movers/2; //start with bit 1
    			mover = movers&1;
    		    if (mover==1){//there is a mover at this place
    		    	if ((emptyLoc & place/32)!=0){//can move backward right
    		    		if ((place/32)%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-5);
    		    			mv.setStartLocation(i);
    		    			mv.setEndLocation(i-5);
    		    			//System.err.println("Board:: getNonJumpMoves: finding from BAw "+i+" to "+(i-5));
    		    		}
    		    	}
    		    }
    		    place=place*2;
    		}
    		break;
    		default: System.err.println("Board::allNonJumps: default"); 
    		break;
    	}
    	
	      return som;
}
    private BoardValue lookUpValue(long wFA, long wBA, long bFA, long bBA, Move.Side side){
    	BoardValue result = null; 
    	if(haveBoardValue){result = this.boardValue;}
    	else{
    		result = new BoardValue();
    		//TODO here we go read in the database
    		this.boardValue =  null; //whatever came back from the database
    	}
    	
    	return result;
    }

    private Move chooseBestMove(SetOfMoves som){//minimax-decision p.166
    	if(som.howMany()==1){return som.getMove(0);}
    	Move best = null;
    	int ply = 0;
		int howManyMoves = som.howMany();
		//int guess = (int) Math.floor(Math.random()*howManyMoves);
	   	Board copyOfBoard = copyBoard();
    	copyOfBoard.setWhoAmI(this.whoAmI);
    	System.err.println("Board::chooseBest: howMany= "+howManyMoves+" for "+copyOfBoard.getWhoAmI());
		BoardValue currentBestVal = new BoardValue();
		long argmax = -9999L;
		Move maxTheMin = null;
		long tentargmax = argmax;
		
		for(int moveIndex = 0; moveIndex < howManyMoves; moveIndex++ ){//figure out argmax a, which is the moveindex, in Actions(bd, s) ourMinValue(ourResult (bd,s,a))
			ply=0;
		    tentargmax = Math.max(argmax,  ourMinValue(ourResult (copyOfBoard, moveIndex, som), ply+1));	
			if (argmax < tentargmax){
				maxTheMin = som.getMove(moveIndex);
				argmax = tentargmax;
			}
			//generate the board from this board and the move //return that action that corresponds to the maximum among the minimum values
			//evaluate the resulting board
			//keep the best move
		}
    	return maxTheMin;
    }
    private  long ourMaxValue(Board bd, int ply){//see p. 166 maybe we'll want something more complicated than long? maybe short is enough
    	//System.err.println("Board::ourMaxValue: with ply "+ply+" and side "+bd.getWhoAmI());
    	long v = 0;
    	if (ourTerminalTest(bd, ply)) {return ourUtility(bd);}
    	v = -9999L;
    	//here's where we figure out the set of moves
    	bd.changeSides();//opponent
    	SetOfMoves som = bd.getMoves();//opponent's moves
    	int howMany = som.howMany();
    	for (int a = 0; a< howMany; a++){
    		v = Math.max(v, ourMinValue(ourResult(bd, a, som),  ply+1 ));//result of move includes changing sides
    	}
    	return v;
    }
    private long ourMinValue(Board bd,  int ply){//see p. 166 
    	//System.err.println("Board::ourMinValue: with ply "+ply+" and side "+bd.getWhoAmI());
    	long v = 0;
    	if (ourTerminalTest(bd, ply)) {return ourUtility(bd);}
    	v = 9999L;
    	//here's where we figure out the set of moves
    	bd.changeSides(); //the opponent
    	SetOfMoves som = bd.getMoves();  //the opponent's moves	
    	int howMany = som.howMany();
    	for (int a = 0; a< howMany; a++){
    		v = Math.min(v, ourMaxValue(ourResult(bd,  a, som), ply+1));//result of move includes changing sides
    	}
    	return v;
    }
    private long ourUtility(Board bd){//see p. 166 
    	long u = 0L;
    	int[] theFeatureValues = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    	//TODO
    	//Have we recorded this board:
    	theFeatureValues =myPersistence.GetStateEvaluation(BAw, FAb, FAw, BAb);
    	if (theFeatureValues != null){
    		System.err.println("Board::ourUtility: found something in database");
    		//now what do we do? we should get back a vector of individual values for features of the board, which then
    		//get evaluated with the weighted sum, where the weights are what we have learned them to be
    		u=myEvaluator.weightedSum(theFeatureValues);
    	}
    	//u=(long) Math.floor(Math.random()*1000);
    	u=bd.pieceAdvantage();
    	//System.err.println("Board::ourUtility "+u);
    	return u;
    }
    private Board ourResult(Board bd, int a, SetOfMoves som){//see p. 166, the result is a different board, a different side, a different set of moves
    	//do I want to create a data structure?, yes, a board, whose "whose turn" is set
        //applies one move, the one identified by a
    	Board resBd = new Board(bd);//supposed to be a copy

    	//just apply the move
    	//place piece at end
    	//remove piece at start
    	//remove the jumped pieces
    	Move mv = som.getMove(a);
    	resBd.setWhoAmI(mv.getSide()); 
    	resBd.updateBoard(mv);
    	return resBd;
    }
    private boolean ourTerminalTest(Board bd, int ply){
    	
    	//what's the terminal test?
    	//Samuel's terminal test is p.214 always look ahead 3 (unit is one move of one player) plies
    	//evaluate here unless
    	//1 next move is a jump
    	//2 last move was a jump
    	//3 exchange offer possible
    	//if ply ==4, evaluate unless 1 or 3
    	//if ply >20 because he ran out of memory here, we could run out of time -- if a pending jump, adjust score
    	if(ply >=11){return true;}//TODO temp
    	if(ply >20){return true;}
    	boolean noJumps=!bd.anyJumps();
    	//if ply >= 11 evaluate unless (jump ^ NOT(piece advantage >2 kings
    	if(ply >= 11){if (noJumps || (bd.pieceAdvantageKings()>2)){return true;}}
    	//if ply >= 5 evaluate unless 1
    	if(ply >= 5){if (noJumps){return true;}}   	
    	return false;
    }
    
    private Board copyBoard(){
    	Board theCopy = new Board(myPersistence);
    	theCopy.setFAw(this.FAw);
    	theCopy.setFAb(this.FAb);
    	theCopy.setBAw(this.BAw);
    	theCopy.setBAb(this.BAb);
    	theCopy.setFPw(this.FPw);
    	theCopy.setFPb(this.FPb);
    	theCopy.setBPw(this.BPw);
    	theCopy.setBPb(this.BPb);
    	theCopy.setFirstMove(this.firstMove);
    	theCopy.setRFw(this.RFw);
    	theCopy.setLFw(this.LFw);
    	theCopy.setRBw(this.RBw);
    	theCopy.setLBw(this.LBw);
    	theCopy.setRFb(this.RFb);
    	theCopy.setLFb(this.LFb);
    	theCopy.setRBb(this.RBb);
    	theCopy.setLBb(this.LBb);
    	theCopy.setHaveBoardValue(this.haveBoardValue);
    	theCopy.setBoardValue(this.boardValue);
    	theCopy.whoAmI = this.whoAmI;
    	return theCopy;
    	
    }
    public void setFAw(long val){
    	this.FAw = val;
    }
    public void setFAb(long val){
    	this.FAb = val;
    }
    public void setBAw(long val){
    	this.BAw = val;
    }
    public void setBAb(long val){
    	this.BAb = val;
    }
    public void setFPw(long val){
    	this.FPw = val;
    }
    public void setFPb(long val){
    	this.FPb = val;
    }
    public void setBPw(long val){
    	this.BPw = val;
    }
    public void setBPb(long val){
    	this.BPb = val;
    }
    public void setFirstMove(boolean val){
    	this.firstMove = val;
    }
    public void setRFw(long val){
    	this.RFw = val;
    }
    public void setLFw(long val){
    	this.LFw = val;
    }
    public void setRBw(long val){
    	this.RBw = val;
    }
    public void setLBw(long val){
    	this.LBw = val;
    }
    public void setRFb(long val){
    	this.RFb = val;
    }
    public void setLFb(long val){
    	this.LFb = val;
    }
    public void setRBb(long val){
    	this.RBb = val;
    }
    public void setLBb(long val){
    	this.LBb = val;
    }
    public long getFAw(){
    	return this.FAw;
    }
    public long getFAb(){
    	return this.FAb;
    }
    public long getBAw(){
    	return this.BAw;
    }
    public long getBAb(){
    	return this.BAb;
    }
    public long getFPw(){
    	return this.FPw;
    }
    public long getFPb(){
    	return this.FPb;
    }
    public long getBPw(){
    	return this.BPw;
    }
    public long getBPb(){
    	return this.BPb;
    }

    public long getRFw(){
    	return this.RFw;
    }
    public long getLFw(){
    	return this.LFw;
    }
    public long getRBw(){
    	return this.RBw;
    }
    public long getLBw(){
    	return this.LBw;
    }
    public long getRFb(){
    	return this.RFb;
    }
    public long getLFb(){
    	return this.LFb;
    }
    public long getRBb(){
    	return this.RBb;
    }
    public long getLBb(){
    	return this.LBb;
    }
    public void setHaveBoardValue(boolean val){
    	this.haveBoardValue=val;
    }
    public void setBoardValue(BoardValue val){
    	this.boardValue=val;
    }

    private void updateBoard(Move mv){
    	//the variables that are getting fixed are FAb, FAw, BAb, BAw, FPb, FPw, BPb, BPw
    	int startPt = mv.getStartLocation();
    	whoseTurn = mv.getSide();
    	//System.err.println("Board::updateBoard: startPt "+startPt+" by "+whoseTurn);   	
    	//System.err.println("Board::updateBoard: removing at "+startPt);
    	removePiece(startPt);
    	int howManySteps = mv.getHowManySteps();
    	for (int stepIndex = 0; stepIndex<howManySteps;stepIndex++){
    		//get the step
    		Step step = mv.getStep(stepIndex);
    		//see if it was a jump
    		int stepStart = step.getStartLocation();
    		int stepEnd = step.getEndLocation();
    		int diff = Math.abs(stepStart-stepEnd);
    		if((diff ==4) || (diff==5)){
    			;
    		}
    		else{//jump
    			int avg = (stepEnd + stepStart)/2;
    		    //remove piece from this location
    			//System.err.println("Board::update: noticed capture by "+mv.getSide());
    			removePiece(avg);    		    
    		}
    		if (stepIndex == (howManySteps-1)){
    			//System.err.println("Board::update: calling place piece with "+mv.getEndLocation());
    			Piece.Rank r = Piece.Rank.PAWN;
    			switch(mv.getSide()){
    				case BLACK:
    					if(mv.getEndLocation()>31){r = Piece.Rank.KING;}
    					break;
    				case WHITE:
    					if(mv.getEndLocation()<5){r = Piece.Rank.KING;}
    			}
    			placePiece(mv.getEndLocation(), mv.getSide(),r); 
    		}	
    	}
    	/* TODO caused problems? if ((mv.getRankAtStart()) != (mv.getRankAtEnd()) ){//if move has made piece a king, update the board variables I think placePiece does this
    		//add to backward active or forward active, depending on side
    		switch(mv.getSide()){
    		case BLACK:
    			BAb = BAb | mv.getEndLocation();
    			break;
    		case WHITE:
    			FAw = FAw | mv.getEndLocation();
    		}}*/
    }
    private void removePiece(long bitLoc){//active or passive, they're gone. Cheaper to do all than check B/W
    	long powerbit = (long)(Math.pow(2, bitLoc));
    	long notBitLoc = powerbit ^ (long)(Math.pow(2,36)-1);
    	FAb = FAb & notBitLoc;
    	FAw = FAw & notBitLoc;
    	BAb = BAb & notBitLoc;
    	BAw = BAw & notBitLoc;
    	FPb = FPb & notBitLoc;
    	FPw = FPw & notBitLoc;
    	BPb = BPb & notBitLoc;
    	BPw = BPw & notBitLoc; 	
    	//showBitz(emptyLoc);
    	setEmpty(); //updating empty is this where emptiness of a move start gets lost?
    	//showBitz(emptyLoc);
    }
    
    private void placePiece(long bitLoc, Move.Side whosePiece, Piece.Rank r){
    	//need to or piece into correct words
    	//it could get stuck and become passive, or it could still be active, F or B 
    	//isn't both active and passive
    	long powerBit = (long)Math.pow(2, bitLoc);
    	switch(whosePiece){
    	case BLACK:
    	
    	FAb = FAb | powerBit;
    	FPb = FPb | powerBit;
    	switch(r){
    	case KING:
    		BAb = BAb | powerBit;
        	BPb = BPb | powerBit;
    	}
    	

    	break;
    	case WHITE:
    	BPw = BPw | powerBit;
    	BAw = BAw | powerBit;
    	switch(r){
    	case KING:   	
    	  FAw = FAw | powerBit;
    	  FPw = FPw | powerBit;
    	}
    	break;
    	}
    	setEmpty();
    }
    public void showBitz(long num){
    	long impulse = (long) Math.pow(2,35);
    	boolean evenRow = true;
    	System.err.print(" ");
    	for(int bitpos = 35; bitpos >0; bitpos--){
    		 if((bitpos%9)==0){bitpos--; impulse=impulse/2;}
             if((impulse & num)!=0){System.err.print("1");}
             else{System.err.print("0");}
             impulse = impulse/2;
             System.err.print(" ");
             if (bitpos==32||bitpos==28||bitpos==23||bitpos ==19||bitpos==14||bitpos==10||bitpos==5||bitpos==1){
            	 System.err.println(" ");
            	 evenRow = !evenRow;
        		 if(evenRow)System.err.print(" ");
     		}
    	}
    	System.err.println("***************");
    }
    private boolean opponentAt(int i, Move.Side whoAmI){
    	boolean result = false;
    	long impulse = (long)Math.pow(2, i);
    	switch(whoAmI){
    	case BLACK:
    		result = ((impulse & (FAw | BAw | FPw | BPw)))!=0;
    		break;
    	case WHITE:
    		result = ((impulse & (FAb | BAb | FPb | BPb)))!=0;
    		break;
    		default:
    			break;
    	}
       // System.err.println("Board::opponentAt "+i+" is "+result);
    	return result;
    }
    public int pieceAdvantageKings(){
    	int nKings = 0;
    	//TODO
    	return nKings;
    }
    public void setWhoAmI(Move.Side s){
    	this.whoAmI = s;
    }
    public Move.Side getWhoAmI(){
    	return this.whoAmI;
    }
    public void changeSides(){
    	switch(whoAmI){
    	case BLACK:
    		this.whoAmI = Move.Side.WHITE;
    		break;
    	case WHITE:
    		this.whoAmI = Move.Side.BLACK;
    	}
    }
    public boolean anyJumps(int loc, Move.Side s, Piece.Rank r){//Piece mover){//don't call me with a mover of the wrong side, because I do not check
    	setEmpty();
    	//check all possible jumps. If any change in variables, there was a jump
    	//kings can jump any thing, so what's not empty?
    	//forward active, try going forward, backward active try going backward. 
    	long opponents = 0L;
		long jumpers = 0L;//they move differently so we check them separately
		long successful = 0L;
    	//System.err.println("Board::anyJumps(3 args): with "+whoAmI);
    	switch(s){
    	case BLACK:
    		//black pawns are never backward active
    		//get the empty 
    		opponents = FAw | FPw | BAw | BPw;
    		jumpers = (long) Math.pow(2, loc);//location of mover FAb;//they move differently so we check them separately
    		successful =  jumpers & (opponents>>4) & (emptyLoc>>8);
    		//System.err.println("Board::anyJumps: showing successful");
    		//showBitz(successful);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		successful =  jumpers & (opponents>>5) & (emptyLoc>>10);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		switch(r){
    		case KING:
    		
    		successful =   jumpers & (opponents<<5) & (emptyLoc<<10);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		successful =  jumpers & (opponents<<4) & (emptyLoc<<8);
    		if (successful != 0){//there are jumps
    			return true;
    		}}
            break;
    		
    	case WHITE:
    		//white pawns are never forward active
    		opponents = FAb | FPb | BAb | BPb;
    		jumpers = (long) Math.pow(2, loc);//FAw;
    		successful =  jumpers & (opponents<<5) & (emptyLoc<<10);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		successful = jumpers & (opponents<<4) & (emptyLoc<<8);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		switch(r){
    		case KING:
    		
    		successful =   jumpers & (opponents>>4) & (emptyLoc>>8);
    		if (successful != 0){//there are jumps
    			return true;
    		}
    		successful =   jumpers & (opponents>>5) & (emptyLoc>>10);
    		if (successful != 0){//there are jumps
    			return true;
    		}}
    		break;
    		default: System.err.println("Board::anyJumpsOnePiece: default"); return false; 
    	}
    	return false;
    }
    public boolean canJumpForwardRight(int loc, Move.Side s){
    	boolean answer = false;
    	long opponents = 0L;
    	long successful = 0L;
    	long jumpers = (long) Math.pow(2, loc);//location of mover FAb;//they move differently so we check them separately
    	switch(s){
    	  case BLACK: 
    		opponents = FAw | FPw | BAw | BPw;   		
            break;    		
    	  case WHITE:
    		opponents = FAb | FPb | BAb | BPb;
           }
    	successful =   jumpers & (opponents<<4) & (emptyLoc<<8);
    	if (successful != 0){//there are jumps
    			return true;
    	}
    	return answer;
    }
    public boolean canJumpForwardLeft(int loc, Move.Side s){
    	boolean answer = false;
    	long opponents = 0L;
    	long successful = 0L;
    	long jumpers = (long) Math.pow(2, loc);//location of mover FAb;//they move differently so we check them separately
    	switch(s){
    	  case BLACK: 
    		opponents = FAw | FPw | BAw | BPw;   		
            break;    		
    	  case WHITE:
    		opponents = FAb | FPb | BAb | BPb;
           }
    	successful =   jumpers & (opponents<<5) & (emptyLoc<<10);
    	if (successful != 0){//there are jumps
    			return true;
    	}
    	return answer;
    }
    public boolean canJumpBackwardRight(int loc, Move.Side s){
    	boolean answer = false;
    	long opponents = 0L;
    	long successful = 0L;
    	long jumpers = (long) Math.pow(2, loc);//location of mover FAb;//they move differently so we check them separately
    	switch(s){
    	  case BLACK: 
    		opponents = FAw | FPw | BAw | BPw;   		
            break;    		
    	  case WHITE:
    		opponents = FAb | FPb | BAb | BPb;
           }
    	successful =   jumpers & (opponents>>5) & (emptyLoc>>10);
    	if (successful != 0){//there are jumps
    			return true;
    	}
    	return answer;
    }
    public boolean canJumpBackwardLeft(int loc, Move.Side s){
    	boolean answer = false;
    	long opponents = 0L;
    	long successful = 0L;
    	long jumpers = (long) Math.pow(2, loc);//location of mover FAb;//they move differently so we check them separately
    	switch(s){
    	  case BLACK: 
    		opponents = FAw | FPw | BAw | BPw;   		
            break;    		
    	  case WHITE:
    		opponents = FAb | FPb | BAb | BPb;
           }
    	successful =   jumpers & (opponents>>4) & (emptyLoc>>8);
    	if (successful != 0){//there are jumps
    			return true;
    	}
    	return answer;
    }
    public SetOfMoves getMoves(){
    	SetOfMoves som = null;
    	if (anyJumps()){
    		//System.err.println("Board::getMoves: there are jumps");
    		som = getJumpMoves();
    	}
    	else{
    		som = getNonJumpMoves();
    	}
    	return som;
    }
    public int pieceAdvantage(){
    	int adv= 0;
    	//count up pieces of whoAmI side
    	long beCounted = 0L;
    	switch(whoAmI){
    	case BLACK:
    		beCounted = FAb/2; //don't look at 0 bit
    		for(int i = 1; i< 36; i++){
    			if(i%9==0){i++; beCounted= beCounted/2;}
    			if(beCounted%2==1){adv++;}
    			beCounted= beCounted/2;   			
    		}
    		beCounted = BAb/2;
    		for(int i = 1; i< 36; i++){
    			if(i%9==0){i++; beCounted= beCounted/2;}
    			if(beCounted%2==1){adv--;}
    			beCounted= beCounted/2;   			
    		}
    		break;
    	case WHITE:
    		beCounted = BAw/2; //don't look at 0 bit
    		for(int i = 1; i< 36; i++){
    			if(i%9==0){i++; beCounted= beCounted/2;}
    			if(beCounted%2==1){adv++;}
    			beCounted= beCounted/2;   			
    		}
    		beCounted = FAw/2;
    		for(int i = 1; i< 36; i++){
    			if(i%9==0){i++; beCounted= beCounted/2;}
    			if(beCounted%2==1){adv--;}
    			beCounted= beCounted/2;   			
    		}   		
    	}
    	//count down pieces of other side
    	return adv;
    }
 
}
    
    
