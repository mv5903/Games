package poker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
	private int port;
	private Set<String> userNames = new HashSet<>();
	private Set<UserThread> userThreads = new HashSet<>();
	
	public Server(int port) {
		this.port = port;
	}
	
	public void execute() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server is now listening on port " + port);
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("Someone has connected.");
				UserThread newUser = new UserThread(socket, this);
				userThreads.add(newUser);
				newUser.start();
			}
		} catch (IOException e) {
			System.out.println("An error occured: " + e.getMessage());
		} 
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Syntax: java server <port-number>");
			System.exit(0);	
		}
		int port = Integer.parseInt(args[0]);
		Server server = new Server(port);
		server.execute();
	}
	
	void broadcast(String message, UserThread excludeUser) {
		for (UserThread aUser: userThreads) {
			if (aUser != excludeUser) {
				aUser.sendMessage(message);
			}
		}
	}
	
	void addUserName(String userName) {
		userNames.add(userName);
	}
	
	void removeUser(String userName, UserThread aUser) {
		if (userNames.remove(userName)) {
			userThreads.remove(aUser);
			System.out.println(userName + " has left.");
		}
	}
	
	Set<String> getUserNames() {
		return this.userNames;
	}
	
	boolean hasUsers() {
		return !this.userNames.isEmpty();
	}
}
