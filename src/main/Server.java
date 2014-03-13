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
		this.clients = new ArrayList<SocketServer>();
		
		// Init database-connection
		try {
			this.db = new DatabaseHandler();
			this.didConnect = true;
		} catch (Exception e) {
			this.didConnect = false;
		}
		
		// Init serverSocket
		this.serverSocket = new ServerSocket(9000);
		this.serverSocket.setSoTimeout(Integer.MAX_VALUE);
	}
	
	/*
	 * Run
	 */
	
	public void run() {
		// Define a few variables
		Socket server;
		SocketServer client;
		
		// Listen until the program is killed
		while(true) {
			// Try to accept incoming requests
			try {
				// Accept request
				server = this.serverSocket.accept();
				
				// Set alive forever to avoid sockettimeout
				server.setKeepAlive(true);
				
				// Initialize a new instance of ClientThread
				client = new SocketServer(this, server, this.db);
				client.start();
				
				// Add to ArrayList to preserve all the sockets
				this.clients.add(client);
			} catch(SocketTimeoutException s) {
			} catch(IOException e) {
			}
		}
	}
	
	/*
	 * Send message to every user in the chat
	 */
	
	public void notifyAllClients(String msg) {
		// Loop all clients
		for (int i = 0; i < this.clients.size(); i++) {
			// User each client's own sendMessage-method
			this.clients.get(i).sendMessage(msg);
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
