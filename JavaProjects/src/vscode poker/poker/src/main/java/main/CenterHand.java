//♠
import java.util.ArrayList;
/**
 * This is the hand that is specifically the cards that wil be
 * dealt to the center of the table.
 * @author matt
 *
 */
public class CenterHand {
	private ArrayList<Card> hand = new ArrayList<Card>();
	
	public CenterHand(Card a, Card b) {
		hand.add(a);
		hand.add(b);
	}
	
	public ArrayList<Card> getCenter() {
		return hand;
	}
	
	public void dealNextCard(Card c) {
		hand.add(c);
	}
	
	public String toString() {
		if (hand.size() == 2) {
			return "[ ? , ? , ? , ? , ? ]";
		} else if (hand.size() == 3) {
			return String.format("[%s, %s, %s, ? , ? ]", hand.get(0), hand.get(1), hand.get(2));
		} else if (hand.size() == 4) {
			return String.format("[%s, %s, %s, %s, ? ]", hand.get(0), hand.get(1), hand.get(2), hand.get(3));
		} else {
			return hand.toString();
		}
	}
}
