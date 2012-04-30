package valuation;
/* This code is adapted from some by Nicholas Robitaille, 
 * TMS: reading Samuel for features
 * p.212 dominant criterion is number of pieces of each color 
 * p. 212 piece ratio (2nd column)
 * p. 212 positional advantage
 * p. 212 dominant criterion is inability for one or other to move, not in scoring polynomial
 * p. 212 piece advantage -- includes trade when ahead, not when behind, and ratio pawn king value "this first term of polynomial"p. 214
 * p. 217 (1) piece advantage
 *        (2) denial of occupancy
 *        (3) mobility
 *        (4) hybrid of control of the center and piece advancement
 * p. 228,229 list the features
 * 
 */

import persistence.DBHandler;
import actions.Move;
import state.Board;

public class Evaluator {
	/*Let's say, when Evaluator (this, e.g.) instance is created, an object board is passed, so when a board
	 * wants to evaluate itself, the board uses its evaluator and the evaluator can obtain the relevant info from its parent board
	 */
	private Board myBoard = null;
	/*private int materialCredit, myMaterialCredit, enemyMaterialCredit;*/
	/* This is just a list of all of the parameters mentioned at the end of the Samuel paper.*/
/*	private int advancement, apex, backRowBridge, centralControl1, centralControl2, doubleCornerCredit, 
				cramp, denialOfOccupancy, doubleDiagonalFile, diagonalMomentValue, dyke, exchange, 
				exposure, threatOfFork, gap, backRowControl, hole, centralKingControl1, centralKingControl2, 
				totalEnemyMobility, undeniedEnemyMobility, move, node, triangleOfOreos, pole, threat;*/
	/* ADV = 0;
	 APEX = 1;
	 BACK = 2;
	 CENT = 3;
	 CNTR = 4;
	 CORN = 5;
	 CRAMP = 6;
	 DENY = 7;
	 DIA = 8;
	 DIAV = 9;
     DYKE = 10;
	 EXCH = 11;
	 EXPOS = 12;
	 FORK = 13;
	 GAP = 14;
	 GUARD = 15;
	 HOLE = 16;
	 KCENT = 17;
	 MOB = 18;
	 MOBIL = 19;
	 MOVE = 20;
	 NODE = 21;
	 OREO = 22;
	 POLE = 23;
	 RECAP = 24;
	 THRET = 25;
	 DEMO = 26;
	 DEMMO = 27;
	 DDEMO = 28;
	 DDMM = 29;
	 MODE1 = 30;
	 MODE2 = 31;
	 MODE3 = 32;
	 MODE4 = 33;
	 MOC1 = 34;
	 MOC2 = 35;
	 MOC3 = 36;
	 MOC4 = 37;
	 KCNTC = 38;
	 PIECEADVANTAGE = 39;*/
	private double weights[]={
			0,//-1,//adv 0
			0,//1,//apex 1
			0,//-1,//back 2
			0,//1,//cent 3
			0,//+1<<5,//cntr 4
			0,//1,//corn 5
			0,//1,//cramp 6
			0,//1,//deny 7
			0,//1,//dia 8
			0,//1,//diav 9 
			0,//1,//dyke 10
			0,//-(1<<3),//exch 11
			0,//1,//expos 12
			0,//1,//fork 13
			0,//1,//gap 14 
			0,//1,//guard  15
			0,//1,//hole 16
			0,//+1<<16,//kcent  17 
			0,//1,//mob  18 
			0,//1,//mobil 19 
			0,//+1<<8,//move 20
			0,//-(1<<2),//node  21 
			0,//1<<2,//oreo  22
			0,//1,//pole  23
			0,//1<<5,// 24
			0,//+1,//thret 25
			0,//1,//demo26
			0,//-1,//demmo 27
			0,//1,//ddemo 28
			0,//1,//ddmm 29
			0,//1,//mode1 30
			0,//-1,//mode2 31
			0,//-1,//mode3 32
			0,//1,//mode4 33
			0,//1,//moc1 34
			0,//-1,//moc2 35
			0,//+1,//moc3 36
			0,//-1,//moc4 37
			0,//1,//kcntc 38
			1<<10//pieceadvantage 39
			};
	/*private int howManyWeights = 27;*/
	
	long allOnes = (long) Math.pow(2, 36)-1;
	int numMyPawns=0; //initialize
	int numMyKings=0; //initialize
	long movers = 0L;
	long mover= 0L;
	int numEnemyPawns=0; //initialize
	int numEnemyKings=0; //initialize
	long myPawns = 0L;
	long enemyPawns = 0L;
	long myKings = 0L;
	long enemyKings = 0L;
	long doubleCorners = 2L+(1L<<5)+(1L<<31)+(1L<<35);
	long crampingToBlack = 1L<<14;//could be 22
	long crampingToWhite = 1L<<22;//could be 14
	long nearbyCrampingToBlack = (1L<<10)+(1L<<15); //, from 9,14 could be 19,24
	long nearbyCrampingToWhite = (1L<<21)+(1L<<26); //from 19,24, typoed as 19,20
	long certainSquaresBlack=(1L<<19)+(1L<<23)+(1L<<24)+(1L<<28);//from 17,21,22,25
	long certainSquaresWhite=(1L<<8)+(1L<<12)+(1L<<13)+(1L<<17);//from 8,11,12,16
	long doubleFileSquares=(1L<< 1)+(1L<< 6)+(1L<< 11)+(1L<< 16)+
			(1L<< 21)+(1L<< 26)+(1L<< 31)+(1L<< 5)+(1L<< 10)+
			(1L<< 15)+(1L<< 20)+(1L<< 25)+(1L<< 30)+(1L<< 35);
	long doubleFile1RemovedSquares=(1L<< 2)+(1L<< 14)+(1L<< 7)+(1L<< 19)+
			(1L<< 12)+(1L<< 24)+(1L<< 17)+(1L<< 29)+(1L<< 22)+
			(1L<< 34);
	long doubleFile2RemovedSquares=(1L<< 3)+(1L<< 23)+(1L<< 8)+(1L<< 28)+
			(1L<< 13)+(1L<< 33);
	long [] theThreesomes ={
			(1L<< 2)+(1L<< 6)+(1L<< 10),
			(1L<< 3)+(1L<< 7)+(1L<< 11),
			(1L<< 4)+(1L<< 8)+(1L<< 12),
			(1L<< 6)+(1L<< 10)+(1L<< 14),
			(1L<< 7)+(1L<< 11)+(1L<< 15),
			(1L<< 8)+(1L<< 12)+(1L<< 16),
			(1L<< 11)+(1L<< 15)+(1L<< 19),
			(1L<< 12)+(1L<< 16)+(1L<< 20),
			(1L<< 13)+(1L<< 17)+(1L<< 21),
			(1L<< 15)+(1L<< 19)+(1L<< 23),
			(1L<< 16)+(1L<< 20)+(1L<< 24),
			(1L<< 17)+(1L<< 21)+(1L<< 25),
			(1L<< 20)+(1L<< 24)+(1L<< 28),
			(1L<< 21)+(1L<< 25)+(1L<< 29),
			(1L<< 22)+(1L<< 26)+(1L<< 30),
			(1L<< 24)+(1L<< 28)+(1L<< 32),
			(1L<< 25)+(1L<< 29)+(1L<< 33),
			(1L<< 26)+(1L<< 30)+(1L<< 34),
			(1L<< 1)+(1L<< 6)+(1L<< 11),
			(1L<< 2)+(1L<< 7)+(1L<< 12),
			(1L<< 3)+(1L<< 8)+(1L<< 13),
			(1L<< 5)+(1L<< 10)+(1L<< 15),
			(1L<< 6)+(1L<< 11)+(1L<< 16),
			(1L<< 7)+(1L<< 12)+(1L<< 17),
			(1L<< 10)+(1L<< 15)+(1L<< 20),
			(1L<< 11)+(1L<< 16)+(1L<< 21),
			(1L<< 12)+(1L<< 17)+(1L<< 22),
			(1L<< 14)+(1L<< 19)+(1L<< 24),
			(1L<< 15)+(1L<< 20)+(1L<< 25),
			(1L<< 16)+(1L<< 21)+(1L<< 26),
			(1L<< 19)+(1L<< 24)+(1L<< 29),
			(1L<< 20)+(1L<< 25)+(1L<< 30),
			(1L<< 21)+(1L<< 26)+(1L<< 31),
			(1L<< 23)+(1L<< 28)+(1L<< 33),
			(1L<< 24)+(1L<< 29)+(1L<< 34),
			(1L<< 25)+(1L<< 30)+(1L<< 35)			
			};
	int[] theThreesomeStarts ={1,2,3,5,6,7,10,11,12,14,15,16,19,20,21,23,24,25,
			2,3,4,6,7,8,11,12,13,15,16,17,20,21,22,24,25,26};
	int[] theThreesomeEnds ={11,12,13,15,16,17,20,21,22,24,25,26,29,30,31,33,34,35,
			10,11,12,14,15,16,19,20,21,23,24,25,28,29,30,32,33,34};
	long blacks5thRow = (1L<<19)+(1L<<20)+(1L<<21)+(1L<<22);
	long blacks6thRow = (1L<<23)+(1L<<24)+(1L<<25)+(1L<<26);
	long whites5thRow = (1L<<14)+(1L<<15)+(1L<<16)+(1L<<17);
	long whites6thRow = (1L<<10)+(1L<<11)+(1L<<12)+(1L<<13);
	long impulse1 =  1L<<1;
	long impulse3 =  1L<<3;
	long impulse33=  1L<<33;
	long impulse35=  1L<<35;
	//for apex
	long impulse7 =1L<<7;
	long impulse29 = 1L<<29;
	long centerLocs = (1L<<11)+ (1L<<12)+ (1L<<15)+ (1L<<16)+ (1L<<20)+ (1L<<21)+ (1L<<24)+ (1L<<25);
	long onEdge = (1L<<1) + (1L<<2)+ (1L<<3) + (1L<<4) + (1L<<5) + (1L<<13)  + (1L<<14)  + (1L<<22) + (1L<<23) + (1L<<31) + (1L<<32) + (1L<<33) + (1L<<34) + (1L<<35);
	long blackTriangleOreo = (1L<<2)+(1L<<3)+(1L<<7);
	long whiteTriangleOreo = (1L<<29)+(1L<<33)+(1L<<34);
	long moveSystem = (1L<<1)+(1L<<2)+(1L<<3)+(1L<<4)+(1L<<10)+(1L<<11)+(1L<<12)+(1L<<13)+(1L<<19)+(1L<<20)+(1L<<21)+(1L<<22)+(1L<<28)+(1L<<29)+(1L<<30)+(1L<<31);
	
