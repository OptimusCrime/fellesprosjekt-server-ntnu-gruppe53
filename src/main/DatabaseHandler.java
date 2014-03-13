package main;

import java.sql.*;

public class DatabaseHandler {
	private Connection connect = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	  
	public DatabaseHandler() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		
		connect = DriverManager.getConnection("jdbc:mysql://sql27.webhuset.no/optimuscrimene4?" + "user=optimuscrimene4&password=geTABU747");
	}
	
	public boolean selectUser(String username, String password) throws Exception {
		// The query itself
		preparedStatement = connect.prepareStatement("SELECT id FROM user WHERE email = ? AND password = ? LIMIT 1");
		preparedStatement.setString(1, username);
		preparedStatement.setString(2, password);
		
		// Execute the query
		resultSet = preparedStatement.executeQuery();
		
		// Check if found a user
		return resultSet.next();
	}
}
