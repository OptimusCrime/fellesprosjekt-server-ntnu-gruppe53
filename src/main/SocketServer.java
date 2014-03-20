package main;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.mysql.jdbc.StringUtils;

/*
 * SocketServer
 * 
 * Class that deals with one specific socket-connection
 * 
 */

public class SocketServer extends Thread {
	
	/*
	 * Varables
	 */
	
	private Server server;
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	private DatabaseHandler db;
	
	/*
	 * Constructor
	 */
	
	public SocketServer(Server serv, Socket s) {
		// Set initial
		this.server = serv;
		this.socket = s;
		this.db = new DatabaseHandler();
	}
	
	/*
	 * Run
	 */
	
	public void run() {
		try {
			// Read the steam
			out = new DataOutputStream(this.socket.getOutputStream());
			
			// Try to set up stream in
			InputStream inFromServer = socket.getInputStream();
			in = new DataInputStream(inFromServer);
			
			// Listen forever
			listen();
		} catch (IOException e) {}
	}
	
	/*
	 * Listen in the stream here
	 */
	
	public void listen() {
		// Store message here
		String msg;
		
		// Read forever
		while (true) {
			
			// Try to read stream
			try {
				// Store information in variable
				msg = in.readUTF();
				
				// Check if the variable got actual content
				if (msg.length() > 0) {
					// DEBUG TODO
					System.out.println("Got this: " + msg);
					
					// Decode the content (formatted as json)
					decodeMessage(msg);
				}
			} catch (IOException e) {
				// Kill thread
				interrupt();
				
				// Break thread
				break;
			}
		}
	}
	
	/*
	 * Decode the request and send a response back
	 */
	
	@SuppressWarnings("unchecked")
	public void decodeMessage(String msg) {
		// First reconnect to the database
		try {
			db.reconnect();
			
			// Decode json
			JSONObject requestObj = (JSONObject)JSONValue.parse(msg);
			
			// Get action and type
			String action = (String) requestObj.get("action");
			String type = (String) requestObj.get("type");
			
			// Get login
			JSONObject loginObj = (JSONObject) requestObj.get("login");
			String username = (String) loginObj.get("username");
			String password = (String) loginObj.get("password");
			
			// Create response-object
			JSONObject responseObj = new JSONObject();
			responseObj.put("method", "response");
			responseObj.put("action", action);
			responseObj.put("type", type);
			
			// Check what action we're dealing with
			if (action.equals("login")) {
				// Login
				if (type.equals("put")) {
					// Set login to false first
					boolean isCorrectLogin = false;
					
					// Try to run the query
					try {
						isCorrectLogin = db.selectUser(username, password);
					}
					catch (Exception e) {}
					
					// Check if it was successful or not
					if (isCorrectLogin) {
						int userId = db.getUserId(username, password);
						responseObj.put("id", userId);
						responseObj.put("code", 200);
					}
					else {
						responseObj.put("code", 500);
					}
				}
			}
			else if (action.equals("logout")) {
				// TODO
			}
			else if (action.equals("appointments")) {
				// Appoinement
				if (type.equals("get")) {
					// Loading all appointments
					JSONArray appointments = new JSONArray();
					
					// Try to run the query
					try {
						// Fetch user to make sure we're logged in
						db.getUserId(username, password);
						
						// Get ids to fetch
						JSONArray calendarObj = (JSONArray) requestObj.get("data");
						String calendarIds = "";
						for (int i = 0; i < calendarObj.size(); i++) {
							calendarIds += Integer.toString(new BigDecimal((long) calendarObj.get(i)).intValueExact()) + ",";
						}
						
						// The query
						ResultSet res = db.getAllAppointments(calendarIds.substring(0, calendarIds.length() - 1));
						
						while (res.next()) {
							System.out.println("Has next...");
							JSONObject tempJSONObj = new JSONObject();
							
							// Add each field to the object
							tempJSONObj.put("id", res.getInt("id"));
							tempJSONObj.put("title", res.getString("title"));
							tempJSONObj.put("description", res.getString("description"));
							tempJSONObj.put("location", res.getString("location"));
							tempJSONObj.put("room", res.getInt("room"));
							tempJSONObj.put("owner", res.getInt("owner"));
							tempJSONObj.put("start", res.getString("appointmentStart"));
							tempJSONObj.put("end", res.getString("appointmentEnd"));
							tempJSONObj.put("participate", res.getBoolean("participate"));
							tempJSONObj.put("hide", res.getBoolean("hide"));
							tempJSONObj.put("alarm", res.getBoolean("alarm"));
							tempJSONObj.put("alarm_time", res.getString("alarmTime"));
							tempJSONObj.put("user", res.getInt("user"));
							
							// Add to array
							appointments.add(tempJSONObj);
						}
						
						// Add the array to the data
						responseObj.put("data", appointments);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else if (action.equals("employees")) {
				// Employees
				if (type.equals("get")) {
					// Loading all employees
					JSONArray employees = new JSONArray();
					
					// Try to run the query
					try {
						ResultSet res = db.getAllEmployees();
						
						while (res.next()) {
							JSONObject tempJSONObj = new JSONObject();
							
							// Add each field to the object
							tempJSONObj.put("id", res.getInt("id"));
							tempJSONObj.put("email", res.getString("email"));
							tempJSONObj.put("name", res.getString("name"));
							
							// Add to array
							employees.add(tempJSONObj);
						}
						
						// Add the array to the data
						responseObj.put("data", employees);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			// Debug TODO
			System.out.println("Sendt this: " + responseObj.toJSONString());
			
			// Send message
			if (responseObj != null) {
				sendMessage(responseObj.toJSONString());
			}
			
			// Close connection to database
			db.closeConnection();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Send the message to the stream
	 */
	
	public void sendMessage(String s) {
		try {
			out.writeUTF(s);
		} catch (IOException e) {}
	}
}
