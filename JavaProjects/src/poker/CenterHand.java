package poker;
//♠
import java.util.ArrayList;

public class CenterHand {
	private ArrayList<Card> hand = new ArrayList<Card>();
	
	public CenterHand(Card a, Card b, Card c) {
		hand.add(a);
		hand.add(b);
		hand.add(c);
	}
	
	public ArrayList<Card> getCenter() {
		return hand;
	}
	
	public void dealNextCard(Card c) {
		hand.add(c);
	}
	
	public String toString() {
		return hand.toString();
	}
}
