package poker;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.charset.Charset;

import static java.lang.System.out;

public class ChatServer implements Constants {
	Vector<String> users = new Vector<String>();
	Vector<HandleClient> clients = new Vector<HandleClient>();
	String response = "";

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
			String line;
			try {
				while (true) {
					line = input.readLine();
					response = line.substring(line.indexOf(":") + 1);
					if ((name + ": " + line).equals("admin: start")) { //start the game
						Game g = new Game();
						g.start();
						break;
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
					broadcast(name, line);
				}
			}
			catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		
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
				bet = new Bet();
			}
			
			public String toString() {
				String fold = hasFolded ? "has folded" : "has not folded";
				return String.format("%s has %s and %s\n", name, hand, fold);
			}
		}
		// CHECK RAISE LOGIC
		class Game extends Thread {
			ArrayList<Player> players;
			DeckOfCards cards;
			CenterHand center;
			boolean isFreshGame;
			int currentPlayer = 0, raisedPlayer = 0, totalPlayers = 0, currentBet = 0, round = 0;
			
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
				startNewRound();
			}
			
			public void startNewRound() {
				if (round != 0) {
					sendToAll("A card has been added to the center. The center is now: " + center);
				}
				for (Player p: players) {
					sendPrivately(p.name, "Your hand is " + p.hand + ".");
					setEditable(p.name, false);
				}
				takeTurn();
				currentPlayer++;
				while (currentPlayer != raisedPlayer) {
					if (currentPlayer == totalPlayers) {
						currentPlayer = 0;
					}
					takeTurn();
					currentPlayer++;
				}
			}
			
			public void takeTurn() {
				if (!players.get(currentPlayer).hasFolded) {
					String selection = waitForPlayerInput(players.get(currentPlayer).name, "Place a bet of " + currentBet + ", type \"raise\" to raise, or type \"fold\" to fold.");
					if (selection.equalsIgnoreCase("fold")) {
						players.get(currentPlayer).hasFolded = true;
					} else if (selection.equalsIgnoreCase("raise")) {
						while (true) {
							raisedPlayer = currentPlayer;
							String raiseAmount = waitForPlayerInput(players.get(currentPlayer).name, "How much would you like to raise by?");
							int amountRaised = 0;
							try {
								amountRaised = Integer.parseInt(raiseAmount);
							} catch (Exception e) {
								sendPrivately(players.get(currentPlayer).name, "Looks like you entered something other than a number. Please try again");
								continue;
							}
							if (amountRaised == 0) {
								sendPrivately(players.get(currentPlayer).name, "Uh, why did you raise in the first place?");
								takeTurn();
							}
							sendToAll(players.get(currentPlayer).name + "raised the bet by " + amountRaised + "! All remaining players must call " + currentBet + " to continue playing this round.");
							currentBet+=amountRaised;
						}
					} else {
						int amount;
						try {
							amount = Integer.parseInt(selection);
						} catch (Exception e) {
							sendPrivately(players.get(currentPlayer).name, "Sorry, you entered something that is not recognized. Please try again.");
							takeTurn();
						}
						amount = Integer.parseInt(selection);
						if (amount != currentBet) {
							sendPrivately(players.get(currentPlayer).name, "Sorry, you must enter the current amount or you may type \"raise\" to indicate that you want to raise.");
							takeTurn();
						}
						players.get(currentPlayer).bet.bet(amount);
					}
				}
			}
			
			public boolean allFoldedButOne() {
				boolean oneFolded = false;
				for (Player p: players) {
					if (oneFolded) {
						return false;
					}
					if (p.hasFolded) {
						oneFolded = true;
					}
				}
				return true;
			}
			
			public String waitForPlayerInput(String user, String message) {
				String theResponse = "";		
				setEditable(user, true);
				sendPrivately(user, message);
				response = "";
				while (true) {
					sleep();
					if (!response.equals("")) {
						theResponse = response;
						response = "";
						break;
					}
				}
				setEditable(user, false);
				return theResponse;
			}
			
			public void sendToAll(String message) {
				broadcast("From server", message);
			}
			
			public void allowEntryAllPlayers(boolean canSend) {
				for (Player p: players) {
					setEditable(p.name, canSend);
				}
			}
			
			public void setEditable(String user, boolean canSend) {
				String toSend = canSend ? "allow send button" : "disable send button";
				sendPrivately(user, toSend);
			}
			
			public void sleep() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {};
			}
		}
	} 

	

}