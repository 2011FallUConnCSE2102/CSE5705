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
			RECAP,		THRET
		}
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
		public final String[] ParameterNames = new String[]{"ADV",		"APEX",		"BACK",		"CENT",
															"CNTR",		"CORN",		"CRAMP",	"DENY",
															"DIA",		"DIAV",		"DYKE",		"EXCH",
															"EXPOS",	"FORK",		"GAP",		"GUARD",
															"HOLE",		"KCENT",	"MOB",		"MOBIL",
															"MOVE",		"NODE",		"OREO",		"POLE",
															"RECAP",	"THRET"};
		
		
		public DBHandler()
		{
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			try{
				_connection = DriverManager.getConnection(_serverUrl, "root", "root");
				Output("URL: " + _serverUrl);
				Output("Connection [" + _connection.isValid(0) + "]: " + _connection);
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

		public int[] GetStateEvaluation(long whitepawns, long blackpawns, long whitekings, long blackkings)
		{
			ResultSet rs;
			Statement stmt;
			String selectString = "SELECT * FROM boards WHERE WhitePieces = " + whitepawns + 
					" AND BlackPieces = " + blackpawns + 
					" AND WhiteKings = " + whitekings + 
					" AND BlackKings = " + blackkings;
			try{
				stmt = _connection.createStatement();
				Output("Submitting Query: " + selectString);
				rs = stmt.executeQuery(selectString);
				
				int evals[] = new int[26];
				int numRows = 0;
				while (rs.next())
				{
					for (int i = 0; i < 26; i++)
					{
						evals[i] = rs.getInt(ParameterNames[i]);
					}
					numRows++;
				}
				Output("Fetch Complete.  Found " + numRows + " rows.");

				stmt.close();
				return evals;
			}
			catch (SQLException e)
			{HandleException(e);}
			return null;
		}
		public int GetSingleParameterEvaluation(long whitepawns, long blackpawns, long whitekings, long blackkings, String parameterName)
		{
			ResultSet rs;
			Statement stmt;
			String selectString = "SELECT * FROM boards WHERE WhitePieces = " + whitepawns + 
					" AND BlackPieces = " + blackpawns + 
					" AND WhiteKings = " + whitekings + 
					" AND BlackKings = " + blackkings;
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
				return evaluation;
			}
			catch (SQLException e)
			{HandleException(e);}
			return 0;
		}
		
		public void Insert(long whitepawns, long blackpawns, long whitekings, long blackkings, int adv, int apex, int back, int cent, int cntr, int corn, int cramp, int deny, int dia,
				int diav, int dyke, int exch, int expos, int fork, int gap, int guard, int hole, int kcent, int mob, int mobil, int move, int node, int oreo, int pole, int recap, int thret)
		{
			String insertString = "INSERT into boards VALUES(" + 
			whitepawns + ", " +
			blackpawns + ", " +
			whitekings + ", " +
			blackkings + ", " +
			adv +  ", " + apex + ", " + back + ", " + cent +", " +  cntr + ", " + corn + ", " + cramp + ", " + deny + ", " + dia + ", " + diav + ", " +
			dyke + ", " + exch + ", " + expos + ", " + fork + ", " + gap + ", " + guard +", " + hole + ", " + kcent + ", " + mob + ", " + mobil + ", " +
			move + ", " + node + ", " + oreo + ", " + pole + ", " + recap + ", " + thret + ")";
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
		public void Insert(long whitepawns, long blackpawns, long whitekings, long blackkings, int[] evals)
		{
			String insertString = "INSERT into boards VALUES(" + 
			whitepawns + ", " +
			blackpawns + ", " +
			whitekings + ", " +
			blackkings + ", " +
			evals[0] +  ", " + evals[1] + ", " + evals[2] + ", " + evals[3] +", " +  evals[4] + ", " + evals[5] + ", " + evals[6] + ", " + evals[7] + ", " + evals[8] + ", " + evals[9] + ", " +
			evals[10] + ", " + evals[11] + ", " + evals[12] + ", " + evals[13] + ", " + evals[14] + ", " + evals[15] +", " + evals[16] + ", " + evals[17] + ", " + evals[18] + ", " + evals[19] + ", " +
			evals[20] + ", " + evals[21] + ", " + evals[22] + ", " + evals[23] + ", " + evals[24] + ", " + evals[25] + ")";
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
		public void InsertSingleParameter(long whitepawns, long blackpawns, long whitekings, long blackkings, Parameter parameterName, int value)
		{
			String insertString = "INSERT into boards (WhitePieces, BlackPieces, WhiteKings, BlackKings, " + parameterName.toString() + ") VALUES (" +
					whitepawns + ", " + blackpawns + ", " + whitekings + ", " + blackkings + ", " + value + ")";
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
		
		public void RemoveQuery(long whitePawns, long blackPawns, long whiteKings, long blackKings)
		{
			String removeString = "DELETE FROM boards WHERE whitepieces = " + whitePawns +
					" AND blackpieces = " + blackPawns + " AND whitekings = " + whiteKings +
					" AND blackkings = " + blackKings;
			try{
				Statement stmt = _connection.createStatement();
				Output("Submitting Update: " + removeString + "...");
				stmt.executeUpdate(removeString);
				stmt.close();
			}
			catch (SQLException e)
			{HandleException(e);}
		}
		
		private void Output(String message)
		{
			System.out.println("[DBHandler]: " + message);
		}
		private void HandleException(SQLException e)
		{
			System.out.println(e.getMessage() + ": " + e.getErrorCode());
		}
	}



