package valuation;

public class chooseBestMove2 {


	/* You can pretty much ignore everything from here until the next comment. This is all just stuff that I
	 * added in to make all of the error notifications disappear, since I wasn't working with the actual code
	 * while writing these.
	 */
	

	
	private int whoAmI;
	private char myPawn;
	private char enemyPawn ; 
	private char myKing;
	private char enemyKing;
	private char empty;
	
	private int numMyPawns;
	private int numMyKings;
	private int numEnemyPawns;
	private int numEnemyKings;
	private int materialCredit, myMaterialCredit, enemyMaterialCredit;
	
	private int[][] samloc ={
			{32, -1, 33, -1, 34, -1, 35, -1},//row 0
			{-1, 28, -1, 29, -1, 30, -1, 31},//row 1
			{23, -1, 24, -1, 25, -1, 26, -1},//row 2
			{-1, 19, -1, 20, -1, 21, -1, 22},//row 3
			{14, -1, 15, -1, 16, -1, 17, -1},//row 4
			{-1, 10, -1, 11, -1, 12, -1, 13},//row 5
			{5, -1, 6, -1, 7, -1, 8, -1},//row 6
			{-1, 1, -1, 2, -1, 3, -1, 4}//row 7
			};
	
	/* This is just a list of all of the parameters mentioned at the end of the Samuel paper.*/
	private int advancement, apex, backRowBridge, centralControl1, centralControl2, doubleCornerCredit, 
				cramp, denialOfOccupancy, doubleDiagonalFile, diagonalMomentValue, dyke, exchange, 
				exposure, threatOfFork, gap, backRowControl, hole, centralKingControl1, centralKingControl2, 
				totalEnemyMobility, undeniedEnemyMobility, move, node, triangleOfOreos, pole, threat;
	
