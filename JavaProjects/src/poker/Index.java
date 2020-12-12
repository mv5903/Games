package poker;

import java.util.ArrayList;
import java.util.Scanner;

//♠
public class Index {

	static DeckOfCards cards;
	static CenterHand center;
	static ArrayList<Hand> hands;
	static ArrayList<Bet> bets;
	static int currentPlayer = 0;
	static int totalPlayers;
	static Scanner kbd = new Scanner(System.in);

	public static void main(String[] args) {
		System.out.println("Welcome to Blackjack!");
		sleep(500);
		cards = new DeckOfCards();
		center = new CenterHand(cards.getNextCard(), cards.getNextCard(), cards.getNextCard());
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
		}
		showPlayerTheirHand();
	}

	public static void showPlayerTheirHand() {
		System.out.println("Hi player " + (currentPlayer + 1) + "! Here's your hand:");
		System.out.println(hands.get(currentPlayer));
		System.out.println("And here is the center hand: ");
		System.out.println(center);
		for (int i = 0; i < 2; i++) {
			dealAnotherCard();
		}
		seeWhoWins();
	}

	public static void dealAnotherCard() {
		System.out.println("How much would you like to bet? Enter as an Integer: ");
		bets.get(currentPlayer).bet(kbd.nextInt());
		center.dealNextCard(cards.getNextCard());
		System.out.println(showRandomReaction(true) + " Here's the center hand now: ");
		System.out.println(center);
	}

	public static void seeWhoWins() {
		int playerNumber = 0;
		ArrayList<ArrayList<Card>> allCards = new ArrayList<ArrayList<Card>>();
		for (int i = 0; i < totalPlayers; i++) {
			allCards.add(new ArrayList<Card>());
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
		for (ArrayList<Card> c : allCards) {
			System.out.println("You have a " + UniqueHands.hasWhichHand(c));
		}
		// Now each list has their hand combined with the center, we can check for any
		// unique items

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
