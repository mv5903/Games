package poker;
import java.util.ArrayList;
//♠
import java.util.Arrays;
import java.util.List;


public class Hand {
    private Card[] cards = new Card[2];

    public Hand(Card a, Card b) {
        cards[0] = a;
        cards[1] = b;
    }

    public Card getCard(int cardNumber) {
        return cards[cardNumber];
    }
    
    public void reset(Card a, Card b) {
    	cards[0] = a;
    	cards[1] = b;
    }

    public List<Card> getAllCardsInHand() {
    	return Arrays.asList(cards);
    }
    public ArrayList<Card> getArrayListOfHand() {
    	ArrayList<Card> temp = new ArrayList<Card>();
    	temp.add(cards[0]);
    	temp.add(cards[1]);
    	return temp;
    }
    
    public String toString() {
        return Arrays.toString(cards);
    }
}
