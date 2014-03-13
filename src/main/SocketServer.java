package main;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
	
	public SocketServer(Server serv, Socket s, DatabaseHandler d) {
		System.out.println("Called!!!");
		// Set initial data for user
		this.server = serv;
		this.socket = s;
		this.db = d;
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
					// Decode the content (formatted as json)
					System.out.println("Got this: " + msg);
					decodeMessage(msg);
				}
			} catch (IOException e) {}
		}
	}
	
	/*
	 * Decode the request and send a response back
	 */
	
	@SuppressWarnings("unchecked")
	public void decodeMessage(String msg) {
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
					responseObj.put("code", 200);
				}
				else {
					responseObj.put("code", 500);
				}
			}
		}
		else if (action.equals("logout")) {
			
		}
		else if (action.equals("appointment")) {
			JSONArray appointments = new JSONArray();
			JSONObject derp1 = new JSONObject();
			derp1.put("id", 1);
			derp1.put("title", "Fuck off");
			derp1.put("description", "Todo 123");
			derp1.put("start", 100000);
			derp1.put("end", 100000);
			derp1.put("place", "Todo 123");
			derp1.put("room", null);
			
			derp1.put("participates", true);
			derp1.put("hide", false);
			derp1.put("alarm", false);
			derp1.put("alarm_time", 0);

			appointments.add(derp1);
			responseObj.put("data", appointments);
		}
		System.out.println("Sendt this: " + responseObj.toJSONString());
		sendMessage(responseObj.toJSONString());
	}
	
	/*
	 * Send the message to the stream (only self)
	 */
	
	public void sendMessage(String s) {
		try {
			out.writeUTF(s);
		} catch (IOException e) {
			System.out.println("Something died");
		}
	}
}
