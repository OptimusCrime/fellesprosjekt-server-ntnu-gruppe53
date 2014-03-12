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
	
	public SocketServer(Server serv, Socket s) {
		System.out.println("Called!!!");
		// Set initial data for user
		server = serv;
		socket = s;
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
		
		String action = (String) requestObj.get("action");
		String type = (String) requestObj.get("type");
		
		JSONObject responseObj = new JSONObject();
		responseObj.put("method", "response");
		responseObj.put("action", action);
		responseObj.put("type", type);
		
		if (action.equals("login")) {
			if (type.equals("put")) {
				responseObj.put("code", 200);
			}
		}
		else if (action.equals("logout")) {
			
		}
		else if (action.equals("appointment")) {
			JSONArray appointments = new JSONArray();
			JSONObject derp1 = new JSONObject();
			derp1.put("id", 1);
			derp1.put("avtale_start", 10000);
			derp1.put("avtale_slutt", 10000);
			derp1.put("tittel", "Fuck off");
			derp1.put("beskrivelse", "Todo 123");
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
