package valuation;
/* This code is adapted from some by Nicholas Robitaille, 
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
	
	public Evaluator (Board bd){
		this.myBoard = bd;
		this.whoAmI = bd.getWhoAmI();
	}
	/*Material Credit*/
	/*This is not a parameter from Samuel's paper. I added this as a potentially temporary paramater for initial evaluation.*/
	
	/* This parameter credits us with 2 points per Pawn we control and 3 points per King we control. Likewise, it credits the 
	 * opponent with 2 points per Pawn and 3 points per King they control. The Total Material Credit (materialCredit) is the 
	 * difference between our material credit (myMaterialCredit) and the opponent's (opponentMaterialCredit).
	 */
	public int evalMaterialCredit(){
		
	int materialCredit = 0;
	numMyPawns=0; //initialize
	numMyKings=0; //initialize
	movers = 0L;
	mover= 0L;
	numEnemyPawns=0; //initialize
	numEnemyKings=0; //initialize
	
	switch(whoAmI){
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
	switch(whoAmI){
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
	switch(whoAmI){
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
	
	public int evalApex(){
		/*Apex*/
		int apex = 0;//haven't read to see whether it should be init'ed to 0
	
	/* I'm honestly not quite sure what this parameter is for. You can read how they explain this parameter 
	 * in the Samuel paper, but beyond that I'm not sure how useful this parameter is. 
	 */
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
	
	public int evalCenterControl(int which){
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
		long centerLocs = 32;//TODO figure it out
		int firstLoc = 1;//TODO
		int lastLoc = 35;//TODO
		int centralControl1 = 0;//my side
		int centralControl2 = 0; //enemy
		int centralKingControl1 = 0; //myside
		int centralKingControl2 = 0; //enemy
		
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
		
		if(which==0){return centralControl1;}
		else if (which ==1) {return centralControl2;}
	    else if (which ==2) {return  centralKingControl1;}
	    else {return centralKingControl2;}
		
		
	}//end CenterControl
	
	/*Double-Corner Credit*/
	/* The code for this parameter is currently unfinished. */
	//TODO if (myMaterialCredit > enemyMaterialCredit){doubleCornerCredit += 1;}
	//TODO if (enemyMaterialCredit < 7){doubleCornerCredit += 1;}
	
	
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



	
	public int countTheOnes(long places, int start, int end){
		int howMany = 0;
		 for(int i=start; i<end; i++){
         	places = places >>1;
         	if(i%9 ==0){i++; places = places>>1;}
         	else{
         		howMany += places%2;//if someone is there, add
         	}
         }//end for
		
		return howMany;
	}
	public int weightedSum(int[] theFeatureValues){
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
	
}
