package main;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server extends Thread {
	
	/*
	 * Variables
	 */
	
	private ServerSocket serverSocket;
	private DatabaseHandler db;
	private ArrayList<SocketServer> clients;
	private boolean didConnect;
	
	/*
	 * Constructor
	 */
	
	public Server() throws IOException {
		// Init arrayList of clients
		clients = new ArrayList<SocketServer>();
		
		// Init database-connection
		try {
			db = new DatabaseHandler();
			didConnect = true;
		} catch (Exception e) {
			didConnect = false;
			System.out.println("Hallo");
			e.printStackTrace();
		}
		
		// Init serverSocket
		serverSocket = new ServerSocket(9000);
		serverSocket.setSoTimeout(Integer.MAX_VALUE);
	}
	
	/*
	 * Run
	 */
	
	public void run() {
		// Define a few variables
		Socket server;
		SocketServer client;
		System.out.println("Running");
		// Listen until the program is killed
		while(true) {
			// Try to accept incoming requests
			try {
				// Accept request
				server = serverSocket.accept();
				
				// Set alive forever to avoid sockettimeout
				server.setKeepAlive(true);
				System.out.println("Got connection!");
				// Initialize a new instance of ClientThread
				client = new SocketServer(this, server);
				client.start();
				
				// Add to ArrayList to preserve all the sockets
				clients.add(client);
			} catch(SocketTimeoutException s) {
				System.out.println("Fuck1");
			} catch(IOException e) {
				
				System.out.println("Fuck2");
			}
		}
	}
	
	/*
	 * Send message to every user in the chat
	 */
	
	public void notifyAllClients(String msg) {
		// Loop all clients
		for (int i = 0; i < clients.size(); i++) {
			// User each client's own sendMessage-method
			clients.get(i).sendMessage(msg);
		}
	}
	
	/*
	 * Main
	 */
	
	public static void main(String[] args) {
		// Try to initalize a new instance of the server with provided port
		try {
			// New instance
			Thread t = new Server();
			
			// Run thread
			t.start();
		} catch(IOException e) {
			// Something went to hell
			e.printStackTrace();
		}
	}
}
