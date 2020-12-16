package poker;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//♠ (To force UTF-8 saving)
/**
 * Poker! Where everything comes together.
 * @author matt
 *
 */
public class Index {
	// UTF-8 PrintStream is required for running game in command prompt.
	static PrintStream charStream;
	static DeckOfCards cards;
	static CenterHand center;
	static ArrayList<Hand> hands;
	static ArrayList<Bet> bets;
	static ArrayList<Boolean> hasFolded = new ArrayList<Boolean>();
	static int currentPlayer = 0, raisedPlayer = 0, totalPlayers, currentBet = 0;
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
	/**
	 * Deals all cards to each player and keeps track of who folds.
	 */
	public static void dealAllCards() {
		System.out.println("Dealing cards: ");
		showLoadingAnimation();
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
		if (allFoldedButOne()) {
			winner(hasFolded.indexOf(false), "by default");
		}
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
				// If they enter another amount, as long as they have enough to bet
				if (bets.get(currentPlayer).getBalance() > currentBet && playersBet != currentBet) {
					System.out.println("Sorry, your bet needs to be " + currentBet + " to continue. Raise with -2.");
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
		// Recall that if someone raises, everyone ahead of them also needs to raise.
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
	
	public static boolean allFoldedButOne() {
		boolean foundOne = false;
		for (int i = 0; i < hasFolded.size(); i++) {
			if (foundOne && !hasFolded.get(i)) {
				return false;
			}
			if (!hasFolded.get(i)) {
				foundOne = true;
			}
		}
		return true;
	}

	public static boolean hasFolded(int player) {
		return hasFolded.get(player);
	}
	/**
	 * Decides the winner. Winner is determined by the following:<br>
	 * -If only one player has the best hand, then that player wins<br>
	 * -If multiple players have the best hand, then winner is determined by who has the best card in their hand via {@link #compareTo()} in the Card class.
	 * 
	 */
	public static void seeWhoWins() {
		/**
		 * Helps me keep track of:<br>
		 * -What each player has<br>
		 * -Their hand combined with the center, used for determining what hand they have<br>
		 * -What player they are (Player 1, Player 2, etc.)<br>
		 * -Whether or not they should be included when finding who wins, as long as they haven't folded<br>
		 * -The highest card they have, used for determining winner in case of a a tie<br>
		 * @author matt
		 *
		 */
		class WhoHasWhat {
			ArrayList<Card> hand;
			String whatTheyHave;
			int playerNumber;
			boolean folded;
			@SuppressWarnings("unused")
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
		// Solo out just those who have the highest hand
		// Remove all players that either folded or have a lower hand
		String highestHand = who.get(0).whatTheyHave;
		for (int i = who.size() - 1; i >= 0; i--) {
			if (!who.get(i).whatTheyHave.equals(highestHand) || who.get(i).folded) {
				who.remove(i);
			}
		}

		// If there's only one player left in this list
		if (who.size() == 1) {
			winner(who.get(0).playerNumber-1, who.get(0).whatTheyHave);
		}
		// If more players
		WhoHasWhat w = who.get(0);
		for (int i = 0; i < who.size(); i++) {
			if (who.get(i).highestCard.compareTo(w.highestCard) > 0) {
				w = who.get(i);
			}
		}
		winner(w.playerNumber-1, w.whatTheyHave);

		
	}
	/**
	 * Displays the winner
	 * @param player The player that wins
	 * @param hand The hand they had, also tells the winning player what they had.
	 */
	public static void winner(int player, String hand) {
		String toSend = "";
		if (hand.equals("by default")) {
			toSend = hand;
		} else {
			toSend += "with a " + hand;
		}
		System.out.println("Congrats, player " + (player+1) + ", you win " +  toSend + "!");
		bets.get(player).win();
		askToKeepPlaying();
	}
	/**
	 * Asks the user whether or not they want to start a new game or continue.
	 */
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
	/**
	 * Displays a random reaction
	 * @param isPositive Gives a positive reaction, false for a negative reaction.
	 * @return See the parameter.
	 */
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
		for (int i = 0; i < 10; i++) {
			System.out.print('.');
			sleep(200);
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
