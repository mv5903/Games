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
						g.setName("Game Thread");
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
			String handType;
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
			int currentPlayer = 0, raisedPlayer = 0, totalPlayers = 0, currentBet = 0, round = -1;
			
			public void run() {
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
				round++;
				if (round != 0) {
					center.dealNextCard(cards.getNextCard());
					sendToAll("A card has been added to the center. The center is now: " + center);
				}
				if (round == 4) {
					decideWinner();
				}
				raisedPlayer = 0;
				for (Player p: players) {
					sendPrivately(p.name, "Your hand is " + p.hand + ".");
					setEditable(p.name, false);
				}
				takeTurn();
				while (true) {
					decideNextPlayer();
				}
			}
			
			public void takeTurn() {
				if (!players.get(currentPlayer).hasFolded) {
					String selection = waitForPlayerInput(players.get(currentPlayer).name, players.get(currentPlayer).name + " Place a bet of " + currentBet + ", type \"raise\" to raise, or type \"fold\" to fold. Your current balance: " + players.get(currentPlayer).bet.getBalance());
					if (selection.equalsIgnoreCase("fold")) {
						players.get(currentPlayer).hasFolded = true;
						return;
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
							sendToAll(players.get(currentPlayer).name + " raised the bet by " + amountRaised + "! All remaining players must call " + currentBet + " to continue playing this round.");
							currentBet+=amountRaised;
							return;
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
						return;
					}
				}
			}
			
			public void decideNextPlayer() {
				currentPlayer++;
				if (currentPlayer == raisedPlayer) {
					startNewRound();
				} else if (currentPlayer == totalPlayers) {
					currentPlayer = 0;
					if (currentPlayer == raisedPlayer) {
						startNewRound();
					}
					takeTurn();
				} else {
					takeTurn();
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
			
			public void decideWinner() {
				//Combine hand with center
				for (Player p: players) {
					p.handCombinedWithCenter = new ArrayList<Card>();
					p.handCombinedWithCenter.addAll(p.hand.getArrayListOfHand());
					p.handCombinedWithCenter.addAll(center.getCenter());
				}
				//Find what hand name each player has
				for (Player p: players) {
					p.handType = UniqueHands.hasWhichHand(p.handCombinedWithCenter);
				}
				//Create the order of hands
				HashMap<String, Integer> handOrder = new HashMap<String, Integer>();
				String[] handNames = { "Royal Flush", "Straight Flush", "Four of a Kind", "Full House", "Flush", "Straight",
						"Three of a Kind", "Two Pair", "Pair", "High Card" };
				for (int i = 0; i < handNames.length; i++) {
					handOrder.put(handNames[i], i + 1);
				}
				//Sort players based on hand
				Player min;
				int minIndex;
				for (int i = 0; i < players.size() - 1; i++) {
					min = players.get(i);
					minIndex = i;
					for (int j = i; j < players.size(); j++) {
						if (handOrder.get(players.get(j).handType).compareTo(handOrder.get(min.handType)) < 0) {
							min = players.get(j);
							minIndex = j;
						}
					}
					Player temp = players.get(i);
					players.set(i, players.get(minIndex));
					players.set(i, temp);
				}
				ArrayList<Player> decidePlayers = new ArrayList<Player>();
				decidePlayers.addAll(players);
				//Removed everyone that folded
				for (Player p: decidePlayers) {
					if (p.hasFolded) {
						decidePlayers.remove(p);
					}
				}
				//Remove all that have a lower hand
				String highestHand = decidePlayers.get(0).handType;
				for (Player p: decidePlayers) {
					if (!p.handType.equals(highestHand)) {
						decidePlayers.remove(p);
					}
				}
				//If only one person remains in this list (one person has the highest hand)
				if (decidePlayers.size() == 1) {
					winner(decidePlayers.get(0));
				}
				//If multiple people remain
				//1: See if the higher of the hand is enough
				try {
					Card temp, highest = UniqueHands.highCard(UniqueHands.isolateHand(decidePlayers.get(0).handCombinedWithCenter, highestHand));
					for (Player p: decidePlayers) {
						temp = UniqueHands.highCard(UniqueHands.isolateHand(p.handCombinedWithCenter, highestHand));
						if (temp.getFaceValue() < highest.getFaceValue()) {
							decidePlayers.remove(p);
						}
					}
				} catch (Exception e) {};
				//2: Absolute worse case scenario: tie for now, figure out kickers later.
				if (decidePlayers.size() > 1) {
					splitPot(decidePlayers.size(), decidePlayers);
				}
				
			}
			
			public void splitPot(int amountOfWinners, ArrayList<Player> players) {
				int pot = Bet.pot.winner();
				int potSplit = pot / players.size();
				sendToAll("There are multiple winners because " + listOfPlayers(players) + " tied with the same " + players.get(0).handType + ". The pot will be split amongst them.");
				for (Player p: players) {
					p.bet.updateBalance(p.bet.getBalance() + potSplit);
				}
				keepPlaying();
			}
			
			public void winner(Player player) {
				sendToAll("Congrats, " + player.name + "! You won the round!");
				player.bet.win();
				keepPlaying();
			}
			
			public void keepPlaying() {
				round = -1;
				raisedPlayer = 0;
				currentPlayer = 0;
				center = new CenterHand(cards.getNextCard(), cards.getNextCard());
				currentBet = 0;
				for (Player p: players) {
					p.hasFolded = false;
					p.hand = new Hand(cards.getNextCard(), cards.getNextCard());
					p.handType = "Unassigned";
					p.handCombinedWithCenter = new ArrayList<Card>();
				}
				startNewRound();
			}
			
			public String listOfPlayers(ArrayList<Player> players) { 
				String str = "";
				for (int i = 0; i < players.size(); i++) {
					if (i == players.size() - 1) {
						str+=players.get(i).name;
					} else {
						str+=players.get(i).name + ", ";
					}
				}
				return str;
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
				sleep();
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