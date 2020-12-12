package poker;
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

    public List<Card> getAllCardsInHand() {
    	return Arrays.asList(cards);
    }
    
    public String toString() {
        return Arrays.toString(cards);
    }
}
