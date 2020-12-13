package poker;
//♠
import java.util.ArrayList;

public class Test {
	public static void main(String[] args) {
		ArrayList<Card> cards = new ArrayList<Card>();
		cards.add(new Card(11, 2));
		cards.add(new Card(9, 1));
		cards.add(new Card(3, 2));
		cards.add(new Card(14, 0));
		cards.add(new Card(6, 1));
		cards.add(new Card(5, 2));
		cards.add(new Card(12, 0));
		System.out.println(cards);
		System.out.println(UniqueHands.hasWhichHand(cards));
		
	}
}
