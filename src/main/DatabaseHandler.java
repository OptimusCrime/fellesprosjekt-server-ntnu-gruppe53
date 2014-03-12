package main;

import java.sql.*;

public class DatabaseHandler {
	private Connection connect = null;
	
	public DatabaseHandler() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		
		connect = DriverManager.getConnection("jdbc:mysql://sql27.webhuset.no/optimuscrimene4?" + "user=optimuscrimene4&password=geTABU747");
	}
}
