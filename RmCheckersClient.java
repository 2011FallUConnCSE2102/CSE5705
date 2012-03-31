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
	import parsing.*;
	import state.*;

	public class RmCheckersClient {

	 private final static String _user = "3";  // need legit id here
	 private final static String _password = "518760";  // need password here
	 private final static String _opponent = "0";
	 private final String _machine  = "icarus2.engr.uconn.edu"; 
	 private int _port = 3499;
	 private Socket _socket = null;
	 private PrintWriter _out = null;
	 private BufferedReader _in = null;

	 private String _gameID;
	 private String _myColor;
	 

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
		String readMessage;
		 System.err.println("I am in main");
		RmCheckersClient myClient = new RmCheckersClient();
		CheckersLearningAgent cla = new CheckersLearningAgent();
		Player p = new Player();
		StringBuffer answer= new StringBuffer("");
		 System.err.println("I have initiated a client");
		try{
			System.err.println("Before readAndEcho");
		    myClient.readAndEcho(); // start message
		  //TODO be sure to use version without echo when done debugging
		  //TODO be sure to use version without echo when done debugging
		    myClient.readAndEcho(); // ID query
		  //TODO be sure to use version without echo when done debugging
		    System.err.println("I have tried to readAndEcho");
		    myClient.writeMessageAndEcho(_user); // user ID
		  //TODO be sure to use version without echo when done debugging
		    
		    myClient.readAndEcho(); // password query 
		  //TODO be sure to use version without echo when done debugging
		    myClient.writeMessage(_password);  // password

		    myClient.readAndEcho(); // opponent query
		  //TODO be sure to use version without echo when done debugging
		    myClient.writeMessageAndEcho(_opponent);  // opponent
		  //TODO be sure to use version without echo when done debugging

		    myClient.setGameID(myClient.readAndEcho().substring(5,9)); // game 
		  //TODO be sure to use version without echo when done debugging
		    myClient.setColor(myClient.readAndEcho().substring(6,11));  // color
		  //TODO be sure to use version without echo when done debugging
		    p.setSideFromString(myClient.getColor()); //Player p knows its color
		    System.out.println("I am playing as "+myClient.getColor()+ " in game number "+ myClient.getGameID());
		    answer = cla.init(myClient.getColor()); //CheckersLearningAgent knows its color
		   
		   // if (myClient.getColor().equals("White")) { //next thing server does is send a move
		    	
		    	boolean itsOver = false;
			    while(!itsOver){
			    	readMessage = myClient.readAndEcho();  // depends on color--a black move if i am white, Move:Black:i:j
			    	//TODO be sure to use version without echo when done debugging
			    	StringBuffer server_sb = new StringBuffer(readMessage);
			    	//this could be move by server, or error, or result, or query, but not move by me, which will be consumed elsewhere
			    	char msgType = server_sb.charAt(0);
			    	switch (msgType){
			    		case 'R':
			    			//process result, extract learning
			    			itsOver = true;
			    			break;
			    		case 'E':
			    			//process error, 
			    			itsOver = true;
			    			break;
			    		case 'M':
			    			//this is server move because my move echo is consumed after I make it
			    			//remember to convert moves to and from Samuel's form
			    			//a move has been made, need to figure out response, but don't send yet
			    			answer = cla.acceptMoveAndRespond(server_sb);
			    			break;
			    		case '?':
			    			//this is server prompting for a move, send prepared answer
			    			myClient.writeMessageAndEcho("(2:4):(3:5)"); 
			    			readMessage = myClient.readAndEcho(); //here consume the server's echo of my move, which always occurs
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
		 System.err.println("read: before");
		String readMessage = _in.readLine();
		System.err.println("read: "+readMessage);
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
	 private StringBuffer convertMove2Draughts(StringBuffer mv_sb){
		 StringBuffer answer_sb = new StringBuffer(mv_sb);
		 return answer_sb;
	 }
	 private StringBuffer convertMove2Sam(StringBuffer mv_sb){
		 StringBuffer answer_sb = new StringBuffer(mv_sb);
		 return answer_sb;
	 }

}
