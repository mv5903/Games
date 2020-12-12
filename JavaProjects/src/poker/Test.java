package poker;

import java.util.ArrayList;
import java.util.List;

//♠
public class Test {
	public static void main(String[] args) {
		DeckOfCards dc = new DeckOfCards();
		List<Card> cards = dc.getAllCards();
		ArrayList<Card> temp = new ArrayList<Card>();
		temp.addAll(cards);
		temp = UniqueHands.sortCardsBySuit(temp);
		System.out.println(temp);
		
	}
}
