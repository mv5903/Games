package poker;

import java.util.ArrayList;
import java.util.Collections;
/**
 * Responsible for determining the hand a player has, given an ArrayList of Cards.
 * @author matt
 *
 */
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
	/**
	 * Sort the cards by number
	 * @param cards Cards to sort
	 * @return The cards sorted by number
	 */
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
	/**
	 * Sort the cards by suit
	 * @param cards Cards to sort
	 * @return The cards sorted by suit
	 */
	public static ArrayList<Card> sortCardsBySuit(ArrayList<Card> cards) {
		// Sorted by suit, lowest to highest
		ArrayList<Card> clubs = new ArrayList<Card>();
		ArrayList<Card> diamonds = new ArrayList<Card>();
		ArrayList<Card> hearts = new ArrayList<Card>();
		ArrayList<Card> spades = new ArrayList<Card>();

		for (Card c : cards) {
			if (c.getSuit().equalsIgnoreCase(String.valueOf(CLUBS))) {
				clubs.add(c);
			} else if (c.getSuit().equalsIgnoreCase(String.valueOf(DIAMONDS))) {
				diamonds.add(c);
			} else if (c.getSuit().equalsIgnoreCase(String.valueOf(HEARTS))) {
				hearts.add(c);
			} else if (c.getSuit().equalsIgnoreCase(String.valueOf(SPADES))) {
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
		ArrayList<Card> temp = new ArrayList<Card>();
		temp.addAll(cards);
		if (hasStraightFlush(cards)) {
			temp = isolateStraightFlush(temp);
			cards = sortCardsByNumber(cards);
			if (isAce(getHighStraightCard(temp))) {
				return true;
			}
		}
		return false;
	}
	/**
	 * See if the straight and the flush line up to the exact same cards.
	 * @param cards Cards to find a straight flush
	 * @return If the cards are a straight flush
	 */
	public static boolean hasStraightFlush(ArrayList<Card> cards) {
		if (!hasStraight(cards) || !hasFlush(cards))
			return false;
		cards = isolateFlush(cards);
		if (cards.size() < 5) {
			return false;
		}
		cards = sortCardsByNumber(cards);
		cards = removeDuplicates(cards);
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

		return straightCards.size() == 5;
	}
	/**
	 * Sees if there is a four of a kind
	 * @param cards Sees if there is a four of a kind by first sorting the cards by number
	 * @return true is the cards are a four of a kind.
	 */
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
	/**
	 * Finds the three of a kind, then removes that from the ArrayList of cards. Then sees if there is a two pair
	 * out of the remaining cards.
	 * @param cards The cards to find a full house at of
	 * @return true if the cards contain a full house
	 */
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
	/**
	 * Sort the cards by suit, then see if there are five cards together of the same suit.
	 * @param cards Cards to find a flush for
	 * @return true if the cards has a flush
	 */
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
	/**
	 * Sort the card by faceValue, then see if there are five cards that have a difference of one
	 * between each of them.
	 * @param cards Cards to find a straight with.
	 * @return true if the cards contain a straight.
	 */
	public static boolean hasStraight(ArrayList<Card> cards) {
		cards = sortCardsByNumber(cards);
		//Remove duplicates
		ArrayList<Card> duplicatesRemoved = new ArrayList<Card>();
		duplicatesRemoved.addAll(cards);
		duplicatesRemoved = removeDuplicates(duplicatesRemoved);
		boolean straight = false;
		if (duplicatesRemoved.size() < 5) return false;
		for (int i = duplicatesRemoved.size() - 5; i >= 0; i--) {
			straight = duplicatesRemoved.get(i + 4).getFaceValue() - duplicatesRemoved.get(i + 3).getFaceValue() == 1
					&& duplicatesRemoved.get(i + 3).getFaceValue() - duplicatesRemoved.get(i + 2).getFaceValue() == 1
					&& duplicatesRemoved.get(i + 2).getFaceValue() - duplicatesRemoved.get(i + 1).getFaceValue() == 1
					&& duplicatesRemoved.get(i + 1).getFaceValue() - duplicatesRemoved.get(i).getFaceValue() == 1;
			if (straight) return true;
		}
		return false;
	}

	public static Card getHighStraightCard(ArrayList<Card> cards) {
		cards = sortCardsByNumber(cards);
		cards = isolateStraight(cards);
		return cards.get(cards.size() - 1);
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
	/**
	 * Get just the cards which has the winning hand to compare.
	 * @param cards Card to detect the given hand.
	 * @param hand  
	 * @return
	 * @throws NotFoundException 
	 */
	public static ArrayList<Card> isolateHand(ArrayList<Card> cards, String hand) throws NotFoundException {
		boolean canContinue = false;
		switch (hand) {
		case "Royal Flush": canContinue = hasRoyalFlush(cards); break;
		case "Straight Flush": canContinue = hasStraightFlush(cards); break;
		case "Four of a Kind": canContinue = hasFourOfAKind(cards); break;
		case "Full House": canContinue = hasFullHouse(cards); break;
		case "Flush": canContinue = hasFlush(cards); break;
		case "Straight": canContinue = hasStraight(cards); break;
		case "Three of a Kind": canContinue = hasThreeOfAKind(cards); break;
		case "Two Pair": canContinue = hasTwoPair(cards); break;
		case "Pair": canContinue = hasPair(cards); break;
		case "High Card": ArrayList<Card> card = new ArrayList<Card>(); card.add(cards.get(0)); return card;
		}
		if (!canContinue) {
			throw new NotFoundException("Hand not found in the given hand.");
		}
		switch (hand) {
		case "Royal Flush": return isolateRoyalFlush(cards);
		case "Straight Flush": return isolateStraightFlush(cards);
		case "Four of a Kind": return isolateFourOfAKind(cards);
		case "Full House": return isolateFullHouse(cards);
		case "Flush": return isolateFlush(cards);
		case "Straight":
			ArrayList<Card> temp = new ArrayList<Card>();
			temp.addAll(isolateStraight(cards));
			if (temp.size() > 5) {
				for (int i = temp.size() - 1; i >= 5; i--) {
					temp.remove(i);
				}
			}
			return temp;
		case "Three of a Kind": return isolateThreeOfAKind(cards);
		case "Two Pair": return isolateTwoPair(cards);
		case "Pair": return isolatePair(cards);
		}
		return new ArrayList<Card>();
	}
	
	public static ArrayList<Card> isolateRoyalFlush(ArrayList<Card> cards) {
		ArrayList<Card> isolatedCards = new ArrayList<Card>();
		cards = sortCardsByNumber(cards);
		cards = isolateFlush(cards);
		for (int i = cards.size() - 5; i >= 0; i--) {
			if (cards.get(i+4).getFaceValue() == 14 &&
				cards.get(i+3).getFaceValue() == 13 &&
				cards.get(i+2).getFaceValue() == 12 &&
				cards.get(i+1).getFaceValue() == 11 &&
				cards.get(i).getFaceValue() == 10) {
				for (int j = i; j <= i+4; j++) {
					isolatedCards.add(cards.get(j));
				}
			}
		}
		return isolatedCards;
	}
	
	public static ArrayList<Card> isolateStraightFlush(ArrayList<Card> cards) {
		cards = sortCardsByNumber(cards);
		cards = isolateFlush(cards);
		// Check straight
		ArrayList<Card> isolatedCards = new ArrayList<Card>();
		boolean straight = false;
		for (int i = cards.size() - 5; i >= 0; i--) {
			straight = cards.get(i + 4).getFaceValue() - cards.get(i + 3).getFaceValue() == 1
					&& cards.get(i + 3).getFaceValue() - cards.get(i + 2).getFaceValue() == 1
					&& cards.get(i + 2).getFaceValue() - cards.get(i + 1).getFaceValue() == 1
					&& cards.get(i + 1).getFaceValue() - cards.get(i).getFaceValue() == 1;
			if (straight) {
				isolatedCards.add(cards.get(i));
				isolatedCards.add(cards.get(i + 1));
				isolatedCards.add(cards.get(i + 2));
				isolatedCards.add(cards.get(i + 3));
				isolatedCards.add(cards.get(i + 4));
				break;
			}
		}
		return isolatedCards;
	}
	
	public static ArrayList<Card> isolateFourOfAKind(ArrayList<Card> cards) {
		ArrayList<Card> isolatedCards = new ArrayList<Card>();
		cards = sortCardsByNumber(cards);
		Collections.reverse(cards);
		for (int i = 0; i < cards.size() - 3; i++) {
			if (cards.get(i).getFaceValue() == cards.get(i + 1).getFaceValue()
					&& cards.get(i + 1).getFaceValue() == cards.get(i + 2).getFaceValue()
					&& cards.get(i + 2).getFaceValue() == cards.get(i + 3).getFaceValue()) {
				isolatedCards.add(cards.get(i));
				isolatedCards.add(cards.get(i+1));
				isolatedCards.add(cards.get(i+2));
				isolatedCards.add(cards.get(i+3));
			}
		}
		return isolatedCards;
	}
	
	public static ArrayList<Card> isolateFullHouse(ArrayList<Card> cards) {
		ArrayList<Card> isolatedCards = new ArrayList<Card>();
		ArrayList<Card> temp = new ArrayList<Card>();
		cards = sortCardsByNumber(cards);
		for (Card c : cards) {
			temp.add(c);
		}

		for (int i = temp.size() - 3; i >= 0; i--) {
			if (temp.get(i).getFaceValue() == temp.get(i + 1).getFaceValue()
					&& temp.get(i + 1).getFaceValue() == temp.get(i + 2).getFaceValue()) {
				isolatedCards.add(cards.get(i));
				isolatedCards.add(cards.get(i+1));
				isolatedCards.add(cards.get(i+2));
				temp.remove(i);
				temp.remove(i);
				temp.remove(i);
				break;
			}
		}
		isolatedCards.addAll(isolatePair(temp));
		return isolatedCards;
	}
	
	public static ArrayList<Card> isolateFlush(ArrayList<Card> cards) {
		ArrayList<Card> isolatedCards = new ArrayList<Card>();
		cards = sortCardsBySuit(cards);
		for (int i = 0; i < cards.size() - 4; i++) {
			if (cards.get(i).getSuit().equals(cards.get(i + 1).getSuit())
					&& cards.get(i + 1).getSuit().equals(cards.get(i + 2).getSuit())
					&& cards.get(i + 2).getSuit().equals(cards.get(i + 3).getSuit())
					&& cards.get(i + 3).getSuit().equals(cards.get(i + 4).getSuit())) {
				for (int j = i; j <= i+4; j++) {
					isolatedCards.add(cards.get(j));
				}
			}
		}
		return isolatedCards;
	}
	
	public static ArrayList<Card> isolateStraight(ArrayList<Card> cards) {
		ArrayList<Card> isolatedCards = new ArrayList<Card>();
		cards = sortCardsByNumber(cards);
		boolean straight = false;
		for (int i = cards.size() - 5; i >= 0; i--) {
			straight = cards.get(i + 4).getFaceValue() - cards.get(i + 3).getFaceValue() == 1
					&& cards.get(i + 3).getFaceValue() - cards.get(i + 2).getFaceValue() == 1
					&& cards.get(i + 2).getFaceValue() - cards.get(i + 1).getFaceValue() == 1
					&& cards.get(i + 1).getFaceValue() - cards.get(i).getFaceValue() == 1;
			if (straight) {
				for (int j = i; j <= i+4; j++) {
					isolatedCards.add(cards.get(j));
				}
			}
		}
		return isolatedCards;
	}
	
	public static ArrayList<Card> isolateThreeOfAKind(ArrayList<Card> cards) {
		ArrayList<Card> isolatedCards = new ArrayList<Card>();
		cards = sortCardsByNumber(cards);
		for (int i = cards.size() - 3; i >= 0; i--) {
			if (cards.get(i).getFaceValue() == cards.get(i + 1).getFaceValue()
					&& cards.get(i + 1).getFaceValue() == cards.get(i + 2).getFaceValue()) {
				isolatedCards.add(cards.get(i));
				isolatedCards.add(cards.get(i+1));
				isolatedCards.add(cards.get(i+2));
				break;
			}
		}
		return isolatedCards;
	}
	
	public static ArrayList<Card> isolateTwoPair(ArrayList<Card> cards) {
		ArrayList<Card> isolatedCards = new ArrayList<Card>();
		cards = sortCardsByNumber(cards);
		return isolatePair(cards);
	}
	
	public static ArrayList<Card> isolatePair(ArrayList<Card> cards) {
		ArrayList<Card> isolatedCards = new ArrayList<Card>();
		cards = sortCardsByNumber(cards);
		for (int i = cards.size() - 2; i >= 0; i--) {
			if (cards.get(i).getFaceValue() == cards.get(i + 1).getFaceValue()) {
				isolatedCards.add(cards.get(i));
				isolatedCards.add(cards.get(i+1));
			}
		}
		return isolatedCards;
	}
	
	public static ArrayList<Card> removeDuplicates(ArrayList<Card> cards) {
		ArrayList<Card> duplicatesRemoved = new ArrayList<Card>();
		ArrayList<Integer> nums = new ArrayList<Integer>();
		for (int i = 0; i < cards.size(); i++) {
			if (nums.contains(cards.get(i).getFaceValue())) {
				continue;
			}
			nums.add(cards.get(i).getFaceValue());
			duplicatesRemoved.add(cards.get(i));
		}
		return duplicatesRemoved;
	}
	
} 
