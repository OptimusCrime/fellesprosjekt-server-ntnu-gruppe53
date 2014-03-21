package main;

import java.sql.*;
import java.util.ArrayList;

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
		connect = DriverManager.getConnection("jdbc:mysql://sql27.webhuset.no/optimuscrimene4?user=optimuscrimene4&password=geTABU747");
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
	
	public ResultSet getAllAppointments(String ids) throws Exception {
		// Create statement
		statement = connect.createStatement();
		
		if (ids.length() == 0) {
			return statement.executeQuery("SELECT ap.* , ua.* FROM appointment ap LEFT JOIN userAppointment AS ua ON ap.id = ua.appointment WHERE ua.user = NULL ORDER BY ap.id ASC");
		}
		else {
			return statement.executeQuery("SELECT ap.* , ua.* FROM appointment ap LEFT JOIN userAppointment AS ua ON ap.id = ua.appointment WHERE ua.user IN (" + ids + ") ORDER BY ap.id ASC");
		}
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
	
	/*
	 * Get all rooms
	 */
	
	public ResultSet getAllRooms() throws Exception {
		// Create statement
		statement = connect.createStatement();
		
		// Return queryset
		return statement.executeQuery("SELECT id, name, capacity FROM room ORDER BY id ASC");
	}
	
	/*
	 * Get available rooms
	 */
	
	public ResultSet getRoomsAvailable(String from, String to, int num) throws Exception {
		// Create statement
		statement = connect.createStatement();
		
		// Return queryset
		return statement.executeQuery("SELECT r.* FROM room r WHERE id NOT IN (SELECT room FROM appointment WHERE appointmentStart > DATE('" + from + "') AND appointmentEnd > DATE('" + from + "') OR (appointmentStart > DATE('" + from + "') AND appointmentEnd > DATE('" + to + "')) OR (appointmentStart < DATE('" + from + "') AND appointmentEnd > DATE('" + to + "'))) AND capacity > " + num + " ORDER BY capacity ASC");
	}
	
	/*
	 * Create new appointment!
	 */
	
	public void createNewAppointment(int id, String title, String description, String from, String to, Integer room, int participants, ArrayList<Integer>participantsArr) throws Exception {
		// The query itself
		preparedStatement = connect.prepareStatement("INSERT INTO appointment (appointmentStart, appointmentEnd, title, description, location, owner, room) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		preparedStatement.setString(1, from);
		preparedStatement.setString(2, to);
		preparedStatement.setString(3, title);
		preparedStatement.setString(4, description);
		preparedStatement.setString(5, "Location here");
		preparedStatement.setInt(6, id);
		preparedStatement.setInt(7, room);
		
		// Execute the query
		preparedStatement.executeUpdate();
		
		ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
		preparedStatement = null;
        if (generatedKeys.next()) {
             // Insert to users
        	int appointmentId = generatedKeys.getInt(1);
        	
        	if (participantsArr.size() > 0) {
        		for (int i = 0; i < participantsArr.size(); i++) {
        			preparedStatement = null;
        			
        			// Insert each row here
        			preparedStatement = connect.prepareStatement("INSERT INTO userAppointment (user, appointment, participate, hide, alarm) VALUES (?, ?, ?, ?, ?)");
        			preparedStatement.setInt(1, participantsArr.get(i));
        			preparedStatement.setInt(2, appointmentId);
        			preparedStatement.setInt(3, 0);
        			preparedStatement.setInt(4, 0);
        			preparedStatement.setInt(5, 0);
        			
        			// Execute the query
        			preparedStatement.executeUpdate();
        		}
        	}
        	
        } else {
            throw new SQLException("Creating user failed, no generated key obtained.");
        }
	}
	
	public ResultSet getAllParticipates(int appointment) throws Exception {
		// The query itself
		preparedStatement = connect.prepareStatement("SELECT user, participate, hide FROM userAppointment WHERE appointment = ?");
		preparedStatement.setInt(1, appointment);
		
		// Execute the query
		return preparedStatement.executeQuery();
	}
	
	public void updateParticipates(int user, int id, int status) throws Exception {
		// :D:D:D
		preparedStatement = connect.prepareStatement("UPDATE userAppointment SET participate = ? WHERE appointment = ? AND user = ?");
		preparedStatement.setInt(1, status);
		preparedStatement.setInt(2, id);
		preparedStatement.setInt(3, user);
		
		// Execute the query
		preparedStatement.executeUpdate();
	}
}
