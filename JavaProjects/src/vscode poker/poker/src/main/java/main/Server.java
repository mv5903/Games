import java.io.BufferedReader;
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
	static int currentPlayer = 0;

	public void process() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket server = new ServerSocket(9999, 10);
		System.out.println("Server Started...");
		while (true) {
			Socket client = server.accept();
			HandleClient c;
			try {
				c = new HandleClient(client);
				System.out.println(users);
				clients.add(c);
				broadcast(c.getUserName(), c.getUserName() + " has joined! " + getRandomSuit());
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
		int counter = 0;
		for (HandleClient c : clients) {
			if (hasStarted && !c.name.equals("admin")) {
				try {
					c.sendMessage("DATA", String.format("POT:%s;CENTER:%s~CP:%s^BAL:%d>HAND:%s", Bet.pot.toString(), center.toString(), players.get(currentPlayer).name, players.get(counter).bet.getBalance(), players.get(counter).hand.toString()));
					counter++;
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			c.sendMessage(user, message);
		}
			
	}
	
	public void sendPrivately(String user, String message) {
		int counter = 0;
		for (int i = 0; i < clients.size(); i++) {
			if (hasStarted) {
				try {
					if (!(clients.get(i).name.equals("admin"))) {
						clients.get(i).sendMessage("DATA", String.format("POT:%s;CENTER:%s~CP:%s^BAL:%d>HAND:%s", Bet.pot.toString(), center.toString(), players.get(currentPlayer).name, players.get(counter).bet.getBalance(), players.get(counter).hand.toString()));
						counter++;
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
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
			String playerResponse;
			try {
				while (true) {
					line = input.readLine();
					response = line.substring(line.indexOf(":") + 1);
					playerResponse = line.substring(line.indexOf(":") + 1);
					if (playerResponse.equals("leave")) {
						broadcast(name, name + " has left.");
						for (Player p: players) {
							if (p.name.equals(name)) {
								p.hasFolded = true;
								p.active = false;
							}
						}
					}
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
			int raisedPlayer = 0, totalPlayers = 0, roundBet = 0, round = -1, dealer = 0;
			boolean firstBettingRound = true, isFirstRound = true, isFirstPlayer = true;
			
			public void run() {
				players = new ArrayList<Player>();
				cards = new DeckOfCards();
				center = new CenterHand(cards.getNextCard(), cards.getNextCard());
				for (String user: users) {
					if (user.equals("admin")) {
						continue;
					}
					totalPlayers++;
					players.add(new Player(new Hand(cards.getNextCard(), cards.getNextCard()), false, user));
				}
				hasStarted = true;
				sendToAll("Welcome to Poker! Dealing cards...");
				sleep();
				startNewRound();
			}
			
			public void startNewRound() {
				isFirstPlayer = true;
				round++;
				if (round > 0) {
					sleep();
					showBalances();
					center.dealNextCard(cards.getNextCard());
				}
				roundBet = 0;
				raisedPlayer = 0;
				if (round == 4) {
					decideWinner();
				}
				for (Player p: players) {
					p.currentBet = 0;
					p.betAlreadyThisRound = false;
					sleep();
					setEditable(p.name, false);
					p.bet.resetRoundBet();
				} 
				if (round > 0) {
					currentPlayer = dealer - 1;
				}
				while (true) {
					if (round == 0 && firstBettingRound) {
						firstBettingRound = false;
						handleBlinds();
						decideNextPlayer(false);
					}
					if (isFirstPlayer) {
						isFirstPlayer = false;
						decideNextPlayer(true);
						continue;
					} 
					decideNextPlayer(false);
				}
			}
			
			public void handleBlinds() {
				//increment dealer (little blind)
				if (!isFirstRound) {
					dealer++;
				}
				isFirstRound = false;
				if (dealer == totalPlayers) {
					dealer = 0;
				}
				int smallBlind, bigBlind;
				smallBlind = dealer;
				if (dealer + 1 == totalPlayers) {
					bigBlind = 0;
				} else {
					bigBlind = dealer + 1;
				}

				players.get(smallBlind).betAlreadyThisRound = true;
				players.get(bigBlind).betAlreadyThisRound = true;
				//forcefully bet because that's what we do around here
				if (players.get(smallBlind).bet.getBalance() < SMALL_BLIND) {
					int playerAmount = players.get(smallBlind).bet.getBalance();
					players.get(smallBlind).bet.bet(playerAmount);
					players.get(smallBlind).currentBet+=playerAmount;
					sendToAll(players.get(smallBlind).name + " is small blind and was only able to bet " + playerAmount + " because that's all they have left.");
				} else {
					players.get(smallBlind).bet.bet(SMALL_BLIND);
					players.get(smallBlind).currentBet+=SMALL_BLIND;
					sendToAll(players.get(smallBlind).name + " is small blind and bet " + SMALL_BLIND);
				}
				if (players.get(bigBlind).bet.getBalance() < BIG_BLIND) {
					int playerAmount = players.get(bigBlind).bet.getBalance();
					players.get(bigBlind).bet.bet(playerAmount);
					roundBet = playerAmount;
					players.get(bigBlind).currentBet+=playerAmount;
					sendToAll(players.get(bigBlind).name + " is big blind and was only able to bet " + playerAmount + " because that's all they have left.");
				} else {
					players.get(bigBlind).bet.bet(BIG_BLIND);
					roundBet = BIG_BLIND;
					players.get(bigBlind).currentBet+=BIG_BLIND;
					sendToAll(players.get(bigBlind).name + " is big blind and bet " + BIG_BLIND);
				}
				
				if (bigBlind + 1 == totalPlayers) {
					raisedPlayer = 0;
				} else {
					raisedPlayer = bigBlind + 1;
				}
				currentPlayer = raisedPlayer;
				//Get the next player going because blinds affect the raised player in unintended ways
				takeTurn();
				currentPlayer = getNextPlayer();
				takeTurn();
				decideNextPlayer(false);
			}
			
			public void takeTurn() {
				if (allFoldedButOne()) {
					for (Player p: players) {
						if (!p.hasFolded) {
							winner(p);
						}
					}
				}
				try {
					if (!players.get(currentPlayer).hasFolded) {
						response = "";
						if (players.get(currentPlayer).bet.getBalance() == 0 && roundBet > 0) {
							sendPrivately(players.get(currentPlayer).name, "Looks like you don't have any money. You will now be folded.");
							players.get(currentPlayer).hasFolded = true;
							return;
						}
						if (players.get(currentPlayer).bet.getBalance() + players.get(currentPlayer).currentBet < roundBet) {
							String selection = waitForPlayerInput(players.get(currentPlayer).name, "Sorry " + players.get(currentPlayer).name + ", you have to go all in or fold. Please type \"all in\" or type \"fold\" to fold.");
							if (selection.equalsIgnoreCase("all in")) {
								players.get(currentPlayer).bet.bet(players.get(currentPlayer).bet.getBalance());
								sendToAll(players.get(currentPlayer).name + " is poor, so they forcefully went all in.");
								return;
							} else if (selection.equalsIgnoreCase("fold")) {
								players.get(currentPlayer).hasFolded = true;
								return;
							} else {
								sendPrivately(players.get(currentPlayer).name, "Sorry, I don't know what you entered. Please try again.");
								takeTurn();
							}
						}
						String question = players.get(currentPlayer).betAlreadyThisRound ? " call the remaining " : ", place a bet of ";
						String selection = waitForPlayerInput(players.get(currentPlayer).name, players.get(currentPlayer).name + question + (roundBet - players.get(currentPlayer).currentBet) + ", type \"raise\" to raise, or type \"fold\" to fold.");
						if (selection.equalsIgnoreCase("fold")) {
							players.get(currentPlayer).hasFolded = true;
							sendToAll(players.get(currentPlayer).name + " has folded!");
							return;
						} else if (selection.equalsIgnoreCase("raise")) {
							while (true) {
								raisedPlayer = currentPlayer;
								response = "";
								String raiseAmount = waitForPlayerInput(players.get(currentPlayer).name, "What would you like to raise your bet to?");
								int amountRaised = grabInt(raiseAmount);
								if (amountRaised == -1) {
									sendPrivately(players.get(currentPlayer).name, "Looks like you entered something other than a number. Please try again");
									continue;
								} else if (amountRaised == 0) {
									sendPrivately(players.get(currentPlayer).name, "Uh, why did you raise in the first place?");
									continue;
								} else if (amountRaised > players.get(currentPlayer).bet.getBalance()) {
									sendPrivately(players.get(currentPlayer).name, "This is more than you have. Please bet again.");
									continue;
								} else if (amountRaised < players.get(currentPlayer).currentBet) {
									sendPrivately(players.get(currentPlayer).name, "You need to raise more than what you already have.");
									continue;
								} else if (amountRaised < roundBet) {
									sendPrivately(players.get(currentPlayer).name, "You need to raise more than the current round bet.");
									continue;
								} else {
									roundBet = amountRaised;
									players.get(currentPlayer).bet.bet(roundBet - players.get(currentPlayer).currentBet);
									players.get(currentPlayer).currentBet += roundBet;
									players.get(currentPlayer).betAlreadyThisRound = true;
									sendToAll(players.get(currentPlayer).name + " raised the bet to " + roundBet);
									return;
								}
							}
						} else {
							int amount = grabInt(selection);
							if (amount == -1) {
								sendPrivately(players.get(currentPlayer).name, "Sorry, you entered something that is not recognized. Please try again.");
								takeTurn();
							} else if (amount + players.get(currentPlayer).currentBet != roundBet) {
								sendPrivately(players.get(currentPlayer).name, "Sorry, you must enter the current amount or you may type \"raise\" to indicate that you want to raise.");
								takeTurn();
							} else if (amount == players.get(currentPlayer).bet.getBalance()) {
								sendPrivately(players.get(currentPlayer).name, "Going all in, huh?");
								return;
							} 
							else {
								players.get(currentPlayer).bet.bet(amount);
								players.get(currentPlayer).currentBet += amount;
								players.get(currentPlayer).betAlreadyThisRound = true;
								sendToAll(players.get(currentPlayer).name + " has checked.");
								return;
							}
						}
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
					startNewRound();
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
			
			public void decideNextPlayer(boolean isNewRound) {
				currentPlayer++;
				if (isNewRound) {
					takeTurn();
					decideNextPlayer(false);
					return;
				}
				if (currentPlayer == totalPlayers) {
					currentPlayer = 0;
				}
				if (currentPlayer == raisedPlayer) {
					startNewRound();
				}  
				else {
					takeTurn();
				}
			}
		
			public boolean allFoldedButOne() {
				int count = 0;
				for (Player p: players) {
					if (!p.hasFolded) {
						count++;
					}
				}
				return count <= 1;
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
						@SuppressWarnings("unused")
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
				firstBettingRound = true;
				isFirstPlayer = true;
				int pot = Bet.pot.winner();
				int potSplit = pot / players.size();
				sendToAll("There are multiple winners because " + listOfPlayers(players) + " tied with the same " + players.get(0).handType + ". The pot will be split amongst them.");
				for (Player p: players) {
					p.bet.updateBalance(p.bet.getBalance() + potSplit);
				}
				keepPlaying();
			}
			
			public void winner(Player player) {
				firstBettingRound = true;
				isFirstPlayer = true;
				if (allFoldedButOne()) {
					sendToAll(player.name + " won the round because everyone else folded!");
				} else {
					sendToAll(player.name + " won the round with a " + player.handType + "!");
				}
				player.bet.win();
				keepPlaying();
			}
			
			public void keepPlaying() {
				round = -1;
				raisedPlayer = 0;
				currentPlayer = 0;
				center = new CenterHand(cards.getNextCard(), cards.getNextCard());
				roundBet = 0;
				for (Player p: players) {
					p.hasFolded = p.active ? false : true;
					p.hand = new Hand(cards.getNextCard(), cards.getNextCard());
					p.handType = "Unassigned";
					p.handCombinedWithCenter = new ArrayList<Card>();
				}
				sendToAll(newRoundSaying());
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

			public int getNextPlayer() {
				int player = currentPlayer;
				player++;
				if (player == totalPlayers) {
					return 0;
				}
				return player;
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
			
			public String newRoundSaying() {
				String[] temp = {"Initiating next sequence.", "Starting a new round.", "On to the next round.", "Next round.", "Commencing next round.", "Time for another round.", "Beginning another round."};
				return temp[(int) (Math.random() * temp.length)];
			}

			public void showBalances() {
				String str = "|";
				for (Player p: players) {
					if (p.active) {
						str+=String.format(" %s : %s |", p.name, p.bet.getBalance());
					}
				}
				sendToAll(str);
			}
		}
	} 

	

}