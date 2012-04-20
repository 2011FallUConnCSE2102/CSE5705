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

import actions.Move;
import state.Board;

public class Evaluator {
	/*Let's say, when Evaluator (this, e.g.) instance is created, an object board is passed, so when a board
	 * wants to evaluate itself, the board uses its evaluator and the evaluator can obtain the relevant info from its parent board
	 */
	private Board myBoard = null;
	private int materialCredit, myMaterialCredit, enemyMaterialCredit;
	/* This is just a list of all of the parameters mentioned at the end of the Samuel paper.*/
	private int advancement, apex, backRowBridge, centralControl1, centralControl2, doubleCornerCredit, 
				cramp, denialOfOccupancy, doubleDiagonalFile, diagonalMomentValue, dyke, exchange, 
				exposure, threatOfFork, gap, backRowControl, hole, centralKingControl1, centralKingControl2, 
				totalEnemyMobility, undeniedEnemyMobility, move, node, triangleOfOreos, pole, threat;
	private double weights[]={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
	private int howManyWeights = 25;
	Move.Side whoAmI;
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
	long doubleCorners = 2L+(long)Math.pow(2,5)+(long)Math.pow(2,31)+(long)Math.pow(2,35);
	long crampingToBlack = (long)Math.pow(2,14);//could be 22
	long crampingToWhite = (long)Math.pow(2,22);//could be 14
	long nearbyCrampingToBlack = (long)Math.pow(2,10)+(long)Math.pow(2,15); //, from 9,14 could be 19,24
	long nearbyCrampingToWhite = (long)Math.pow(2,21)+(long)Math.pow(2,26); //from 19,24, typoed as 19,20
	long certainSquaresBlack=(long)Math.pow(2,19)+(long)Math.pow(2,23)+(long)Math.pow(2,24)+(long)Math.pow(2,28);//from 17,21,22,25
	long certainSquaresWhite=(long)Math.pow(2,8)+(long)Math.pow(2,12)+(long)Math.pow(2,13)+(long)Math.pow(2,17);//from 8,11,12,16
	long doubleFileSquares=(long)Math.pow(2, 1)+(long)Math.pow(2, 6)+(long)Math.pow(2, 11)+(long)Math.pow(2, 16)+
			(long)Math.pow(2, 21)+(long)Math.pow(2, 26)+(long)Math.pow(2, 31)+(long)Math.pow(2, 5)+(long)Math.pow(2, 10)+
			(long)Math.pow(2, 15)+(long)Math.pow(2, 20)+(long)Math.pow(2, 25)+(long)Math.pow(2, 30)+(long)Math.pow(2, 35);
	long doubleFile1RemovedSquares=(long)Math.pow(2, 2)+(long)Math.pow(2, 14)+(long)Math.pow(2, 7)+(long)Math.pow(2, 19)+
			(long)Math.pow(2, 12)+(long)Math.pow(2, 24)+(long)Math.pow(2, 17)+(long)Math.pow(2, 29)+(long)Math.pow(2, 22)+
			(long)Math.pow(2, 34);
	long doubleFile2RemovedSquares=(long)Math.pow(2, 3)+(long)Math.pow(2, 23)+(long)Math.pow(2, 8)+(long)Math.pow(2, 28)+
			(long)Math.pow(2, 13)+(long)Math.pow(2, 33);
	long [] theThreesomes ={
			(long)Math.pow(2, 2)+(long)Math.pow(2, 6)+(long)Math.pow(2, 10),
			(long)Math.pow(2, 3)+(long)Math.pow(2, 7)+(long)Math.pow(2, 11),
			(long)Math.pow(2, 4)+(long)Math.pow(2, 8)+(long)Math.pow(2, 12),
			(long)Math.pow(2, 6)+(long)Math.pow(2, 10)+(long)Math.pow(2, 14),
			(long)Math.pow(2, 7)+(long)Math.pow(2, 11)+(long)Math.pow(2, 15),
			(long)Math.pow(2, 8)+(long)Math.pow(2, 12)+(long)Math.pow(2, 16),
			(long)Math.pow(2, 11)+(long)Math.pow(2, 15)+(long)Math.pow(2, 19),
			(long)Math.pow(2, 12)+(long)Math.pow(2, 16)+(long)Math.pow(2, 20),
			(long)Math.pow(2, 13)+(long)Math.pow(2, 17)+(long)Math.pow(2, 21),
			(long)Math.pow(2, 15)+(long)Math.pow(2, 19)+(long)Math.pow(2, 23),
			(long)Math.pow(2, 16)+(long)Math.pow(2, 20)+(long)Math.pow(2, 24),
			(long)Math.pow(2, 17)+(long)Math.pow(2, 21)+(long)Math.pow(2, 25),
			(long)Math.pow(2, 20)+(long)Math.pow(2, 24)+(long)Math.pow(2, 28),
			(long)Math.pow(2, 21)+(long)Math.pow(2, 25)+(long)Math.pow(2, 29),
			(long)Math.pow(2, 22)+(long)Math.pow(2, 26)+(long)Math.pow(2, 30),
			(long)Math.pow(2, 24)+(long)Math.pow(2, 28)+(long)Math.pow(2, 32),
			(long)Math.pow(2, 25)+(long)Math.pow(2, 29)+(long)Math.pow(2, 33),
			(long)Math.pow(2, 26)+(long)Math.pow(2, 30)+(long)Math.pow(2, 34),
			(long)Math.pow(2, 1)+(long)Math.pow(2, 6)+(long)Math.pow(2, 11),
			(long)Math.pow(2, 2)+(long)Math.pow(2, 7)+(long)Math.pow(2, 12),
			(long)Math.pow(2, 3)+(long)Math.pow(2, 8)+(long)Math.pow(2, 13),
			(long)Math.pow(2, 5)+(long)Math.pow(2, 10)+(long)Math.pow(2, 15),
			(long)Math.pow(2, 6)+(long)Math.pow(2, 11)+(long)Math.pow(2, 16),
			(long)Math.pow(2, 7)+(long)Math.pow(2, 12)+(long)Math.pow(2, 17),
			(long)Math.pow(2, 10)+(long)Math.pow(2, 15)+(long)Math.pow(2, 20),
			(long)Math.pow(2, 11)+(long)Math.pow(2, 16)+(long)Math.pow(2, 21),
			(long)Math.pow(2, 12)+(long)Math.pow(2, 17)+(long)Math.pow(2, 22),
			(long)Math.pow(2, 14)+(long)Math.pow(2, 19)+(long)Math.pow(2, 24),
			(long)Math.pow(2, 15)+(long)Math.pow(2, 20)+(long)Math.pow(2, 25),
			(long)Math.pow(2, 16)+(long)Math.pow(2, 21)+(long)Math.pow(2, 26),
			(long)Math.pow(2, 19)+(long)Math.pow(2, 24)+(long)Math.pow(2, 29),
			(long)Math.pow(2, 20)+(long)Math.pow(2, 25)+(long)Math.pow(2, 30),
			(long)Math.pow(2, 21)+(long)Math.pow(2, 26)+(long)Math.pow(2, 31),
			(long)Math.pow(2, 23)+(long)Math.pow(2, 28)+(long)Math.pow(2, 33),
			(long)Math.pow(2, 24)+(long)Math.pow(2, 29)+(long)Math.pow(2, 34),
			(long)Math.pow(2, 25)+(long)Math.pow(2, 30)+(long)Math.pow(2, 35)			
			};
	int[] theThreesomeStarts ={1,2,3,5,6,7,10,11,12,14,15,16,19,20,21,23,24,25,
			2,3,4,6,7,8,11,12,13,15,16,17,20,21,22,24,25,26};
	int[] theThreesomeEnds ={11,12,13,15,16,17,20,21,22,24,25,26,29,30,31,33,34,35,
			10,11,12,14,15,16,19,20,21,23,24,25,28,29,30,32,33,34};
	
	/* I gave each of the parameters a positive or negative coefficient based on my understanding of them.
	 * The values of the coefficients will definitely need to be adjusted.
	 * A gameState with a high value is desirable. The higher the gamestate, the larger our advantage.
	 */
	private int boardValue =(int) weights[0]*materialCredit
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
							;
	
	
	/* For details on any specific parameter, refer to the last 2 pages of the Samuel paper. They don't explicitly describe the motivation
	 * or strategy behind each parameter, but they do indicate how each parameter is scored, giving an idea on how to code them.
	 */
	
	public Evaluator (Board bd, boolean alphaBeta){
		this.myBoard = bd;
		this.whoAmI = bd.getWhoAmI();
	}
	/*Material Credit*/
	/*This is not a parameter from Samuel's paper. I added this as a potentially temporary paramater for initial evaluation.*/
	
	/* This parameter credits us with 2 points per Pawn we control and 3 points per King we control. Likewise, it credits the 
	 * opponent with 2 points per Pawn and 3 points per King they control. The Total Material Credit (materialCredit) is the 
	 * difference between our material credit (myMaterialCredit) and the opponent's (opponentMaterialCredit).
	 */
	public int evalMaterialCredit(Move.Side s){
		
	materialCredit = 0;
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

	myMaterialCredit = 2*numMyPawns + 3*numMyKings;
	enemyMaterialCredit = 2*numEnemyPawns + 3*numEnemyKings;
	materialCredit = myMaterialCredit + enemyMaterialCredit;

	 return materialCredit; }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public int evalAdvancement(){
		/*Advancement*/
		/* This parameter increases the more you advance forward across the board, giving +1 for each piece you control
		 * in rows 2 and 3 (if BLACK) and -1 for each piece you control in rows 4 and 5.
		 * 
		 * Note: This parameter assumes that BLACK starts at the bottom of the board and WHITE starts at the top.
		 */
		advancement = 0; //initialize
		long movers = 0L;
		long mover= 0L;
	switch(myBoard.getWhoAmI()){
	case BLACK:
		/*for (int j=0; j<8; j++){
			for( int i=0; i+2<4; i++){
				if (samloc[i][j] == myPawn || samloc[i][j] == myKing){advancement += 1;}	
			}
			for (int i=0; i+4<6; i++){
				if (samloc[i][j] == myPawn || samloc[i][j] == myKing){advancement -= 1;}
			}
		}*/
		movers = myBoard.getFAb();//this includes pawns and kings
		for (int i = 19; i<=26; i++){//rows 2 and 3
			if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
				movers = movers/2; //start with bit 1
				mover = movers&1;
				if (mover==1){//there is a mover at this place
					advancement += 1;
				}
		}
		movers = myBoard.getFAb();//this includes pawns and kings
		for (int i = 10; i<=17; i++){//rows 4 and 5
			if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
				movers = movers/2; //start with bit 1
				mover = movers&1;
				if (mover==1){//there is a mover at this place
					advancement -= 1;
				}
		}
		
		break;
	case WHITE:
		movers = myBoard.getBAw();//this includes pawns and kings
		for (int i = 10; i<=17; i++){//rows 4 and 5
			if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
				movers = movers/2; //start with bit 1
				mover = movers&1;
				if (mover==1){//there is a mover at this place
					advancement += 1;
				}
		}
		movers = myBoard.getFAb();//this includes pawns and kings
		for (int i = 19; i<=26; i++){//rows 2 and 3
			if((i%9)==0){i++;}//this assures we do not test movers at places that are 9ish
				movers = movers/2; //start with bit 1
				mover = movers&1;
				if (mover==1){//there is a mover at this place
					advancement -= 1;
				}
		}
		break;
	}
	
	return advancement;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/* debited with 1 if there are no kings on the board
	 * if either square 7 or 26 is occupied by an active piece
	 * and if neither of the squares is occupied by a  passive piece
	 */
	public int evalApex(){
		/*Apex*/
		int apex = 0;// 

	/*boolean kingsOnBoard = false;
	for(int i=0; i<8; i++){
		for (int j=0; j<8; j++){
			if (samloc[i][j] == myKing || samloc[i][j] == enemyKing){
				kingsOnBoard = true;
			}
		}
	}
	if (kingsOnBoard == true){
		apex -= 1;
	}
	if (samloc[1][5] == enemyPawn || samloc[1][5] == enemyKing){
		apex -= 1;
	} else if (samloc[6][2] == enemyPawn || samloc[6][2] == enemyKing){
		apex -= 1;
	}
	if (samloc[1][5] != myPawn && samloc[1][5] != myKing && samloc[6][2] != myPawn && samloc[6][2] != myKing){
		apex -= 1;
	}*/
		
		long blackKingLocs = myBoard.getBAb();
		long whiteKingLocs = myBoard.getFAw();
		long blackPieceLocs = myBoard.getFAb();
		long whitePieceLocs = myBoard.getBAw();
		
		long blackPawnLocs = blackPieceLocs & (blackKingLocs^allOnes);
		long whitePawnLocs = whitePieceLocs & (whiteKingLocs^allOnes);
		long kingLocs = blackKingLocs | whiteKingLocs; 
		boolean kingsOnBoard = (kingLocs != 0);
		long impulse62 = (long) 1L<<6;
		long impulse15 = (long) 1L<<30;
		long blackPawnAt15 = blackPawnLocs & impulse15;
		long blackPawnAt62 = blackPawnLocs & impulse62;
		long whitePawnAt15 = whitePawnLocs & impulse15;
		long whitePawnAt62 = whitePawnLocs & impulse62;
		long blackKingAt15 = blackKingLocs & impulse15;
		long blackKingAt62 = blackKingLocs & impulse62;
		long whiteKingAt15 = whiteKingLocs & impulse15;
		long whiteKingAt62 = whiteKingLocs & impulse62;
		long place = 0L;
		switch(myBoard.getWhoAmI()){
		case BLACK:
			//enemy king would be in FAw
			place =  whitePieceLocs; //any enemy
			place = place & impulse62; //look at square 6
			if (place!=0){apex -= 1;} //this accomplishes, any enemy at [6][2]
			else {
				place = whitePieceLocs; //any enemy
				place = place & impulse15;
				if (place!=0){apex -= 1;} //this accomplishes, any enemy at [1][5]
			} 
			//now want my pawns and my kings
			//no black piece at [6][2]
			if  (blackPawnAt15==0){} //myPawn at [1][5], done
			else if(blackKingAt15==0){}//myKing at [1][5], done
			else if(blackPawnAt62==0){}
			else if(blackKingAt62==0){}
			else {apex -= 1;}
			
			break;
		case WHITE:
			place =  blackPieceLocs; //any enemy
			place = place & impulse62; //look at square 6
			if (place!=0){apex -= 1;} //this accomplishes, any enemy at [6][2]
			else {
				place = blackPieceLocs; //any enemy
				place = place & impulse15;
				if (place!=0){apex -= 1;} //this accomplishes, any enemy at [1][5]
			} 
			//now want my pawns and my kings
			//no black piece at [6][2]
			if  (whitePawnAt15==0){} //myPawn at [1][5], done
			else if(whiteKingAt15==0){}//myKing at [1][5], done
			else if(whitePawnAt62==0){}
			else if(whiteKingAt62==0){}
			else {apex -= 1;}		
		}//end switch
		return apex;	
	}//end evalApex
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*Back Row Bridge*/
	/* This is a defensive strategy. By leaving a piece on each of the two back row tiles specified below,
	 * the player is able to defend against incoming Pawns attempting to be Kinged 
	 */
	/*int numEnemyKings = 0;
	for (int i=0; i<8; i++){
		for (int j=0; j<8; j++){
			if (samloc[i][j] == enemyKing){
				numEnemyKings += 1;
			}
		}
	}
	if (numEnemyKings == 0){
		backRowBridge += 1;
	}
	switch(whoAmI){
		case BLACK:
			if (samloc[7][6] == myPawn || samloc[7][6] == myKing){
				if (samloc[7][2] == myPawn || samloc[7][2] == myKing){
					backRowBridge += 1;
				}
			}
			break;
		
		case WHITE:
			if (samloc[0][1] == myPawn || samloc[0][1] == myKing){
				if (samloc[0][5] == myPawn || samloc[0][5] == myKing){
					backRowBridge += 1;
				}
			}
			break;
	}*/
	/* credit with 1 if there are no active kings
	 * and if the two bridge squares 1 and 3 or 30 and 32 are occupied by passive pieces
	 */
	public int evalBackRowBridge(){
		long blackKingLocs = myBoard.getBAb();
		long whiteKingLocs = myBoard.getFAw();
		long blackPieceLocs = myBoard.getFAb();
		long whitePieceLocs = myBoard.getBAw();
		long allOnes = (long) Math.pow(2, 36)-1;
		long blackPawnLocs = blackPieceLocs & (blackKingLocs^allOnes);
		long whitePawnLocs = whitePieceLocs & (whiteKingLocs^allOnes);
		long kingLocs = blackKingLocs | whiteKingLocs; 
		boolean kingsOnBoard = (kingLocs != 0);
		long impulse62 = (long) 1L<<6;
		long impulse15 = (long) 1L<<30;
		long impulse76 = (long) 1L<<32;//TODO what is this really? [7][6] doesn't look right
		long impulse72 = (long) 1L<<32;//TODO what is this really? [7][6] doesn't look right
		long impulse01 = (long) 1L<<1;//TODO what is this really? [0][1] doesn't look right
		long impulse05 = (long) 1L<<5;//TODO what is this really? [0][5] doesn't look right
		long blackPawnAt15 = blackPawnLocs & impulse15;
		long blackPawnAt62 = blackPawnLocs & impulse62;
		long whitePawnAt15 = whitePawnLocs & impulse15;
		long whitePawnAt62 = whitePawnLocs & impulse62;
		long blackKingAt15 = blackKingLocs & impulse15;
		long blackKingAt62 = blackKingLocs & impulse62;
		long whiteKingAt15 = whiteKingLocs & impulse15;
		long whiteKingAt62 = whiteKingLocs & impulse62;
		long place = 0L;
		int backRowBridge = 0;
		int kingCount = 0;
		   long places = 0L;
		//determine number of enemy kings
		switch(myBoard.getWhoAmI()){
		case BLACK:
            places = whiteKingLocs;
            for(int i=1; i<36; i++){
            	places = places >>1;
            	if(i%9 ==0){i++; places = places>>1;}
            	else{
            		kingCount += places%2;//if someone is there, add
            	}
            }//end for
            if(kingCount ==0){
            	backRowBridge++;
            }
            //if my piece at [7][6]and my piece at [7][2]
            if ((blackPieceLocs&impulse76&impulse72)!=0){backRowBridge++;}
			
			break;
		case WHITE:
			  places = blackKingLocs;
	            for(int i=1; i<36; i++){
	            	places = places >>1;
	            	if(i%9 ==0){i++; places = places>>1;}
	            	else{
	            		kingCount += places%2;//if someone is there, add
	            	}
	            }//end for
	            if(kingCount ==0){
	            	backRowBridge++;
	            }
	            //if my piece at [0][1] and my piece at [0][5]
	            if ((whitePieceLocs&impulse01&impulse05)!=0){backRowBridge++;}
				
			
		}//end switch
		
		return backRowBridge;
	}//end evalBackRowBridge
	
	/* credited with 1 for each of the following squares occupied by passive piece
	 * 
	 */
	
	public int[] evalCenterControl1(){
		/*Center Control*/
		/* I split this concept into 4 separate parameters: centralControl1, centralControl2, centralKingControl1,
		 * and centralKingControl2. centralControl1 and centralKingControl1 relate to the player's pieces while
		 * centralControl2 and centralKingControl2 relate to the enemy pieces. Each parameter is accordingly 
		 * credited with a point for each of that type of piece in on of the central tiles on the board (i.e. tiles
		 * from columns 2 through 6 and rows 2 through 6. There are 8 such tiles. 
		 */
		/*for (int i=0; i+2<6; i++){
			for( int j=0; j+2<6; j++){
				if (samloc[i][j] == myPawn){
					centralControl1 += 1;
				} else if (samloc[i][j] == enemyPawn){
					centralControl2 += 1;
				} else if (samloc[i][j] == myKing){
					centralKingControl1 += 1;
				} else if (samloc[i][j] == enemyKing){
					centralKingControl2 += 1;
				}
			}
		}*/
		long centerLocs = (long) Math.pow(2, 12)+
				(long) Math.pow(2,13)+
				(long) Math.pow(2,16)+
				(long) Math.pow(2,22)+
				(long) Math.pow(2,23)+
				(long) Math.pow(2,26)+
				(long) Math.pow(2,28);//TODO figure it out
		//Samuel gives 11,12,15,16,20,21,24 and 25, and he is using ones with factors of 9
		// in our notation, these are 12, 13, 16, 22, 23, 26, 28
		
		int firstLoc = 1;//TODO
		int lastLoc = 35;//TODO
		centralControl1 = 0;//my side
		centralControl2 = 0; //enemy
		centralKingControl1 = 0; //myside
		centralKingControl2 = 0; //enemy
		int[]retval = {0,0,0,0};
		
		switch(myBoard.getWhoAmI()){
		case BLACK:
           myPawns = myBoard.getFAb() & (allOnes^myBoard.getBAb());
           myKings = myBoard.getBAb();
           enemyPawns = myBoard.getBAw() &(allOnes^myBoard.getFAw());
           enemyKings = myBoard.getFAw();
			break;
		case WHITE:
			  myPawns = myBoard.getBAw() &(allOnes^myBoard.getFAw());
	          myKings = myBoard.getFAw();
	          enemyPawns = myBoard.getFAb() & (allOnes^myBoard.getBAb());
	          enemyKings = myBoard.getBAb();
				
			
		}//end switch
		
		//do the AND to select the pieces, then add them up
		long myCenterPawns = centerLocs & myPawns;
		centralControl1 = countTheOnes(myCenterPawns, firstLoc, lastLoc);
		long myCenterKings = centerLocs & myKings;
		centralControl2 = countTheOnes(myCenterKings, firstLoc, lastLoc);
		long enemyCenterPawns = centerLocs & enemyPawns;
		centralControl2 = countTheOnes(enemyCenterPawns, firstLoc, lastLoc);
		long enemyCenterKings = centerLocs & enemyKings;
		centralKingControl2 = countTheOnes(enemyCenterKings, firstLoc, lastLoc);
		
		retval[0]= centralControl1;
		retval[1]= centralControl2;
	    retval[2]=  centralKingControl1;
	    retval[3]= centralKingControl2;
		return retval;
		
	}//end CenterControl
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*center control 2
	 * 
	 */
	public int[] evalCenterControl2(){
		/*Center Control*/
		/* I split this concept into 4 separate parameters: centralControl1, centralControl2, centralKingControl1,
		 * and centralKingControl2. centralControl1 and centralKingControl1 relate to the player's pieces while
		 * centralControl2 and centralKingControl2 relate to the enemy pieces. Each parameter is accordingly 
		 * credited with a point for each of that type of piece in on of the central tiles on the board (i.e. tiles
		 * from columns 2 through 6 and rows 2 through 6. There are 8 such tiles. 
		 */
		/*for (int i=0; i+2<6; i++){
			for( int j=0; j+2<6; j++){
				if (samloc[i][j] == myPawn){
					centralControl1 += 1;
				} else if (samloc[i][j] == enemyPawn){
					centralControl2 += 1;
				} else if (samloc[i][j] == myKing){
					centralKingControl1 += 1;
				} else if (samloc[i][j] == enemyKing){
					centralKingControl2 += 1;
				}
			}
		}*/
		long centerLocs = (long) Math.pow(2, 12)+
				(long) Math.pow(2,13)+
				(long) Math.pow(2,16)+
				(long) Math.pow(2,22)+
				(long) Math.pow(2,23)+
				(long) Math.pow(2,26)+
				(long) Math.pow(2,28);//TODO figure it out
		//Samuel gives 11,12,15,16,20,21,24 and 25, and he is using ones with factors of 9
		// in our notation, these are 12, 13, 16, 22, 23, 26, 28
		
		int firstLoc = 1;//TODO
		int lastLoc = 35;//TODO
		centralControl1 = 0;//my side
		centralControl2 = 0; //enemy
		centralKingControl1 = 0; //myside
		centralKingControl2 = 0; //enemy
		int[]retval = {0,0,0,0};
		
		switch(myBoard.getWhoAmI()){
		case BLACK:
           myPawns = myBoard.getFAb() & (allOnes^myBoard.getBAb());
           myKings = myBoard.getBAb();
           enemyPawns = myBoard.getBAw() &(allOnes^myBoard.getFAw());
           enemyKings = myBoard.getFAw();
			break;
		case WHITE:
			  myPawns = myBoard.getBAw() &(allOnes^myBoard.getFAw());
	          myKings = myBoard.getFAw();
	          enemyPawns = myBoard.getFAb() & (allOnes^myBoard.getBAb());
	          enemyKings = myBoard.getBAb();
				
			
		}//end switch
		
		//do the AND to select the pieces, then add them up
		long myCenterPawns = centerLocs & myPawns;
		centralControl1 = countTheOnes(myCenterPawns, firstLoc, lastLoc);
		long myCenterKings = centerLocs & myKings;
		centralControl2 = countTheOnes(myCenterKings, firstLoc, lastLoc);
		long enemyCenterPawns = centerLocs & enemyPawns;
		centralControl2 = countTheOnes(enemyCenterPawns, firstLoc, lastLoc);
		long enemyCenterKings = centerLocs & enemyKings;
		centralKingControl2 = countTheOnes(enemyCenterKings, firstLoc, lastLoc);
		
		retval[0]= centralControl1;
		retval[1]= centralControl2;
	    retval[2]=  centralKingControl1;
	    retval[3]= centralKingControl2;
		return retval;
		
	}//end CenterControl
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
		switch(me){
		case  WHITE:
			opponent= Move.Side.BLACK;
			movers = myBoard.getFAb() | myBoard.getBAb();
			break;
		case  BLACK:
			opponent= Move.Side.WHITE;
			movers = myBoard.getFAw() | myBoard.getBAw();
		}
		//TODO material credit for active is 6 or less
		if(myMaterialCredit<=6){doubleCornerCredit++;}
		//TODO opponent is ahead in material credit
		if(evalMaterialCredit(opponent)>myMaterialCredit){doubleCornerCredit++;}
		//TODO active side can move into one of the double corner squares
		//looked at www.checkerschest.com/play-checkers-online/fundamentals3.htm to find what is a double corner square
		//they say 1,5,32,28 are those. Our numbers for those squares are 31, 35 and 1,5
		if( ((movers & doubleCorners<<4) !=0) |
			((movers & doubleCorners<<5) !=0) |
			((movers & doubleCorners>>4) !=0) |
			((movers & doubleCorners>>5) !=0)){doubleCornerCredit++;}
	//TODO if (myMaterialCredit > enemyMaterialCredit){doubleCornerCredit += 1;}//Nicholas I'm not seeing this in Samuel's text. Am I missing sthg?
	//TODO if (enemyMaterialCredit < 7){doubleCornerCredit += 1;}//Nicholas I'm not seeing this in Samuel's text. Am I missing sthg?
		return doubleCornerCredit;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * CRAMP
	 * credited with 2 if the passive side occupies the cramping aquare, 13 for black and 20 for white
	 * and at least one other nearby square 9 or 14 for black, 19 or 24 for white
	 * while certain others, 17,21,22,25 for black 8.11.12.16 for white
	 * are all occupied by the active side
	 */
	
	public int evalCramp(){
		int cramp=0;
		//p.228 credited with 2 if passive side occupies the cramping square, i.e. 13 for black and 20 for white
		//to me it looks that either interpretation is cramping
		//TODO resolve ambiguity about where 14, where 22 belong
		long crampingSquare = 0L;
		long nearbySquare = 0L;
		long certainOccupiedSquares = 0L;
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			crampingSquare = crampingToWhite; //I'm white, opponent is black, notice 13->14, deliberate notation adjustment
			nearbySquare = nearbyCrampingToWhite;
			certainOccupiedSquares = certainSquaresWhite;
			if((crampingSquare != 0) && (countTheOnes(nearbySquare,21,26)>1) && (countTheOnes(certainOccupiedSquares & myBoard.getBAw(), 8,17)>= 4)){cramp+=2;}
			break;
		case  BLACK:
			crampingSquare = crampingToBlack;//I'm black, opponent is white, notice 20->22, deliberate 
			nearbySquare = nearbyCrampingToBlack;
			certainOccupiedSquares = certainSquaresBlack;
			if((crampingSquare != 0) && (countTheOnes(nearbySquare,10,15)>1) && (countTheOnes(certainOccupiedSquares & myBoard.getFAb(), 19,28)>= 4)){cramp+=2;}
			
		}
 		return cramp;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	/* DENY
	 * credited with 1 for each square defined in MOB 
	 * if on the next move a piece occupying this square could be captured without an exchange
	 */
	
	public int evalDenialOccupancy(){//TODO do after MOB
		int denies=0;
		//credited 1 for each square in MOB, if piece in this square could be captured without exchange in next move
		long captives=0L;
		long potentialCaptives=0L;
		
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			
			 potentialCaptives = 0L;//TODO
			break;
		case  BLACK:
	 
			
		}
		 potentialCaptives = 0L;//TODO
		return denies;
	}
	////////////////////////////////////////////////////////////////////////////
	/* DIA
	 * credit with 1 for each passive piece located inthe diagonal files terminating in the double corner squares
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
	/* DIA
	 * credited with 1 for each passive piece located in the diagonal file termination in the double corner squares
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
	public int evalExpos(){
		int expos = 0;
		//credited 1 for each passive piece that is flanked along one or the other diagonal by two empty squares
		long passives = 0L;
		long emptyLoc = myBoard.getEmpty();
		Move.Side me = myBoard.getWhoAmI();
		switch(me){
		case  WHITE:
			//passives are white	
			passives =  myBoard.getBAw(); //pawns and kings
			break;
		case  BLACK:
			passives = myBoard.getFAb();
		}
		//want both sides of either diagonal empty
		expos += countTheOnes(passives & emptyLoc<<4 & emptyLoc >> 4, 6,30);
		expos += countTheOnes(passives & emptyLoc<<5 & emptyLoc >>5,6,30);
		
		return expos;
	}
	public int evalFork(){
		int fork = 0;
		return fork;
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
		return (int) weights[0]*materialCredit
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
				;
		
		 
	}
	public void setWeight(int which, double what){
		this.weights[which]=what;
	}
	public double getWeight(int which){
		return this.weights[which];
	}
	   public int getPieceAdvantage(){
		   long FAb = myBoard.getFAb();
		   long BAb = myBoard.getBAb();
		   long BAw = myBoard.getBAw();
		   long FAw = myBoard.getFAw();
		   
	    	int adv= 0;
	    	//count up pieces of whoAmI side
	    	long beCounted = 0L;
	    	switch(myBoard.getWhoAmI()){
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
	   public double averageOfTheCoefficients(){
		   double averageWeight = 0;
		   for(int i=0; i<howManyWeights;i++){
			   averageWeight+=weights[i];
		   }
		   averageWeight = averageWeight / howManyWeights;
		   return averageWeight;
	   }
	
}

