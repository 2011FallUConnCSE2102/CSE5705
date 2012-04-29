package state;


import exceptions.IllegalMoveException;
import actions.*;
import valuation.*;
import persistence.DBHandler;
import learning.Correlation;
import learning.SamuelStrategy;


public class Board {

	public int moveCounter=0;
	/*private Move.Side whoseTurn = null;*/
	private Move.Side whoAmI = null;
	private boolean previousMoveJump= false;
	private Piece[] theWhitePieces = null;
	private Piece[] theBlackPieces = null;
	private DBHandler myPersistence = null;
	public Evaluator myEvaluator = null;
	public Correlation myCorrelation = null;
	public SamuelStrategy mySamuelStrategy = null;
	boolean alphaBeta = true;
	long epsilon = 1L;//this value is used to reduce the utility value of a board each time that value is reported up a ply, see samuel p216
	long arbitraryMinimum = 2L;// see Samuel p. 219, for delta big enough
	public VisualBoard vb = null;
	long score = 0L;
	
	public long FAw = 0L;                      // white pieces that move "forward" will only be kings, and there are none at start
	public long FAb =  (1L<<1)+ //forward active when black's turn: pawns  
			(1L<<2)+
			     (1L<<3)+
			   (1L<<4)+
			   (1L<<5)+
			    (1L<<6)+
			    (1L<<7)+ 
			    (1L<<8)+
			    (1L<<10)+
			    (1L<<11)+  
			    (1L<<12)+			    
			    (1L<<13);
	public long BAw =  (1L<<23)+ //backward active on white's turn: pawns   
		    	(1L<<24)+
		    	(1L<<25)+
		    	(1L<<26)+
		    	(1L<<28)+
		    	(1L<<29)+
		    	(1L<<30)+ 
		    	(1L<<31)+
		    	(1L<<32)+
		    	(1L<<33)+  
			    (1L<<34)+
			    (1L<<35);
	public long BAb =  0L;                    //backward active on black's turn: nobody, until kings				
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

    private long RFw; //right forward, these are locations, corresponding to steps taken
    private long LFw; //left forward
    private long RBw; //right backward
    private long LBw; //left backward
    private long RFb; //right forward, these are locations, corresponding to steps taken
    private long LFb; //left forward
    private long RBb; //right backward
    private long LBb; //left backward
    private int firstBreakPly = 2;
    private int secondBreakPly = 4;
    private int thirdBreakPly = 5;
    private int fourthBreakPly = 11;
    private int fifthBreakPly = 13;//samuel 20
    private int numberPiecesOnBoardThreshold = 8;
    private boolean haveBoardValue = false;
    private double boardValue = 0;
    int[] theFeatureValues = new int[DBHandler.NUMPARAMS];
    long almostAllOnes = (long)Math.pow(2,63)-1;
    long allOnes = almostAllOnes+almostAllOnes +1;
    long onesLSB0 = almostAllOnes+almostAllOnes;
    StringBuffer firstBlackMove = new StringBuffer("(5:5):(4:4)");
    SetOfBoardsWithPolynomial boardHistory =null;
	
	public Board(DBHandler db, boolean alphaBeta, SetOfBoardsWithPolynomial history, VisualBoard vb){//this used by RMCheckersClient
		FAw = 0L;
		BAb =  0L;
		FAb =   (1L<<1)+ //forward active when black's turn: pawns  
				(1L<<2)+
			    (1L<<3)+
			    (1L<<4)+
			    (1L<<5)+
			    (1L<<6)+
			    (1L<<7)+ 
			    (1L<<8)+
			    (1L<<10)+
			    (1L<<11)+  
			    (1L<<12)+			    
			    (1L<<13);
		
		 
		BAw =  (1L<<23)+ //backward active on white's turn: pawns   
		    	(1L<<24)+
		    	(1L<<25)+
		    	(1L<<26)+
		    	(1L<<28)+
		    	(1L<<29)+
		    	(1L<<30)+ 
		    	(1L<<31)+
		    	(1L<<32)+
		    	(1L<<33)+  
			    (1L<<34)+
			    (1L<<35);
	
		this.boardHistory = history;
		this.alphaBeta = alphaBeta;
		 myPersistence = db;
		 myEvaluator = new Evaluator(this, alphaBeta);
		 myCorrelation = new Correlation(this);
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
		
		this.vb = vb;
		mySamuelStrategy = new SamuelStrategy();
		
	}
	
	public Board(Board bd, DBHandler db, boolean alphaBeta, VisualBoard vb){
		//all the components of board copied into new board
		FAw = bd.FAw;
		BAb =  bd.BAb;
		FAb =  bd.FAb;		 
		BAw =  bd.BAw;
		
		this.alphaBeta=alphaBeta;
		this.boardHistory = bd.boardHistory;
		this.myPersistence = db;
		this.myEvaluator = new Evaluator(this, alphaBeta);
		this.firstMove = bd.firstMove;
		this.vb = vb;
		
	}
	public Board(long faw,
			long fab,
			long baw,
			long bab,
			DBHandler db, 
			boolean alphaBeta, SetOfBoardsWithPolynomial history, VisualBoard vb){
			
		 this.alphaBeta=alphaBeta;
		 this.boardHistory=history;
		 myPersistence = db;
		 myEvaluator = new Evaluator(this, alphaBeta);
		 
		 this.FAw = faw;
		 this.FAb = fab;
		 this.BAw = baw;
		 this.BAb = bab;
		 this.vb = vb;
	}
	  public Board(Board bd){
		  this.FAw = bd.FAw;
			 this.FAb = bd.FAb;
			 this.BAw = bd.BAw;
			 this.BAb = bd.BAb;
			 this.FAw=bd.FAw;
	    	this.alphaBeta = bd.alphaBeta;
	    	this.myEvaluator=new Evaluator(this, this.alphaBeta);
	    	//TODO temp out of heap this.myCorrelation = new Correlation(this);	 
	    	this.myPersistence = bd.myPersistence;
	    	this.boardHistory = bd.boardHistory;
	    	this.firstMove=bd.firstMove;
	    	this.RFw = bd.RFw;
	    	this.LFw=bd.LFw;
	    	this.RBw=bd.RBw;
	    	this.LBw= bd.LBw;
	    	this.RFb=bd.RFb;
	    	this.LFb=bd.LFb;
	    	this.RBb=bd.RBb;
	    	this.LBb=bd.LBb;
	    	this.haveBoardValue=bd.haveBoardValue;
	    	this.boardValue=bd.boardValue;
	    	this.whoAmI = bd.whoAmI;
	    	this.vb = bd.vb;
	    	}
    