	/* I gave each of the parameters a positive or negative coefficient based on my understanding of them.
	 * The values of the coefficients will definitely need to be adjusted.
	 * A gameState with a high value is desirable. The higher the gamestate, the larger our advantage.
	 */
	/*private int boardValue =(int) weights[0]*materialCredit
							+ (int)weights[1]*advancement 
							+ (int)weights[2]*apex 
							+ (int)weights[3]*backRowBridge 
							+ 2*(int)(weights[4]*centralControl1 - (int)weights[5]*centralControl2) 
							+ (int)weights[6]*doubleCornerCredit 
							+ (int)weights[7]*cramp 
							+ (int)weights[8]* denialOfOccupancy 
							+ (int)weights[9]*doubleDiagonalFile 
							+ (int)weights[10]*diagonalMomentValue 
							+ (int)weights[11]*dyke 
							- (int)weights[12]*exchange 
							+ (int)weights[13]*exposure 
							- (int)weights[14]*threatOfFork 
							- (int)weights[15]*gap 
							+ (int)weights[16]*backRowControl
							+ (int)weights[17]*hole
							+ 3*((int)weights[0]*centralKingControl1 - (int)weights[0]*centralKingControl2) 
							- (int)weights[18]*totalEnemyMobility
							- (int)weights[19]*undeniedEnemyMobility
							+ (int)weights[20]*move
							+ (int)weights[21]*node
							+ (int)weights[22]*triangleOfOreos
							+ (int)weights[23]*pole
							- (int)weights[24]*threat
							;*/
	
	
	/* For details on any specific parameter, refer to the last 2 pages of the Samuel paper. They don't explicitly describe the motivation
	 * or strategy behind each parameter, but they do indicate how each parameter is scored, giving an idea on how to code them.
	 */
	
