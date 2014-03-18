package main;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/*
 * Server
 * 
 * The main-sever-class that delegates Threads
 * 
 */

public class Server extends Thread {
	
	/*
	 * Variables
	 */
	
	private ServerSocket serverSocket;
	private ArrayList<SocketServer> clients;
	private boolean didConnect;
	private int userId;
	
	/*
	 * Constructor
	 */
	
	public Server() throws IOException {
		// Init arrayList of clients
		this.clients = new ArrayList<SocketServer>();
		
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
				client = new SocketServer(this, server);
				
				// Start the thread for this user
				client.start();
				
				// Add to ArrayList to preserve all the sockets
				this.clients.add(client);
			} catch(SocketTimeoutException s) {
			} catch(IOException e) {
			}
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
		} catch(IOException e) {}
	}
}
