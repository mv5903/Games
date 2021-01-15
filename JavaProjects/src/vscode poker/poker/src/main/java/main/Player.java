

import java.util.ArrayList;

public class Player {
	Bet bet;
	Hand hand;
	boolean hasFolded, betAlreadyThisRound, active;
	String name;
	String handType;
	ArrayList<Card> handCombinedWithCenter;
	int currentBet;
	
	Player(Hand hand, boolean hasFolded, String name) {
		this.hand = hand;
		this.hasFolded = hasFolded;
		this.name = name;
		bet = new Bet();
		currentBet = 0;
		betAlreadyThisRound = false;
		active = true;
	}
	
	public String toString() {
		String fold = hasFolded ? "has folded" : "has not folded";
		return String.format("%s has %s and %s\n", name, hand, fold);
	}
}
