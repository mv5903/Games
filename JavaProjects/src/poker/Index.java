package poker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//♠ (To force UTF-8 saving)
public class Index {

	static DeckOfCards cards;
	static CenterHand center;
	static ArrayList<Hand> hands;
	static ArrayList<Bet> bets;
	static HashMap<Integer, Boolean> hasFolded = new HashMap<Integer, Boolean>();
	static int currentPlayer = 0;
	static int totalPlayers;
	static int currentBet = 0;
	static Scanner kbd = new Scanner(System.in);

	public static void main(String[] args) {
		System.out.println("Welcome to Blackjack!");
		cards = new DeckOfCards();
		center = new CenterHand(cards.getNextCard(), cards.getNextCard());
		System.out.println("How many players?");
		totalPlayers = kbd.nextInt();
		dealAllCards();
	}

	public static void dealAllCards() {
		System.out.println("Dealing cards: ");
		// showLoadingAnimation();
		hands = new ArrayList<Hand>();
		bets = new ArrayList<Bet>();
		for (int i = 0; i < totalPlayers; i++) {
			hands.add(new Hand(cards.getNextCard(), cards.getNextCard()));
			bets.add(new Bet());
			hasFolded.put((i + 1), false);
		}
		firstRoundBeforeCenterDeals();
	}

	public static void firstRoundBeforeCenterDeals() {
		if (!hasFolded(currentPlayer+1)) {
			System.out.println("Hi player " + (currentPlayer + 1) + "! Here's your hand.");
			System.out.println(hands.get(currentPlayer));
			System.out.println("How much would you like to bet? If you want to fold, type -1.");
			int thisPlayersBet = kbd.nextInt();
			while (thisPlayersBet < 100) {
				System.out.println("Sorry, your bet needs to be at least " + currentBet + ". Try again.");
				thisPlayersBet = kbd.nextInt();
			}
			if (currentPlayer != 0 && thisPlayersBet > currentBet) {
				currentBet = thisPlayersBet;
				
			}
			if (thisPlayersBet == -1) {
				fold(currentPlayer+1);
			} else {
				bets.get(currentPlayer).bet(thisPlayersBet);
				if (currentPlayer == 0) {
					currentBet = thisPlayersBet;
				} else {
					keepBetOrRaise();
				}
				if (currentPlayer != (totalPlayers - 1)) {
					currentPlayer++;
					firstRoundBeforeCenterDeals();
				}
			}
		}
		if (currentPlayer+1 == totalPlayers) {
			currentPlayer = 0;
			continueGame();
		}
	}
	
	public static void continueGame() {
		if (currentPlayer == 0) {
			center.dealNextCard(cards.getNextCard());
		}
		System.out.println("Here is the center: " + center);
		System.out.println("Hello again, player " + (currentPlayer+1) + ". ");
		if (currentPlayer == 0) {
			System.out.println("Start the betting off with how much? Rememeber, you can type -1 instead to fold.");
			int newBet = kbd.nextInt();
			if (newBet == -1) {
				fold(currentPlayer+1);
				nextPlayer();
				if (currentPlayer+1 == totalPlayers) {
					if (center.getCenter().size() == 5) {
						seeWhoWins();
					}
				} else {
					continueGame();
				}
			} else {
				bets.get(currentPlayer).bet(newBet);
				nextPlayer();
				if (currentPlayer+1 == totalPlayers) {
					if (center.getCenter().size() == 5) {
						seeWhoWins();
					}
				} else {
					continueGame();
				}
			}
		}
	}
	
	public static void nextPlayer() {
		if (currentPlayer - 1 == totalPlayers) {
			currentPlayer = 0;
		} else {
			currentPlayer++;
		}
	}
	
	public static void fold(int player) {
		hasFolded.replace(player, true);
	}
	
	public static boolean hasFolded(int player) {
		return hasFolded.get(player);
	}

	public static void keepBetOrRaise() {
		System.out.println("Keep the current bet of " + currentBet + " or raise it?");
		String choice = kbd.next();
		if (choice.equalsIgnoreCase("raise")) {
			System.out.println("What would you like to raise the bet to?");
			int raiseAmount = kbd.nextInt();
			currentBet = raiseAmount;
			System.out.println("Okay, sounds good. The current bet is now " + currentBet);
			bets.get(currentPlayer).bet(currentBet);
		} else {
			bets.get(currentPlayer).bet(currentBet);
		}
	}

	public static void seeWhoWins() {
		int playerNumber = 0;
		ArrayList<ArrayList<Card>> allCards = new ArrayList<ArrayList<Card>>();
		for (int i = 0; i < totalPlayers; i++) {
			if (!hasFolded(i+1)) {
				allCards.add(new ArrayList<Card>());
			}
		}
		for (ArrayList<Card> c : allCards) {
			// Add each hand to card list
			for (Card handCard : hands.get(playerNumber).getAllCardsInHand()) {
				// fix here
				c.add(handCard);
			}
			// Add center hand to list
			for (Card centerCard : center.getCenter()) {
				c.add(centerCard);
			}
			playerNumber++;
		}
		ArrayList<String> uniqueHandType = new ArrayList<String>();
		for (ArrayList<Card> c : allCards) {
			String temp = UniqueHands.hasWhichHand(c);
			uniqueHandType.add(temp);
		}
		// Add which player and what they had to a hashmap
		ArrayList<Point> whoHadWhat = new ArrayList<Point>();
		for (int i = 0; i < uniqueHandType.size(); i++) {
			if (!hasFolded(i+1)) {
				whoHadWhat.add(new Point((i + 1), uniqueHandType.get(i)));
			}
		}
		System.out.println(whoHadWhat);

		// Define hands order
		HashMap<String, Integer> validHands = new HashMap<String, Integer>();
		String[] handNames = { "Royal Flush", "Straight Flush", "Four of a Kind", "Full House", "Flush", "Straight",
				"Three of a Kind", "Two Pair", "Pair", "High Card" };
		for (int i = 0; i < handNames.length; i++) {
			validHands.put(handNames[i], (i + 1));
		}

		// Sort Player Hands by order via Selection Sort
		Point min;
		int minIndex;
		for (int i = 0; i < whoHadWhat.size() - 1; i++) {
			min = whoHadWhat.get(i);
			minIndex = i;
			for (int j = i; j < whoHadWhat.size(); j++) {
				if (validHands.get(whoHadWhat.get(j).getHand()) < validHands.get(min.getHand())) {
					min = whoHadWhat.get(j);
					minIndex = j;
				}
			}
			Point temp = whoHadWhat.get(i);
			whoHadWhat.set(i, whoHadWhat.get(minIndex));
			whoHadWhat.set(minIndex, temp);
		}
		System.out.println(whoHadWhat);

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
