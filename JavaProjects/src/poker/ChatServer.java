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
				broadcast(c.getUserName(), "has joined! " + getRandomSuit());
			} catch (Exception e) {
				if (e.getMessage().equals("Duplicate User")) {
					continue;
				}
			}
		} // end of while
	}
	
	public String getRandomSuit() {
		char[] suits = {CLUBS, DIAMONDS, HEARTS, SPADES};
		return String.valueOf(suits[(int) (Math.random() * 4)]);
	}

	public static void main(String... args) throws Exception {
		new ChatServer().process();
	} // end of main
	
	public void broadcast(String user, String message) {
		// send message to all connected users
		for (HandleClient c : clients)
			c.sendMessage(user, message);
	}
	
	public void sendPrivately(String user, String message) {
		for (HandleClient client: clients) {
			if (client.getUserName().equals(user)) {
				client.sendMessage("Privately to you", message);
			}
 		}
	}

	class HandleClient extends Thread {
		String name = "";
		BufferedReader input;
		PrintWriter output;

		public HandleClient(Socket client) throws Exception {
			input = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
			output = new PrintWriter(client.getOutputStream(), true, Charset.forName("UTF-8"));
			name = input.readLine();
			if (users.size() > 1 && users.contains(name)) {
				sendMessage(name + "-privately", "duplicate user name exists");
				throw new Exception("Duplicate User");
			} else {
				users.add(name);
				start();
			}
		}

		public void sendMessage(String uname, String msg) {
			output.println(uname + ": " + msg);
		}

		public String getUserName() {
			return name;
		}

		public void run() {
			Game g;
			String line;
			try {
				while (true) {
					line = input.readLine();
					System.out.println("Server sees: " + name +  ": " + line);
					if ((name + ": " + line).equals("admin: start")) { //start the game
						g = new Game();
						g.start();
					}
					if ((name + ": " + line).equals("admin: stop")) {
						System.exit(0);
					}
					if (line.contains("PRIVATE TO")) {
						String userToSendTo = line.substring(11, line.indexOf(":"));
						System.out.println(userToSendTo);
						String messageToSend = line.substring(line.indexOf(":") + 1);
						out.println(messageToSend);
						sendPrivately(userToSendTo, messageToSend);
						continue;
					}
					if (line.equals("end")) {
						clients.remove(this);
						users.remove(name);
						out.println(users);
						break;
					}
					broadcast(name, line);
				} 
			}
			catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	} 
	
	// WHERE THE GAME TAKES PLACE
	class Player {
		Bet bet;
		Hand hand;
		boolean hasFolded;
		String name;
		ArrayList<Card> handCombinedWithCenter;
		
		Player(Hand hand, boolean hasFolded, String name) {
			this.hand = hand;
			this.hasFolded = hasFolded;
			this.name = name;
		}
		
		public String toString() {
			String fold = hasFolded ? "has folded" : "has not folded";
			return String.format("%s has %s and %s\n", name, hand, fold);
		}
	}
	
	class Game extends Thread {
		ArrayList<Player> players;
		DeckOfCards cards;
		CenterHand center;
		boolean isFreshGame;
		int currentPlayer = 0, raisedPlayer = 0, totalPlayers = 0, currentBet = 0;
		
		public void run() {
			isFreshGame = false;
			players = new ArrayList<Player>();
			cards = new DeckOfCards();
			center = new CenterHand(cards.getNextCard(), cards.getNextCard());
			sendToAll("Welcome to Poker! Dealing cards...");
			for (String user: users) {
				if (user.equals("admin")) {
					continue;
				}
				totalPlayers++;
				players.add(new Player(new Hand(cards.getNextCard(), cards.getNextCard()), false, user));
			}
			sleep();
			for (Player p: players) {
				sendPrivately(p.name, "Hi " + p.name + "! Here is your hand: " + p.hand);
				
				allowEntry(p.name, false);
			}
		}
		
		public void sendToAll(String message) {
			broadcast("From server", message);
		}
		
		public void allowEntry(String user, boolean canSend) {
			String toSend = canSend ? "allow send button" : "disable send button";
			String endis = canSend ? "Enabling" : "Disabling";
			sendPrivately(user, endis + " user input for " + user); // Remove for final version
			sendPrivately(user, toSend);
		}
		
		public void sleep() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {};
		}
	}

}