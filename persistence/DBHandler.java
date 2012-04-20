package persistence;
	
	import java.security.Policy.Parameters;
	import java.sql.*;
public class DBHandler 
{
	String _serverUrl = "jdbc:mysql://137.99.11.100:3306/checkers";
	Connection _connection;
	
	enum Parameter{
		ADV,		APEX,		BACK,		CENT,
		CNTR,		CORN,		CRAMP,		DENY,
		DIA,		DIAV,		DYKE,		EXCH,
		EXPOS,		FORK,		GAP,		GUARD,
		HOLE,		KCENT,		MOB,		MOBIL,
		MOVE,		NODE,		OREO,		POLE,
		RECAP,		THRET,		KCNTC
	}
	public final static int NUMPARAMS = 27;//TMS 20April
	public final int ADV = 0;
	public final int APEX = 1;
	public final int BACK = 2;
	public final int CENT = 3;
	public final int CNTR = 4;
	public final int CORN = 5;
	public final int CRAMP = 6;
	public final int DENY = 7;
	public final int DIA = 8;
	public final int DIAV = 9;
	public final int DYKE = 10;
	public final int EXCH = 11;
	public final int EXPOS = 12;
	public final int FORK = 13;
	public final int GAP = 14;
	public final int GUARD = 15;
	public final int HOLE = 16;
	public final int KCENT = 17;
	public final int MOB = 18;
	public final int MOBIL = 19;
	public final int MOVE = 20;
	public final int NODE = 21;
	public final int OREO = 22;
	public final int POLE = 23;
	public final int RECAP = 24;
	public final int THRET = 25;
	public final int KCNTC = 26;
	public final String[] ParameterNames = new String[]{"ADV",		"APEX",		"BACK",		"CENT",
														"CNTR",		"CORN",		"CRAMP",	"DENY",
														"DIA",		"DIAV",		"DYKE",		"EXCH",
														"EXPOS",	"FORK",		"GAP",		"GUARD",
														"HOLE",		"KCENT",	"MOB",		"MOBIL",
														"MOVE",		"NODE",		"OREO",		"POLE",
														"RECAP",	"THRET", 	"KCNTC"};
	
	
	public DBHandler()
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");	//Load JDBC driver
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try{
			_connection = DriverManager.getConnection(_serverUrl, "root", "root");	//Log in under root
			Output("URL: " + _serverUrl);
			Output("Connection [" + _connection.isValid(0) + "]: " + _connection);	//Print connection
		}
		catch (SQLException sqle)
		{HandleException(sqle);}
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
			Output("Submitting Query: " + selectString);
			rs = stmt.executeQuery(selectString);
			
			int evaluation = 0;
			int numRows = 0;
			while (rs.next())
			{
				evaluation = rs.getInt(parameterName);
				numRows++;
			}
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
			int kcntc)
	{
		String insertString = "INSERT into boards VALUES(" + 
		fAW + ", " +
		fAB + ", " +
		bAW + ", " +
		bAB + ", " +
		adv +  ", " + apex + ", " + back + ", " + cent +", " +  cntr + ", " + corn + ", " + cramp + ", " + deny + ", " + dia + ", " + diav + ", " +
		dyke + ", " + exch + ", " + expos + ", " + fork + ", " + gap + ", " + guard +", " + hole + ", " + kcent + ", " + mob + ", " + mobil + ", " +
		move + ", " + node + ", " + oreo + ", " + pole + ", " + recap + ", " + thret + ", " + kcntc + ")";
		try{
			Statement stmt = _connection.createStatement();	//Open statement
			Output("Submitting Update: " + insertString + "...");
			int rowCount = stmt.executeUpdate(insertString);	//Execute
			Output("Successfully Updated " + rowCount + " rows.");
			stmt.close();
		}
		catch (SQLException e)
		{HandleException(e);}
	}
	//Inserts a vector to the database using a vector of parameters.  This is the preferred method.
	public void Insert(long fAW, long fAB, long bAW, long bAB, int[] evals)
	{
		//Build a string of the parameter values
		char[] paramString = new char[evals.length * 2];
		for (int i = 0; i < paramString.length;i += 2)
		{
			paramString[i] = (char)(evals[i/2] + 48);
			paramString[i + 1] = ',';
		}
		paramString[paramString.length - 1] = ')';
		String insertString = "INSERT into boards VALUES(" + 	//Build insert statement using board and concatenate the parameter string
		fAW + "," +
		fAB + "," +
		bAW + "," +
		bAB + ", ";
		insertString = insertString + new String(paramString);
		try{
			Statement stmt = _connection.createStatement();
			Output("Submitting Update: " + insertString + "...");
			int rowCount = stmt.executeUpdate(insertString);	//Execute
			Output("Successfully Updated " + rowCount + " rows.");
			stmt.close();
		}
		catch (SQLException e)
		{HandleException(e);}
	}
	public void InsertSingleParameter(long fAW, long fAB, long bAW, long bAB, Parameter parameterName, int value)
	{
		String insertString = "INSERT into boards (FAW, FAB, BAW, BAB, " + parameterName.toString() + ") VALUES (" +
				fAW + ", " + fAB + ", " + bAW + ", " + bAB + ", " + value + ")";
		try{
			Statement stmt = _connection.createStatement();
			Output("Submitting Update: " + insertString + "...");
			int rowCount = stmt.executeUpdate(insertString);
			Output("Successfully Updated " + rowCount + " rows.");
			stmt.close();
		}
		catch (SQLException e)
		{HandleException(e);}
	}
	
	public void RemoveQuery(long fAW, long fAB, long bAW, long bAB)
	{
		String removeString = "DELETE FROM boards WHERE FAW = " + fAW +
				" AND FAB = " + fAB + " AND BAW = " + bAW + " AND BAB = " + bAB;
		try{
			Statement stmt = _connection.createStatement();
			Output("Submitting Update: " + removeString + "...");
			stmt.executeUpdate(removeString);
			stmt.close();
		}
		catch (SQLException e)
		{HandleException(e);}
	}
	
	//Provides an easier way to print to console with less typing and adds the "[DBHandler]" tag so we know where the output is coming from
	private void Output(String message)
	{
		System.out.println("[DBHandler]: " + message);
	}
	private void HandleException(SQLException e)
	{
		System.out.println("[DBHandler]:" + e.getMessage() + ": " + e.getErrorCode());
	}
}
