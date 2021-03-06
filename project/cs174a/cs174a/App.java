package cs174a;                                             // THE BASE PACKAGE FOR YOUR APP MUST BE THIS ONE.  But you may add subpackages.

// You may have as many imports as you need.
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import java.util.Formatter;


import java.util.Scanner;

/**
 * The most important class for your application.
 * DO NOT CHANGE ITS SIGNATURE.
 */
class Date
{
    public int year; 
    public int month;  
    public int day; 
};

public class App implements Testable
{
	private OracleConnection _connection;                   // Example connection object to your DB.
	private String key="databasePinKey";
	private int nextTransKey;
	private int nextKey(){ return ++nextTransKey;}
	/**
	 * Default constructor.
	 * DO NOT REMOVE.
	 */
	App()
	{
		// TODO: Any actions you need.
		today = new Date ();
		setDate(2011, 3, 1);
	}

	/**
	 * This is an example access operation to the DB.
	 */

	private Date today;

	void exampleAccessToDB()
	{
		// Statement and ResultSet are AutoCloseable and closed automatically.
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select owner, table_name from all_tables" ) )
			{
				while( resultSet.next() )
					System.out.println( resultSet.getString( 1 ) + " " + resultSet.getString( 2 ) + " " );
			}
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage() );
		}
	}

	////////////////////////////// Implement all of the methods given in the interface /////////////////////////////////
	// Check the Testable.java interface for the function signatures and descriptions.

	@Override
	public String initializeSystem()
	{
		// Some constants to connect to your DB.
		final String DB_URL = "jdbc:oracle:thin:@cs174a.cs.ucsb.edu:1521/ORCL";
		final String DB_USER = "c##zholoien";
		final String DB_PASSWORD = "9463274";

		// Initialize your system.  Probably setting up the DB connection.
		Properties info = new Properties();
		info.put( OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER );
		info.put( OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD );
		info.put( OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20" );

		try
		{
			OracleDataSource ods = new OracleDataSource();
			ods.setURL( DB_URL );
			ods.setConnectionProperties( info );
			_connection = (OracleConnection) ods.getConnection();

			// Get the JDBC driver name and version.
			DatabaseMetaData dbmd = _connection.getMetaData();
			System.out.println( "Driver Name: " + dbmd.getDriverName() );
			System.out.println( "Driver Version: " + dbmd.getDriverVersion() );

			// Print some connection properties.
			System.out.println( "Default Row Prefetch Value is: " + _connection.getDefaultRowPrefetch() );
			System.out.println( "Database Username is: " + _connection.getUserName() );
			System.out.println();

			return "0";
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}

	public String setDate( int year, int month, int day ){
		today.year=year;
		today.month=month;
		today.day=day;
		return "0 "+year+" "+month+" "+day;
	}
	
	public String getDate(){
		return today.year+"-"+today.month+"-"+today.day;
	}

	public int getDaysInMonth(){
		switch (today.month){
			case 1: return 31;
			case 2: return 28;
			case 3: return 31;
			case 4: return 30;
			case 5: return 31;
			case 6: return 30;
			case 7: return 31;
			case 8: return 31;
			case 9: return 30;
			case 10: return 31;
			case 11: return 30;
			case 12: return 31;
		}return 0;
	}

    	public String getMonth(){
		switch (today.month){
			case 1: return "January";
			case 2: return "February";
			case 3: return "March";
			case 4: return "April";
			case 5: return "May";
			case 6: return "June";
			case 7: return "July";
			case 8: return "August";
			case 9: return "September";
			case 10: return "October";
			case 11: return "November";
			case 12: return "December";
		}return "";
	}

	public String updateAvgBalance(String id){
		int days = today.day;
		String sql1 = "Select A.balance, A.lastTrans, A.avgBalance from Account2 A where A.aid = '" +id + "'";
		
		float avg = 0;
		float balance = 0;
		String date = getDate();
		int month = today.month;
		int day =today.day;
		

		Statement stmt = null;
		try{ stmt = _connection.createStatement();
            		ResultSet rs = stmt.executeQuery(sql1);
			rs.next();
			balance=rs.getFloat("balance");
			avg = rs.getFloat("avgBalance");
			date=rs.getString("lastTrans");
			month=Integer.parseInt(date.split("-")[1]);
			day=Integer.parseInt(date.split("-")[2]);
			if (month==today.month){
				days=days-day;
				//System.out.println("It has been "+days+" days");			
			}avg = avg + (balance*days/(getDaysInMonth()-1));
			
			//System.out.println("The new average is "+avg);
			String sql2 = "Update Account2 " +
                            "set avgBalance = "+ avg +
                            ", lastTrans = '"+ getDate() +
                            "' where aid = '" + id + "'";
			stmt.executeUpdate(sql2);
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}

	
		return "0";

	}

	public String accureInterest(String aid, String taxid){
		if (today.day != getDaysInMonth())
			return "Not the last day of the month";

		
		String sql1 = "Select A.balance, A.rate, A.avgBalance from Account2 A where A.aid = '" +aid + "'";

		float rate=0;
		float iBalance=0;
		float interest=0;
		float avg=0;
		
		Statement stmt = null;
		try{ stmt = _connection.createStatement();
            		ResultSet sr = stmt.executeQuery(sql1);
			if (sr.next()){
			sr.close();
			
			} else {System.out.println("No Account exists");return "0 No account exists";}
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
		
		updateAvgBalance(aid);
		try{ stmt = _connection.createStatement();	
			ResultSet rs = stmt.executeQuery(sql1);
			rs.next();
			
			iBalance=rs.getFloat("balance");
			rate = rs.getFloat("rate");
			avg=rs.getFloat("avgBalance");
			interest= rate*avg;
			//System.out.println("interest = "+interest);
			String sql2 = "Update Account2 " +
			    "set balance = balance +" + interest + 
			    " where aid = '" + aid + "'";
			stmt.executeUpdate(sql2);
			String sql3 = "Insert into Transaction2 " +
                            "Values ( '" + aid + "', 'null', 'accure-interest','" + taxid + "', '" + interest + "', '"+getDate()+"', '"+nextKey()+"')";
			stmt.executeUpdate(sql3);

			String sql4 = "Update Account2 " +
                            "set avgBalance = "+ 0 +
                            " where aid = '" + aid + "'";
			stmt.executeUpdate(sql4);
			return "0 "+aid+" "+iBalance+" "+(iBalance+interest);
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}

	}
	
	public String resetAccount(String accountID, float balance){
		String sql2 = "Update Account2 " +
			    "set init_bala = " + balance + 
			    " where aid = '" + accountID + "'";	
		Statement stmt=null;
		try{stmt=_connection.createStatement();
			stmt.executeUpdate(sql2);	

		
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
		return "0 Reset completed";	

	}
	

	public String resetPocket(String accountID, float balance){
		String sql2 = "Update Pocket2 " +
			    "set init_bal = " + balance + 
			    " where aid = '" + accountID + "'";	
		Statement stmt=null;
		try{stmt=_connection.createStatement();
			stmt.executeUpdate(sql2);	

		
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
		return "0 Reset completed";	

	}
	public String resetInitialBalance(){
		String sql1 = "SELECT aid, balance from Account2"; 
		String sql3 = "SELECT aid, balance from Pocket2";
		Statement stmt=null;
		try{stmt=_connection.createStatement();
			ResultSet rs= stmt.executeQuery(sql1);
			while (rs.next()){
				
				Float balance=rs.getFloat("balance");
				String accountID = rs.getString("aid"); 
				resetAccount(accountID, balance);

			}
			rs.close();
			ResultSet sr = stmt.executeQuery(sql3);
			while (sr.next()){
				Float balance=sr.getFloat("balance");
				String accountID = sr.getString("aid"); 
				resetPocket(accountID, balance);

			}
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
		return "0 Reset completed";
	}

	public String addInterest(){
		String sql1 = "SELECT aid, taxid from Account2";
		if (today.day!=getDaysInMonth()){
			System.out.println("Not the last Day of The Month");		
			return "0 Not last day of month";
		}Statement stmt=null;
		try{stmt=_connection.createStatement();
			ResultSet rs= stmt.executeQuery(sql1);
			while (rs.next())
				accureInterest(rs.getString("aid"), rs.getString("taxid"));				


		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}


		return "0";
	}

	@Override
	public String createTables(){
		Statement stmt = null;
		String sql1 = "CREATE TABLE Owner2 " +
                   "(taxid VARCHAR(255) not NULL, " +
                   " name VARCHAR(255), " + 
                   " address VARCHAR(255), " +  
                   " PRIMARY KEY ( taxid ))";

		String sql2 = "CREATE TABLE Account2 " +
                   "(taxid VARCHAR(255) not NULL, " +
                   " name VARCHAR(255), " + 
		   " type VARCHAR(255), " + 
		   " status VARCHAR(255), " + 
		   " lastTrans VARCHAR(255), " + 
		   " balance FLOAT, " +  
		   " avgBalance FLOAT, " +  
		   " rate FLOAT, " + 
                   " address VARCHAR(255), " + 
		   " aid VARCHAR(255) not NULL, " +
		    " init_bala FLOAT, " +
		   " CONSTRAINT FK_PrimaryOwner FOREIGN KEY (taxid) REFERENCES Owner2(taxid) on delete cascade," +
                   " PRIMARY KEY ( aid ))";

		String sql3 = "CREATE TABLE OwnRelationship " +
                   "(taxid VARCHAR(255) not NULL, " +
		   " aid VARCHAR(255) not NULL, " + 
		    " pin VARCHAR(255) not NULL," + 
		   " CONSTRAINT FK_Owner FOREIGN KEY (taxid) REFERENCES Owner2(taxid) on delete cascade," +
		  " CONSTRAINT FK_Account FOREIGN KEY (aid) REFERENCES Account2(aid) on delete cascade," +
                   " PRIMARY KEY ( taxid, aid ))";   

		String sql4 = "Create Table Transaction2 " +
            "(account1 Varchar(255) not NULL, " +
		    "account2 Varchar(255), " +
		    "trans_type VARCHAR(255) not null, " +
		    "ownid Varchar(255) not Null, " +
		    "Amount float not null, " +
		    "t_date varchar(255) not null, " +
		    "t_key varchar(16) not null, " +
		    " Constraint FK_acc1 foreign key (account1) references Account2(aid) on delete cascade," +
		    " Constraint FK_ownT foreign key (ownid) references Owner2(taxid) on delete cascade," +
		    " Primary Key (t_key))";


		String sql5 = "CREATE TABLE  Pocket2" +
                   "(taxid VARCHAR(255) not NULL, " +
                   " status VARCHAR(255), " + 
		   " balance FLOAT, " +
		   " parent_aid VARCHAR(255) not NULL, " +  
		   " aid VARCHAR(255) not NULL, " +
		   " init_bal FLOAT," +
		   " CONSTRAINT FK_ParentAccount FOREIGN KEY (parent_aid) REFERENCES Account2(aid) on delete cascade," +
		   " CONSTRAINT FK_PocketOwner FOREIGN KEY (taxid) REFERENCES Owner2(taxid) on delete cascade," +
                   " PRIMARY KEY ( aid ))";

		String sql6 = "CREATE TABLE PocketOwn " +
                   "(taxid VARCHAR(255) not NULL, " +
		   " aid VARCHAR(255) not NULL, " + 
		    " pin VARCHAR(255) not NULL," + 
		   " CONSTRAINT FK_PocketOwnerRelationship FOREIGN KEY (taxid) REFERENCES Owner2(taxid) on delete cascade," +
		  " CONSTRAINT FK_PocketAccount FOREIGN KEY (aid) REFERENCES Pocket2(aid) on delete cascade," +
                   " PRIMARY KEY ( taxid, aid ))";
 
		String sql7 = "CREATE TABLE PocketTransaction " +
			"( aid VARCHAR(255) not NULL, " + 
		   " aid2 VARCHAR(255) , " +
		   " t_date VARCHAR(255) not NULL," +
		   " t_type VARCHAR(255) not NULL," +
		   " CONSTRAINT FK_PocketAccountT FOREIGN KEY (aid) REFERENCES Pocket2(aid) on delete cascade," +
                   " PRIMARY KEY ( aid, t_date, t_type ) )";

		try{stmt = _connection.createStatement();
			stmt.executeUpdate(sql1);
			stmt.executeUpdate(sql2);
			stmt.executeUpdate(sql3);
			stmt.executeUpdate(sql4);
			stmt.executeUpdate(sql5);
			stmt.executeUpdate(sql6);
			stmt.executeUpdate(sql7);
			//System.out.println("Created Owner2 table");
			return "0";
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}		
		


		
	}

	@Override
	public String dropTables(){
		Statement stmt = null;
		String sql1 = "DROP TABLE Owner2 ";
		String sql2 = "DROP TABLE Account2 "; 
		String sql3 = "DROP TABLE OwnRelationship ";
		String sql4 = "DROP TABLE Transaction2 ";
		String sql5 = "DROP TABLE Pocket2 ";
		String sql6 = "DROP TABLE PocketOwn ";
		String sql7 = "DROP TABLE PocketTransaction ";
		try{stmt = _connection.createStatement();
			stmt.executeUpdate(sql7);
			stmt.executeUpdate(sql6);
			stmt.executeUpdate(sql5);
			stmt.executeUpdate(sql4);
			stmt.executeUpdate(sql3);
			stmt.executeUpdate(sql2);
			stmt.executeUpdate(sql1);

		
		
			//System.out.println("Dropped Owner2 table");
			return "0";
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}		
		


	}


	public String setPin(String taxid){
		

		Statement stmt=null, stmt2=null;

		String sql1 = "Select A.pin from ownRelationship A where A.taxid = '" +taxid + "'";	
		 try{ stmt = _connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql1);
            if(rs.next() ){

                    String oldPin = rs.getString("pin");
                    

                    //TODO: Check if oldPin matches
			Scanner in = new Scanner(System.in);
        		System.out.println("Enter New PIN\n");
        		String input = in.nextLine();
			String newPin ="";
			try{
				newPin = encrypt(input, key);
			} catch (Exception e) {
 			   e.printStackTrace();
			}
                        String sql3 = "Update OwnRelationship " +
                            "set pin = '" + newPin +
                            "' where taxid = '" + taxid + "'";
			String sql4 = "Update PocketOwn " +
                            "set pin = '" + newPin +
                            "' where taxid = '" + taxid + "'";
			try{stmt2 = _connection.createStatement();
                            stmt2.executeUpdate(sql3);
			    return "0 New Pin Set";

                        }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";

                }
                    }return "0 Owner does not exist in our records";
                
         }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";

                }
		

	}

	public String setPin(String taxid, String input){
		

		Statement stmt=null, stmt2=null;

		String sql1 = "Select A.pin from ownRelationship A where A.taxid = '" +taxid + "'";	
		 try{ stmt = _connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql1);
            if(rs.next() ){

                    String oldPin = rs.getString("pin");
                    

                    //TODO: Check if oldPin matches
			
			String newPin ="";
			try{
				newPin = encrypt(input, key);
			} catch (Exception e) {
 			   e.printStackTrace();
			}
                        String sql3 = "Update OwnRelationship " +
                            "set pin = '" + newPin +
                            "' where taxid = '" + taxid + "'";
			String sql4 = "Update PocketOwn " +
                            "set pin = '" + newPin +
                            "' where taxid = '" + taxid + "'";
			try{stmt2 = _connection.createStatement();
                            stmt2.executeUpdate(sql3);
			    return "0 New Pin Set";

                        }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";

                }
                    }return "0 Owner does not exist in our records";
                
         }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";

                }
		

	}

	


	public static String encrypt(String strClearText,String strKey) throws Exception{
	String strData="";
	
	try {
		SecretKeySpec skeyspec=new SecretKeySpec(strKey.getBytes(),"Blowfish");
		Cipher cipher=Cipher.getInstance("Blowfish");
		cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
		byte[] encrypted=cipher.doFinal(strClearText.getBytes());
		strData=new String(encrypted);
		
	} catch (Exception e) {
		e.printStackTrace();
		throw new Exception(e);
	}
	return strData;
	}

	@Override
	public String createCheckingSavingsAccount( AccountType accountType, String id, double initialBalance, String tin, String name, String address ){
		Statement stmt = null;		
		if (initialBalance<1000.0||accountType==AccountType.POCKET)
			return "1";
		double rate=0;
		if (accountType==AccountType.INTEREST_CHECKING)
			rate=.03;
		else if (accountType==AccountType.SAVINGS)
			rate=.048;
		String pin ="";
		String sql3 = "Select A.pin from ownRelationship A where A.taxid = '" +tin + "'";
		try{stmt = _connection.createStatement();		
			ResultSet rs = stmt.executeQuery(sql3);
			if (rs.next())
				pin=rs.getString("pin");
			else{
                        try{
                                pin = encrypt("1717", key);
                        } catch (Exception e) {
                           e.printStackTrace();
                        }
			}
		}catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";

                }
		String sql1 = "INSERT INTO Account2 " +
                   "VALUES ('"+tin+"', '"+name+"', '"+accountType+"', 'OPEN',"+"'"+getDate()+"', "+ 0+", "+0+", "+rate+", '"+address+"', '"+id+"',"+0.0+")";
		String sql2 = "INSERT INTO OwnRelationship " +  "VALUES ('"+tin+"', '"+id+"', '"+pin+"')";
		    /*"VALUES ('"+tin+"', '"+id+"', '"+pin+"')";*/

		try{stmt = _connection.createStatement();			
			stmt.executeUpdate(sql1);
			stmt.executeUpdate(sql2);
			

		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}

		deposit(id, initialBalance);
		return "0 " + id + " " + accountType + " " + initialBalance + " " + tin;

	}

    //@Override
    public String deposit(String accountID, double amount){
	
	Statement stmt = null, stmt2 = null, stmt3 = null;
	float balance = 0, newAmount = 0;
	boolean flag = true;
	String taxid="";
        String sql1 = "Select A.taxid, A.pin from ownRelationship A where A.aid = '" +accountID + "'";
	String sql4 = "Select A.taxid, A.balance from Account2 A where A.aid = '" +accountID + "'";
	try{stmt =  _connection.createStatement();
	    ResultSet sr = stmt.executeQuery(sql4);
	     if(sr.next()){
		balance = sr.getFloat("Balance");
		taxid=sr.getString("taxid");
	        
		}
	}catch( SQLException e )
	    {
			System.err.println( e.getMessage() );
			flag = false;
			return "1";
			    
	 }
	
	 try{ stmt = _connection.createStatement();
	    ResultSet rs = stmt.executeQuery(sql1);
	    if(rs.next() && flag){
		
			updateAvgBalance(accountID);
			String sql2 = "Insert into Transaction2 " +
			    "Values ( '" + accountID + "', 'null', 'deposit','" + taxid + "', '" + amount + "', '"+getDate()+"', '"+nextKey()+"')";
			
			String sql3 = "Update Account2 " +
			    "set balance = balance +" + amount + 
			    " where aid = '" + accountID + "'";
		       
			try{stmt2 = _connection.createStatement();
			    stmt2.executeUpdate(sql2);
			    stmt2.executeUpdate(sql3);
			    flag = false;
			}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			flag = false;
			return "1";
			    
		}
		    
		}else System.out.println("Account does not exist");
	 }catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";

		}


	String sql5 = "Select A.balance from Account2 A where A.aid = '" +accountID + "'";
	try{stmt =  _connection.createStatement();
	    ResultSet sr = stmt.executeQuery(sql4);
	     if(sr.next()){
		newAmount = sr.getFloat("Balance");
	        
		}
	}catch( SQLException e )
	    {
			System.err.println( e.getMessage() );
			flag = false;
			return "1";
			    
	 }
	 return "0 " + balance+ " " + newAmount;
	

    }


	public String withdrawal(String accountID, double amount ){
		Statement stmt=null, stmt2 =null;
		if (checkBalance(accountID, amount)==false)
			return "0 Insuffiecient Funds";
		float balance = 0, newAmount = 0;
        	boolean flag = true;
		String taxid="";
        	String sql1 = "Select A.taxid, A.pin from ownRelationship A where A.aid = '" +accountID + "'";
        	String sql4 = "Select A.balance, A.taxid from Account2 A where A.aid = '" +accountID + "'";
        	try{stmt =  _connection.createStatement();
            		ResultSet sr = stmt.executeQuery(sql4);
             		if(sr.next()){
                		balance = sr.getFloat("Balance");
				taxid=sr.getString("taxid");
                	}
        	}catch( SQLException e )
            	{
                        System.err.println( e.getMessage() );
                        flag = false;
                        return "1";

         	}

		try{ stmt = _connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql1);
            if(rs.next() && flag){

                    
                        String sql2 = "Insert into Transaction2 " +
                            "Values ( '" + accountID + "', 'null', 'withdrawal','" + taxid + "', '" + amount + "', '"+getDate()+"', '"+nextKey()+"')";
			updateAvgBalance(accountID);
                        String sql3 = "Update Account2 " +
                            "set balance = balance -" + amount +
                            " where aid = '" + accountID + "'";

                        try{stmt2 = _connection.createStatement();
                            stmt2.executeUpdate(sql2);
                            stmt2.executeUpdate(sql3);
                            flag = false;
                        }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        flag = false;
                        return "1";

                }
                    
                }else System.out.println("Account does not exist");
         }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";

                }


        String sql5 = "Select A.balance from Account2 A where A.aid = '" +accountID + "'";
        try{stmt =  _connection.createStatement();
            ResultSet sr = stmt.executeQuery(sql4);
             if(sr.next()){
                newAmount = sr.getFloat("Balance");

                }
        }catch( SQLException e )
            {
                        System.err.println( e.getMessage() );
                        flag = false;
                        return "1";

         }
	conditionalClose(accountID);	
         return "0 " + balance+ " " + newAmount;



		


	}

	public String writeCheck(String accountID, double amount ){
		Statement stmt=null, stmt2 =null;
		if (checkBalance(accountID, amount)==false)
			return "0 Insuffiecient Funds";
		float balance = 0, newAmount = 0;
        	boolean flag = true;
		String taxid="";
        	String sql1 = "Select A.taxid, A.pin from ownRelationship A where A.aid = '" +accountID + "'";
        	String sql4 = "Select A.taxid, A.balance from Account2 A where A.aid = '" +accountID + "'";
        	try{stmt =  _connection.createStatement();
            		ResultSet sr = stmt.executeQuery(sql4);
             		if(sr.next()){
                		balance = sr.getFloat("Balance");
				taxid=sr.getString("taxid");

                	}
        	}catch( SQLException e )
            	{
                        System.err.println( e.getMessage() );
                        flag = false;
                        return "1";

         	}

		try{ stmt = _connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql1);
            if(rs.next() && flag){

                    
                        String sql2 = "Insert into Transaction2 " +
                            "Values ( '" + accountID + "', 'null', 'write-check','" + rs.getString("taxid") + "', '" + amount + "', '"+getDate()+"', '"+nextKey()+"')";
			updateAvgBalance(accountID);
                        String sql3 = "Update Account2 " +
                            "set balance = balance -" + amount +
                            " where aid = '" + accountID + "'";

                        try{stmt2 = _connection.createStatement();
                            stmt2.executeUpdate(sql2);
                            stmt2.executeUpdate(sql3);
                            flag = false;
                        }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        flag = false;
                        return "1";

                }
                    
                }else System.out.println("Account does not exist");
         }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";

                }


        String sql5 = "Select A.balance from Account2 A where A.aid = '" +accountID + "'";
        try{stmt =  _connection.createStatement();
            ResultSet sr = stmt.executeQuery(sql4);
             if(sr.next()){
                newAmount = sr.getFloat("Balance");

                }
        }catch( SQLException e )
            {
                        System.err.println( e.getMessage() );
                        flag = false;
                        return "1";

         }
	conditionalClose(accountID);	
         return "0 " + balance+ " " + newAmount;



		


	}


    public boolean checkPin(String taxId){
	Statement stmt = null;
        String sql1 = "Select A.pin from ownrelationship A where A.taxid = '" +taxId + "'";
	String enteredPin ="";
	String pin = "";
	try{ stmt = _connection.createStatement();
	    ResultSet rs = stmt.executeQuery(sql1);
	    if(rs.next()){
	        pin = rs.getString("pin");
	    }
	}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return false;
		}
    
	

	Scanner scan = new Scanner(System.in);
	System.out.println("Please enter your Pin");
	String input = scan.nextLine();
	System.out.println("Pin:" + pin + " <-");
	try{
	    enteredPin = encrypt(input, key);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	if(pin.equals(enteredPin)){
	    return true;
	}
	else{
	    return false;
	    }
    }

    public boolean checkBalance(String aid, double amount){
	Statement stmt = null;
	String sql = "Select A.balance from Account2 A where A.aid = '" + aid + "'";
	try{ stmt = _connection.createStatement();
	    ResultSet rs = stmt.executeQuery(sql);
	    if(rs.next()){
	        float result = rs.getFloat("Balance");
		if (result < amount){
		    return false;
		}
		else{
		    return true;
		}
		
	    }
	}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return false;
		}
	return false;
    }


    public boolean checkPocketBalance(String aid, double amount){
        Statement stmt = null;
        String sql = "Select A.balance from pocket2 A where A.aid = '" + aid + "'";
        try{ stmt = _connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                float result = rs.getFloat("Balance");
                if (result < amount){
                    return false;
                }
                else{
                    return true;
                }

            }
        }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return false;
                }
        return false;
    }

    @Override
    public String showBalance( String accountId ){
	Statement stmt = null;
	float balance = 0;
	String sql1 = "Select A.balance from Account2 A where A.aid = '" +accountId + "'";
        try{stmt =  _connection.createStatement();
            ResultSet sr = stmt.executeQuery(sql1);
             if(sr.next()){
                balance = sr.getFloat("Balance");

                }
        }catch( SQLException e )
            {
                        System.err.println( e.getMessage() );
                        return "1";

         }
	System.out.println(String.format("%.2f", balance));
	return "0 " + balance;
    }




    




    public String payFriend( String from, String to, double amount ){
	Statement stmt = null;
	float from_balance = 0, to_balance = 0;
	String pin = "";
	String sql1 = "select R.pin from pocketOwn R where R.aid='"+from+"'";
	String sql2 = "Update Pocket2 set balance=balance-"+amount+" where aid='"+from+"'";
	String sql3 = "Update Pocket2 set balance=balance+"+amount+" where aid='"+to+"'";
        String sql4 = "Insert INTO PocketTransaction " +
                   "VALUES ('"+from+"', '"+ to+ "', '"+ getDate()+"', 'pay-friend')";
        try{stmt = _connection.createStatement();
                        ResultSet rs = stmt.executeQuery(sql1);
			if(rs.next()){
			    pin = rs.getString("pin");
			}
	}catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
			return "1";
                }
        	
	if(checkPocketBalance(from, amount)){
	    String sql5 = "SELECT a.balance FROM pocket2 a WHERE a.aid='"+from+"'";
	    try{stmt = _connection.createStatement();
		stmt.executeUpdate(sql2);
		stmt.executeUpdate(sql3);
		stmt.executeUpdate(sql4);
		conditionalClose(from);
		ResultSet rs = stmt.executeQuery(sql5);
		if(rs.next()){
		    //System.out.println("first account");
		    from_balance = rs.getFloat("balance");
		}
		else{
		    return "1";
		}
	    }catch( SQLException e )
                {
		    System.err.println( e.getMessage() );
		    return "1";
                }		
	}
	else{
	    return "1";
	}
	
	String sql6 = "SELECT p.balance FROM pocket2 p WHERE p.aid ='"+to+"'";
                try{stmt = _connection.createStatement();
                        ResultSet rs = stmt.executeQuery(sql6);
			//System.out.println("About to get account2");
                        if(rs.next()){
                            to_balance = rs.getFloat("balance");
                        }
			else{
                            return "1";
			}
                }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";
                }
		return String.format("0 %.2f %.2f", from_balance, to_balance);
    }


	@Override
	public String createPocketAccount( String id, String linkedId, double initialTopUp, String tin ){
		Statement stmt = null;	
		

		String sql1 = "select R.pin from OwnRelationship R where R.aid='"+linkedId+"' and R.taxid='"+tin+"'";		
		String sql2= "INSERT INTO Pocket2 " +
		"VALUES ('"+tin+"', 'OPEN', "+ 0.0+", '"+linkedId+"', '"+id+"', "+0.0+")";
		
		
		
		if (checkBalance(linkedId, initialTopUp)==false)
			return "0 Pocket Account not created. Insufficient Funds\n";

		try{stmt = _connection.createStatement();	
			ResultSet rs = stmt.executeQuery(sql1);
			if (rs.next()){

				String pin=rs.getString("pin");
				String sql3 = "INSERT INTO PocketOwn " +
                   "VALUES ('"+tin+"', '"+id+"', '"+pin +"')";


				stmt.executeUpdate(sql2);
//				System.out.println("Trying to add Pocket Account");
				stmt.executeUpdate(sql3);
				topUp(id, initialTopUp);
				return String.format("0 "+id+" POCKET %.2f",initialTopUp);
			}else{ System.out.println("No matching table");}
			
			
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}

		

		return "0";
	}

	@Override
	public String createCustomer( String accountId, String tin, String name, String address ){	Statement stmt = null;
		String sql1 = "INSERT INTO Owner2 " +
                   "VALUES ('"+tin+"', '"+name+"', '"+address+"')";
		String pin ="";
                        try{
                                pin = encrypt("1717", key);
                        } catch (Exception e) {
                           e.printStackTrace();
                        }

		String sql2 = "select A.aid from account2 A where A.aid='"+accountId+"'";
		String sql3 = "INSERT INTO OwnRelationship " +
                   "VALUES ('"+tin+"', '"+accountId+"', '"+pin+"')";
		try{stmt = _connection.createStatement();			
			stmt.executeUpdate(sql1);
			ResultSet rs = stmt.executeQuery(sql2);
			if (rs.next()){
				stmt.executeUpdate(sql3);
			}
			//System.out.println("New Onwer added");
			return "0";
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}		
	
	}


	String getParentAccount(String pocket){
		String sql="Select P.parent_aid From Pocket2 P Where P.aid ='"+pocket+"'";
		Statement stmt = null;
		try{stmt = _connection.createStatement();			
			ResultSet rs=stmt.executeQuery(sql);
			if (rs.next())
				return rs.getString("parent_aid");
			
		}catch ( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}	
		return "";
	}


		
	
	public String collect( String accountId, double amount ){
		
		Statement stmt = null;
		String parent = getParentAccount(accountId);
		if (checkBalance(parent, amount)==false)
			return "0 Insufficient Funds\n";
		
		String pin = "";
		String sql5 = "select R.pin from pocketOwn R where R.aid='"+accountId+"'";
		String sql1 = "Update Account2 set balance=balance+"+(amount*.97)+" where aid='"+parent+"'";
		String sql2 = "Update Pocket2 set balance=balance-"+amount+" where aid='"+accountId+"'";
		String sql3 = "Insert INTO PocketTransaction " +
                   "VALUES ('"+accountId+"', NULL, '"+ getDate()+"', 'COLLECT')";		
		String sql4 = "SELECT a.balance, p.balance FROM Account2 a, pocket2 p WHERE a.aid='"+parent+"' AND p.aid ='"+accountId+"'";
	
	try{stmt = _connection.createStatement();
                        ResultSet rs = stmt.executeQuery(sql5);
                        if(rs.next()){
                            pin = rs.getString("pin");
                        }
        }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";
                }

	
		try{stmt = _connection.createStatement();			
		    
			updateAvgBalance(parent);
			stmt.executeUpdate(sql1);
			stmt.executeUpdate(sql2);
			stmt.executeUpdate(sql3);
			conditionalClose(parent);
			ResultSet rs = stmt.executeQuery(sql4);
			rs.next();
			System.out.println(String.format("0 %.2f %.2f", rs.getFloat(1), rs.getFloat(2)));
							
			return String.format("0 %.2f %.2f", rs.getFloat(1), rs.getFloat(2));
		        
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	
		

	}

	public String topUp( String accountId, double amount ){
		
		Statement stmt = null;
		String parent = getParentAccount(accountId);
		if (checkBalance(parent, amount)==false)
			return "0 Insufficient Funds\n";
		
		String pin = "";
		String sql5 = "select R.pin from pocketOwn R where R.aid='"+accountId+"'";
		String sql1 = "Update Account2 set balance=balance-"+amount+" where aid='"+parent+"'";
		String sql2 = "Update Pocket2 set balance=balance+"+amount+" where aid='"+accountId+"'";
		String sql3 = "Insert INTO PocketTransaction " +
                   "VALUES ('"+accountId+"', NULL, '"+ getDate()+"', 'TOPUP')";		
		String sql4 = "SELECT a.balance, p.balance FROM Account2 a, pocket2 p WHERE a.aid='"+parent+"' AND p.aid ='"+accountId+"'";
	
	try{stmt = _connection.createStatement();
                        ResultSet rs = stmt.executeQuery(sql5);
                        if(rs.next()){
                            pin = rs.getString("pin");
                        }
        }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";
                }

	
		try{stmt = _connection.createStatement();			
		    
			updateAvgBalance(parent);
			stmt.executeUpdate(sql1);
			stmt.executeUpdate(sql2);
			stmt.executeUpdate(sql3);
			conditionalClose(parent);
			ResultSet rs = stmt.executeQuery(sql4);
			rs.next();
			System.out.println(String.format("0 %.2f %.2f", rs.getFloat(1), rs.getFloat(2)));
							
			return String.format("0 %.2f %.2f", rs.getFloat(1), rs.getFloat(2));
		        
		}catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	
		

	}


	public String purchase( String accountId, double amount ){
		if (checkPocketBalance(accountId, amount)==false)
			return "0 Insufficient Funds";
		Statement stmt;
		 float balance = 0, newAmount = 0;
		String pin = "";
                String sql5 = "select R.pin from pocketOwn R where R.aid='"+accountId+"'";
                String sql2 = "Update Pocket2 set balance=balance-"+amount+" where aid='"+accountId+"'";
                String sql3 = "Insert INTO PocketTransaction " +
                   "VALUES ('"+accountId+"', NULL, '"+ getDate()+"', 'purchase')";
                String sql4 = "SELECT p.balance FROM pocket2 p WHERE p.aid ='"+accountId+"'";

		 try{stmt = _connection.createStatement();
                        ResultSet rs = stmt.executeQuery(sql5);
                        if(rs.next()){
                            pin = rs.getString("pin");
                        }
			
        }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";
                }



		try{stmt = _connection.createStatement();

                      
                        ResultSet rs = stmt.executeQuery(sql4);
                        rs.next();
                        balance = rs.getFloat(1);
			stmt.executeUpdate(sql2);
                        stmt.executeUpdate(sql3);
                        ResultSet rs2 = stmt.executeQuery(sql4);
                        rs2.next();
                        newAmount = rs2.getFloat(1);
			conditionalClose(accountId);
                        return String.format("0 %.2f %.2f", balance, newAmount);
                        
                }catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";
                }







	}

	
	
	public String transfer( String from, String to, double amount ){
	Statement stmt = null;
	float from_balance = 0, to_balance = 0;
	if (amount>2000)
		return "0 Too much money attempting to be transferred";
	String taxid="";
	String pin = "";
	String sql1 = "Select A.taxid, A.pin from ownRelationship A, OwnRelationship B where A.aid = '" +from + "' and B.taxid = A.taxid and B.aid='"+to+"'";
	String sql2 = "Update Account2 set balance=balance-"+amount+" where aid='"+from+"'";
	String sql3 = "Update Account2 set balance=balance+"+amount+" where aid='"+to+"'";
        
        
	try{stmt = _connection.createStatement();
                        ResultSet rs = stmt.executeQuery(sql1);
			if(rs.next()){
			    pin = rs.getString("pin");
			    taxid=rs.getString("taxid");
			}else{
				System.out.println("Cannot Transfer; No Common Owner between Accounts");
				return "0 no Owner in common";
			}
	}catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
			return "1";
                }
        	
	if(checkBalance(from, amount)){
		updateAvgBalance(to);
		updateAvgBalance(from);
		String sql5 = "SELECT a.balance FROM Account2 a WHERE a.aid='"+from+"'";
		String sql6 = "SELECT a.balance FROM Account2 a WHERE a.aid='"+to+"'";
		String sql4 = "Insert INTO Transaction2 " +
                   "VALUES ('"+from+"', '"+ to+ "', 'transfer', '"+ taxid+"', "+amount+", '"+getDate()+"', '"+nextKey()+"')";
                try{stmt = _connection.createStatement();
                        stmt.executeUpdate(sql2);
                        stmt.executeUpdate(sql3);
                        stmt.executeUpdate(sql4);
                        ResultSet rs = stmt.executeQuery(sql5);
			if(rs.next()){
			    //System.out.println("first account");
			    from_balance = rs.getFloat("balance");
			}
			else{
			    return "1";
			}ResultSet sr = stmt.executeQuery(sql6);
			if(sr.next()){
			    //System.out.println("second account");
			    from_balance = sr.getFloat("balance");
			}
			else{
			    return "1";
			}
			
		}catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";
                }		
	}else{
	    return "1";
	}
	
		conditionalClose(from);
		return String.format("0 %.2f %.2f", from_balance, to_balance);
    }



	public String wire( String from, String to, double amount, String taxid ){
	Statement stmt = null;
	float from_balance = 0, to_balance = 0;
	
	
	String pin = "";
	String sql1 = "Select A.taxid, A.pin from ownRelationship A, OwnRelationship B where A.aid = '" +from + "' and B.taxid = A.taxid and B.aid='"+to+"'";
	String sql2 = "Update Account2 set balance=balance-"+amount+" where aid='"+from+"'";
	String sql3 = "Update Account2 set balance=balance+"+(amount*.98)+" where aid='"+to+"'";
        
        
	
        	
	if(checkBalance(from, amount)){
		updateAvgBalance(to);
		updateAvgBalance(from);
		String sql5 = "SELECT a.balance FROM Account2 a WHERE a.aid='"+from+"'";
		String sql6 = "SELECT a.balance FROM Account2 a WHERE a.aid='"+to+"'";
		String sql4 = "Insert INTO Transaction2 " +
                   "VALUES ('"+from+"', '"+ to+ "', 'wire', '"+ taxid+"', "+amount+", '"+getDate()+"', '"+nextKey()+"')";
                try{stmt = _connection.createStatement();
                        stmt.executeUpdate(sql2);
                        stmt.executeUpdate(sql3);
                        stmt.executeUpdate(sql4);
                        ResultSet rs = stmt.executeQuery(sql5);
			if(rs.next()){
			    //System.out.println("first account");
			    from_balance = rs.getFloat("balance");
			}
			else{
			    return "1";
			}ResultSet sr = stmt.executeQuery(sql6);
			if(sr.next()){
			    //System.out.println("second account");
			    from_balance = sr.getFloat("balance");
			}
			else{
			    return "1";
			}
			
		}catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";
                }		
	}else{
	    return "1";
	}
	
		conditionalClose(from);
		return String.format("0 %.2f %.2f", from_balance, to_balance);
    }



	public String requestTransfer(String from, String to, String taxid, double amount){
		String sql1 = "Select A.pin from ownRelationship A where A.aid = '" +from + "' and A.taxid = '"+taxid+"'";
		Statement stmt = null;
		try{stmt = _connection.createStatement();
                        ResultSet rs = stmt.executeQuery(sql1);
			if(rs.next()){
			    transfer(from, to, amount);
			}else{
				System.out.println("0 Cannot request from this account");
			}
	}catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
			return "1";
                }
		return "0";

	}

	public String conditionalClose(String aid){
		String sql1 = "SELECT a.balance FROM Account2 a WHERE a.aid='"+aid+"'";
		String sql2 = "SELECT a.balance FROM Pocket2 a WHERE a.aid='"+aid+"'";
		float balance = 0;
		String sql3 = "Update Account2 set status = 'CLOSED' where aid='"+aid+"'";
		String sql4 = "Update Pocket2 set status = 'CLOSED' where aid='"+aid+"'";
		Statement stmt = null;
		try{stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql1);
			if(rs.next()){
			    balance=rs.getFloat("balance");
				if (balance==0) stmt.executeUpdate(sql3);
			}else{
				rs= stmt.executeQuery(sql2);
				if (rs.next()){
					balance=rs.getFloat("balance");
					if (balance==0) stmt.executeUpdate(sql4);
				}
			}	
		}catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";
                }
		return "0";
	}
	/**
	 * Example of one of the testable functions.
	 */
	//@Override
	public String listClosedAccounts()
	{
		String  builder="0";
		String sql1 = "SELECT a.aid FROM Account2 a WHERE a.status='CLOSED'";
		String sql2 = "SELECT a.aid FROM Pocket2 a WHERE a.status='CLOSED'";
		Statement stmt = null;
		try{stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql1);
			while (rs.next()){
				builder=builder+" "+rs.getString("aid");
			}
			rs = stmt.executeQuery(sql2);
			while (rs.next()){
				builder=builder+" "+rs.getString("aid");
			}
		}catch( SQLException e )
                {
                        System.err.println( e.getMessage() );
                        return "1";
                }
		

		return builder;
	}



    public void printAccounts(String taxId){
	Statement stmt = null;
	String sql1 = "SELECT a.aid, a.balance, a.type FROM Account2 a, OwnRelationship R, Owner2 O WHERE O.taxid='"+taxId+"' AND a.aid=R.aid AND O.taxid = R.taxid";
	String sql2 = "SELECT * FROM Pocket2 a WHERE a.taxid='"+taxId+"'";
	try{stmt = _connection.createStatement();
	    ResultSet rs = stmt.executeQuery(sql1);
	    while (rs.next()){
		System.out.println("Account ID: " + rs.getString("aid")+ " " + "Balance: " + rs.getInt("Balance") + " " + "Account Type: " + rs.getString("type"));
	    }
	    rs = stmt.executeQuery(sql2);
	    while (rs.next()){
			System.out.println("Account ID: " + rs.getString("aid")+ " " + "Balance: " + rs.getInt("Balance") + " " + "Account Type: Pocket");
	    }
	}catch( SQLException e )
	    {
		    System.err.println( e.getMessage() );
	    }

	
    }
    public boolean checkClosed(String aid){
	String sql1 = "SELECT a.status FROM Account2 a WHERE a.aid='"+aid+"'";
	String sql2 = "SELECT a.status FROM Pocket2 a WHERE a.aid='"+aid+"'";
	Statement stmt = null;
	try{stmt = _connection.createStatement();
	    ResultSet rs = stmt.executeQuery(sql1);
	    if (rs.next()){
		if (rs.getString("status").equals("CLOSED"))
			return true;
		else
			return false;
	    }else{
		rs = stmt.executeQuery(sql2);
	    if (rs.next()){
		if (rs.getString("status").equals("CLOSED"))
			return true;
		else
			return false;
	    }

		}
	}catch( SQLException e )
	    {
		    System.err.println( e.getMessage() );
	    }
	


	return false;
    }



    public String getAccountType(String accountId){
	Statement stmt = null;
	String sql1 = "SELECT * FROM Account2 a WHERE a.aid='"+accountId+"'";
	String sql2 = "SELECT * FROM Pocket2 a WHERE a.aid='"+accountId+"'";
	try{stmt = _connection.createStatement();
	    ResultSet rs = stmt.executeQuery(sql1);
	    if (rs.next()){
		return rs.getString("type");
	    }
	    else{
	    rs = stmt.executeQuery(sql2);
	    if (rs.next()){
		return "POCKET";
	    }
	    }
	}catch( SQLException e )
	    {
		    System.err.println( e.getMessage() );
	    }
	return "";
	
    }

    public void generateMonthlyStatement(String taxid){
	boolean hasPocket = false;
	double finbalance = 0.00, pocketbal = 0.00, initbalCheck = 0.00, initbalPocket = 0.00;
	String aid = "", address = "", name = "";
	if (today.day == getDaysInMonth()){
	    Statement stmt = null;
	    String sql1 = "SELECT * FROM transaction2 a WHERE a.ownid='"+taxid+"'";
	    String sql3 = "SELECT a.aid FROM pocket2 a WHERE a.taxid='"+ taxid+"'";
	    String sql4 = "SELECT a.address, a.name FROM account2 a WHERE a.taxid='"+ taxid+"'";
	    String sql6 = "SELECT a.init_bala, a.balance FROM account2 a WHERE a.taxid='"+ taxid+"'";
	    String sql7 = "SELECT b.init_bal, b.balance FROM Pocket2 b WHERE b.taxid='"+ taxid+"'";
	    try{stmt = _connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql6);
		if(rs.next()){
		    finbalance = rs.getDouble("balance");
		    initbalCheck = rs.getDouble("init_bala");
		    
		}
		rs = stmt.executeQuery(sql7);
		if(rs.next()){
		    pocketbal = rs.getDouble("balance");
		    initbalPocket = rs.getDouble("init_bal");
		    
		}
	    }catch( SQLException e )
		{
		    System.err.println( e.getMessage() );
		}





	    
	    try{stmt = _connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql3);
		if(rs.next()){
		    aid = rs.getString("aid");
		    
		    hasPocket = true;
		}
	    }catch( SQLException e )
		{
		    System.err.println( e.getMessage() );
		}
	    
	    try{stmt = _connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql4);
		if(rs.next()){
		    name = rs.getString("name");
		    System.out.println(today.day + " " + getDaysInMonth());
		    address = rs.getString("address");
		    System.out.println("Monthly Statement for the Month of " + getMonth() + " " + today.year);
		    System.out.println("Owner: " + name + ", Address: "+ address);
		}
	    }catch( SQLException e )
		{
		    System.err.println( e.getMessage() );
		}
	

	
	    String sql2 = "SELECT * FROM PocketTransaction a WHERE a.aid='"+aid+"'";
	    try{stmt = _connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql1);
		System.out.println("Transactions Made on Checking/Savings Account:");
		while (rs.next()){
		    
		    System.out.println("Transaction Type: " + rs.getString("trans_type")+ " " + ", From Account: " + rs.getString("account1") + " " + ", To Account: (May be NULL) "+ rs.getString("account2") + ", On the Date: " + rs.getString("t_date"));
		}
		System.out.println(String.format("Initial Amount for CheckingSavings Account: %.2f, Final Balance: %.2f", initbalCheck, finbalance));
    
		if (hasPocket){
		rs = stmt.executeQuery(sql2);
		System.out.println("Transactions made on Pocket Account:");
		while (rs.next()){
		    System.out.println("Transaction Type: " + rs.getString("t_type")+ ", From Account: " + rs.getString("aid") + " " + ", To Account: (May be NULL) "+ rs.getString("aid2") + ", On the Date: " + rs.getString("t_date"));
		}
		System.out.println(String.format("Initial Amount for Pocket Account: %.2f, Final Balance: %.2f", initbalPocket, pocketbal));
		}

		if((finbalance + pocketbal) > 100000.00){
		    System.out.println("Limit of the insurance has been reached");
		}
	    }catch( SQLException e )
		{
		    System.err.println( e.getMessage() );
		}









	    
	}
	else{
	    System.out.println("Cannot generate Monthly Statement as it is not the last day of the month");
	}

	
    }

	public String addCoowner(String aid, String taxid){
		String pin="";
		
		String sql1 = "SELECT pin FROM OwnRelationship a WHERE a.taxid='"+taxid+"'";
		Statement stmt = null;
		try{stmt = _connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql1);
		if(rs.next()){
		    pin = rs.getString("pin");
		    
		    
		}else return "Need to add customer to records first";
	    }catch( SQLException e )
		{
		    System.err.println( e.getMessage() );
		}
		String sql3 = "INSERT INTO OwnRelationship " +
                   "VALUES ('"+taxid+"', '"+aid+"', '"+pin+"')";		
		try{stmt = _connection.createStatement();
		stmt.executeUpdate(sql3);
		}catch( SQLException e )
		{
		    System.err.println( e.getMessage() );
			return "1";
		}
		return "0";
}

    public void dter(){
	Statement stmt = null;
	int count = 0;
	String sql1 = "Select SUM(t.amount), o.taxid FROM Owner2 o, Transaction2 t WHERE t.ownid = o.taxid AND (t.trans_type = 'deposit' OR t.trans_type = 'wire' OR t.trans_type = 'transfer') GROUP BY o.taxid";
	try{stmt = _connection.createStatement();
	    System.out.println("Government Drug and Tax Evasion Report (DTER) \nListed Below are the Tax IDs of those whose Transfer Deposit and Wire sum is greater than $10,000");
	    ResultSet rs = stmt.executeQuery(sql1);
	    while(rs.next()){
		double sum = rs.getDouble(1);
		System.out.println(sum);
		if (sum > 10000){
		    count++;
		    System.out.println(count + ". " + rs.getString("taxid"));
		}
		    
	    }
	    if (count == 0){
		System.out.println("No such record exists in our System");
	    }
	    
	}catch( SQLException e )
	    {
		System.err.println( e.getMessage() );
	    }
	
	
    }


	/**
	 * Another example.
	 */
	
}