    public void setEmpty(){
    	long invertedEmpty = (FAw | BAw | FAb | BAb  );//want bitwise or's
    	invertedEmpty = invertedEmpty | (1L<<0) |(1L<<9) |(1L<<18) | (1L<<27);//factors of 9 are unavailable
    	for(int i=36; i<63; i++){//anything over 35 is unavailable
    			invertedEmpty = invertedEmpty | (1L<<i);
    	}
    	emptyLoc = myEvaluator.parNOT(invertedEmpty);
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
    	                    &  (FAb | BAb );     //these are any opponent piece, occupying the spot over which jumping
    	return nextRFw;
    	
    }
    public long calcjRFb(){
    	long nextRFb = RFb*16*16 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    	                    &  RFb*16 //this is the intermediate location
    	                    &  (FAw | BAw );     //these are any opponent piece; //this is the logical and with occupied
    	return nextRFb;
    	
    }
    public long calcjLFw(){
    	long nextLFw = LFw*32*32 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    	                    &  LFw*32 //this is the intermediate location
    	                    &  (FAb | BAb );     //these are any opponent piece; //this is the logical and with occupied
    	return nextLFw;
    	
    }
    public long calcjLFb(){
    	long nextLFb = LFb*32*32 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            &  LFb*32 //this is the intermediate location
    	                    &  (FAw | BAw );     //these are any opponent piece; //this is the logical and with occupied
    	return nextLFb;
    	
    }
    public long calcjRBw(){
    	long nextRBw = (RBw/16)/16 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            &  RBw/16 //this is the intermediate location
    	                    &  (FAb | BAb );     //these are any opponent piece; //this is the logical and withoccupied
    	return nextRBw;
    	
    }
    public long calcjRBb(){
    	long nextRBb = (RBb/16)/16 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            & RBb/16 //this is the intermediate location
    	                    &  (FAw | BAw );     //these are any opponent piece; //this is the logical and with occupied
    	return nextRBb;
    	
    }
    public long calcjLBw(){
    	long nextLBw = (LBw/32)/32 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            &  LBw/32 //this is the intermediate location
    	                    &  (FAb | BAb );     //these are any opponent piece; //this is the logical and with occupied
    	return nextLBw;
    	
    }
    public long calcjLBb(){
    	long nextLBb = (LBb/32)/32 //this is the shift by 4 
    			            & emptyLoc //this is the logical and with empty
    			            &  LBb/32 //this is the intermediate location
    	                    &  (FAw | BAw );     //these are any opponent piece; //this is the logical and with occupied
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
    	System.err.println("Setting FA");
    	switch(side){
    	case BLACK:
    		FAb = FA;
    		break;
    	case WHITE:
    		FAw = FA;
    		break;
    	}
    }
    public Board changeFA(long FA, Move.Side side, boolean alphaBeta){
        Board result = new Board(this, myPersistence, alphaBeta, vb);
        result.setFA(FA, side);
        return result;
    }
    
    
  /*  public double evalUsingMiniMax(long oldFA, long newFA,
    		                     long oldBA, long newBA, Move.Side side, int ply, boolean alphaBeta){ //whatever the side is, maybe don't need to know
    	//what do we want to come back from evals? the value of the board
    	//let's keep all the features scores separately, a vector of scores
    	//to be used to determine, ultimately, the choice of move
    	//grow the tree forward some number of plies
    	//generate all possible moves in parallel, explore then one by one p.212 top
    	//we're taking in results from a step
    	//BoardValue result = new BoardValue();//have to know the right number
    	double result = 0;
    	//Do I know this board already?
    	result = lookUpValue(FAw, BAw, FAb, BAb, side);
    	if (result != 0){
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
    	    	  Board nbd =  changeFA(testFA, side, alphaBeta);
    	    	  nbd.evalUsingMiniMax(testFA, newFA, oldBA, newBA, side, ply+1, alphaBeta); //recursive call, need base case
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
    	
    }*/
    public String acceptMoveAndRespond(Move mv){
    	moveCounter+=2; 
    	//System.err.println("Board::acceptMoveAndRespond:");
    	StringBuffer bestMove_sb = new StringBuffer("(2:4):(3:5)");
    	Move bestMove = null;
    	switch( mv.getSide()){
    	case BLACK://they are BLACK
    		this.whoAmI = Move.Side.WHITE;
    		/*this.whoseTurn =  Move.Side.BLACK;*/
    		break;
    	case WHITE:
    		this.whoAmI = Move.Side.BLACK;
    		/*this.whoseTurn = Move.Side.WHITE;*/
    		break;
    	default:
    		System.err.println("Board::acceptMoveAndRespond: no side set in Move");
    	}
    	//update the board
    		//find the piece at the start part of the move
    	    //capture any pieces that get jumped
    	    //update the board
    	//consider the possibilities for response
    	
    	//update the board
    	   //find the piece as the start part of the move, remove it
    	   updateBoard(mv);
    	   vb.addState(FAw, FAb, BAw, BAb);
    	
    	//showBitz(emptyLoc);
    	  
    	 
    	SetOfMoves som = null;
    	if (anyJumps()){
    		
    		som = getJumpMoves();
        	if (som.howMany()>0){   				
        		bestMove =chooseBestMove(som);
        		 updateBoard(bestMove);
        		 vb.addState(FAw, FAb, BAw, BAb);
        		return(bestMove.toString());
        	}
    	}
    	else{
    		
    		som = getNonJumpMoves();
    		
    	}
    	if (som.howMany()>0){
    		bestMove = chooseBestMove(som);
    		//now update board with this chosen move    		
    		 updateBoard(bestMove);//make sure updateBoard updates theFeatureValues
    		 vb.addState(FAw, FAb, BAw, BAb);
    		 boardHistory.addBoardPolynomial(this,  theFeatureValues, myEvaluator.getWeightValues());
    		//System.err.println("Board:: acceptRespond recording my own move");
    		//showBitz(emptyLoc);
    		return(bestMove.toString());
    	}   	
    	//if we got to here, there is no move 
    	return "NO MOVE: CONCEDE";
    }
    public StringBuffer initiateMove(Move.Side side){
    	 //need to figure out right values for place and remove
    	//int placeLoc=16;
    	//int removeLoc=12;
    	System.err.println("Board::initiateMove with"+firstBlackMove);
    	StringBuffer justForMove = new StringBuffer("Move:Black:"+firstBlackMove);
    	Move mv = new Move(justForMove, this);
    	int removeLoc = mv.getStartLocation();
    	int placeLoc = mv.getEndLocation();
        setEmpty();
        //showBitz(emptyLoc);
    	placePiece(placeLoc, Move.Side.BLACK, Piece.Rank.PAWN );  
        //showBitz(emptyLoc);
    	removePiece(removeLoc);//this takes away the piece from its start
    	//showBitz(emptyLoc);
    	if(this.firstMove){this.firstMove=false;}
    	else{
    		//only happens when we start out as black, otherwise will be accept and respond
    	}
    	return firstBlackMove;
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
    		opponents = FAw |  BAw ;
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
    		opponents = FAb |  BAb ;
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
    		    	if (((i+4)%9) != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i+4, whoAmI)){
    		    	//and there is space at +8
    		    	if ((emptyLoc & (place*16*16))!=0){//can move forward right
    		    		if (((i+8)%9)!=0){//valid destination
    		    			Move mv = new Move(i);   		    			
    		    			mv.setSide(Move.Side.BLACK);
    		    			if(((1L<<i) & BAb)!=0){//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();    		    			
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+8);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = new Board(this);
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
    		    				//System.err.println("Board::getJumpMoves, with howManyExtensions "+howManyExtensions);
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
    		    						//System.err.println("Board::getJumpMoves: adding Extension Move, start "+theExtension.getStartLocation());
    		    						som.addMove(theExtension);
    		    					}
    		    				}
    		    				//for the first move, we extend mv, but for more than one, we make a copy of mv and extend it that specific way
    		    				if(howManyExtensions==1){
	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}}
    		    				//System.err.println("Board::getJumpMoves, with howManyExtensions "+howManyExtensions);
    		    				if(howManyExtensions==0){
    		    					mv.setEndLocation(step.getEndLocation());
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
    		    	if (((i+5)%9) != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i+5, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & (place*32*32))!=0){//can move forward left
    		    		if (((i+10)%9)!=0){
    		    			Move mv = new Move(i);		    			
    		    			mv.setSide(Move.Side.BLACK);
    		    			if(((1L<<i) & BAb)!=0){//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();   		    			
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+10);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd = new Board(this);
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

    		    				if(howManyExtensions==1){
    		    					int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
    		    				
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}}
    		    				if(howManyExtensions==0){
    		    					mv.setEndLocation(step.getEndLocation());
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
    		    	if (((i-4)%9) != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i-4, whoAmI)){
    		    	//and there is space at -8
    		    	if ((emptyLoc & place/(16*16))!=0){//can move backward left
    		    		if (((i-8)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.BLACK);
    		    			{//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-8);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd =  new Board(this);
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
    		    				if(howManyExtensions==1){
	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}}
    		    				if(howManyExtensions==0){
    		    					mv.setEndLocation(step.getEndLocation());
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
    		    	if (((i-5)%9) != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i-5, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & place/(32*32))!=0){//can move backward right
    		    		if (((i-8)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.BLACK);
    		    			{//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-10);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd =  new Board(this);
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
    		    				if(howManyExtensions==1){
	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
	    							Step theStep = new Step(extStep);
	    							mv.addStep(theStep);
	    						}}
    		    				if(howManyExtensions==0){
    		    					mv.setEndLocation(step.getEndLocation());
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
    		    	if (((i+4)%9) != 0){//don't want 9ish intermediary locations 
    		    	if (opponentAt(i+4, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & (place*16*16))!=0){//can move forward right
    		    		if (((i+8)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			{//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+8);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: FAw created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd =  new Board(this);
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

    		    				if(howManyExtensions==1){
    	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
    	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
    	    							Step theStep = new Step(extStep);
    	    							mv.addStep(theStep);
    	    						}}
        		    				if(howManyExtensions==0){
        		    					mv.setEndLocation(step.getEndLocation());
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
    		    		if (((i+10)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			{//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+10);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: FAw created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd =  new Board(this);
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

    		    				if(howManyExtensions==1){
    	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
    	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
    	    							Step theStep = new Step(extStep);
    	    							mv.addStep(theStep);
    	    						}}
        		    				if(howManyExtensions==0){
        		    					mv.setEndLocation(step.getEndLocation());
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
    		    	if (((i-4)%9) != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i-4, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & (place/(16*16)))!=0){//can move backward left
    		    		if (((i-8)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			if(((1L<<i) & FAw)!=0){//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-8);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: BAw created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd =  new Board(this); 			 
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

    		    				if(howManyExtensions==1){
    	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
    	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
    	    							Step theStep = new Step(extStep);
    	    							mv.addStep(theStep);
    	    						}}
        		    				if(howManyExtensions==0){
        		    					mv.setEndLocation(step.getEndLocation());
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
    		    	if (((i-5)%9) != 0){//don't want 9ish intermediary locations
    		    	if (opponentAt(i-5, whoAmI)){
    		    	//and there is space at +10
    		    	if ((emptyLoc & (place/(32*32)))!=0){//can move backward right
    		    		if (((i-10)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			if(((1L<<i) & FAw)!=0){//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-10);
    		    			mv.addStep(step);
    		    			//System.err.println("Board::getJumpMoves: BAw created move, start "+mv.getStartLocation()+" end "+mv.getEndLocation());
    		    			//OK, need recursive call, probably here. As recursive, only need to extend by depth of one step each recursion, could get two moves
    		    			Board nextBd =  new Board(this);
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

    		    				if(howManyExtensions==1){
    	    						int howManyStepsThisMove = jumpExtensions.getMove(0).getHowManySteps();
    	    						for (int stepIndex=0; stepIndex<howManyStepsThisMove; stepIndex++){
    	    							Step extStep = jumpExtensions.getMove(0).getStep(stepIndex);
    	    							Step theStep = new Step(extStep);
    	    							mv.addStep(theStep);
    	    						}}
        		    				if(howManyExtensions==0){
        		    					mv.setEndLocation(step.getEndLocation());
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
    	//do forward active and backward active
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
    		    	if ((emptyLoc & (place*16))!=0){//can move forward right
    		    		if (((i+4)%9)!=0){//valid destination
    		    			Move mv = new Move(i);   		    			
    		    			mv.setSide(Move.Side.BLACK);
    		    			if(((1L<<i) & BAb)!=0){//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+4);
    		    			if(((i+4)%9)==0){System.err.println("Board.getNonJumpMovesi+4");}
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
    		    	if ((emptyLoc & (place*32))!=0){//can move forward left
    		    		if (((i+5)%9)!=0){
    		    			Move mv = new Move(i);		    			
    		    			mv.setSide(Move.Side.BLACK);
    		    			if(((1L<<i) & BAb)!=0){//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+5);
    		    			if(((i+5)%9)==0){System.err.println("Board.getNonJumpMoves i+5 first");}
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
    		    	if ((emptyLoc & (place/16))!=0){//can move backward left
    		    		if (((i-4)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.BLACK);
    		    			{//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-4);
    		    			if(((i-4)%9)==0){System.err.println("Board.getNonJumpMovesi-4");}
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
    		    	if ((emptyLoc & (place/32))!=0){//can move backward right
    		    		if (((i-5)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.BLACK);
    		    			{//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-5);
    		    			if(((i-5)%9)==0){System.err.println("Board.getNonJumpMovesi-5");}
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
    		    	if ((emptyLoc & (place*16))!=0){//can move forward right
    		    		if ((i+4)%9!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			{//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+4);
    		    			if(((i+4)%9)==0){System.err.println("Board.getNonJumpMovesi+4");}
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
    		    	if ((emptyLoc & (place*32))!=0){//can move forward left
    		    		if (  ((i+5)%9) !=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			{//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i+5);
    		    			if(((i+5)%9)==0){System.err.println("Board.getNonJumpMoves i+5 king, place*32 is "+place*32);}
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
    		    	if ((emptyLoc & (place/16))!=0){//can move backward left
    		    		if (((i-4)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			if(((1L<<i) & FAw)!=0){//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-4);
    		    			if(((i-4)%9)==0){System.err.println("Board.getNonJumpMovesi-4");}
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
    		    	if ((emptyLoc & (place/32))!=0){//can move backward right
    		    		if (((i-5)%9)!=0){
    		    			Move mv = new Move(i);
    		    			mv.setSide(Move.Side.WHITE);
    		    			if(((1L<<i) & FAw)!=0){//we are king
    		    				mv.setRankAtStart(Piece.Rank.KING);
    		    			}
    		    			som.addMove(mv);
    		    			Step step = new Step();
    		    			mv.addStep(step);
    		    			step.setStartLocation(i);
    		    			step.setEndLocation(i-5);
    		    			if(((i-5)%9)==0){System.err.println("Board.getNonJumpMovesi-5");}
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
    private double lookUpValue(long localFAw, long localBAw, long localFAb, long localBAb, Move.Side side){
    	//BoardValue result = null; 
    	double result = 0;
    	int[] theFeatureValues=new int[DBHandler.NUMPARAMS];
    	/*int[] theCenters = {0,0,0,0};*/
    	if(haveBoardValue){result = this.boardValue;}
    	else{
    		//result = new BoardValue();
    		//TODO here we go read in the database
    		//theFeatureValues =myPersistence.GetStateEvaluation(localBAw, localFAb, localFAw, localBAb);
        	if (theFeatureValues != null){
        		//System.err.println("Board::ourUtility: found something in database");
        		//now what do we do? we should get back a vector of individual values for features of the board, which then
        		//get evaluated with the weighted sum, where the weights are what we have learned them to be
        		result=myEvaluator.weightedSum(theFeatureValues);
        	}
        	else{ 
        		int howMany =  DBHandler.NUMPARAMS;
        		for (int i=0; i< howMany; i++){
        			theFeatureValues[i]=0;
        		}
        	}
        	//u=(long) Math.floor(Math.random()*1000);
        	
        	setFeatureValues();
        	       	
        	//theFeatureValues[0] = myEvaluator.evalMaterialCredit(whoAmI);
        	       	
        	//u=bd.pieceAdvantage();
        	result=myEvaluator.weightedSum(theFeatureValues);
    		return result; //TODO whatever came back from the database
    	}
    	
    	return result;
    }

    private Move chooseBestMove(SetOfMoves som){//minimax-decision p.166
    	//System.err.println("Board::chooseBest: whoAmI "+whoAmI+" Move.side "+som.getMove(0).getSide());
    	//samuel p. 225, a complete record is kept of the sequence of boards, so no computing needed to retract moves.
    	//we are examining the tree without pruning
    	//we are not considering that boards can be reached by permuted sequences of moves
    	//but we check the database every time.
    	double scoreBefore = boardHistory.getMostRecentScore();
    	int howManyMoves = som.howMany();
    	if(howManyMoves==1){return som.getMove(0);}
    	//Move best = null;
    	int ply = 0;
		//int guess = (int) Math.floor(Math.random()*howManyMoves);
	   	Board copyOfBoard =  new Board(this);
    	copyOfBoard.setWhoAmI(this.whoAmI);//baseline for trying out various possible moves
    	
    	
		long argmax = -99999999999L;
		double alpha = argmax;
		double beta = 99999999999L;
		Move maxTheMin = null;
		long tentargmax = argmax;
		
		SetOfMoves orderedSom = new SetOfMoves();
		//set their color to ours
		orderedSom = orderSetOfMoves(som);
		//SetOfMoves prunedSom = new SetOfMoves();
		//prunedSom = prune(som);
		//howManyMoves = orderedSom.howMany();
		//int howManyKingMovesAfterPruning = 0;
		//int howManyPawnMovesAfterPruning = 0;
		
		for(int moveIndex = 0; moveIndex < howManyMoves; moveIndex++ ){//figure out argmax a, which is the moveindex, in Actions(bd, s) ourMinValue(ourResult (bd,s,a))
			
			//System.err.println("Board:ChooseBest: new move "+moveIndex+" ply "+ply);
			//if (orderedSom.getMove(moveIndex).getRankAtStart()==state.Piece.Rank.KING){
				//howManyKingMovesAfterPruning++;
				tentargmax = Math.max(argmax,  copyOfBoard.ourResult(moveIndex, orderedSom).ourMinValue( 1, alpha, beta, whoAmI));	
				if (argmax < tentargmax){
					maxTheMin = orderedSom.getMove(moveIndex);
					//if(moveIndex!=0)
					//{System.err.println("Board::chooseBestMove: updated choice to "+moveIndex+" of "+ howManyMoves+" because "+tentargmax+" is better");}
					argmax = tentargmax;
			}}//}//having found the best king move, we can decide whether we want to look at pawn moves
		//System.err.println("Board::chooseBestMove: pruned left howManyMoves "+ howManyMoves+" for Kings "+howManyKingMovesAfterPruning);
		/*for(int moveIndex = 0; moveIndex < howManyMoves; moveIndex++ ){//figure out argmax a, which is the moveindex, in Actions(bd, s) ourMinValue(ourResult (bd,s,a))
			ply=0;
			if (orderedSom.getMove(moveIndex).getRankAtStart()==state.Piece.Rank.PAWN){
				howManyPawnMovesAfterPruning++;
				//System.err.println("Board::chooseBestMove: considering pawn move starting at "+prunedSom.getMove(moveIndex).getStartLocation());
				tentargmax = Math.max(argmax,  ourMinValue(ourResult (copyOfBoard, moveIndex, orderedSom), ply+1, alpha, beta));	
				System.err.println("Board::chooseBestMove: tentargmax "+tentargmax+" argmax "+argmax);
				if (argmax < tentargmax){
					System.err.println("Board::chooseBestMove: set pawn choice to "+moveIndex);
					maxTheMin = orderedSom.getMove(moveIndex);
					argmax = tentargmax;
			} 
			
		//generate the board from this board and the move //return that action that corresponds to the maximum among the minimum values
		//evaluate the resulting board
		//keep the best move
			}*/
			//}System.err.println("Board::chooseBestMove: pruned left howManyMoves "+ howManyMoves+" for Pawns "+howManyPawnMovesAfterPruning);

		if(alphaBeta){
			//System.err.println("Board::chooseBestMove: being alpha");
			//At each play by Alpha, the initial board score, as saved from previous move, is compared with the backed up score for the current position
			//The difference is called delta.
			long backedUpScore = argmax;
			//go to last in history to get scoreBefore
			
			long delta = (long) (backedUpScore - scoreBefore);
		
			if(Math.abs(delta)>arbitraryMinimum){
				//here change weights of eval polynomial, see second column 219
				//also see p.220 top and 219 bottom, correlation coefficients changed more
				//also see p.221 bottom left and upper right, positive delta make corrections to selected terms in the poly only
				//as we are returning the move, we have the feature values and weights of that move; we need to store them in the history
				//the feature values were calculated when the move was considered
				//there is an update board with move, and that resulting board should have its features evaluated with current weights and all saved
				//update saved the previous move into history
				//we have a delta, we have its sign, we have weights, we can see 
				myCorrelation.correlateSignsFeaturesDelta(delta, this);
				mySamuelStrategy.calculateProposedWeights();
				
			}
			recomputeArbitraryMinimum();
			//WE ARE AT anticipation play, p.220 right column
			//rate the individual board features here, with the chosen move having been made, save for future reference
		}//end of alphaBeta
		/*switch(maxTheMin.getRankAtEnd()){
		case PAWN: break;
		case KING: System.err.println("Board::choosing king move "); break;
		}*/
		if(maxTheMin.getHowManySteps() >1){previousMoveJump = true;} else {previousMoveJump = false;}
		//System.err.println("Choosing move with argmax "+argmax);
    	return maxTheMin;
    }
    private  long ourMaxValue(int ply, double alpha, double beta, Move.Side whosMax){//see p. 166 maybe we'll want something more complicated than long? maybe short is enough
    	
    	long v = 0;
    	if (ourTerminalTest(ply)) {return ourUtility(whosMax);}
    	v = -999999L;
    	//here's where we figure out the set of moves
    	SetOfMoves som = getMoves();//opponent's moves
    	//System.err.println("Board::ourMax: whoAmI "+bd.whoAmI+" Move.side "+som.getMove(0).getSide());
    	int howMany = som.howMany();
    	Board resultBoard = null;
    	if(howMany==0){/*System.err.println("no moves");*/ v= -99999;}
    	if(howMany==1){resultBoard = ourResult(0,som);
    	                v=resultBoard.ourUtility(whosMax);}
    	
    	for (int a = 0; a< howMany; a++){
    			//System.err.println("Board::ourMaxValue: with ply "+ply+" and side "+bd.getWhoAmI()+" moves "+howMany+" move "+a);
    			resultBoard = ourResult(a, som);
    			v = Math.max(v, resultBoard.ourMinValue(ply+1, alpha, beta, whosMax ));//result of move includes changing sides
    			//if(!resultBoard.doCheck())
    			{resultBoard.doInsert();}
    			if( v >= beta){ return v-epsilon;}
    			alpha = Math.max(alpha, v-epsilon);
    	}
    	return (v-epsilon);
    }
    private long ourMinValue(  int ply, double alpha, double beta, Move.Side whosMax){//see p. 166 
    	
    	long v = 0;
    	if (ourTerminalTest(ply)) {return ourUtility(whosMax);}
    	v = 999999L;
    	//here's where we figure out the set of moves
    	SetOfMoves som = getMoves();  //the opponent's moves	
    	int howMany = som.howMany();
    	//System.err.println("Board::ourMin: whoAmI "+bd.whoAmI+" Move.side "+som.getMove(0).getSide());
    	Board resultBoard = null;
    	if(howMany==0){/*System.err.println("no moves");*/ v= -99999;}
    	if(howMany==1){
    		resultBoard = ourResult(0,som);
    		v=resultBoard.ourUtility(whosMax);
        }
    	for (int a = 0; a< howMany; a++){
    		//System.err.println("Board::ourMinValue: with ply "+ply+" and side "+bd.getWhoAmI()+" moves "+howMany+" move "+a);
    		resultBoard = ourResult(a, som);
    		v = Math.min(v, resultBoard.ourMaxValue( ply+1, alpha, beta, whosMax));//result of move includes changing sides
    		 
    		{resultBoard.doInsert();}
    		if( v <= alpha ){return v-epsilon;}
    	}
    	//System.err.println("Board::ourMinValue with v "+v);
    	return (v-epsilon);
    }
    private long ourUtility(Move.Side whosMax){//see p. 166 
    	long u = 0L;
    	long u4Max = 0L;

    	if (doCheck()){
    		
    		//now what do we do? we should get back a vector of individual values for features of the board, which then
    		//get evaluated with the weighted sum, where the weights are what we have learned them to be
    		u=myEvaluator.weightedSum(theFeatureValues);  //using local feature values
    		System.err.println("Board::ourUtility: found something worth "+u+" in database");
    	}
    	else{
    		setFeatureValues(); //instruct the board to determine its feature values
        	doInsert();
        	}
    	//end of else, where we only calculate the value for this board if we don't have it already
    	//u=bd.pieceAdvantage();
    	u=weightedSum();
    	//TODO do we want to update??  otherwise don't need this myPersistence.Insert(localBAw, localFAb, localFAw, localBAb, theFeatureValues);
    	
    	switch(whosMax){
    	case WHITE:
    		switch(whoAmI){
    		case WHITE:
    			u4Max = u;
    			break;
    		case BLACK:
    			u4Max = -u;
    		}
    		break;
    	case BLACK:
    		switch(whoAmI){
    		case WHITE:
    			u4Max = -u;
    			break;
    		case BLACK:
    			u4Max = u;
    			
    		}
    	}
    	//if(u4Max!=0){System.err.println("Board::ourUtility "+u4Max);}
    	return u4Max;
    }
    private Board ourResult(int a, SetOfMoves som){//see p. 166, the result is a different board, a different side, a different set of moves
    	//do I want to create a data structure?, yes, a board, whose "whose turn" is set
        //applies one move, the one identified by a
    	Board resBd = new Board(this);
    	//just apply the move
    	//place piece at end
    	//remove piece at start
    	//remove the jumped pieces
    	Move mv = som.getMove(a);
    	//System.err.println("Board::ourResult: with a move starting at "+mv.getStartLocation());
    	resBd.setWhoAmI(mv.getSide()); 
    	resBd.updateBoard(mv);
    	boardHistory.addBoardPolynomial(resBd, theFeatureValues, myEvaluator.getWeightValues() );
    	resBd.changeSides();
    	return resBd;
    }
    private boolean ourTerminalTest(int ply){
    	
    	//what's the terminal test?="are we done?"
    	//Samuel's terminal test is p.214 always look ahead 3 (unit is one move of one player) plies
    	//evaluate here unless
    	//1 next move is a jump
    	//2 last move was a jump
    	//3 exchange offer possible
    	//if ply ==4, evaluate unless 1. or 3.
    	//if ply >20 because he ran out of memory here, we could run out of time -- if a pending jump, adjust score
    	if(ply >fifthBreakPly){/*System.err.println("Board::ourTT: 5th"); */return true;}
    	boolean noJumps=!anyJumps();
    	//if ply >= 11 evaluate unless (jump ^ NOT(piece advantage >2 kings
    	if(ply >= fourthBreakPly){if (noJumps || (pieceAdvantageKings()>2)){/*System.err.println("Board::ourTT: 4th");*/ return true;}}
    	//if ply >= 5 evaluate unless 1. next more is a jump
    	if(ply >= thirdBreakPly){if (noJumps){/*System.err.println("Board::ourTT 3rd:");*/ return true;}} 
    	boolean noExchangePossible = !isExchangePossible();
    	if(ply>= secondBreakPly){if (noJumps && noExchangePossible){/*System.err.println("Board::ourTT: 2n");*/return true;}}
    	boolean notPreviousMoveWasJump = !previousMoveJump;
    	if(ply>= firstBreakPly){if (noJumps && notPreviousMoveWasJump && noExchangePossible){/*System.err.println("Board::ourTT: 1st");*/return true;}}
    	return false;
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
    public void setBoardValue(double val){
    	this.boardValue=val;
    }

    private void updateBoard(Move mv){
    	//the variables that are getting fixed are FAb, FAw, BAb, BAw, FPb, FPw, BPb, BPw
    	int startPt = mv.getStartLocation();
    	/*whoseTurn = mv.getSide();*/
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
    			Piece.Rank r = mv.getRankAtStart();
    			switch(mv.getSide()){
    				case BLACK:
    					if(mv.getEndLocation()>31){r = Piece.Rank.KING; mv.setRankAtStart(Piece.Rank.KING);}
    					break;
    				case WHITE:
    					if(mv.getEndLocation()<5){r = Piece.Rank.KING; mv.setRankAtStart(Piece.Rank.KING);}
    			}
    			placePiece(mv.getEndLocation(), mv.getSide(),r); 
    		}	
    	}
    
    	//setFeatureValues
    	setFeatureValues();
    	
    	
    		//add to backward active or forward active, depending on side
    	int stopped =  mv.getEndLocation();
    	if (stopped < 5 || stopped>31){
    		switch(mv.getSide()){
    		case BLACK:
    			
    			if (stopped>31){
    			BAb = BAb | (1L<<stopped);
    			}
    			break;
    		case WHITE:
    			if(stopped<5){
    			FAw = FAw | (1L<<stopped);
    		}}}
    }
    private void removePiece(long bitLoc){//active or passive, they're gone. Cheaper to do all than check B/W
    	if((bitLoc%9)==0){System.err.println("Board::removePiece: asked to remove from factor of 9");}//factors of 9 always unavailable
    	else{
    	long powerbit = 1L<<bitLoc;
    	long notBitLoc = myEvaluator.parNOT(powerbit);
    	FAb = FAb & notBitLoc;
    	FAw = FAw & notBitLoc;
    	BAb = BAb & notBitLoc;
    	BAw = BAw & notBitLoc;
    	
    	//showBitz(emptyLoc);
    	setEmpty(); //updating empty is this where emptiness of a move start gets lost?
    	//showBitz(emptyLoc);
    	}
    }
    
    private void placePiece(long bitLoc, Move.Side whosePiece, Piece.Rank r){
    	//need to or piece into correct words
    	//it could get stuck and become passive, or it could still be active, F or B 
    	//isn't both active and passive
    	//if((bitLoc%9)==0){System.err.println("Board::placePiece: asked to place to factor of 9");}//factors of 9 always unavailable
    	//else{
    	long powerBit = 1L<<bitLoc;
    	switch(whosePiece){
    	case BLACK:   	
    		FAb = FAb | powerBit;
    		switch(r){case KING: BAb = BAb | powerBit;}
    	break;
    	case WHITE:   
    		BAw = BAw | powerBit;
    		switch(r){case KING:  FAw = FAw | powerBit;}
    	break;
    	}
    	setEmpty();
    	//showBitz(emptyLoc);
    	//}
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
    		result = ((impulse & (FAw | BAw )))!=0;
    		break;
    	case WHITE:
    		result = ((impulse & (FAb | BAb )))!=0;
    		break;
    		default:
    			break;
    	}
       // System.err.println("Board::opponentAt "+i+" is "+result);
    	return result;
    }
    public int pieceAdvantageKings(){
    	int nKings = 0;
    	switch(whoAmI){
    	case BLACK:
    		return(myEvaluator.countTheOnes(BAb, 1, 35)-myEvaluator.countTheOnes(FAw, 1, 35));
    	case WHITE:
    		this.whoAmI = Move.Side.BLACK;
    		return(myEvaluator.countTheOnes(FAw, 1, 35)-myEvaluator.countTheOnes(BAb, 1, 35));
    	}
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
    		opponents = FAw  | BAw ;
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
    		opponents = FAb |  BAb ;
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
    		opponents = FAw  | BAw ;   		
            break;    		
    	  case WHITE:
    		opponents = FAb  | BAb ;
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
    		opponents = FAw  | BAw ;   		
            break;    		
    	  case WHITE:
    		opponents = FAb  | BAb ;
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
    		opponents = FAw  | BAw ;   		
            break;    		
    	  case WHITE:
    		opponents = FAb  | BAb ;
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
    		opponents = FAw  | BAw ;   		
            break;    		
    	  case WHITE:
    		opponents = FAb  | BAb ;
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
  
    public boolean getAlphaBeta(){
    	return this.alphaBeta;
    }
    public void recomputeArbitraryMinimum(){
           this.arbitraryMinimum = (long) myEvaluator.averageOfTheCoefficients(); 
    }
    public void setFirstBlackMove(StringBuffer first){
    	this.firstBlackMove.replace(0, firstBlackMove.length(), first.toString());
    	System.err.println("Board::setFirstBlackMove: with "+firstBlackMove);
    }
    public SetOfMoves orderSetOfMoves(SetOfMoves som){
    	SetOfMoves orderedSet = new SetOfMoves();
    	int howManyMoves = som.howMany();
    	int howManyCaptures = 0;
    
    	for(int moveIndex = 0; moveIndex < howManyMoves; moveIndex++ ){//figure out argmax a, which is the moveindex, in Actions(bd, s) ourMinValue(ourResult (bd,s,a))
    		howManyCaptures= som.getMove(moveIndex).getHowManySteps()-1;
			if (howManyCaptures>=4){
				orderedSet.addMove(som.getMove(moveIndex));
			}
		}
    	for(int ncaptures = 3; ncaptures > -1; ncaptures--){
    		for(int moveIndex = 0; moveIndex < howManyMoves; moveIndex++ ){//figure out argmax a, which is the moveindex, in Actions(bd, s) ourMinValue(ourResult (bd,s,a))
    			howManyCaptures= som.getMove(moveIndex).getHowManySteps()-1;
    			if (howManyCaptures==ncaptures){
    				orderedSet.addMove(som.getMove(moveIndex));
    			}
    			
    			//if (orderedSet.howMany()>3){return orderedSet;}
    		}   		
    	}
    	return orderedSet;   	
    }
    public void setFeatureValues(){
    	theFeatureValues[DBHandler.ADV]=myEvaluator.evalAdvancement();
    	theFeatureValues[DBHandler.APEX]=myEvaluator.evalApex();
    	theFeatureValues[DBHandler.BACK]=myEvaluator.evalBackRowBridge();
    	theFeatureValues[DBHandler.CENT]=myEvaluator.evalCenterControl1();
    	theFeatureValues[DBHandler.CNTR]=myEvaluator.evalCenterControl2();
    	theFeatureValues[DBHandler.CORN]=myEvaluator.evalDoubleCornerCredit();
    	theFeatureValues[DBHandler.CRAMP]=myEvaluator.evalCramp();
    	theFeatureValues[DBHandler.DENY]=myEvaluator.evalDenialOccupancy();
    	theFeatureValues[DBHandler.DIA]=myEvaluator.evalDoubleDiagonalFile();
    	theFeatureValues[DBHandler.DIAV]=myEvaluator.evalDIAV();
    	theFeatureValues[DBHandler.DYKE]=myEvaluator.evalDyke();
    	theFeatureValues[DBHandler.EXCH]=myEvaluator.evalExch();
    	theFeatureValues[DBHandler.EXPOS]=myEvaluator.evalExpos();
    	theFeatureValues[DBHandler.FORK]=myEvaluator.evalFork();
    	theFeatureValues[DBHandler.GAP]=myEvaluator.evalGap();
    	theFeatureValues[DBHandler.GUARD]=myEvaluator.evalGuard();
    	theFeatureValues[DBHandler.HOLE]=myEvaluator.evalHole();
    	theFeatureValues[DBHandler.KCENT]=myEvaluator.evalKcent();
    	theFeatureValues[DBHandler.MOB]=myEvaluator.evalMob();
    	theFeatureValues[DBHandler.MOBIL]=myEvaluator.evalMobil();
    	theFeatureValues[DBHandler.MOVE]=myEvaluator.evalMove();
    	theFeatureValues[DBHandler.NODE]=myEvaluator.evalNode();
    	theFeatureValues[DBHandler.OREO]=myEvaluator.evalOreo();
    	theFeatureValues[DBHandler.POLE]=myEvaluator.evalPole();
    	theFeatureValues[DBHandler.THRET]=myEvaluator.evalThret();
    	theFeatureValues[DBHandler.DEMO]=myEvaluator.evalDEMO();
    	theFeatureValues[DBHandler.DEMMO]=myEvaluator.evalDEMMO();
    	theFeatureValues[DBHandler.DDEMO]=myEvaluator.evalDDEMO();
    	theFeatureValues[DBHandler.DDMM]=myEvaluator.evalDDMM();
    	theFeatureValues[DBHandler.MODE1]=myEvaluator.evalMODE1();
    	theFeatureValues[DBHandler.MODE2]=myEvaluator.evalMODE2();
    	theFeatureValues[DBHandler.MODE3]=myEvaluator.evalMODE3();
    	theFeatureValues[DBHandler.MODE4]=myEvaluator.evalMODE4();
    	theFeatureValues[DBHandler.MOC1]=myEvaluator.evalMOC1();
    	theFeatureValues[DBHandler.MOC2]=myEvaluator.evalMOC2();
    	theFeatureValues[DBHandler.MOC3]=myEvaluator.evalMOC3();
    	theFeatureValues[DBHandler.MOC4]=myEvaluator.evalMOC4();
    	theFeatureValues[DBHandler.PADV]=myEvaluator.evalPieceAdvantage();
    }
    public int weightedSum(){
    	return(myEvaluator.weightedSum(theFeatureValues));
    }
    public void doInsert(){
    	long localBAw=0L;
    	switch(whoAmI){
		case WHITE:
			localBAw = BAw | 1L;
			break;
		case BLACK:
			
			localBAw = BAw & onesLSB0;
	}
    	 myPersistence.Insert( getFAw(), getFAb(),localBAw, getBAb(), theFeatureValues);
    }
    public boolean doCheck(){
    	boolean itsThere = false;
    	long localBAw=0L;
    	switch(getWhoAmI()){
		case WHITE:
			localBAw = BAw | 1L;
			break;
		case BLACK:
			
			localBAw = BAw & onesLSB0;
    	}
   	int[] theFeatureValuesBackup = new int[DBHandler.NUMPARAMS];
    	
    	//Here we are checking whether we have recorded this board:
        //return false;
    	theFeatureValuesBackup =myPersistence.GetStateEvaluation( FAw, FAb, localBAw,  BAb);//they can become null!

    	if (theFeatureValuesBackup != null){
    		int howMany = DBHandler.NUMPARAMS;
    		for(int i=0; i< howMany; i++){
    			theFeatureValues[i]=theFeatureValuesBackup[i];//referring to local feature values
    		}   	
        }
    	return itsThere;
    }
    public boolean isExchangePossible(){//;for the active side
    	boolean yorn = false;
    	long forwardActive = 0L;
    	long backwardActive = 0L;
    	long forwardPassive = 0L;
    	long backwardPassive = 0L;
    	switch(whoAmI){
    	case BLACK:
    		forwardActive = FAb;
    		forwardPassive = FAw;
    		backwardActive = BAb;
    		backwardPassive = BAw;
    		
    		break;
    	case WHITE:
    		forwardActive = FAw;
    		forwardPassive = FAb;
    		backwardActive = BAw;
    		backwardPassive = BAb;
    		
    	}
    	
    	long exchangePossibilities= (
    			                     (forwardActive & (emptyLoc>>4)) 
    			                     & (    (backwardPassive>>8)
    				                     |  ((forwardPassive<<1) & (emptyLoc>>9))
    				                     |  ((backwardPassive>>9) & (emptyLoc<<1))
    				                    )
    			                  |	(forwardActive & (emptyLoc>>5))
    			                     &  (    (backwardPassive>>10)
    			                    	  |  ((forwardPassive>>1) & (emptyLoc>>9))
    			                          |  ((backwardPassive>>9) & (emptyLoc>>1))
    			                         )
    			                   | (backwardActive & (emptyLoc<<4))
    			                      &   (   (forwardPassive<<8)
    			                		   |  ((forwardPassive<<9) & (emptyLoc>>1))
    			                		   |  ((backwardPassive>>1) & (emptyLoc<<9))
    			                		  )
    			                   | ((backwardActive & (emptyLoc<<5))
    			                	   &   (   (forwardPassive<<10))
    			                	        | ((forwardPassive<<9)&(emptyLoc<<1))
    			                	        |((backwardPassive<<1)&(emptyLoc<<9))
    			                	      )
    			                	);
    	if(exchangePossibilities ==0L){yorn=false;}
    	else{yorn=true;}
    			
    	return yorn;
    }
}
    
    
