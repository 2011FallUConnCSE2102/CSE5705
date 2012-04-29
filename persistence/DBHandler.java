package persistence;
import java.sql.*;

public class DBHandler 
{
	String _serverUrl = "jdbc:mysql://137.99.11.100:3306/checkers";
	Connection _connection;
	
	public enum DebugMode{
		Silent,
		Quiet,
		Verbose
	}
	public DebugMode Debug = DebugMode.Silent;
	enum Parameter{
		ADV,		APEX,		BACK,		CENT,
		CNTR,		CORN,		CRAMP,		DENY,
		DIA,		DIAV,		DYKE,		EXCH,
		EXPOS,		FORK,		GAP,		GUARD,
		HOLE,		KCENT,		MOB,		MOBIL,
		MOVE,		NODE,		OREO,		POLE,
		RECAP,		THRET,		KCNTC,		DEMO,
		DEMMO,		DDEMO,		DDMM,		MODE1,
		MODE2,		MODE3,		MODE4,		MOC1,
		MOC2,		MOC3,		MOC4,		PADV,
		WIN
	}
	public final static int NUMPARAMS = 40; //Does not include the WIN field or the 4 key fields.
	public final static int ADV = 0;
	public final static int APEX = 1;
	public final static int BACK = 2;
	public final static int CENT = 3;
	public final static int CNTR = 4;
	public final static int CORN = 5;
	public final static int CRAMP = 6;
	public final static int DENY = 7;
	public final static int DIA = 8;
	public final static int DIAV = 9;
	public final static int DYKE = 10;
	public final static int EXCH = 11;
	public final static int EXPOS = 12;
	public final static int FORK = 13;
	public final static int GAP = 14;
	public final static int GUARD = 15;
	public final static int HOLE = 16;
	public final static int KCENT = 17;
	public final static int MOB = 18;
	public final static int MOBIL = 19;
	public final static int MOVE = 20;
	public final static int NODE = 21;
	public final static int OREO = 22;
	public final static int POLE = 23;
	public final static int RECAP = 24;
	public final static int THRET = 25;
	public final static int KCNTC = 26;
	public final static int DEMO = 27;
	public final static int DEMMO = 28;
	public final static int DDEMO = 29;
	public final static int DDMM = 30;
	public final static int MODE1 = 31;
	public final static int MODE2 = 32;
	public final static int MODE3 = 33;
	public final static int MODE4 = 34;
	public final static int MOC1 = 35;
	public final static int MOC2 = 36;
	public final static int MOC3 = 37;
	public final static int MOC4 = 38;
	public final static int PADV = 39;
	public final static int WIN = 40;	//Make sure to keep this value at the "end" of the list.
	public final static String[] ParameterNames = new String[]{"ADV",		"APEX",		"BACK",		"CENT",
																"CNTR",		"CORN",		"CRAMP",	"DENY",
																"DIA",		"DIAV",		"DYKE",		"EXCH",
																"EXPOS",	"FORK",		"GAP",		"GUARD",
																"HOLE",		"KCENT",	"MOB",		"MOBIL",
																"MOVE",		"NODE",		"OREO",		"POLE",
																"RECAP",	"THRET", 	"KCNTC",	"DEMO",
																"DEMMO",	"DDEMO",	"DDMM",		"MODE1",
																"MODE2",	"MODE3",	"MODE4",	"MOC1",
																"MOC2",		"MOC3",		"MOC4",		"PADV",
																"WIN"};
	
	boolean learningMode;
	State[] gameStates;
	int gameStateIndex = 0;
	
	public DBHandler(boolean learning)
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");	//Load JDBC driver
			Output("Driver Loaded");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try{
			_connection = DriverManager.getConnection(_serverUrl, "root", "root");	//Log in under root
			if (Debug == DebugMode.Verbose)
			{
				Output("URL: " + _serverUrl);
				Output("Connection [" + _connection.isValid(0) + "]: " + _connection);	//Print connection
			}
		}
		catch (SQLException sqle)
		{HandleException(sqle);}
		
