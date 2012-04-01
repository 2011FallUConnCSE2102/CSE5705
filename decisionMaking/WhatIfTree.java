package decisionmaking;

public class WhatIfTree {
	
	CheckersTreeNode root =null;
	
	public WhatIfTree(){
		
	}
	public WhatIfTree(CheckersTreeNode n){
		root = n;
	}
	public CheckersTreeNode getRoot(){
		return root;
	}
	public void setRoot(CheckersTreeNode n){
		root = n;
	}
	

}
