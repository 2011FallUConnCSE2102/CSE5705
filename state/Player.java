package state;

import actions.Move;

public class Player {
	private Move.Side mySide = Move.Side.BLACK; 
	public Player(){
		
	}
	public Player(String color){
		char[] theChars = color.toCharArray();
		char theChar= theChars[0];
		switch(theChar){
		case 'W':
			mySide =  Move.Side.WHITE;
			//break;
		}
	}//end constructor
	public Move.Side getSide(){
		return mySide;
	}
	public void setSide(Move.Side side){
		mySide = side;
	}
	public String getSideAsString(){
		StringBuffer answer = new StringBuffer("Black");
		if ( mySide == Move.Side.WHITE){
			answer.replace(0, 5, "White");
		}
		return answer.toString();
	}
	public void setSideFromString(String color){
		char[] theChars = color.toCharArray();
		char theChar= theChars[0];
		switch(theChar){
		case 'W':
			mySide =  Move.Side.WHITE;
			//break;
	}}

}
