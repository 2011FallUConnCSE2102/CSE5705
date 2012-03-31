package actions;

public class Step {
	//a move can have multiple steps, all completed in one turn
	int startLocation;
	int endLocation;
	public Step(){
		
	}
	public Step (int start, int end){
		this.startLocation = start;
		this.endLocation = end;
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
		this.endLocation= end;
	}
	

}
