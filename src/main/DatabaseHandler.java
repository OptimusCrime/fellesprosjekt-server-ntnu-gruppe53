package main;

import java.sql.*;

/*
 * DatabaseHandler
 * 
 * Handles connection to the database
 * 
 */

public class DatabaseHandler {
	
	/*
	 * Variables we need in order to connect to the database
	 */
	
	public Connection connect = null;
	private PreparedStatement preparedStatement = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	
	/*
	 * (Re)Connect to the database
	 */
	
	public void reconnect() throws Exception {
		// Load datbase-driver
		Class.forName("com.mysql.jdbc.Driver");
		
		// Connect to MySQL here
		connect = DriverManager.getConnection("jdbc:mysql://sql27.webhuset.no/optimuscrimene4?" + "user=optimuscrimene4&password=geTABU747");
	}
	
	/*
	 * Method for closing the connection
	 */
	
	public void closeConnection() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {}
	}
	
	/*
	 * Check if login is valid (does not return userid
	 */
	
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
	
	/*
	 * Fetching login (used by all the queries)
	 */
	
	public int getUserId(String username, String password) throws Exception {
		// The query itself
		preparedStatement = connect.prepareStatement("SELECT id FROM user WHERE email = ? AND password = ? LIMIT 1");
		preparedStatement.setString(1, username);
		preparedStatement.setString(2, password);
		
		// Execute the query
		resultSet = preparedStatement.executeQuery();
		
		// Check if found a user
		if (!resultSet.next()) {
			throw new Exception("Error");
			
		}
		
		return resultSet.getInt("id");
	}
	
	/*
	 * Get all appointments
	 */
	
	public ResultSet getAllAppointments(int id) throws Exception {
		// The query itself
		preparedStatement = connect.prepareStatement("SELECT ap.* , ua.* FROM appointment ap LEFT JOIN userAppointment AS ua ON ap.id = ua.appointment WHERE ua.user = ? ORDER BY ap.id ASC");
		preparedStatement.setInt(1, id);
		
		// Return queryset
		return preparedStatement.executeQuery();
	}
	
	/*
	 * Get all employees
	 */
	
	public ResultSet getAllEmployees() throws Exception {
		// Create statement
		statement = connect.createStatement();
		
		// Return queryset
		return statement.executeQuery("SELECT id, email, name FROM user ORDER BY name ASC");
	}
}