	public Evaluator (Board bd, boolean alphaBeta){
		this.myBoard = bd;
		for(int i = 0; i<63; i++){
			allOnes = allOnes | 1L<<i;
		}
	}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public int evalAdvancement(){
		/*Advancement*/
		/* credit with 1 for each passive man in the 5th and 6th rows (counting in passives direction)
		 * and debited with 1 for each passive man in the 3rd and 4th row.
		 */
		int advancement = 0; //initialize
		long passives = 0L;
	
	switch(myBoard.getWhoAmI()){
	case BLACK:
           //if I am black, the passives are white
		passives = myBoard.getBAw();
		return (countTheOnes(passives & (blacks5thRow | blacks6thRow), 19,26));
		
	case WHITE:
		passives = myBoard.getFAb();
		return(countTheOnes(passives & (whites5thRow | whites6thRow), 10, 17));
	}
	
	return advancement;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/* debited with 1 if there are no kings on the board
	 * if either square 7 or 26 is occupied by an active piece (7 is really 7 and 26 is really 29)
	 * and if neither of the squares is occupied by a  passive piece
	 * these are impulse7 and impulse29, other board coordinates
	 */
	public int evalApex(){
		/*Apex*/
		int apex = 0;// 

		
		long blackKingLocs = myBoard.getBAb();
		long whiteKingLocs = myBoard.getFAw();	
		long kingLocs = blackKingLocs | whiteKingLocs; 
		boolean kingsOnBoard = (kingLocs != 0);		
		long actives = 0L;
		long passives = 0L;
		
		if (!kingsOnBoard){apex--;}
		
		switch(myBoard.getWhoAmI()){
		case BLACK:
			actives = myBoard.getFAb();
			passives = myBoard.getBAw();
			break;
		case WHITE:
			actives = myBoard.getBAw();
			passives = myBoard.getFAb();
		}//end switch

		if((((actives & impulse7) !=0L) || ((actives & impulse29)!=0L))
				&& ((passives & impulse7)==0L)
				&& ((passives & impulse29)==0L) ){apex--;}
		return apex;	
	}//end evalApex
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*Back Row Bridge*/
	/* This is a defensive strategy. By leaving a piece on each of the two back row tiles specified below,
	 * the player is able to defend against incoming Pawns attempting to be Kinged 
	 */
	/* credit with 1 if there are no active kings
	 * and if the two bridge squares 1 and 3 or 30 and 32 are occupied by passive pieces (really 33 and 35)
	 *  why should this have a positive impact?
	 */
	public int evalBackRowBridge(){
		int backRowBridge = 0;
		long activeKings = 0L;
		long passives = 0L;

		switch(myBoard.getWhoAmI()){
		case BLACK:
			
			activeKings =  myBoard.getBAb();
			if(activeKings!=0L){return 0;}
			
			passives = myBoard.getBAw();
			if(
					((passives&impulse33)!=0L)
			      &&((passives&impulse35)!=0L)){backRowBridge++;}
			break;
		case WHITE:
			activeKings =  myBoard.getFAw();
			if(activeKings!=0L){return 0;}
			
			passives = myBoard.getFAb();
			if(
					((passives&impulse1)!=0L)
			      &&((passives&impulse3)!=0L)){backRowBridge++;}
			 
			
		}//end switch
		
		return backRowBridge;
	}//end evalBackRowBridge
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public int evalCenterControl1(){
		/* credited with 1 for each of the following squares occupied by passive piece
		 * 11,12,15,16,20,21,24,25 and here Samuel has used real coordinates for center
		 */
		long passives =0L;
		
		switch(myBoard.getWhoAmI()){
		case BLACK:
            passives = myBoard.getBAw();
			break;
		case WHITE:
			passives = myBoard.getFAb();			
		}//end switch
		
		return countTheOnes(passives & centerLocs,11,25);
		
	}//end CenterControl
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*center control 2
	 * 
	 */
	public int evalCenterControl2(){
		/* credited with 1 for each of the following squares occupied by active piece or to which an active piece can move
		 * 11,12,15,16,20,21,24,25 and here Samuel has used real coordinates for center
		 */
		
		long actives =0L;
		long passives=0L;
		long kings =0L;
		long emptyLocs =myBoard.getEmpty();
		long theSelect = 0L;
		
		switch(myBoard.getWhoAmI()){
		case BLACK:
            actives = myBoard.getFAb();
            kings = myBoard.getBAb();
            passives = myBoard.getBAw();
			break;
		case WHITE:
			actives = myBoard.getBAw();
			kings = myBoard.getFAw();
			passives = myBoard.getFAb();			
		}//end switch
        theSelect = (actives & centerLocs);
        //active piece can move pawnwise to center | king can move retrograde to center
        //active pawn can move
        theSelect = theSelect | (actives & centerLocs<<4 & emptyLocs<<4) | (actives & centerLocs<<5 & emptyLocs<<5);
        theSelect = theSelect | (kings   & centerLocs>>4 & emptyLocs>>4) | (kings & centerLocs>>5&emptyLocs>>5);
        //does not include checking whether piece can get there by jumping, so, single jump
        theSelect = theSelect | (actives & centerLocs<<8 & emptyLocs<<8 &passives<<4) | (actives & centerLocs<<10 & emptyLocs<<10 & passives<<5);
        theSelect = theSelect | (kings   & centerLocs>>4 & emptyLocs>>4) | (kings & centerLocs>>5&emptyLocs>>5);
        
		
		return countTheOnes(theSelect,1,35);
		
	}//end CenterControl2
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*Double-Corner Credit
	 * credited with 1 if the material credit value for the active side is 6 or less
	 * if the passive side is ahead in material credit
	 * and if the active side can move into one of the double corner squares
	 * */
	public int evalDoubleCornerCredit(){
		int doubleCornerCredit = 0;
		Move.Side me = myBoard.getWhoAmI();
		int myMaterialCredit = evalMaterialCredit(me);
		Move.Side opponent = Move.Side.WHITE;
		long emptyLocs = myBoard.getEmpty();
		long actives = 0L;
		long passives =0L;
		long kings = 0L;
		switch(me){
		case  WHITE:
			opponent= Move.Side.BLACK;
			actives = myBoard.getBAw();
			kings = myBoard.getFAw();
			passives = myBoard.getFAb();
			if( ((actives & doubleCorners<<4 & emptyLocs<<4) !=0) |
					((actives & doubleCorners<<5 & emptyLocs<<5) !=0) |
					((kings & doubleCorners>>4 & emptyLocs>>4) !=0) |
					((kings & doubleCorners>>5 & emptyLocs>>5) !=0)){doubleCornerCredit++;}
				//above does not consider arriving by jumping
				if( ((actives & doubleCorners<<8 & emptyLocs<<8 & passives<<4) !=0) |
					((actives & doubleCorners<<10 & emptyLocs<<10 & passives<<5)  !=0) |
					((kings & doubleCorners>>8 & emptyLocs>>8 & passives>>4) !=0) |
					((kings & doubleCorners>>10 & emptyLocs>>10 & passives>>5) !=0)){doubleCornerCredit++;}
			break;
		case  BLACK:
			opponent= Move.Side.WHITE;
			actives = myBoard.getFAb();
			kings = myBoard.getBAb();
			passives = myBoard.getBAw();
			if( ((kings & doubleCorners<<4 & emptyLocs<<4) !=0) |
					((kings & doubleCorners<<5 & emptyLocs<<5) !=0) |
					((actives & doubleCorners>>4 & emptyLocs>>4) !=0) |
					((actives & doubleCorners>>5 & emptyLocs>>5) !=0)){doubleCornerCredit++;}
				//above does not consider arriving by jumping
				if( ((kings & doubleCorners<<8 & emptyLocs<<8 & passives<<4) !=0) |
					((kings & doubleCorners<<10 & emptyLocs<<10 & passives<<5)  !=0) |
					((actives & doubleCorners>>8 & emptyLocs>>8 & passives>>4) !=0) |
					((actives & doubleCorners>>10 & emptyLocs>>10 & passives>>5) !=0)){doubleCornerCredit++;}
		}
		//material credit for active is 6 or less
		if(myMaterialCredit<=6){doubleCornerCredit++;}
		//opponent is ahead in material credit
		if(evalMaterialCredit(opponent)>myMaterialCredit){doubleCornerCredit++;}
		//active side can move into one of the double corner squares
		//looked at www.checkerschest.com/play-checkers-online/fundamentals3.htm to find what is a double corner square
		//they say 1,5,32,28 are those. Our numbers for those squares are 31, 35 and 1,5
		
		
	
		return doubleCornerCredit;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * CRAMP
	 * credited with 2 if the passive side occupies the cramping square, 13 for black and 20 for white
	 * and at least one other nearby square 9 or 14 for black, 19 or 24 for white
	 * while certain others, 17,21,22,25 for black 8.11.12.16 for white
	 * are all occupied by the active side
	 */
	
	public int evalCramp(){
		int cramp=0;
		//p.228 credited with 2 if passive side occupies the cramping square, i.e. 13 for black and 20 for white
		//to me it looks that either interpretation is cramping
		// resolve ambiguity about where 14, where 22 belong
		long crampingSquare = 0L;
		long nearbySquare = 0L;
		long certainOccupiedSquares = 0L;
		Move.Side me = myBoard.getWhoAmI();
		long actives = 0L;
		long passives =0L;
		
		switch(me){
		case  WHITE:
			actives = myBoard.getBAw();
			passives = myBoard.getFAb();
			crampingSquare = crampingToWhite; //I'm white, opponent is black, notice 13->14, deliberate notation adjustment
			nearbySquare = nearbyCrampingToWhite;
			certainOccupiedSquares = certainSquaresWhite;
			if(  ((crampingSquare &passives) != 0) && 
				 ((nearbySquare   &passives) != 0) && 
				 (countTheOnes(certainOccupiedSquares & actives, 8,17)>= 4)){cramp+=2;}
			break;
		case  BLACK:
			actives = myBoard.getFAb();
			passives = myBoard.getBAw();
			crampingSquare = crampingToBlack;//I'm black, opponent is white, notice 20->22, deliberate 
			nearbySquare = nearbyCrampingToBlack;
			certainOccupiedSquares = certainSquaresBlack;
			if( ((crampingSquare&passives) != 0) && 
			    ((nearbySquare   &passives) != 0) && 
				(countTheOnes(certainOccupiedSquares & actives, 19,28)>= 4)){cramp+=2;}
			
		}
 		return cramp;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	/* DENY (do this after MOB)
	 * credited with 1 for each square defined in MOB 
	 * if on the next move a piece occupying this square could be captured without an exchange
	 */
	
	public int evalDenialOccupancy(){

		int couldBeCapturedWithoutExchange = 0;
		long backwardActives = 0L;
		long forwardActives = 0L;
		long backwardPassives = 0L;//useful for forced capture
		long forwardPassives = 0L;
		long emptyLocs = myBoard.getEmpty();
		long mob = 0L;
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();	
  
		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();
 
		}

		//mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
		//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
		couldBeCapturedWithoutExchange = countTheOnes(
				(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
				(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
				(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
				(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9))
				, 6,30);
		
		return couldBeCapturedWithoutExchange;
	}
	////////////////////////////////////////////////////////////////////////////
	/* DIA
	 * credit with 1 for each passive piece located in the diagonal files terminating in the double corner squares
	 */
	
	public int evalDoubleDiagonalFile(){
		int doubleDiagonalFile = 0;
		//the parameter is credited with 1 for each passive piece located in the diagonal files terminating in the double corner squares
		//at last, a reference to passive pieces, and now I understand that it is pieces whose turn it is not
		long passives = 0L;
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passive pieces are black
			passives =  myBoard.getFAb(); //pawns and kings
			doubleDiagonalFile = countTheOnes(passives & doubleFileSquares, 1,35);
			
			break;
		case  BLACK:
			passives =  myBoard.getBAw(); //pawns and kings
			doubleDiagonalFile = countTheOnes(passives & doubleFileSquares, 1,35);			
		}
		return doubleDiagonalFile;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/* DIAV
	 * credited with 1/2 for each passive piece located 2 removed from the diagonal file terminating in the double corner squares
	 * 1 for each passive located 1 removed
	 * 3/2 for each passive on diagonal
	 */
	
	public int evalDIAV(){
		int diav = 0;
		long passives = 0L;
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passive pieces are black
			passives =  myBoard.getFAb(); //pawns and kings			
			break;
		case  BLACK:
			passives =  myBoard.getBAw(); //pawns and kings
		}
		diav = countTheOnes(passives & doubleFile2RemovedSquares, 3,33)/2;
		diav += countTheOnes(passives & doubleFile1RemovedSquares, 2,34);
		diav += (countTheOnes(passives & doubleFileSquares, 1,35)*3)/2;
		return diav;
	}
	/////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each string of passive pieces that occupy three adjacent diagonal squares
	 */
	public int evalDyke(){
		int dyke = 0;
		long passives=0L;
		long threesome=0L;
		int threesomeStart = 0;
		int threesomeEnd = 0;
		//credited with 1 for each string of passive pieces that occupy three adjacent diagonal squares
		//pos, pos+4, pos+8 is one and pos,pos+5, pos+10. I could just prepare them all.
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passive pieces are black
			passives =  myBoard.getFAb(); //pawns and kings			
			break;
		case  BLACK:
			passives =  myBoard.getBAw(); //pawns and kings
		}
		for(int i = 0; i<36;i++){
			threesome = theThreesomes[i];
			threesomeStart = theThreesomeStarts[i];
			threesomeEnd = theThreesomeEnds[i];
			dyke += countTheOnes(passives & threesome, threesomeStart, threesomeEnd);
		}
		
		return dyke;
	}
	////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each square to which the active side may advance a piece, and in so doing, force an exchange
	 */
	public int evalExch(){
		int exch=0;
		long backwardActives = 0L;
		long forwardActives = 0L;
		long backwardPassives = 0L;//useful for forced capture
		long forwardPassives = 0L;
		long passives = 0L;
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//actives are white
			backwardActives =  myBoard.getBAw(); //pawns and kings	
			backwardPassives =  myBoard.getBAb(); //pawns and kings
			forwardActives = myBoard.getFAw();
			forwardPassives = myBoard.getFAb();
			break;
		case  BLACK:
			backwardActives =  myBoard.getBAb(); //pawns and kings	
			backwardPassives =  myBoard.getBAw(); //pawns and kings
			forwardActives = myBoard.getFAb();
			forwardPassives = myBoard.getFAw();
		}
		//credit 1 for each square to which the active may advance a piece and force an exchange, i.e., become adjacent, because the opponent can jump back
		//are these the only? have to be sure not to double count a square, if there are two possibilities of forced exchanges
		exch += countTheOnes(forwardActives & backwardPassives<<4,2,26); //corresponding to actives moving 
		exch += countTheOnes(backwardActives & forwardPassives>>4,10,34); //corresponding to actives moving 
		exch += countTheOnes(forwardActives & backwardPassives<<5,1,25); //corresponding to actives moving 
		exch += countTheOnes(backwardActives & forwardPassives>>5,11,35); //corresponding to actives moving 
		
		return exch;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each passive piece that is flanked along one or other diagonal by two empty squares
	 */
	public int evalExpos(){
		int expos = 0;
		//credited 1 for each passive piece that is flanked along one or the other diagonal by two empty squares
		long passives = 0L;
		long emptyLoc = myBoard.getEmpty();
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passives are black	
			passives =  myBoard.getFAb(); //pawns and kings
			break;
		case  BLACK:
			passives = myBoard.getBAw();
		}
		//want both sides of either diagonal empty
		expos += countTheOnes(passives & emptyLoc<<4 & emptyLoc >> 4, 6,30);
		expos += countTheOnes(passives & emptyLoc<<5 & emptyLoc >>5,6,30);
		
		return expos;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each situation in which passive pieces occupy two adjacent squares in one row and
	 *  in which there three empty squares so disposed that the active side could,
	 *  by occupying one of them, 
	 *  threaten a sure capture of one or the other of the two pieces
	 */
	public int evalFork(){
		int fork = 0;
		return fork;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each single empty square that separates two passive pieces along a diagonal
	 * or that separates two passive pieces along a diagonal
	 * or that separates a passive piece from the edge of the board
	 */
	public int evalGap(){
		int gap = 0;
		long passives = 0L;
		long emptyLocs = myBoard.getEmpty();
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passives are black	
			passives =  myBoard.getFAb(); //pawns and kings
			break;
		case  BLACK:
			passives = myBoard.getBAw();
		}
		//square that separates two pieces along a diagonal,
		gap+=countTheOnes(emptyLocs & passives<<4 & passives>>4,6,30);
		gap+=countTheOnes(emptyLocs & passives<<5 & passives>>5,6,30);
		//square that separates piece from edge of board
		gap+=countTheOnes(emptyLocs & onEdge,1,35);
		return gap;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 if there are no active kings and if either the Bridge or the Triangle of Oreo is occupied by passive pieces
	 */
	public int evalGuard(){
		int guard = 0;
		int backRowBridge = 0;
		long activeKings = 0L;
		long passives = 0L;

		switch(myBoard.getWhoAmI()){
		case BLACK:
			
			activeKings =  myBoard.getBAb();
			if(activeKings!=0L){return 0;}
			
			passives = myBoard.getBAw();
			if(
					((passives&impulse33)!=0L)
			      &&((passives&impulse35)!=0L)
			      ||(countTheOnes(passives&blackTriangleOreo, 2,7)>=3)//triangle of oreo
			      ){guard++;}
			break;
		case WHITE:
			activeKings =  myBoard.getFAw();
			if(activeKings!=0L){return 0;}
			
			passives = myBoard.getFAb();
			if(
					((passives&impulse1)!=0L)
			      &&((passives&impulse3)!=0L)
			      || (countTheOnes(passives&whiteTriangleOreo,29,34)>=3)//triangle of oreo
			      ){guard++;}
			 
			
		}//end switch
		
		return guard;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each empty square that is surrounded by three or more passive pieces
	 */
	public int evalHole(){
		int hole = 0;
		long passives = 0L;
		long emptyLocs = myBoard.getEmpty();
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passives are black	
			passives =  myBoard.getFAb(); //pawns and kings
			break;
		case  BLACK:
			passives = myBoard.getBAw();
		}
		//hole surrounded by 3 or more passives
		return countTheOnes(emptyLocs & 
				(passives<<4 & passives<<5 & passives>>4)| //there are four ways to pick which of 4 is absent when only 3 are necessary
				(passives<<4 & passives<<5 & passives>>5)|
				(passives<<4 & passives>>4 & passives>>5)|
				(passives<<5 & passives>>4 & passives>>5), 6, 30);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each of the following squares: 11,12,15,16,20,21,24 and 25 which is occupied by a passive king
	 */
	public int evalKcent(){
		 
		long passiveKings = 0L;
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passives are black	
			passiveKings =  myBoard.getBAb(); // kings
			break;
		case  BLACK:
			passiveKings = myBoard.getFAw();
		}
		return countTheOnes(passiveKings & centerLocs, 11, 25);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each square to which the active side could move one or more pieces in the normal fashion, 
	 * disregarding the fact that jump moves may or may not be available
	 */
	public int evalMob(){
		
		long backwardActives = 0L;
		long forwardActives = 0L;
		long backwardPassives = 0L;//useful for forced capture
		long forwardPassives = 0L;
		long emptyLocs = myBoard.getEmpty();
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();
		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();
		}
		return countTheOnes(emptyLocs & (backwardActives>>4) | (backwardActives>>5)|(forwardActives<<4)|(forwardActives<<5), 1,35);

	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with the difference between MOB and DENY
	 */
	public int evalMobil(){
		return evalMob()-evalDenialOccupancy();
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 if pieces are even with a total piece count (2 for pawns, 3 for kinds) of less than 24
	 * and if an odd number of pieces are in the move system
	 * defined as those vertical files starting with squares 1,2,3,4
	 */
	public int evalMove(){
		int move = 0;
		long pieces = myBoard.getFAb()|myBoard.getBAw();
		long piecesInMoveSystem = pieces & moveSystem;
		if(countTheOnes(piecesInMoveSystem,1,31)%2==1){return 1;}//and if an odd number in move system
		//are pieces even?
		int blackCount = pieceCount(Move.Side.BLACK);
		if(blackCount==pieceCount(Move.Side.WHITE)){//they are even
			if(blackCount <24){return 1;}//and less than 24
		}	
		return move;
	}
		
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each passive piece that is surrounded by at least three empty squares
	 */
	public int evalNode(){

		long passives = 0L;
		long emptyLocs = myBoard.getEmpty();
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passives are black	
			passives =  myBoard.getFAb(); //pawns and kings
			break;
		case  BLACK:
			passives = myBoard.getBAw();
		}
		//hole surrounded by 3 or more passives
		return countTheOnes(passives & 
				(emptyLocs<<4 &emptyLocs<<5 & emptyLocs>>4)| //there are four ways to pick which of 4 is absent when only 3 are necessary
				(emptyLocs<<4 & emptyLocs<<5 & emptyLocs>>5)|
				(emptyLocs<<4 & emptyLocs>>4 & emptyLocs>>5)|
				(emptyLocs<<5 & emptyLocs>>4 & emptyLocs>>5), 6, 30);
		
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 if there are no passive kings and if the Triangle of Oreo (squares 2,3,7 for black)
	 * (squares 26,30,31 for white) is occupied by passive pieces
	 */
	public int evalOreo(){
		int oreo = 0;
		long passiveKings = 0L;
		long passives = 0L;
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passives are black	
			passiveKings =  myBoard.getBAb(); // kings
			passives =  myBoard.getFAb(); //pawns and kings
			if(countTheOnes(passives & whiteTriangleOreo, 29,34)>=3){oreo++;}
			break;
		case  BLACK:
			passiveKings = myBoard.getFAw();
			passives = myBoard.getBAw();
			if(countTheOnes(passives & blackTriangleOreo, 2,7)>=3){oreo++;}
		}
		if(passiveKings!=0){return 0;}		
		return oreo;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each passive piece that is completely surrounded by empty squares
	 */
	public int evalPole(){
		long passives = 0L;
		long emptyLocs = myBoard.getEmpty();
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passives are black	
			passives =  myBoard.getFAb(); //pawns and kings
			break;
		case  BLACK:
			passives = myBoard.getBAw();
		}
		return countTheOnes(passives & emptyLocs<<4 & emptyLocs<<5 & emptyLocs>>4 & emptyLocs>>5, 6,30);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * credited with 1 for each square to which an active piece may be moved 
	 * and in so doing threaten the capture of a passive piece on a subsequent move
	 */
	public int evalThret(){
		 
		long passives = 0L;
		long backwardActives = 0L;
		long forwardActives = 0L;
		long backwardPassives = 0L;//useful for forced capture
		long forwardPassives = 0L;
		long emptyLocs = myBoard.getEmpty();
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();
		passives = backwardPassives | forwardPassives;
		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();
		passives = backwardPassives | forwardPassives;
		}
		return countTheOnes(emptyLocs & (passives<<4&backwardActives<<4)  |
				                 (passives<<5&backwardActives<<10) |
				                 (passives>>4&forwardActives>>8) |
				                 (passives>>5&forwardActives>>10),10,26 );
	 
	}
	public int evalRECAP(){//Samuel says this is the same as exchange, so we will use it for active kings in the center
		long activeKings = 0L;
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passives are black	
			activeKings =  myBoard.getFAw(); // kings
			break;
		case  BLACK:
			activeKings = myBoard.getBAb();
		}
		return countTheOnes(activeKings & centerLocs, 11, 25);
		
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public int evalDEMO(){//denial of occupancy and total mobility (but denial of occupancy includes mob
		int item=0;
		long backwardActives = 0L;
		long forwardActives = 0L;
		long backwardPassives = 0L;//useful for forced capture
		long forwardPassives = 0L;
		long emptyLocs = myBoard.getEmpty();
		long mob = 0L;
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();	
  
		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();
 
		}

		mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
		//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
		long denialOccupancy =mob & 
				(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
				(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
				(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
				(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));
		

		long totalMobility =emptyLocs & (backwardActives>>4) | (backwardActives>>5)|(forwardActives<<4)|(forwardActives<<5);
		
				
		item=countTheOnes(denialOccupancy & totalMobility, 1,35);
		
		
		return item;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalDEMMO(){//denial of occupancy and not total mobility
	int item=0;
	long backwardActives = 0L;
	long forwardActives = 0L;
	long backwardPassives = 0L;//useful for forced capture
	long forwardPassives = 0L;
	long emptyLocs = myBoard.getEmpty();
	long mob = 0L;
	
	Move.Side me = myBoard.getWhoAmI();
	switch(me){
		case  WHITE:
			//actives are white
			backwardActives =  myBoard.getBAw(); //pawns and kings	
			backwardPassives =  myBoard.getBAb(); //pawns and kings
			forwardActives = myBoard.getFAw();
			forwardPassives = myBoard.getFAb();	

			break;
		case  BLACK:
			backwardActives =  myBoard.getBAb(); //pawns and kings	
			backwardPassives =  myBoard.getBAw(); //pawns and kings
			forwardActives = myBoard.getFAb();
			forwardPassives = myBoard.getFAw();

	}

	mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
	//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
	long denialOccupancy =mob & 
		(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
		(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
		(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
		(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));


	long totalMobility =emptyLocs & (backwardActives>>4) | (backwardActives>>5)|(forwardActives<<4)|(forwardActives<<5);

		
	item=countTheOnes(denialOccupancy & parNOT(totalMobility), 1,35);
	return item;
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalDDEMO(){ //not denial of occupancy and total mobility
int item=0;
long backwardActives = 0L;
long forwardActives = 0L;
long backwardPassives = 0L;//useful for forced capture
long forwardPassives = 0L;
long emptyLocs = myBoard.getEmpty();
long mob = 0L;

Move.Side me = myBoard.getWhoAmI();
switch(me){
	case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();	

		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();

}

mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
long denialOccupancy =mob & 
	(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
	(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
	(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
	(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));


long totalMobility =emptyLocs & (backwardActives>>4) | (backwardActives>>5)|(forwardActives<<4)|(forwardActives<<5);

	
item=countTheOnes(parNOT(denialOccupancy) & totalMobility, 1,35);
return item;
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalDDMM(){  //not denial of occupancy and not total mobility
int item=0;
long backwardActives = 0L;
long forwardActives = 0L;
long backwardPassives = 0L;//useful for forced capture
long forwardPassives = 0L;
long emptyLocs = myBoard.getEmpty();
long mob = 0L;

Move.Side me = myBoard.getWhoAmI();
switch(me){
	case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();	

		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();

}

mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
long denialOccupancy =mob & 
	(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
	(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
	(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
	(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));


long totalMobility =emptyLocs & (backwardActives>>4) | (backwardActives>>5)|(forwardActives<<4)|(forwardActives<<5);

	
item=countTheOnes(parNOT(denialOccupancy) & parNOT(totalMobility), 1,35);
return item;
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalMODE1(){// undenied mobility(MOBIL) and denial of occupancy isn't this empty?gets discarded after 1 adjustment
int item=0;

long backwardActives = 0L;
long forwardActives = 0L;
long backwardPassives = 0L;//useful for forced capture
long forwardPassives = 0L;
long emptyLocs = myBoard.getEmpty();
long mob = 0L;

Move.Side me = myBoard.getWhoAmI();
switch(me){
	case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();	

		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();

}

mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
long denialOccupancy =
	(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
	(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
	(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
	(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));

long mobil = mob^denialOccupancy;

  item=countTheOnes(mobil&denialOccupancy,1,35);

return item;
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalMODE2(){// undenied mobility (MOBIL) and not denial of occupancy, stays
int item=0;

long backwardActives = 0L;
long forwardActives = 0L;
long backwardPassives = 0L;//useful for forced capture
long forwardPassives = 0L;
long emptyLocs = myBoard.getEmpty();
long mob = 0L;

Move.Side me = myBoard.getWhoAmI();
switch(me){
	case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();	

		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();

}

mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
long denialOccupancy =
	(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
	(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
	(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
	(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));

long mobil = mob^denialOccupancy;

  item=countTheOnes(mobil&parNOT(denialOccupancy),1,35);

return item;
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalMODE3(){ //not undenied mobility and denial of occupancy, stays
int item=0;

long backwardActives = 0L;
long forwardActives = 0L;
long backwardPassives = 0L;//useful for forced capture
long forwardPassives = 0L;
long emptyLocs = myBoard.getEmpty();
long mob = 0L;

Move.Side me = myBoard.getWhoAmI();
switch(me){
	case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();	

		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();

}

mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
long denialOccupancy =
	(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
	(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
	(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
	(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));
long mobil = mob^denialOccupancy;

  item=countTheOnes(parNOT(mobil)&denialOccupancy,1,35);

return item;
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalMODE4(){// not undenied mobility and not denial of occupancy, adjusted 0 times before discard
int item=0;
long backwardActives = 0L;
long forwardActives = 0L;
long backwardPassives = 0L;//useful for forced capture
long forwardPassives = 0L;
long emptyLocs = myBoard.getEmpty();
long mob = 0L;

Move.Side me = myBoard.getWhoAmI();
switch(me){
	case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();	

		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();

}

mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
long denialOccupancy =
	(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
	(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
	(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
	(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));

long mobil = mob^denialOccupancy;

  item=countTheOnes(parNOT(mobil)&parNOT(denialOccupancy),1,35);
return item;
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalMOC1(){  //undenied mobility and center control 1, adjusted once before discard


long backwardActives = 0L;
long forwardActives = 0L;
long backwardPassives = 0L;//useful for forced capture
long forwardPassives = 0L;
long emptyLocs = myBoard.getEmpty();
long mob = 0L;

Move.Side me = myBoard.getWhoAmI();
switch(me){
	case  WHITE:
		//actives are white
		backwardActives =  myBoard.getBAw(); //pawns and kings	
		backwardPassives =  myBoard.getBAb(); //pawns and kings
		forwardActives = myBoard.getFAw();
		forwardPassives = myBoard.getFAb();	
		

		break;
	case  BLACK:
		backwardActives =  myBoard.getBAb(); //pawns and kings	
		backwardPassives =  myBoard.getBAw(); //pawns and kings
		forwardActives = myBoard.getFAb();
		forwardPassives = myBoard.getFAw();

}
long passives = forwardPassives | backwardPassives;
mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
long denialOccupancy =
	(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
	(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
	(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
	(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));

long mobil = mob^denialOccupancy;

long centerControl1 = passives & centerLocs;
return(countTheOnes(mobil&centerControl1,11,25));

}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalMOC2(){ //undenied mobility and not center control 1, matters lots, neg
	long backwardActives = 0L;
	long forwardActives = 0L;
	long backwardPassives = 0L;//useful for forced capture
	long forwardPassives = 0L;
	long emptyLocs = myBoard.getEmpty();
	long mob = 0L;

	Move.Side me = myBoard.getWhoAmI();
	switch(me){
		case  WHITE:
			//actives are white
			backwardActives =  myBoard.getBAw(); //pawns and kings	
			backwardPassives =  myBoard.getBAb(); //pawns and kings
			forwardActives = myBoard.getFAw();
			forwardPassives = myBoard.getFAb();	
			

			break;
		case  BLACK:
			backwardActives =  myBoard.getBAb(); //pawns and kings	
			backwardPassives =  myBoard.getBAw(); //pawns and kings
			forwardActives = myBoard.getFAb();
			forwardPassives = myBoard.getFAw();

	}
	long passives = forwardPassives | backwardPassives;
	mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
	//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
	long denialOccupancy =
		(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
		(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
		(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
		(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));

	long mobil = mob^denialOccupancy;

	long centerControl1 = passives & centerLocs;
	return(countTheOnes(mobil&parNOT(centerControl1),1,35));
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalMOC3(){ //not undenied mobility and center control 1, stays
	long backwardActives = 0L;
	long forwardActives = 0L;
	long backwardPassives = 0L;//useful for forced capture
	long forwardPassives = 0L;
	long emptyLocs = myBoard.getEmpty();
	long mob = 0L;

	Move.Side me = myBoard.getWhoAmI();
	switch(me){
		case  WHITE:
			//actives are white
			backwardActives =  myBoard.getBAw(); //pawns and kings	
			backwardPassives =  myBoard.getBAb(); //pawns and kings
			forwardActives = myBoard.getFAw();
			forwardPassives = myBoard.getFAb();	
			

			break;
		case  BLACK:
			backwardActives =  myBoard.getBAb(); //pawns and kings	
			backwardPassives =  myBoard.getBAw(); //pawns and kings
			forwardActives = myBoard.getFAb();
			forwardPassives = myBoard.getFAw();

	}
	long passives = forwardPassives | backwardPassives;
	mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
	//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
	long denialOccupancy =
		(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
		(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
		(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
		(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));

	long mobil = mob^denialOccupancy;

	long centerControl1 = passives & centerLocs;
	return(countTheOnes(parNOT(mobil)&centerControl1,11,25));
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public int evalMOC4(){ //not undenied mobility and not center control 1, important
	long backwardActives = 0L;
	long forwardActives = 0L;
	long backwardPassives = 0L;//useful for forced capture
	long forwardPassives = 0L;
	long emptyLocs = myBoard.getEmpty();
	long mob = 0L;

	Move.Side me = myBoard.getWhoAmI();
	switch(me){
		case  WHITE:
			//actives are white
			backwardActives =  myBoard.getBAw(); //pawns and kings	
			backwardPassives =  myBoard.getBAb(); //pawns and kings
			forwardActives = myBoard.getFAw();
			forwardPassives = myBoard.getFAb();	
			

			break;
		case  BLACK:
			backwardActives =  myBoard.getBAb(); //pawns and kings	
			backwardPassives =  myBoard.getBAw(); //pawns and kings
			forwardActives = myBoard.getFAb();
			forwardPassives = myBoard.getFAw();

	}
	long passives = forwardPassives | backwardPassives;
	mob=emptyLocs & (backwardPassives>>4) | (backwardPassives>>5)|(forwardPassives<<4)|(forwardPassives<<5);//consider passive moving in
	//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
	long denialOccupancy =
		(forwardActives<<4 & emptyLocs>>4 & parNOT(backwardPassives>>8 | (backwardPassives>>9&emptyLocs<<1) | (forwardPassives<<1&emptyLocs>>9) )|
		(forwardActives<<5 & emptyLocs>>5)& parNOT(backwardPassives>>10 |(backwardPassives>>9&emptyLocs>>1))| (forwardPassives>>1&emptyLocs>>9) )|
		(backwardActives>>4& emptyLocs<<4)& parNOT(forwardPassives<<8 |  (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives>>1&emptyLocs<<9))|
		(backwardActives>>5& emptyLocs<<5)& parNOT(forwardPassives<<10 | (forwardPassives<<9&emptyLocs>>1) |  (backwardPassives<<1&emptyLocs<<9));

	long mobil = mob^denialOccupancy;

	long centerControl1 = passives & centerLocs;
	return(countTheOnes(parNOT(mobil)&parNOT(centerControl1),1,35));
}
	
	
			
	/*Total Enemy Mobility*/
	/* Add 1 to this parameter for every square that an enemy piece can move to normally (i.e. without jumping any pieces)
	 * 
	 * Note: Assumes BLACK plays from the bottom of the board and WHITE plays from the top of the board.
	 */
	public int getTotalEnemyMobility(){
		int totalEnemyMobility = 0;
		long successful = 0L;
		long jumpers = 0L;
		long emptyLoc = myBoard.getEmpty();
		
		switch(myBoard.getWhoAmI()){
    	case WHITE://I'm white, opponent is black
    		//black pawns are never backward active
    		//get the empty 

    		jumpers = myBoard.getFAb();//they move differently so we check them separately
    		successful =  jumpers  & (emptyLoc>>4);
    		totalEnemyMobility+= countTheOnes(successful, 1,35);

    		successful =  jumpers & (emptyLoc>>5);
    		totalEnemyMobility+= countTheOnes(successful, 1,35);

    		jumpers = myBoard.getBAb();
    		successful =   jumpers & (emptyLoc<<5);
    		totalEnemyMobility+= countTheOnes(successful, 1,35);

    		successful =  jumpers & (emptyLoc<<4);
    		totalEnemyMobility+= countTheOnes(successful, 1,35);
            break;
    		
    	case BLACK:
    		//white pawns are never forward active

    		jumpers = myBoard.getFAw();
    		successful =   jumpers &  (emptyLoc>>4);
    		totalEnemyMobility+= countTheOnes(successful, 1,35);
    		
    		successful =   jumpers & (emptyLoc>>5);
    		totalEnemyMobility+= countTheOnes(successful, 1,35);
    		
    		jumpers = myBoard.getBAw();
    		successful =  jumpers &  (emptyLoc<<5);
    		totalEnemyMobility+= countTheOnes(successful, 1,35);
    		
    		successful = jumpers & (emptyLoc<<4);
    		totalEnemyMobility+= countTheOnes(successful, 1,35);
    		break;
    		default: System.err.println("Evaluator::getTotalEnemyMobility default");  
    	}
    	return totalEnemyMobility;
    }
	
	/*switch(myBoard.getWhoAmI()){
case BLACK:
	for (int i=0; i<7; i++){
		for (int j=0; j<7; j++){
			if (samloc[i][j] == enemyPawn){
				if (samloc[i+1][j+1] == empty){totalEnemyMobility += 1;}
			}
		}
		for (int j=1; j<8; j++){
			if (samloc[i][j] == enemyPawn){
				if (samloc[i+1][j-1] == empty){totalEnemyMobility += 1;}
			}
		}
	}*/
	//we need to count the number of moves movers can make
	
	
	/*for (int i=0; i<7; i++){
		for (int j=0; j<7; j++){
			if (samloc[i][j] == enemyKing){
				if (samloc[i+1][j+1] == empty){totalEnemyMobility += 1;}
			}
		}
		for (int j=1; j<8; j++){
			if (samloc[i][j] == enemyKing){
				if (samloc[i+1][j-1] == empty){totalEnemyMobility += 1;}
			}
		}
	}
	for (int i=1; i<8; i++){
		for (int j=0; j<7; j++){
			if (samloc[i][j] == enemyKing){
				if (samloc[i-1][j+1] == empty){totalEnemyMobility += 1;}
			}
		}
		for (int j=1; j<8; j++){
			if (samloc[i][j] == enemyKing){
				if (samloc[i-1][j-1] == empty){totalEnemyMobility += 1;}
			}
		}
	}
	break;
	
case WHITE:
	for (int i=1; i<8; i++){
		for (int j=0; j<7; j++){
			if (samloc[i][j] == enemyPawn){
				if (samloc[i-1][j+1] == empty){totalEnemyMobility += 1;}
			}
		}
		for (int j=1; j<8; j++){
			if (samloc[i][j] == enemyPawn){
				if (samloc[i-1][j-1] == empty){totalEnemyMobility += 1;}
			}
		}
	}
	for (int i=0; i<7; i++){
		for (int j=0; j<7; j++){
			if (samloc[i][j] == enemyKing){
				if (samloc[i-1][j+1] == empty){totalEnemyMobility += 1;}
			}
		}
		for (int j=1; j<8; j++){
			if (samloc[i][j] == enemyKing){
				if (samloc[i-1][j-1] == empty){totalEnemyMobility += 1;}
			}
		}
	}
	for (int i=1; i<8; i++){
		for (int j=0; j<7; j++){
			if (samloc[i][j] == enemyKing){
				if (samloc[i+1][j+1] == empty){totalEnemyMobility += 1;}
			}
		}
		for (int j=1; j<8; j++){
			if (samloc[i][j] == enemyKing){
				if (samloc[i+1][j-1] == empty){totalEnemyMobility += 1;}
			}
		}
	}
	break;		
}*/



	
	public int countTheOnes(long places, int start, int end){//Samuel does this by table lookup. For us, compute is fast and memory is slow
		int howMany = 0;
		 for(int i=start; i<=end; i++){
         	places = places >>1;
         	if(i%9 ==0){i++; places = places>>1;}
         	else{
         		howMany += places%2;//if someone is there, add
         	}
         }//end for
		
		return howMany;
	}
	public int weightedSum(int[] theFeatureValues){//Samuel uses power of 2 coefficients, and does multiplies by shifts and adds
		int answer = 0;
		for(int i=0; i<DBHandler.NUMPARAMS; i++){
			answer += weights[i]*theFeatureValues[i];
			//System.err.println("Evaluator::weightedSum: i theFeatureValue the weight "+i+" "+theFeatureValues[i]+" "+weights[i]);
		}
		return answer;
		
		
		 
	}
	public void setWeight(int which, double what){
		//System.err.println("Evaluator::setWeight: weight being set");
		this.weights[which]=what;
	}
	public double getWeight(int which){
		return this.weights[which];
	}
	   public int evalPieceAdvantage(){
		   
	    	int adv= 0;
	    	
	    	int numBlackPieces = countTheOnes( myBoard.getFAb(),1,35);
	    	int numBlackKings = countTheOnes(myBoard.getBAb(),1,35);
	    	int numWhitePieces = countTheOnes( myBoard.getBAw(),1,35);
	    	int numWhiteKings = countTheOnes(myBoard.getFAw(),1,35);
	    	
	    	switch(myBoard.getWhoAmI()){
	    	case BLACK:
	    		adv=2*numBlackPieces+numBlackKings - 2*numWhitePieces - numWhiteKings;
	    		
	    		break;
	    	case WHITE:
	    		adv=2*numWhitePieces+numWhiteKings - 2*numBlackPieces - numBlackKings;
	    	   		
	    	}
	    	//count down pieces of other side
	    	return adv;
	    }
	   public double averageOfTheCoefficients(){
		   double averageWeight = 0;
		   for(int i=0; i<DBHandler.NUMPARAMS;i++){
			   averageWeight+=weights[i];
		   }
		   averageWeight = averageWeight /DBHandler.NUMPARAMS;
		   return averageWeight;
	   }
		/*Material Credit*/
		/*This is not a parameter from Samuel's paper. I added this as a potentially temporary paramater for initial evaluation.*/
		
		/* This parameter credits us with 2 points per Pawn we control and 3 points per King we control. Likewise, it credits the 
		 * opponent with 2 points per Pawn and 3 points per King they control. The Total Material Credit (materialCredit) is the 
		 * difference between our material credit (myMaterialCredit) and the opponent's (opponentMaterialCredit).
		 */
		public int evalMaterialCredit(Move.Side s){
			
			int materialCredit = 0;
			numMyPawns=0; //initialize
			numMyKings=0; //initialize
			movers = 0L;
			mover= 0L;
			numEnemyPawns=0; //initialize
			numEnemyKings=0; //initialize
			
			switch(s){
			  case BLACK:
				//black pawns are never backward active
				movers = myBoard.getFAb();//this includes pawns and kings
				for (int i = 1; i<36; i++){//32 and up cannot move forward 4
					if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
						movers = movers/2; //start with bit 1
						mover = movers&1;
						if (mover==1){//there is a mover at this place
							numMyPawns += 1;
						}
				}
				 movers = myBoard.getBAb(); //this has the kings
				 if(movers != 0){
				 for (int i = 1; i<36; i++){//32 and up cannot move forward 4
						if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
							movers = movers/2; //start with bit 1
							mover = movers&1;
							if (mover==1){//there is a mover at this place
								numMyKings += 1;
							}
					}
				 numMyPawns = numMyPawns - numMyKings;
				 }
				 break;
			  case WHITE:
					//black pawns are never backward active
					movers = myBoard.getBAw();//this includes pawns and kings
					for (int i = 1; i<36; i++){//32 and up cannot move forward 4
						if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
							movers = movers/2; //start with bit 1
							mover = movers&1;
							if (mover==1){//there is a mover at this place
								numMyPawns += 1;
							}
					}
					 movers = myBoard.getFAw(); //this has the kings
					 if(movers != 0){
					 for (int i = 1; i<36; i++){//32 and up cannot move forward 4
							if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
								movers = movers/2; //start with bit 1
								mover = movers&1;
								if (mover==1){//there is a mover at this place
									numMyPawns += 1;
								}
						}}
					 numMyPawns = numMyPawns - numMyKings;}
			switch(myBoard.getWhoAmI()){
			  case WHITE:
				//black pawns are never backward active
				movers = myBoard.getFAb();//this includes pawns and kings
				for (int i = 1; i<36; i++){//32 and up cannot move forward 4
					if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
						movers = movers/2; //start with bit 1
						mover = movers&1;
						if (mover==1){//there is a mover at this place
							numEnemyPawns += 1;
						}
				}
				 movers = myBoard.getBAb(); //this has the kings
				 if(movers != 0){
				 for (int i = 1; i<36; i++){//32 and up cannot move forward 4
						if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
							movers = movers/2; //start with bit 1
							mover = movers&1;
							if (mover==1){//there is a mover at this place
								numEnemyKings += 1;
							}
					}
				 numEnemyPawns = numEnemyPawns - numEnemyKings;
				 }
				 break;
			  case BLACK:
					//black pawns are never backward active
					movers = myBoard.getBAw();//this includes pawns and kings
					for (int i = 1; i<36; i++){//32 and up cannot move forward 4
						if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
							movers = movers/2; //start with bit 1
							mover = movers&1;
							if (mover==1){//there is a mover at this place
								numEnemyPawns += 1;
							}
					}
					 movers = myBoard.getFAw(); //this has the kings
					 if(movers != 0){
					 for (int i = 1; i<36; i++){//32 and up cannot move forward 4
							if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
								movers = movers/2; //start with bit 1
								mover = movers&1;
								if (mover==1){//there is a mover at this place
									numEnemyPawns += 1;
								}
						}}
					 numEnemyPawns = numEnemyPawns - numEnemyKings;}
				 
		/*for (int i=0; i<8; i++){
			for (int j=0; j<8; j++){
				if (samloc[i][j] == myPawn){numMyPawns += 1;}
				if (samloc[i][j] == myKing){numMyKings += 1;}
				if (samloc[i][j] == enemyPawn){numEnemyPawns += 1;}
				if (samloc[i][j] == enemyKing){numEnemyKings += 1;}
			}*/

			int myMaterialCredit = 2*numMyPawns + 3*numMyKings;
			int enemyMaterialCredit = 2*numEnemyPawns + 3*numEnemyKings;
			materialCredit = myMaterialCredit + enemyMaterialCredit;

			 return materialCredit; }
	////////////////////////////////////////////////////////////////////
		public int pieceCount(Move.Side s){//2 for pawns, 3 for kings
			int count = 0;
			
			switch(myBoard.getWhoAmI()){
	    	case WHITE:
	    		return 2*countTheOnes(myBoard.getBAw(),1,35)+countTheOnes(myBoard.getFAw(),1,35);
	     
	    	case BLACK:
	    		return 2*countTheOnes(myBoard.getFAb(),1,35)+countTheOnes(myBoard.getBAb(),1,35);
			}
			return count;
		}
		////////////////////////////////////////////////////////////////////
		public long parNOT(long a){
			for (int i = 0; i < 63; i++){
				allOnes = allOnes | 1L<<i;
			}
			//System.err.println("Evaluator::parNOT");
			//myBoard.showBitz(a);
			//myBoard.showBitz(a^allOnes);
			return (a^allOnes);
		}
		///////////////////////////////////////////////////////////////////
		public double[] getWeightValues(){
			return this.weights;
		}
		////////////////////////////////////////////////////////////////////////
		public void scaleWeights(int which, double scale){
			System.err.println("Evaluator::scaleWeights");
			for(int i=0; i<DBHandler.NUMPARAMS; i++){
				if(i != which){
					weights[i]=weights[i]*scale;
				}
			}
		}
		public void setWeights(double[] weightSet){
			this.weights = weightSet;
		}
		   public int evalPieceAdvantageKings(){
		    	int nKings = 0;
		    	switch(myBoard.getWhoAmI()){
		    	case BLACK:
		    		return(countTheOnes(myBoard.getBAb(), 1, 35)-countTheOnes(myBoard.getFAw(), 1, 35));
		    	case WHITE:
		    		return(countTheOnes(myBoard.getFAw(), 1, 35)-countTheOnes(myBoard.getBAb(), 1, 35));
		    	}
		    	return nKings;
		    }
}

