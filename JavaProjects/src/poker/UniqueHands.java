package poker;

import java.util.ArrayList;
import java.util.Collections;

public class UniqueHands implements Constants {
	// ♠
	public static String[] orderOfHand = { "RoyalFlush", "StraightFlush", "FourOfAKind", "FullHouse", "Flush",
			"Straight", "ThreeOfAKind", "TwoPair", "Pair", "HighCard" };

	public static String hasWhichHand(ArrayList<Card> cards) {
		cards = sortCardsBySuit(cards);
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
		// Sorted by suit, lowest to highest
		ArrayList<Card> clubs = new ArrayList<Card>();
		ArrayList<Card> diamonds = new ArrayList<Card>();
		ArrayList<Card> hearts = new ArrayList<Card>();
		ArrayList<Card> spades = new ArrayList<Card>();

		for (Card c : cards) {
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
		if (hasStraightFlush(cards)) {
			cards = sortCardsByNumber(cards);
			if (isAce(getHighStraightCard(cards))) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasStraightFlush(ArrayList<Card> cards) {
		if (!hasStraight(cards) || !hasFlush(cards))
			return false;
		cards = sortCardsByNumber(cards);
		// Check straight
		ArrayList<Card> straightCards = new ArrayList<Card>();
		boolean straight = false;
		for (int i = cards.size() - 5; i >= 0; i--) {
			straight = cards.get(i + 4).getFaceValue() - cards.get(i + 3).getFaceValue() == 1
					&& cards.get(i + 3).getFaceValue() - cards.get(i + 2).getFaceValue() == 1
					&& cards.get(i + 2).getFaceValue() - cards.get(i + 1).getFaceValue() == 1
					&& cards.get(i + 1).getFaceValue() - cards.get(i).getFaceValue() == 1;
			if (straight) {
				straightCards.add(cards.get(i));
				straightCards.add(cards.get(i + 1));
				straightCards.add(cards.get(i + 2));
				straightCards.add(cards.get(i + 3));
				straightCards.add(cards.get(i + 4));
				break;
			}
		}

		return hasFlush(straightCards);
	}

	public static boolean hasFourOfAKind(ArrayList<Card> cards) {
		cards = sortCardsByNumber(cards);
		Collections.reverse(cards);
		for (int i = 0; i < cards.size() - 3; i++) {
			if (cards.get(i).getFaceValue() == cards.get(i + 1).getFaceValue()
					&& cards.get(i + 1).getFaceValue() == cards.get(i + 2).getFaceValue()
					&& cards.get(i + 2).getFaceValue() == cards.get(i + 3).getFaceValue()) {
				return true;
			}
		}

		return false;
	}

	public static boolean hasFullHouse(ArrayList<Card> cards) {
		// Find three of a kind
		ArrayList<Card> temp = new ArrayList<Card>();
		cards = sortCardsByNumber(cards);
		for (Card c : cards) {
			temp.add(c);
		}

		for (int i = temp.size() - 3; i >= 0; i--) {
			if (temp.get(i).getFaceValue() == temp.get(i + 1).getFaceValue()
					&& temp.get(i + 1).getFaceValue() == temp.get(i + 2).getFaceValue()) {
				temp.remove(i);
				temp.remove(i);
				temp.remove(i);
				break;
			}
			if (i == 0) {
				return false;
			}
		}
		return hasPair(temp);
	}

	public static boolean hasFlush(ArrayList<Card> cards) {
		cards = sortCardsBySuit(cards);
		for (int i = 0; i < cards.size() - 4; i++) {
			if (cards.get(i).getSuit().equals(cards.get(i + 1).getSuit())
					&& cards.get(i + 1).getSuit().equals(cards.get(i + 2).getSuit())
					&& cards.get(i + 2).getSuit().equals(cards.get(i + 3).getSuit())
					&& cards.get(i + 3).getSuit().equals(cards.get(i + 4).getSuit())) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasStraight(ArrayList<Card> cards) {
		cards = sortCardsByNumber(cards);
		boolean straight = false;
		for (int i = cards.size() - 5; i >= 0; i--) {
			straight = cards.get(i + 4).getFaceValue() - cards.get(i + 3).getFaceValue() == 1
					&& cards.get(i + 3).getFaceValue() - cards.get(i + 2).getFaceValue() == 1
					&& cards.get(i + 2).getFaceValue() - cards.get(i + 1).getFaceValue() == 1
					&& cards.get(i + 1).getFaceValue() - cards.get(i).getFaceValue() == 1;
		}
		return straight;
	}

	public static Card getHighStraightCard(ArrayList<Card> cards) {
		boolean straight = false;
		boolean[] straightIndexes = { false, false, false };
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
		cards = sortCardsByNumber(cards);
		for (int i = cards.size() - 3; i >= 0; i--) {
			if (cards.get(i).getFaceValue() == cards.get(i + 1).getFaceValue()
					&& cards.get(i + 1).getFaceValue() == cards.get(i + 2).getFaceValue()) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasTwoPair(ArrayList<Card> cards) {
		ArrayList<Card> minusFirstPair = new ArrayList<Card>();
		cards = sortCardsByNumber(cards);
		for (Card c : cards) {
			minusFirstPair.add(c);
		}
		for (int i = minusFirstPair.size() - 2; i >= 0; i--) {
			if (minusFirstPair.get(i).getFaceValue() == minusFirstPair.get(i + 1).getFaceValue()) {
				minusFirstPair.remove(i);
				minusFirstPair.remove(i);
				break;
			}
		}
		return hasPair(minusFirstPair);
	}

	public static boolean hasPair(ArrayList<Card> cards) {
		cards = sortCardsByNumber(cards);
		for (int i = cards.size() - 2; i >= 0; i--) {
			if (cards.get(i).getFaceValue() == cards.get(i + 1).getFaceValue()) {
				return true;
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
		return c.equals(new Card(14, 1)) || c.equals(new Card(14, 2)) || c.equals(new Card(14, 3))
				|| c.equals(new Card(14, 0));
	}
}
