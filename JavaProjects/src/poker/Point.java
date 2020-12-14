package poker;

import java.util.ArrayList;

public class Point {
	private int player;
	private String hand;
	private ArrayList<Card> allCards;
	
	public Point(int player, String hand, ArrayList<Card> allCards) {
		this.player = player;
		this.hand = hand;
		this.allCards = allCards;
	}
	
	public String getHand() {
		return hand;
	}
	
	public int getPlayer() {
		return player;
	}
	
	public ArrayList<Card> getAllCards() {
		return allCards;
	}
	
	public boolean equals(Point p) {
		return this.getHand().equals(p.getHand()) && this.getPlayer() == p.getPlayer();
	}
	
	public String toString() {
		return String.format("Player %d: %s", player, hand);
	}
}
