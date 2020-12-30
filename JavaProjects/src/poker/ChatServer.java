package poker;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.charset.Charset;

import static java.lang.System.out;

public class ChatServer implements Constants {
	Vector<String> users = new Vector<String>();
	Vector<HandleClient> clients = new Vector<HandleClient>();

	public void process() throws Exception {
		ServerSocket server = new ServerSocket(9999, 10);
		out.println("Server Started...");
		while (true) {
			Socket client = server.accept();
			HandleClient c;
			try {
				c = new HandleClient(client);
				out.println(users);
				clients.add(c);
				broadcast(c.getUserName(), " has joined! " + String.valueOf(SPADES));
			} catch (Exception e) {
				if (e.getMessage().equals("Duplicate User")) {
					continue;
				}
			}
		} // end of while
	}

	public static void main(String... args) throws Exception {
		new ChatServer().process();
	} // end of main
	
	public void broadcast(String user, String message) {
		// send message to all connected users
		for (HandleClient c : clients)
			c.sendMessage(user, message);
	}

	class HandleClient extends Thread {
		String name = "";
		BufferedReader input;
		PrintWriter output;

		public HandleClient(Socket client) throws Exception {
			// get input and output streams
			input = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
			output = new PrintWriter(client.getOutputStream(), true, Charset.forName("UTF-8"));
			// read name
			name = input.readLine();
			if (users.size() > 1 && users.contains(name)) {
				sendMessage(name + "-privately", " duplicate user name exists");
				throw new Exception("Duplicate User");
			} else {
				users.add(name); // add to vector
				start();
				
			}
			
		}

		public void sendMessage(String uname, String msg) {
			output.println(uname + ":" + msg);
		}

		public String getUserName() {
			return name;
		}

		public void run() {
			String line;
			try {
				while (true) {
					line = input.readLine();
					if (line.equals("end")) {
						clients.remove(this);
						users.remove(name);
						out.println(users);
						break;
					}
					broadcast(name, line); // method of outer class - send messages to all
				} // end of while
			} // try
			catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		} // end of run()
	} // end of inner class

} // end of Server