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
	static ArrayList<Boolean> hasFolded = new ArrayList<Boolean>();
	static int currentPlayer = 0, raisedPlayer = 0;
	static int totalPlayers;
	static int currentBet = 0;
	static boolean isFreshGame = true;
	static Scanner kbd = new Scanner(System.in);

	public static void main(String[] args) {
		if (args.length == 0 || args[0].equals("Restart")) {
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
		if (!hasFolded(currentPlayer)) {
			System.out.println("Player " + (currentPlayer + 1) + ", here are your cards: " + hands.get(currentPlayer)
					+ ". Place a bet. Fold with -1, raise with -2.\n(" + bets.get(currentPlayer) + ").");
			int playersBet = kbd.nextInt();
			// If the player folds
			if (playersBet == -1) {
				fold(currentPlayer);
			} else {
				// Player raises
				if (playersBet == -2) {
					raise();
				}
				// If no one has done anything, just set the bet size to the first bet
				if (center.getCenter().size() == 2 && currentPlayer == 0) {
					currentBet = playersBet;
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
		}

		else {
			currentPlayer++;
		}
		// A full round has finished
		if (currentPlayer == raisedPlayer && !raised) {
			if (center.getCenter().size() == 5) {
				seeWhoWins();
				return;
			} else {
				center.dealNextCard(cards.getNextCard());
				System.out.println("There is a new center hand: " + center);
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
		int playerNumber = 0;
		ArrayList<ArrayList<Card>> allCards = new ArrayList<ArrayList<Card>>();
		for (int i = 0; i < totalPlayers; i++) {
			if (!hasFolded(i)) {
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
			if (!hasFolded(i)) {
				whoHadWhat.add(new Point((i + 1), uniqueHandType.get(i) ,hands.get(i).getArrayListOfHand()));
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
		// Find winner
		
		// If there's only one player
		if (totalPlayers == 1) {
			System.out.println("Well, there's only one player, so you win by default with a " + whoHadWhat.get(0).getHand() + "!");
			winner(0);
			askToKeepPlaying();
		}
		
		// Only one person is a winner
		if (whoHadWhat.get(0).getHand().equals(whoHadWhat.get(1).getHand())) {
			// Multiple people are a winner! Solo out the players that tie for the lead.
			int temp = 0;
			while (whoHadWhat.get(temp).getHand().equals(whoHadWhat.get(temp+1).getHand())) {
				temp++;
				if (temp == totalPlayers-1) break;
			}
			// Get each players hand for just the hand portion, then find highest card out of that.
			String uniqueHand = whoHadWhat.get(0).getHand();
			for (int i = whoHadWhat.size() - 1; i >= 0; i--) {
				if (!whoHadWhat.get(i).getHand().equals(uniqueHand)) {
					whoHadWhat.remove(i);
				}
			}
			System.out.println(whoHadWhat);
			
			Point highest = whoHadWhat.get(0);
			Card maxCard = UniqueHands.highCard(whoHadWhat.get(0).getAllCards());;
			for (Point p: whoHadWhat) {
				if (UniqueHands.highCard(p.getAllCards()).compareTo(maxCard) > 0) {
					maxCard = UniqueHands.highCard(p.getAllCards());
					highest = p;
				}
			}

			System.out.printf("Congrats, player %d, you won with a %s!\n", highest.getPlayer(), highest.getHand());
			
			winner(highest.getPlayer()-1);
			askToKeepPlaying();
		} else {
			System.out.printf("Congrats, player %d, you won with a %s!\n", whoHadWhat.get(0).getPlayer(), whoHadWhat.get(0).getHand());
			winner(whoHadWhat.get(0).getPlayer()-1);
			askToKeepPlaying();
		}
	}
	
	public static void winner(int player) {
		bets.get(player).win();
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