		learningMode = learning;
		if (learningMode)
			gameStates = new State[100];
		
	}
	public void Close()
	{
		try{
		_connection.close();
		}
		catch (SQLException e) {
			HandleException(e);
		}
	}
	
	
	public Attribute[] buildAttributeCollection()
	{
		Attribute[] attrCollection = new Attribute[NUMPARAMS];
		for (int i = 0; i < NUMPARAMS; i++)
		{
			Output("Building the " + ParameterNames[i] + " attribute.");
			int numCats = getAttributeDomainSize(ParameterNames[i]);
			Output("Number of categories = " + numCats);
			
			Category[] attrCatList = new Category[numCats];
				try{
					Statement stmt = _connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT DISTINCT " + ParameterNames[i] + " FROM boards");
					int nextCat = 0;
					while (rs.next())
					{
						attrCatList[nextCat] = new Category(rs.getInt(1));
						Output("Created a new category at index " + nextCat + " with value = " + attrCatList[nextCat].Value + ".");
						nextCat++;
					}
					stmt.close();
				}
				catch (SQLException e)
				{
					HandleException(e);
				}
			
			attrCollection[i] = new Attribute(ParameterNames[i], attrCatList);
			fillAttribute(attrCollection[i]);
			Output("Finished Building the " + attrCollection[i].Name + " Attribute.\n------");
		}
		return attrCollection;
	}
	
	private int getAttributeDomainSize(String attributeName)
	{
		String countString = "SELECT COUNT(DISTINCT " + attributeName + ") FROM boards";
		return executeCountQuery(countString);
	}
	
	//Fetches some necessary data from the database to allow an attribute to calculate it's entropy.
	private void fillAttribute(Attribute attribute)
	{
		int attributeSize = 0;
		for (int i = 0; i < attribute.attrCats.length; i++)
		{
			String selectCatSize = "SELECT count(" + attribute.Name + ") FROM boards WHERE win IS NOT NULL AND " +
					attribute.Name + " = " + attribute.attrCats[i].Value;
			String selectPosResults = "SELECT count(" + attribute.Name + ") FROM boards WHERE win = 1 AND " +
					attribute.Name + " = " + attribute.attrCats[i].Value;
			String selectNegResults = "SELECT count(" + attribute.Name + ") FROM boards WHERE win = 0 AND " +
					attribute.Name + " = " + attribute.attrCats[i].Value;

			attribute.attrCats[i].NumEntries = executeCountQuery(selectCatSize);
			attribute.attrCats[i].NumPos = executeCountQuery(selectPosResults);
			attribute.attrCats[i].NumNeg = executeCountQuery(selectNegResults);
			attribute.Size += attribute.attrCats[i].NumEntries;
		}
	}
	

	//Returns null if an error occurs or if no row exists in the database with this board configuration.  Else 
	//returns a vector of all the parameters index accordingly to the public final ints.  For example, parameter DENY is at theVector[DBHANDLER.DENY].
	public int[] GetStateEvaluation(long fAW, long fAB, long bAW, long bAB)
	{
		ResultSet rs;
		Statement stmt;
		String selectString = "SELECT * FROM boards WHERE FAW = " + fAW + //Select query to the server based on configuration
				" AND FAB = " + fAB + 
				" AND BAW = " + bAW + 
				" AND BAB = " + bAB;
		try{
			stmt = _connection.createStatement();
			if (Debug == DebugMode.Verbose)
				Output("Submitting Query: " + selectString);	//Console output for debug and such
			rs = stmt.executeQuery(selectString);	//Execute the query
			
			int evals[] = new int[NUMPARAMS];	//Create the parameter vector
			int numRows = 0;	//Count the number of matching rows (should be only 0 or 1)
			while (rs.next())
			{
				for (int i = 0; i < evals.length; i++)	//Fill the vector
				{
					evals[i] = rs.getInt(ParameterNames[i]);	//Take an integer out of the result and put it in the vector according to the column name
				}
				numRows++;
			}
			if (Debug != DebugMode.Silent)
				Output("Fetch Complete.  Found " + numRows + " rows.");

			stmt.close();
			if (numRows == 0)	//No matches found
				return null;
			else
				return evals;	//Return filled array
		}
		catch (SQLException e)
		{HandleException(e);}
		return null;	//Return null after printing error
	}
	
	//Returns a single integer that matches the value under the given column name parameterName.  Returns -1 if no match.
	public int GetSingleParameterEvaluation(long fAW, long fAB, long bAW, long bAB, String parameterName)
	{
		ResultSet rs;
		Statement stmt;
		String selectString = "SELECT * FROM boards WHERE FAW = " + fAW + 
				" AND FAB = " + fAB + 
				" AND BAW = " + bAW + 
				" AND BAB = " + bAB;
		try{
			stmt = _connection.createStatement();
			if (Debug == DebugMode.Verbose)
				Output("Submitting Query: " + selectString);
			rs = stmt.executeQuery(selectString);
			
			int evaluation = 0;
			int numRows = 0;
			while (rs.next())
			{
				evaluation = rs.getInt(parameterName);
				numRows++;
			}
			if (Debug != DebugMode.Silent)
				Output("Fetch Complete.  Found " + numRows + " rows.");

			stmt.close();
			if (numRows == 0)
				return -1;
			else
				return evaluation;
		}
		catch (SQLException e)
		{HandleException(e);}
		return -1;
	}
	
	//Takes each parameter separately and concatenates them into a single string for insert statement.
	public void Insert(long fAW, long fAB, long bAW, long bAB, int adv, int apex, int back, int cent, int cntr, int corn, int cramp, int deny, int dia,
			int diav, int dyke, int exch, int expos, int fork, int gap, int guard, int hole, int kcent, int mob, int mobil, int move, int node, int oreo, int pole, int recap, int thret,
			int kcntc, int demo, int demmo, int ddemo, int ddmm, int mode1, int mode2, int mode3, int mode4, int moc1, int moc2, int moc3, int moc4, int win)
	{
		String insertString = "INSERT into boards VALUES(" + 
				fAW + ", " +
				fAB + ", " +
				bAW + ", " +
				bAB + ", " +
				adv +  "," + apex + "," + back + "," + cent +"," +  cntr + "," + corn + "," + cramp + "," + deny + "," + dia + "," + diav + "," +
				dyke + "," + exch + "," + expos + "," + fork + "," + gap + "," + guard +"," + hole + "," + kcent + "," + mob + "," + mobil + "," +
				move + "," + node + "," + oreo + "," + pole + ", " + recap + "," + thret + "," + kcntc + "," + demo + "," + demmo + "," + ddemo + "," +
				ddmm + "," + mode1 + "," + mode2 + "," + mode3 + "," + mode4 + "," + moc1 + "," + moc2 + "," + moc3 + "," + moc4 + "," + win + ")";

		if (Debug == DebugMode.Verbose)
			Output("Submitting Insert: " + insertString + "...");
		int rowCount = executeUpdateQuery(insertString);
		if (Debug != DebugMode.Silent)
			Output("Inserted " + rowCount + " rows.");
		if (rowCount == 0)
			Output("There was a problem processing the Database Query.  There may already be an existing entry with this configuration.");
	}
	//Inserts a vector to the database using a vector of parameters.  This is the preferred method.
	public void Insert(long fAW, long fAB, long bAW, long bAB, int[] evals)
	{
		//If we are practicing, add this state to the game state list.
		if (learningMode)
		{
			if (gameStateIndex < gameStates.length)
				{
				gameStates[gameStateIndex] = new State(fAW,fAB, bAW, bAB);
				gameStateIndex++;
			}
		}
		//Build a string of the parameter values
		String parameterString = "";
		for (int i = 0; i < evals.length - 1; i++)
		{
				parameterString = parameterString + Integer.toString(evals[i]) + ",";			
		}
		parameterString = parameterString + Integer.toString(evals[evals.length - 1]) + ", NULL)";

		String insertString = "INSERT into boards VALUES(" + 	//Build insert statement using board and concatenate the parameter string
		fAW + "," +
		fAB + "," +
		bAW + "," +
		bAB + ", "+
		parameterString;
		
		if (Debug == DebugMode.Verbose)
			Output("Submitting Insert: " + insertString + "...");
		int rowCount = executeUpdateQuery(insertString);
		if (Debug != DebugMode.Silent)
			Output("Inserted " + rowCount + " rows.");
		if (rowCount == 0 && Debug == DebugMode.Quiet)
			Output("There was a problem processing the Database Query.  There may already be an existing entry with this configuration.");
	}
	//Inserts a board configuration with only one known evaluation.  This will cause incomplete entries in the database, and is not preferred.
	public void InsertSingleParameter(long fAW, long fAB, long bAW, long bAB, Parameter parameterName, int value)
	{
		String insertString = "INSERT into boards (FAW, FAB, BAW, BAB, " + parameterName.toString() + ") VALUES (" +
				fAW + ", " + fAB + ", " + bAW + ", " + bAB + ", " + value + ")";
		
		if (Debug == DebugMode.Verbose)
			Output("Submitting Insert: " + insertString + "...");
		int rowCount = executeUpdateQuery(insertString);
		if (Debug != DebugMode.Silent)
			Output("Inserted " + rowCount + " rows.");
		if (rowCount == 0)
			Output("There was a problem processing the Database Query.  There may already be an existing entry with this configuration.");
	}
	
	//Removes a board configuration from the database.
	public void RemoveQuery(long fAW, long fAB, long bAW, long bAB)
	{
		String removeString = "DELETE FROM boards WHERE FAW = " + fAW +
				" AND FAB = " + fAB + " AND BAW = " + bAW + " AND BAB = " + bAB;
		if (Debug == DebugMode.Verbose)
			Output("Submitting Remove: " + removeString + "...");
		int rowCount = executeUpdateQuery(removeString);
		if (Debug != DebugMode.Silent)
			Output("Removed " + rowCount + " rows.");
		if (rowCount == 0)
			Output("There was a problem processing the Database Query.  There may not have been a matching board configuration.");
	}
	
	//Sets the Win/Loss field of a particular existing board configuration.
	public void finishGame(int win, double[] weights)
	{
		for (int i = 0; i < gameStates.length; i++)
		{
			if (gameStates[i] != null)
			{
				long fAB = gameStates[i].fab;
				long fAW = gameStates[i].faw;
				long bAB = gameStates[i].bab;
				long bAW = gameStates[i].baw;
				
				String updateString = "UPDATE boards SET WIN = " + win + " WHERE FAW = " + fAW +
						" AND FAB = " + fAB + " AND BAW = " + bAW + " AND BAB = " + bAB;
				if (Debug == DebugMode.Verbose)
					Output("Submitting Update: " + updateString + "...");
				int rowCount = executeUpdateQuery(updateString);
				if (Debug != DebugMode.Silent)
					Output("Updated " + rowCount + " rows.");
				if (rowCount == 0 && Debug == DebugMode.Quiet)
					Output("There was a problem processing the Database Query.  There may not have been a matching board configuration.");
			}
		}
		
		String evalNames = "";
		for (int i = 0; i < ParameterNames.length - 1; i++)
		{
			evalNames = evalNames + ParameterNames[i] + ",";
		}
		evalNames = evalNames + ParameterNames[ParameterNames.length - 1];
		String weightString = "";
		for (int i = 0; i < weights.length - 1; i++)
		{
			weightString = weightString + Double.toString(weights[i]) + ",";			
		}
		weightString = weightString + Double.toString(weights[weights.length - 1]);

		
		String weightTable = "INSERT INTO weights (" + evalNames + ") VALUES (" + weightString + ")";
		if (Debug == DebugMode.Verbose)
			Output("Submitting Insert: " + weightTable + "...");
		int rowCount = executeUpdateQuery(weightTable);
		if (Debug != DebugMode.Silent)
			Output("Inserted " + rowCount + " rows.");
		if (rowCount == 0)
			Output("There was a problem processing the Database Query.  There may already be an existing entry with this configuration.");
	
	}
	
	
	private int executeCountQuery(String query)
	{
		if (Debug == DebugMode.Verbose)
			Output("CountQuery[Submit]:: " + query);
		try{
			Statement stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			int retVal = 0;
			if (rs.next())
			{
				retVal = rs.getInt(1);
			}
			stmt.close();
			return retVal;
		}
		catch (SQLException e)
		{
			HandleException(e);
		}

		return 0;
	}
	private int executeUpdateQuery(String query)
	{
		if (Debug == DebugMode.Verbose)
			Output("UpdateQuery[Submit]:: " + query);
		try{
			Statement stmt = _connection.createStatement();
			int rowsAffected = stmt.executeUpdate(query);
			stmt.close();
			return rowsAffected;
		}
		catch (SQLException e)
		{
			HandleException(e);
		}

		return -1;
	}

	//Provides an easier way to print to console with less typing and adds the "[DBHandler]" tag so we know where the output is coming from
	private void Output(String message)
	{
		System.out.println("[DBHandler]: " + message);
	}
	private void HandleException(SQLException e)
	{
		//System.out.println("[DBHandler]: " + e.getMessage() + ": " + e.getErrorCode());
	}
	
	private class State
	{
		public long baw = 0;
		public long bab = 0;
		public long faw = 0;
		public long fab = 0;
		public State(long FaW, long FaB, long BaW, long BaB)
		{
			faw = FaW;
			fab = FaB;
			baw = BaW;
			bab = BaB;
		}
	}
}
