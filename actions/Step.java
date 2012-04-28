package actions;

import state.Piece;

public class Step {
	//a move can have multiple steps, all completed in one turn
	int startLocation;
	int endLocation;
	Piece.Rank rankAtStart;
	Piece.Rank rankAtEnd;
	public Step(){
		
	}
	public Step (int start, int end){
		this.startLocation = start;
		this.endLocation = end;
	}
	public Step (Step s){
		this.startLocation = s.startLocation;
		this.endLocation = s.endLocation;
		this.rankAtStart = s.rankAtStart;
		this.rankAtEnd = s.rankAtEnd;
	}
	public int getStartLocation(){
		return this.startLocation;
	}
	public int getEndLocation(){
		return this.endLocation;
	}
	public void setStartLocation(int start){
		this.startLocation=start;
	}
	public void setEndLocation(int end){
		if((end%9)==0){System.err.println("Step::setEnd"+end);}
		this.endLocation= end;
	}
	public String toString(){
		Integer help_I = new Integer(startLocation);
		//System.err.println("Step::toString: startLocation "+startLocation);
		StringBuffer step_sb = new StringBuffer();
		step_sb.append('('); //append the beginning parenthesis
		step_sb.append(help_I.toString());
		step_sb.append(':');
		Integer help_E = new Integer(endLocation);
		//System.err.println("Step::toString: endLocation "+endLocation);
		step_sb.append(help_E.toString());
		step_sb.append(')');//append the end parenthesis
		//System.err.println("Step::toString: step "+step_sb);
		return step_sb.toString();
	}

}
