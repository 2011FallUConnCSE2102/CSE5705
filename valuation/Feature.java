package valuation;

import state.*;

public class Feature {
	
	private enum kindOfFeature {
		PIECE_RATIO, POSITIONAL_ADVANTAGE, RELATIVE_PIECE_ADVANTAGE, ABILITY2MOVE
	}
	//ability to move is tested for, by Samuel, separately and not included in scoring polynomial
	//relative piece advantage has to be positive, or will "learn" to give away pieces p.212
	//
	private kindOfFeature myKind = kindOfFeature.PIECE_RATIO;
	private int value = 0;
	
	public Feature(){
		
	}
	public void setKind(kindOfFeature k){
		myKind = k;
	}
	public kindOfFeature getKind(){
		return myKind;
	}
	public void setValue(int v){
		value = v;
	}
	public int getValue(){
		return value;
	}
	public int evalFeature(Board bd){
		int result = 0;
		//TODO so, how is this done?
		
		return result;
	}

}
