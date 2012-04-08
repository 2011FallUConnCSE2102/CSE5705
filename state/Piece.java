package state;

import actions.Move;

public class Piece {
	Move.Side color;
	public enum Rank {
		PAWN, KING
	};
	Rank rank;
	int samLoc;
	public void Piece(){
		this.rank = Rank.PAWN;
	}
	public void setColor(Move.Side c){
		this.color = c;
	}
	public Move.Side getColor(){
		return this.color;
	}
	public void setRank(Rank r){
		this.rank = r;
	}
	public Rank getRank(){
		return this.rank;
	}
	public void setSamLoc(int r){
		this.samLoc = r;
	}
	public int getSamLoc(){
		return this.samLoc;
	}

}
