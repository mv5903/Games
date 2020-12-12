package poker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UniqueHands implements Constants {

	public static String[] orderOfHand = { "RoyalFlush", "StraightFlush", "FourOfAKind", "FullHouse", "Flush",
			"Straight", "ThreeOfAKind", "TwoPair", "Pair", "HighCard" };

	public static String hasWhichHand(ArrayList<Card> cards) {
		cards = sortCardsBySuit(cards);
		System.out.println(cards);
		if (hasRoyalFlush(cards)) {
			return "Royal Flush";
		} else if (hasStraightFlush(cards)) {
			return "Straight Flush";
		} else if (hasFourOfAKind(cards)) {
			return "Four of a Kind";
		} else if (hasFullHouse(cards)) {
			return "Full House";
		} else if (hasFlush(cards)) {
			return "Flush";
		} else if (hasStraight(cards)) {
			return "Straight";
		} else if (hasThreeOfAKind(cards)) {
			return "Three of a Kind";
		} else if (hasTwoPair(cards)) {
			return "Two Pair";
		} else if (hasPair(cards)) {
			return "Pair";
		} else {
			return "High Card";
		}
		
	}
	
	public static ArrayList<Card> sortCardsByNumber(ArrayList<Card> cards) {
		Card minimum;
		int minIndex;
		for (int i = 0; i < cards.size(); i++) {
			minimum = cards.get(i);
			minIndex = i;
			for (int j = i; j < cards.size(); j++) {
				if (cards.get(j).getFaceValue() < minimum.getFaceValue()) {
					minimum = cards.get(j);
					minIndex = j;
				}
			}
			Card temp = cards.get(i);
			cards.set(i, cards.get(minIndex));
			cards.set(minIndex, temp);
		}
		return cards;
	}
	
	public static ArrayList<Card> sortCardsBySuit(ArrayList<Card> cards) {
		//Sorted by suit, lowest to highest
		ArrayList<Card> clubs = new ArrayList<Card>();
		ArrayList<Card> diamonds = new ArrayList<Card>();
		ArrayList<Card> hearts = new ArrayList<Card>();
		ArrayList<Card> spades = new ArrayList<Card>();
		
		for (Card c: cards) {
			if (c.getSuit().equalsIgnoreCase(CLUBS)) {
				clubs.add(c);
			} else if (c.getSuit().equalsIgnoreCase(DIAMONDS)) {
				diamonds.add(c);
			} else if (c.getSuit().equalsIgnoreCase(HEARTS)) {
				hearts.add(c);
			} else if (c.getSuit().equalsIgnoreCase(SPADES)) {
				spades.add(c);
			}
		}
		ArrayList<Card> sorted = new ArrayList<Card>();
		sorted.addAll(clubs);
		sorted.addAll(diamonds);
		sorted.addAll(hearts);
		sorted.addAll(spades);
		return sorted;
	}
	
	public static boolean hasRoyalFlush(ArrayList<Card> cards) {
		if (hasFlush(cards) && hasStraight(cards)) {
			cards = sortCardsByNumber(cards);
			if (isAce(getHighStraightCard(cards))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasStraightFlush(ArrayList<Card> cards) {
		if (!hasStraight(cards) || !hasFlush(cards)) return false;
		
		// Check straight
		ArrayList<Card> straightCards = new ArrayList<Card>();
		boolean straight = false;
		for (int i = 0; i < cards.size() - 4; i++) {
			straight = cards.get(i + 4).getFaceValue() - cards.get(i + 3).getFaceValue() == 1
					&& cards.get(i + 3).getFaceValue() - cards.get(i + 2).getFaceValue() == 1
					&& cards.get(i + 2).getFaceValue() - cards.get(i + 1).getFaceValue() == 1
					&& cards.get(i + 1).getFaceValue() - cards.get(i).getFaceValue() == 1;
			if (straight) {
				straightCards.add(cards.get(i));
				straightCards.add(cards.get(i+1));
				straightCards.add(cards.get(i+2));
				straightCards.add(cards.get(i+3));
				straightCards.add(cards.get(i+4));
				break;
			}
		}
		return hasFlush(straightCards);
	}
	
	public static boolean hasFourOfAKind(ArrayList<Card> cards) {
		cards = sortCardsByNumber(cards);
		
		
		return false;
	}

	public static boolean hasFullHouse(ArrayList<Card> cards) {
		// Find pair
		Card pair = cards.get(0);
		boolean hasPair = false;
		for (int i = 0; i < cards.size(); i++) {
			for (int j = 0; j < cards.size(); j++) {
				if (cards.get(i).equals(cards.get(j)) && i != j) {
					pair = cards.get(i);
					hasPair = true;
				}
			}
		}
		if (!hasPair)
			return false;
		// Find three of a kind and see if they are not the same
		for (int i = 0; i < cards.size(); i++) {
			for (int j = 0; j < cards.size(); j++) {
				for (int k = 0; k < cards.size(); k++) {
					if (cards.get(i).equals(cards.get(j)) && cards.get(j).equals(cards.get(k)) && i != j && j != k) {
						if (!cards.get(i).equals(pair)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean hasFlush(ArrayList<Card> cards) {
		for (int i = 0; i < cards.size() - 4; i++) {
			if (cards.get(i).getSuit().equals(cards.get(i+1).getSuit()) && cards.get(i+1).getSuit().equals(cards.get(i+2).getSuit()) && cards.get(i+2).getSuit().equals(cards.get(i+3).getSuit()) && cards.get(i+3).getSuit().equals(cards.get(i+4).getSuit())) {
				return true;
			}
		}	
		return false;
	}

	public static boolean hasStraight(ArrayList<Card> cards) {
		boolean straight = false;
		for (int i = 0; i < cards.size() - 4; i++) {
			straight = cards.get(i + 4).getFaceValue() - cards.get(i + 3).getFaceValue() == 1
					&& cards.get(i + 3).getFaceValue() - cards.get(i + 2).getFaceValue() == 1
					&& cards.get(i + 2).getFaceValue() - cards.get(i + 1).getFaceValue() == 1
					&& cards.get(i + 1).getFaceValue() - cards.get(i).getFaceValue() == 1;
		}
		return straight;
	}
	
	public static Card getHighStraightCard(ArrayList<Card> cards) {
		boolean straight = false;
		boolean[] straightIndexes = {false, false, false};
		for (int i = 0; i < cards.size() - 4; i++) {
			straight = cards.get(i + 4).getFaceValue() - cards.get(i + 3).getFaceValue() == 1
					&& cards.get(i + 3).getFaceValue() - cards.get(i + 2).getFaceValue() == 1
					&& cards.get(i + 2).getFaceValue() - cards.get(i + 1).getFaceValue() == 1
					&& cards.get(i + 1).getFaceValue() - cards.get(i).getFaceValue() == 1;
			if (straight) {
				straightIndexes[i] = true;
			}
		}
		int lastTrue = 0;
		for (int i = 0; i < straightIndexes.length; i++) {
			if (straightIndexes[i]) {
				lastTrue = i;
			}
		}
		if (straight) {
			return cards.get(lastTrue + 4);
		}
		return null;
	}

	public static boolean hasThreeOfAKind(ArrayList<Card> cards) {
		for (int i = 0; i < cards.size(); i++) {
			for (int j = 0; j < cards.size(); j++) {
				for (int k = 0; k < cards.size(); k++) {
					if (i != j && j != k && cards.get(i).equals(cards.get(j)) && cards.get(j).equals(cards.get(k))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean hasTwoPair(ArrayList<Card> cards) {
		boolean hasAPair = false;
		Card pairCard = new Card(0, 0);
		for (int i = 0; i < cards.size(); i++) {
			for (int j = 0; j < cards.size(); j++) {
				if (i == j) {
					continue;
				}
				if (hasAPair && cards.get(i).equals(cards.get(j)) && !cards.get(i).equals(pairCard)) {
					return true;
				}
				if (!hasAPair && cards.get(i).equals(cards.get(j))) {
					pairCard = cards.get(i);
					hasAPair = true;
				}
			}
		}
		return false;
	}

	public static boolean hasPair(ArrayList<Card> cards) {
		for (int i = 0; i < cards.size(); i++) {
			for (int j = 0; j < cards.size(); j++) {
				if (i == j) {
					continue;
				}
				if (cards.get(i).equals(cards.get(j))) {
					return true;
				}
			}
		}
		return false;
	}

	public static Card highCard(ArrayList<Card> cards) {
		Card highest = cards.get(0);
		for (Card c : cards) {
			if (c.getFaceValue() > highest.getFaceValue()) {
				highest = c;
			}
		}
		return highest;
	}
	
	public static boolean isAce(Card c) {
		return c.equals(new Card(14, 1)) || c.equals(new Card(14, 2)) || c.equals(new Card(14, 3)) || c.equals(new Card(14, 0));
	}
}
