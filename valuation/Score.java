package valuation;

import state.*;

public class Score {
	
public Score(){
	
}
	//how are we going to do scoring?
	public int score(Board bd, ScoreExpression se){
		//TODO sum the weighted features of the board
		int score = se.score(bd);
		return score;
	}
	

}
