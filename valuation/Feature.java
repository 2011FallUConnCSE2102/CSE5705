package valuation;

import state.*;

public class Feature {
	
	private enum kindOfFeature {
		PIECE_RATIO, POSITIONAL_ADVANTAGE, ABILITY2MOVE
	}
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