	/* I gave each of the parameters a positive or negative coefficient based on my understanding of them.
	 * The values of the coefficients will definitely need to be adjusted.
	 * A gameState with a high value is desirable. The higher the gamestate, the larger our advantage.
	 */
	private int gameState = materialCredit
							+ advancement 
							+ apex 
							+ backRowBridge 
							+ 2*(centralControl1 - centralControl2) 
							+ doubleCornerCredit 
							+ cramp 
							+ denialOfOccupancy 
							+ doubleDiagonalFile 
							+ diagonalMomentValue 
							+ dyke 
							- exchange 
							+ exposure 
							- threatOfFork 
							- gap 
							+ backRowControl
							+ hole
							+ 3*(centralKingControl1 - centralKingControl2) 
							- totalEnemyMobility
							- undeniedEnemyMobility
							+ move
							+ node
							+ triangleOfOreos
							+ pole
							- threat
							;
	
	
	/* For details on any specific parameter, refer to the last 2 pages of the Samuel paper. They don't explicitly describe the motivation
	 * or strategy behind each parameter, but they do indicate how each parameter is scored, giving an idea on how to code them.
	 */
	/* Here's a constructor with a board parent furnished
	 * 
	 */
	

	
	public chooseBestMove2(){
	
	/*Material Credit*/
			/*This is not a parameter from Samuel's paper. I added this as a potentially temporary paramater for initial evaluation.*/
			
			/* This parameter credits us with 2 points per Pawn we control and 3 points per King we control. Likewise, it credits the 
			 * opponent with 2 points per Pawn and 3 points per King they control. The Total Material Credit (materialCredit) is the 
			 * difference between our material credit (myMaterialCredit) and the opponent's (opponentMaterialCredit).
			 */
		for (int i=0; i<8; i++){
			for (int j=0; j<8; j++){
				if (samloc[i][j] == myPawn){numMyPawns += 1;}
				if (samloc[i][j] == myKing){numMyKings += 1;}
				if (samloc[i][j] == enemyPawn){numEnemyPawns += 1;}
				if (samloc[i][j] == enemyKing){numEnemyKings += 1;}
			}
		}
		myMaterialCredit = 2*numMyPawns + 3*numMyKings;
		enemyMaterialCredit = 2*numEnemyPawns + 3*numEnemyKings;
		materialCredit = myMaterialCredit + enemyMaterialCredit;
		
	/*Advancement*/
			/* This parameter increases the more you advance foward across the board, giving +1 for each piece you control
			 * in rows 2 and 3 (if BLACK) and -1 for each piece you control in rows 4 and 5.
			 * 
			 * Note: This parameter assumes that BLACK starts at the bottom of the board and WHITE starts at the top.
			 */
/*		switch(whoAmI){
		case BLACK:
			for (int j=0; j<8; j++){
				for( int i=0; i+2<4; i++){
					if (samloc[i][j] == myPawn || samloc[i][j] == myKing){advancement += 1;}	
				}
				for (int i=0; i+4<6; i++){
					if (samloc[i][j] == myPawn || samloc[i][j] == myKing){advancement -= 1;}
				}
			}
			break;
		case WHITE:
			for (int j=0; j<8; j++){
				for (int i=0; i+4<6; i++){
					if (samloc[i][j] == myPawn || samloc[i][j] == myKing){advancement += 1;}	
				}
				for (int i=0; i+2<4; i++){
					if (samloc[i][j] == myPawn || samloc[i][j] == myKing){advancement -= 1;}
				}
			}
			break;
		}*/
		
	/*Apex*/
		/* I'm honestly not quite sure what this parameter is for. You can read how they explain this parameter 
		 * in the Samuel paper, but beyond that I'm not sure how useful this parameter is. 
		 */
	/*	boolean kingsOnBoard = false;
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
		
	/*Back Row Bridge*/
		/* This is a defensive strategy. By leaving a piece on each of the two back row tiles specified below,
		 * the player is able to defend against incoming Pawns attempting to be Kinged 
		 */
	/*	int numEnemyKings = 0;
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
		
	/*Center Control*/
		/* I split this concept into 4 separate parameters: centralControl1, centralControl2, centralKingControl1,
		 * and centralKingControl2. centralControl1 and centralKingControl1 relate to the player's pieces while
		 * centralControl2 and centralKingControl2 relate to the enemy pieces. Each paramter is accordingly 
		 * credited with a point for each of that type of piece in on of the central tiles on the board (i.e. tiles
		 * from columns 2 through 6 and rows 2 through 6. There are 8 such tiles. 
		 */
		for (int i=0; i+2<6; i++){
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
		}
		
	/*Double-Corner Credit*/
		/* The code for this parameter is currently unfinished. */
		if (myMaterialCredit > enemyMaterialCredit){doubleCornerCredit += 1;}
		if (enemyMaterialCredit < 7){doubleCornerCredit += 1;}
		
		
	/*Cramp*/
		
		
	/*Denial of Occupancy*/
		
		
	/*Double Diagonal File*/
		
		
	/*Diagonal Moment Value*/
		
		
	/*Dyke*/
		
		
	/*Exchange*/
		
		
	/*Exposure*/
		
		
	/*Threat of Fork*/
		
		
	/*Gap*/
		
		
	/*Back Row Control*/
		
		
	/*Hole*/
		
		
	/*Total Enemy Mobility*/
			/* Add 1 to this parameter for every square that an enemy piece can move to normally (i.e. without jumping any pieces)
			 * 
			 * Note: Assumes BLACK plays from the bottom of the board and WHITE plays from the top of the board.
			 */
	/*	switch(whoAmI){
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
			}
			for (int i=0; i<7; i++){
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
		
	/*Undenied Enemy Mobility*/
		
		
	/*Move*/
		
		
	/*Node*/
		
		
	/*Triangle of Oreos*/

		
	/*Pole*/
		
		
	/*Threat*/
		
		
	
		
	}
	
	
}



