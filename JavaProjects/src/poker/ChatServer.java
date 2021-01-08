package poker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class ChatServer implements Constants {
	Vector<String> users = new Vector<String>();
	Vector<HandleClient> clients = new Vector<HandleClient>();
	String response = "";
	ArrayList<Player> players;
	DeckOfCards cards;
	CenterHand center;
	boolean hasStarted = false;
	

	public void process() throws Exception {
		ServerSocket server = new ServerSocket(9999, 10);
		System.out.println("Server Started...");
		while (true) {
			Socket client = server.accept();
			HandleClient c;
			try {
				c = new HandleClient(client);
				System.out.println(users);
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
		int counter = 0;
		for (int i = 0; i < clients.size(); i++) {
			if (hasStarted) {
				if (!(clients.get(i).name.equals("admin"))) {
					System.out.println(clients.get(i).name);
					clients.get(i).sendMessage("DATA", String.format("POT:%d;CENTER:%s~CP:%s^BAL:%d>HAND:%s", Integer.parseInt(Bet.pot.toString()), center.toString(), players.get(counter).name, players.get(counter).bet.getBalance(), players.get(counter).hand.toString()));
					counter++;
				}
			}
			if (clients.get(i).getUserName().equals(user)) {
				clients.get(i).sendMessage("Privately to you", message);
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
			if (users.size() >= 1 && users.contains(name)) {
				sendMessage(name, "is duplicate!");
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
						System.out.println(messageToSend);
						sendPrivately(userToSendTo, messageToSend);
						continue;
					}
				}
			}
			catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		
		class Game extends Thread {
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
				currentBet = 0;
				if (round == 4) {
					decideWinner();
				}
				if (round != 0) {
					currentPlayer = 0;
					center.dealNextCard(cards.getNextCard());
					sendToAll("A card has been added to the center. The center is now: " + center);
				}
				raisedPlayer = 0;
				for (Player p: players) {
					sendPrivately(p.name, "Hand: " + p.hand);
					sleep();
					setEditable(p.name, false);
				}
				hasStarted = true;
				if (round == 0) {
					sendPrivately(players.get(0).name, "Welcome to Poker!");
				}
				takeTurn();
				while (true) {
					if (allFoldedButOne()) {
						for (Player p: players) {
							if (!p.hasFolded) {
								winner(p);
							}
						}
					}
					decideNextPlayer();
				}
			}
			
			public void takeTurn() {
				if (!players.get(currentPlayer).hasFolded) {
					response = "";
					if (players.get(currentPlayer).bet.getBalance() == 0 && currentBet > 0) {
						sendPrivately(players.get(currentPlayer).name, "Looks like you don't have any money. You will now be folded.");
						players.get(currentPlayer).hasFolded = true;
						return;
					}
					if (players.get(currentPlayer).bet.getBalance() < currentBet) {
						String selection = waitForPlayerInput(players.get(currentPlayer).name, "Sorry " + players.get(currentPlayer).name + ", you have to go all in or fold. Please type \"all in\" or type \"fold\" to fold.");
						if (selection.equalsIgnoreCase("all in")) {
							players.get(currentPlayer).bet.bet(players.get(currentPlayer).bet.getBalance());
							sendToAll(players.get(currentPlayer).name + " is poor, so he forcefully went all in.");
							return;
						} else if (selection.equalsIgnoreCase("fold")) {
							players.get(currentPlayer).hasFolded = true;
							return;
						} else {
							sendPrivately(players.get(currentPlayer).name, "Sorry, I don't know what you entered. Please try again.");
							takeTurn();
						}
					}
					String selection = waitForPlayerInput(players.get(currentPlayer).name, players.get(currentPlayer).name + " Place a bet of " + currentBet + ", type \"raise\" to raise, or type \"fold\" to fold. Your current balance: " + players.get(currentPlayer).bet.getBalance());
					if (selection.equalsIgnoreCase("fold")) {
						players.get(currentPlayer).hasFolded = true;
						sendToAll(players.get(currentPlayer).name + " has folded!");
						return;
					} else if (selection.equalsIgnoreCase("raise")) {
						while (true) {
							raisedPlayer = currentPlayer;
							response = "";
							String raiseAmount = waitForPlayerInput(players.get(currentPlayer).name, "How much would you like to raise by?");
							int amountRaised = grabInt(raiseAmount);
							if (amountRaised == -1) {
								sendPrivately(players.get(currentPlayer).name, "Looks like you entered something other than a number. Please try again");
								takeTurn();
							} else if (amountRaised == 0) {
								sendPrivately(players.get(currentPlayer).name, "Uh, why did you raise in the first place?");
								takeTurn();
							} else if (amountRaised > players.get(currentPlayer).bet.getBalance()) {
								sendPrivately(players.get(currentPlayer).name, "This is more than you have. Please bet again.");
								takeTurn();
							} else {
								currentBet+=amountRaised;
								sendToAll(players.get(currentPlayer).name + " raised the bet by " + amountRaised + "! All remaining players must call " + currentBet + " to continue playing this round.");
								return;
							}
						}
					} else {
						int amount = grabInt(selection);
						if (amount == -1) {
							sendPrivately(players.get(currentPlayer).name, "Sorry, you entered something that is not recognized. Please try again.");
							takeTurn();
						} else if (amount != currentBet) {
							sendPrivately(players.get(currentPlayer).name, "Sorry, you must enter the current amount or you may type \"raise\" to indicate that you want to raise.");
							takeTurn();
						} else if (amount == players.get(currentPlayer).bet.getBalance()) {
							sendPrivately(players.get(currentPlayer).name, "Going all in, huh?");
							return;
						} 
						else {
							players.get(currentPlayer).bet.bet(amount);
							sendToAll(players.get(currentPlayer).name + " has checked.");
							return;
						}
					}
				}
			}
			
			public int grabInt(String str) {
				String toReturn = "";
				for (char c: str.toCharArray()) {
					if (!Character.isDigit(c)) {
						return -1;
					} else {
						toReturn+=c;
					}
				}
				return Integer.parseInt(toReturn);
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
					if (p.hasFolded && oneFolded) {
						return false;
					}
					if (p.hasFolded) {
						oneFolded = true;
					}
				}
				return oneFolded;
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
					System.out.println(p.name + ": " + p.handType);
				}
				//Create the order of hands
				HashMap<String, Integer> handOrder = new HashMap<String, Integer>();
				String[] handNames = { "Royal Flush", "Straight Flush", "Four of a Kind", "Full House", "Flush", "Straight",
						"Three of a Kind", "Two Pair", "Pair", "High Card" };
				for (int i = 0; i < handNames.length; i++) {
					handOrder.put(handNames[i], i + 1);
				}
				
				ArrayList<Player> decidePlayers = new ArrayList<Player>();
				decidePlayers.addAll(players);
				//Sort players based on hand
				Player min;
				int minIndex;
				for (int i = 0; i < decidePlayers.size() - 1; i++) {
					min = decidePlayers.get(i);
					minIndex = i;
					for (int j = i; j < decidePlayers.size(); j++) {
						if (handOrder.get(decidePlayers.get(j).handType).compareTo(handOrder.get(min.handType)) < 0) {
							min = decidePlayers.get(j);
							minIndex = j;
						}
					}
					Player temp = decidePlayers.get(i);
					decidePlayers.set(i, decidePlayers.get(minIndex));
					decidePlayers.set(minIndex, temp);
				}
				//Removed everyone that folded
				for (Player p: decidePlayers) {
					if (p.hasFolded) {
						decidePlayers.remove(p);
					}
				}
				//Remove all that have a lower hand
				String highestHand = decidePlayers.get(0).handType;
				System.out.println("HIGHEST HAND: " + highestHand);
				for (int i = decidePlayers.size() - 1; i >= 0; i--) {
					if (!decidePlayers.get(i).handType.equals(highestHand)) {
						decidePlayers.remove(i);
					}
				}
				//If only one person remains in this list (one person has the highest hand)
				if (decidePlayers.size() == 1) {
					winner(decidePlayers.get(0));
					return;
				}
				//If multiple people remain
				//1: See if the higher of the hand is enough
				try {
					if (highestHand.equals("Two Pair") || highestHand.equals("Full House")) {
						//If a two pair or full house, we need to check both sets of the hand to see who wins
						ArrayList<ArrayList<Card>> pair = new ArrayList<ArrayList<Card>>();
						for (int i = 0; i < decidePlayers.size(); i++) {
							pair.add(new ArrayList<Card>());
							pair.get(i).addAll(UniqueHands.isolateHand(decidePlayers.get(i).handCombinedWithCenter, highestHand));
							
							//Remove the duplicates
							for (int j = pair.get(i).size() - 2; j >= 0; j--) { 
								if (pair.get(i).get(j).getFaceValue() == pair.get(i).get(j+1).getFaceValue()) {
									pair.get(i).remove(j);
								}
							}
							pair.set(i, UniqueHands.reverse(pair.get(i)));
						}
						//1: Check the first card
						//1a: Find the highest card
						Card highest = pair.get(0).get(0);
						for (ArrayList<Card> c: pair) {
							if (c.get(0).getFaceValue() > highest.getFaceValue()) {
								highest = c.get(0);
							}
						}
						//1b: Anyone who doesn't have this card gets removed
						for (int i = pair.size() - 1; i >= 0; i--) {
							if (pair.get(i).get(0).getFaceValue() < highest.getFaceValue()) {
								pair.remove(i);
								decidePlayers.remove(i);
							}
						}
						//2: If multiple players still remain, look at the second card!
						if (pair.size() == 1) {
							winner(decidePlayers.get(0)); 
							return;
						}
						//2a: Find the highest card
						highest = pair.get(0).get(1);
						for (ArrayList<Card> c: pair) {
							if (c.get(1).getFaceValue() > highest.getFaceValue()) {
								highest = c.get(1);
							}
						}
						//2b: Anyone who doesn't have this card gets removed
						for (int i = pair.size() - 1; i >= 0; i--) {
							if (pair.get(i).get(1).getFaceValue() < highest.getFaceValue()) {
								pair.remove(i);
								decidePlayers.remove(i);
							}
						}
						if (pair.size() == 1) {
							winner(decidePlayers.get(0));
						}
					} else {
						Card temp, higher = UniqueHands.highCard(UniqueHands.isolateHand(decidePlayers.get(0).handCombinedWithCenter, highestHand));
						for (int i = decidePlayers.size() - 1; i >= 0; i--) {
							temp = UniqueHands.highCard(UniqueHands.isolateHand(decidePlayers.get(i).handCombinedWithCenter, highestHand));
							if (temp.getFaceValue() < higher.getFaceValue()) {
								decidePlayers.remove(i);
							}
						}
						//2: If everyone has high card, then the person with the highest cards in their hand wins
						Player higherPlayer = decidePlayers.get(0);
						Card highest = UniqueHands.highCard(decidePlayers.get(0).hand.getArrayListOfHand());
						for (Player p: decidePlayers) {
							if (UniqueHands.highCard(p.hand.getArrayListOfHand()).getFaceValue() > highest.getFaceValue()) {
								higherPlayer = p;
								highest = UniqueHands.highCard(p.hand.getArrayListOfHand());
							}
						}
						for (int i = decidePlayers.size() - 1; i >= 0; i--) {
							if (UniqueHands.highCard(decidePlayers.get(i).hand.getArrayListOfHand()).getFaceValue() < highest.getFaceValue()) {
								decidePlayers.remove(i);
							}
						}
						if (decidePlayers.size() == 1) {
							winner(decidePlayers.get(0));
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				//3: Find 5 highest card combo by adding first five cards
				int highestSum = 0;
				for (int i = decidePlayers.size() - 1; i >= 0; i--) {
					int tempSum = 0;
					decidePlayers.get(i).handCombinedWithCenter = UniqueHands.reverse(UniqueHands.sortCardsByNumber(decidePlayers.get(i).handCombinedWithCenter));
					for (int j = 0; j < 5; j++) {
						tempSum+=decidePlayers.get(i).handCombinedWithCenter.get(j).getFaceValue();
					}
					if (tempSum > highestSum) {
						highestSum = tempSum;
					}
				}
				for (int i = decidePlayers.size() - 1; i >= 0; i--) {
					int tempSum = 0;
					decidePlayers.get(i).handCombinedWithCenter = UniqueHands.reverse(UniqueHands.sortCardsByNumber(decidePlayers.get(i).handCombinedWithCenter));
					for (int j = 0; j < 5; j++) {
						tempSum+=decidePlayers.get(i).handCombinedWithCenter.get(j).getFaceValue();
					}
					if (tempSum < highestSum) {
						decidePlayers.remove(i);
					}
				}
				
				if (decidePlayers.size() == 1) {
					winner(decidePlayers.get(0));
					return;
				}
				
				//4: Absolute worse case scenario: tie for now, figure out kickers later.
				if (decidePlayers.size() > 1) {
					splitPot(decidePlayers.size(), decidePlayers);
					return;
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
					Thread.sleep(20);
				} catch (InterruptedException e) {};
			}
		}
	} 

	

}