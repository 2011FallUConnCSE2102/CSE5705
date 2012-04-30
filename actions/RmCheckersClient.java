package actions;


	//RmCheckersClient.java is a client that interacts with Sam, a checkers 
	//server. It is designed to illustrate how to communicate with the server
	//in a minimal way.  It is not meant to be beautiful java code.
	//Given the correct machine name and port for the server, a user id, and a 
	//password (_machine, _port, _user, and _password in the code), running 
	//this program will initiate connection and start a game with the default 
	//player. (the _machine and _port values used should be correct, but check
	//the protocol document.)
	//
	//the program has been tested and used under Java 5.0 and 6.0, but probably 
	//would work under older or newer versions.
	//
	//Copyright (C) 2008 Robert McCartney

	//This program is free software; you can redistribute it and/or
	//modify it under the terms of the GNU General Public License as
	//published by the Free Software Foundation; either version 2 of the
	//License, or (at your option) any later version.

	//This program is distributed in the hope that it will be useful, but
	//WITHOUT ANY WARRANTY; without even the implied warranty of
	//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	//General Public License for more details.

	//You should have received a copy of the GNU General Public License
	//along with this program; if not, write to the Free Software
	//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
	//USA

	import java.io.*;
	import java.net.*;
	import topLevel.*;
import valuation.SetOfBoardsWithPolynomial;
import state.*;
import persistence.DBHandler;

	public class RmCheckersClient {

	 private final static String _user = "3";  // need legit id here
	 private final static String _password = "518760";  // need password here
	 private final static String _opponent = "4";
	 private final String _machine  = "icarus2.engr.uconn.edu"; 
	 private int _port = 3499;
	 private Socket _socket = null;
	 private PrintWriter _out = null;
	 private BufferedReader _in = null;

	 private String _gameID;
	 private String _myColor;
	 
	 private int timeToGo=0;
  
	 
	
	 

	 public RmCheckersClient(){	
		_socket = openSocket();
	 }

	 public Socket getSocket(){
		return _socket;
	 }

	 public PrintWriter getOut(){
		return _out;
	 }

	 public BufferedReader getIn(){
		return _in;
	 }
	  
	 public void setGameID(String id){
		_gameID = id;
	 }
	 
	 public String getGameID() {
		return _gameID;
	 }

	 public void setColor(String color){
		_myColor = color;
	 }
	 
	 public String getColor() {
		return _myColor;
	 }

	 
	 public static void main(String[] argv){
		 
		 System.gc();
		String readMessage;
		
		 DBHandler db = new DBHandler(true);
		 SetOfBoardsWithPolynomial history = new SetOfBoardsWithPolynomial();
		 boolean alphaBeta = true;//true for alpha, 
		 boolean usingSimAnneal = true;
		 VisualBoard visual = new VisualBoard();
		 Board bd = new Board(db, alphaBeta, history, visual);
		 
		 int rand = (int) (Math.random()*7);
		 switch (rand){
		 case 0: bd.setFirstBlackMove(new StringBuffer("(5:1):(4:0)")); break;
		 case 1: bd.setFirstBlackMove(new StringBuffer("(5:1):(4:2)")); break;
		 case 2: bd.setFirstBlackMove(new StringBuffer("(5:3):(4:2)")); break;
		 case 3: bd.setFirstBlackMove(new StringBuffer("(5:3):(4:4)")); break;
		 case 4: bd.setFirstBlackMove(new StringBuffer("(5:5):(4:4)")); break;
		 case 5: bd.setFirstBlackMove(new StringBuffer("(5:5):(4:6)")); break;
		 case 6: bd.setFirstBlackMove(new StringBuffer("(5:7):(4:6)")); break;
		 default: bd.setFirstBlackMove(new StringBuffer("(5:3):(4:2)"));
		 }
		 for(int i=0; i< DBHandler.NUMPARAMS; i++){
		 bd.myEvaluator.setWeight(i,0);
		 }
		 bd.myEvaluator.setWeight(DBHandler.NODE, 0-(2<<2) );//from graph
		 bd.myEvaluator.setWeight(DBHandler.OREO,(2<<2) ); //from graph
		 bd.myEvaluator.setWeight( DBHandler.MOC3, (2<<4)); //from graph
		 bd.myEvaluator.setWeight( DBHandler.THRET, (2<<5)); //from graph, also p.229
		 bd.myEvaluator.setWeight(DBHandler.MOVE,(2<<8) );  //from graph and p.229
		 bd.myEvaluator.setWeight(DBHandler.KCENT,(2<<16) ); //from graph is 14, page 229 has this as 16
		 bd.myEvaluator.setWeight(DBHandler.MOC2, 0-(2<<18) ); //from page 229
		 bd.myEvaluator.setWeight(DBHandler.MOC4, 0-(2<<14) ); //from page 229
		 bd.myEvaluator.setWeight(DBHandler.MODE3,0-(2<<13) ); //from page 229
		 bd.myEvaluator.setWeight(DBHandler.DEMMO, 0-(2<<11) ); //from page 229
		 bd.myEvaluator.setWeight(DBHandler.ADV, 0-(2<<8) ); //from page 229
		 bd.myEvaluator.setWeight(DBHandler.MODE2, 0-(2<<8) ); //from page 229
		 bd.myEvaluator.setWeight(DBHandler.BACK, 0-(2<<6) ); //from page 229
		 bd.myEvaluator.setWeight( DBHandler.CNTR,(2<<5)); //from page 229
		 bd.myEvaluator.setWeight( DBHandler.MOC3,(2<<4)); //from page 229
		 bd.myEvaluator.setWeight( DBHandler.RECAP,(2<<14));// this is kings in center, arbitrary, just made it up
		 
		 bd.myEvaluator.setWeight( DBHandler.PADV,(2<<10)); //from page 229
		 
		 //TODO get alpha or beta mode from command line
		
		 
		 
		RmCheckersClient myClient = new RmCheckersClient();
		CheckersLearningAgent cla = new CheckersLearningAgent(db, alphaBeta, bd, usingSimAnneal);
		Player p = new Player();
		String answer;
		 
		try{
			//System.err.println("Before readAndEcho");
		    myClient.readAndEcho(); // start message
		    //myClient.readNoEcho(); // start message
		  //TODO be sure to use version without echo when done debugging
		  //TODO be sure to use version without echo when done debugging
		    //myClient.readAndEcho(); // ID query
		    myClient.readNoEcho(); // ID query
		  //TODO be sure to use version without echo when done debugging
		    
		    myClient.writeMessage(_user); // user ID
		  //TODO be sure to use version without echo when done debugging
		    
		    //myClient.readAndEcho(); // password query
		    myClient.readNoEcho(); // password query
		  //TODO be sure to use version without echo when done debugging
		    myClient.writeMessage(_password);  // password

		   // myClient.readAndEcho(); // opponent query
		    myClient.readNoEcho(); // opponent query
		  //TODO be sure to use version without echo when done debugging
		    myClient.writeMessage(_opponent);  // opponent
		  //TODO be sure to use version without echo when done debugging

		    myClient.setGameID(myClient.readAndEcho().substring(5,9)); // game sometimes out of range error
		  //TODO be sure to use version without echo when done debugging
		    myClient.setColor(myClient.readAndEcho().substring(6,11));  // color
		  //TODO be sure to use version without echo when done debugging
		    p.setSideFromString(myClient.getColor()); //Player p knows its color
		    System.out.println("I am playing as "+myClient.getColor()+ " in game number "+ myClient.getGameID());
		    answer = cla.init(myClient.getColor()); //CheckersLearningAgent knows its color
		   
		    //if (myClient.getColor().equals("White")) { //next thing server does is send a move
		    	
		    	boolean itsOver = false;
			    while(!itsOver){
			    	readMessage = myClient.readAndEcho();  // depends on color--a black move if i am white, Move:Black:i:j
			    	//readMessage = myClient.readNoEcho();  // depends on color--a black move if i am white, Move:Black:i:j
			    	//TODO be sure to use version without echo when done debugging
			    	StringBuffer server_sb = new StringBuffer(readMessage);
			    	//this could be move by server, or error, or result, or query, but not move by me, which will be consumed elsewhere
			    	char msgType = server_sb.charAt(0);
			    	switch (msgType){
			    		case 'R':
			    			//process result, extract learning
			    			cla.learnFromExperience(server_sb, myClient.getColor());
			    			//was it a win or a lose Result:B or Result:W
			    			switch(server_sb.charAt(7)){
			    				case 'D'://draw
			    					break;
			    				case 'W'://White
			    					break;
			    				case 'B'://Black
			    					break;
			    					default: System.err.println("RmCheckersClient::main, unexected result message"+server_sb);
			    			}
			    			
			    			//update database
			    			itsOver = true;
			    			System.gc();
			    			break;
			    		case 'E':
			    			//process error, 
			    			System.err.println("RmCheckersClient:: InGameLoop: server said error" +readMessage);
			    			itsOver = true;
			    			System.gc();
			    			break;
			    		case 'M':
			    			//this is server move because my move echo is consumed after I make it
			    			//remember to convert moves to and from Samuel's form
			    			//a move has been made, need to figure out response, but don't send yet
			    			//want to change the server notation into Move, which is list of steps
			    			answer = cla.acceptMoveAndRespond(server_sb);
			    			//System.err.println("RmCheckers::loop: "+answer);
			    			//now, wait to be asked for move
			    			break;
			    		case '?':
			    			//this is server prompting for a move, send prepared answer
			    			cla.howMuchTTG(server_sb);
			    			myClient.writeMessage(answer);
			    			readMessage = myClient.readAndEcho(); //here consume the server's echo of my move, which always occurs
			    			//readMessage = myClient.readNoEcho(); //here consume the server's echo of my move, which always occurs
			    			//TODO be sure to use version without echo when done debugging
			    			break;
			    		default:
			    			System.err.println("RmCheckersClient:: InGameLoop: unknown message" +readMessage);
			    			break;
		    	}//end of switch construct
			   }//while it's not over

		   // }
/*		    else {  //I'm playing black, so next thing server does is send a query
		    	
			myClient.writeMessageAndEcho("(5:3):(4:4)");
			readMessage = myClient.readAndEcho();  // black move
			readMessage = myClient.readAndEcho();  // white move
			readMessage = myClient.readAndEcho();  // move query
			// here you would need to move again
		    }*/
		   
		    myClient.getSocket().close();
		} catch  (IOException e) {
		    System.out.println("Failed in read/close");
		    System.exit(1);
		}
	 }

	 public String readAndEcho() throws IOException
	 {
		
		//System.err.println("before first read");
		String readMessage = _in.readLine();
		System.err.println("read: "+readMessage);
		return readMessage;
	 }
	 public String readNoEcho() throws IOException
	 { 
		String readMessage = _in.readLine();
		//System.err.println("read: "+readMessage);
		return readMessage;
	 }

	 public void writeMessage(String message) throws IOException
	 {
		_out.print(message+"\r\n");  
		_out.flush();
	 }

	 public void writeMessageAndEcho(String message) throws IOException
	 {
		_out.print(message+"\r\n");  
		_out.flush();
		System.out.println("sent: "+ message);
	 }
				       
	 public  Socket openSocket(){
		//Create socket connection, adapted from Sun example
		try{
	    _socket = new Socket(_machine, _port);
	    _out = new PrintWriter(_socket.getOutputStream(), true);
	    _in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
	  } catch (UnknownHostException e) {
	    System.out.println("Unknown host: " + _machine);
	    System.exit(1);
	  } catch  (IOException e) {
	    System.out.println("No I/O");
	    System.exit(1);
	  }
	  return _socket;
	}


}
