package main;
//♠
/**
 * Each card contains the face value, the suit it represents, and
 * the suit reperesented as an interger used with {@link #compareTo(Card)}.
 * @author matt
 *
 */
public class Card {
	private int faceValue;
	private String suit;
	private int suitInt;
	
	public Card(int faceValue, int suit) {
		this.suitInt = suit;
		switch (suit) {
			case 0: this.suit = "\u2660"; break;
			case 1: this.suit = "\u2665"; break;
			case 2: this.suit = "\u2663"; break;
			case 3: this.suit = "\u2666"; break;
		}
		this.faceValue = faceValue;
	}

	public int getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(int faceValue) {
		this.faceValue = faceValue;
	}

	public String getSuit() {
		return suit;
	}

	public int getSuitInt() {
		return suitInt;
	}

	public void setSuit(String suit) {
		this.suit = suit;
	}
	/**
	 * Cards are compared based on their face value first. If they
	 * are the same, then they are compared by their suit order.
	 * @param c The Card to be compared to this.Card
	 * @return The difference in the cards. Number will be positive
	 * if this comes after c, similar to how the String compareTo works.
	 * 
	 */
	public int compareTo(Card c) {
		if (c.getFaceValue() == this.getFaceValue()) {
			return c.getSuitInt() - this.getSuitInt();
		} else {
			return this.getFaceValue() - c.getFaceValue();
		}
	}
	
	public boolean equals(Card c) {
		return this.getFaceValue() == c.getFaceValue() && this.getSuit().equals(c.getSuit());
	}
	
	public String toString() {
		String toReturn = "";
		switch (faceValue) {
		case 14: toReturn+="A"; break;
		case 11: toReturn+="J"; break;
		case 12: toReturn+="Q"; break;
		case 13: toReturn+="K"; break;
		default: toReturn+=faceValue;
		}
		return toReturn + " " + suit;
	}
	
	
}
