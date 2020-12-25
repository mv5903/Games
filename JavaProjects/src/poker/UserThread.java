package poker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class UserThread extends Thread {
	private Socket socket;
	private Server server;
	private PrintWriter writer;
	
	public UserThread(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
	}

	public void run() {
		try {
			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			OutputStream output = socket.getOutputStream();
			writer = new PrintWriter(output, true);
			printUsers();
			String userName = reader.readLine();
			server.addUserName(userName);
			String serverMessage = userName + " has joined the poker server.";
			server.broadcast(serverMessage, this);
			String clientMessage;
			do {
				clientMessage = reader.readLine();
				serverMessage = userName + ": " + clientMessage;
				server.broadcast(serverMessage, this);
			} while (!clientMessage.equalsIgnoreCase("quit"));
			server.removeUser(userName, this);
			socket.close();
			serverMessage = userName + " has left.";
			server.broadcast(serverMessage, this);
		} catch (IOException e) {
			System.out.println("Error in UserThread: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	void printUsers() {
		if (server.hasUsers()) {
			writer.println("Users connected: " + server.getUserNames());
		} else {
			writer.println("No other users connected!");
		}
	}
	
	void sendMessage(String message) {
		writer.println(message);
	}
}
