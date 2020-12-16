package poker;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//♠ (To force UTF-8 saving)
public class Index {
	static PrintStream charStream;
	static DeckOfCards cards;
	static CenterHand center;
	static ArrayList<Hand> hands;
	static ArrayList<Bet> bets;
	static ArrayList<Boolean> hasFolded = new ArrayList<Boolean>();
	static int currentPlayer = 0, raisedPlayer = 0;
	static int totalPlayers;
	static int currentBet = 0;
	static boolean isFreshGame = true;
	static Scanner kbd = new Scanner(System.in);

	public static void main(String[] args) {
		if (args.length == 0 || args[0].equals("Restart")) {
			try {
				charStream =  new PrintStream(System.out, true, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			System.out.println("Welcome to Poker!");
			cards = new DeckOfCards();
			center = new CenterHand(cards.getNextCard(), cards.getNextCard());
			System.out.println("How many players?");
			totalPlayers = kbd.nextInt();
			dealAllCards();
		} else if (args[0].equals("New Game")) {
			isFreshGame = true;
			center.getCenter().clear();
			hands.clear();
			bets.clear();
			hasFolded.clear();
			hasFolded = new ArrayList<Boolean>();
			currentPlayer = 0;
			raisedPlayer = 0;
			totalPlayers = 0;
			currentBet = 0;
			String[] restart = {"Restart"};
			main(restart);
		} else if (args[0].equals("Continue Game")) {
			isFreshGame = false;
			center.getCenter().clear();
			hands.clear();
			currentBet = 0;
			currentPlayer = 0;
			raisedPlayer = 0;
			hasFolded.clear();
			dealAllCards();
		}
	}

	public static void dealAllCards() {
		System.out.println("Dealing cards: ");
		//showLoadingAnimation();
		hands = new ArrayList<Hand>();
		if (isFreshGame) {
			bets = new ArrayList<Bet>();
		}
		for (int i = 0; i < totalPlayers; i++) {
			hands.add(new Hand(cards.getNextCard(), cards.getNextCard()));
			if (isFreshGame) {
				bets.add(new Bet());
			}
			hasFolded.add(false);
		}
		isFreshGame = false;
		startARound();
	}

	public static void startARound() {
		if (!hasFolded(currentPlayer)) {
			charStream.println("Player " + (currentPlayer + 1) + ", here are your cards: " + hands.get(currentPlayer)
					+ ". Place a bet of at least " + currentBet + ". Fold with -1, raise with -2.\n(" + bets.get(currentPlayer) + ").");
			int playersBet = kbd.nextInt();
			
			// If the player folds
			if (playersBet == -1) {
				fold(currentPlayer);
				nextPlayer(false);
			} else {
				// Player raises
				if (playersBet == -2) {
					raise();
				}
				// If no one has done anything, just set the bet size to the first bet
				if (center.getCenter().size() == 2 && currentPlayer == 0) {
					currentBet = playersBet;
				}
				// If they enter another amount
				if (playersBet != currentBet) {
					System.out.println("Sorry, your bet needs to be at least " + currentBet + " to continue.");
					startARound();
				}
				// Otherwise, bet the amount and go on to the next player
				bets.get(currentPlayer).bet(playersBet);
				nextPlayer(false);
			}
		} else {
			nextPlayer(false);
		}
	}

	public static void raise() {
		System.out.println("What will you be raising the total bet to?");
		int newBet = kbd.nextInt();
		bets.get(currentPlayer).bet(newBet - currentBet);
		raisedPlayer = currentPlayer;
		currentBet = newBet;
		nextPlayer(true);
	}

	public static void nextPlayer(boolean raised) {
		if (currentPlayer+1 == totalPlayers) {
			currentPlayer = 0;
		} else {
			currentPlayer++;
		}
		// A full round has finished
		if (currentPlayer == raisedPlayer && !raised) {
			if (center.getCenter().size() == 5) {
				seeWhoWins();
				return;
			} else {
				center.dealNextCard(cards.getNextCard());
				charStream.println("There is a new center hand: " + center);
				startARound();
			}
		}
		startARound();
	}

	public static void fold(int player) {
		hasFolded.set(player, true);
	}

	public static boolean hasFolded(int player) {
		return hasFolded.get(player);
	}

	public static void seeWhoWins() {
		class WhoHasWhat {
			ArrayList<Card> hand;
			String whatTheyHave;
			int playerNumber;
			boolean folded;
			ArrayList<Card> theirTwoCards;
			Card highestCard;
			WhoHasWhat(int playerNumber, ArrayList<Card> hand, boolean folded, ArrayList<Card> theirTwoCards) {
				this.playerNumber = playerNumber;
				this.hand = hand;
				this.folded = folded;
				whatTheyHave = UniqueHands.hasWhichHand(hand);
				this.theirTwoCards = theirTwoCards;
				highestCard = UniqueHands.highCard(theirTwoCards);
			}
			public String toString() {
				String didTheyFold = folded ? "folded" : "not folded";
				return String.format("Player %d has %s (%s) and has %s.\n", playerNumber, hand, whatTheyHave, didTheyFold);
			}
		}
		ArrayList<ArrayList<Card>> handCombinedWithCenter = new ArrayList<ArrayList<Card>>();
		for (int i = 0; i < totalPlayers; i++) {
			handCombinedWithCenter.add(new ArrayList<Card>());
			handCombinedWithCenter.get(i).addAll(center.getCenter());
			handCombinedWithCenter.get(i).addAll(hands.get(i).getArrayListOfHand());
		}
		
		ArrayList<WhoHasWhat> who = new ArrayList<WhoHasWhat>();
		for (int i = 0; i < totalPlayers; i++) {
			who.add(new WhoHasWhat(i+1, handCombinedWithCenter.get(i), hasFolded.get(i), hands.get(i).getArrayListOfHand()));
		}
		
		// Define the order of hands and sort hands accordingly
		HashMap<String, Integer> handOrder = new HashMap<String, Integer>();
		String[] handNames = { "Royal Flush", "Straight Flush", "Four of a Kind", "Full House", "Flush", "Straight",
				"Three of a Kind", "Two Pair", "Pair", "High Card" };
		for (int i = 0; i < handNames.length; i++) {
			handOrder.put(handNames[i], i+1);
		}
		WhoHasWhat min;
		int minIndex;
		for (int i = 0; i < who.size() - 1; i++) {
			minIndex = i;
			min = who.get(i);
			for (int j = i; j < who.size(); j++) {
				if (handOrder.get(who.get(j).whatTheyHave) < handOrder.get(min.whatTheyHave)) {
					min = who.get(j);
					minIndex = j;
				}
			}
			WhoHasWhat temp = who.get(i);
			who.set(i, who.get(minIndex));
			who.set(minIndex, temp);
		}
		charStream.println(who);
		
		// Solo out just those who have the highest hand
		// Remove all players that either folded or have a lower hand
		String highestHand = who.get(0).whatTheyHave;
		for (int i = who.size() - 1; i >= 0; i--) {
			if (!who.get(i).whatTheyHave.equals(highestHand) || who.get(i).folded) {
				who.remove(i);
			}
		}
		System.out.println(who);
		
		// If there's only one player left in this list
		if (who.size() == 1) {
			winner(0, who.get(0).whatTheyHave);
		}
		
		// If just two players
		
		// If more players, for now winner will be highest card out of hand
		WhoHasWhat w = who.get(0);
		for (int i = 0; i < who.size(); i++) {
			if (who.get(i).highestCard.compareTo(w.highestCard) > 0) {
				w = who.get(i);
			}
		}
		winner(w.playerNumber-1, w.whatTheyHave);

		
	}
	
	public static void winner(int player, String hand) {
		System.out.println("Congrats, player " + (player+1) + ", you won with a " + hand + "!");
		bets.get(player).win();
		askToKeepPlaying();
	}
	
	public static void askToKeepPlaying() {
		// Check for players who have no balance
		for (int i = bets.size() - 1; i >= 0; i--) {
			if (bets.get(i).getBalance() == 0) {
				bets.remove(i);
			}
		}
		System.out.println("1. Keep Playing\t2. New Game\t3. Quit");
		int choice = kbd.nextInt();
		if (choice == 1) {
			String[] args = {"Continue Game"};
			main(args);
		} else if (choice == 2) {
			String[] args = {"New Game"};
			main(args);
		} else {
			System.out.println("Thanks for playing!");
			System.exit(0);
		}
	}

	public static String showRandomReaction(boolean isPositive) {
		if (isPositive) {
			String[] positiveReactions = { "Sounds good!", "Wowzers!", "Alright!", "Looks good!", "Okay.",
					"Looks good." };
			return positiveReactions[(int) (Math.random() * positiveReactions.length)];
		} else {
			String[] negativeReactions = { "Oh no!", "That's not good!", "Yikes!", "Not looking good.", "Watch out!" };
			return negativeReactions[(int) (Math.random() * negativeReactions.length)];
		}
	}

	public static void showLoadingAnimation() {
		for (int i = 0; i < 4; i++) {
			System.out.print('.');
			sleep(1000);
		}
		System.out.println();
	}

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
