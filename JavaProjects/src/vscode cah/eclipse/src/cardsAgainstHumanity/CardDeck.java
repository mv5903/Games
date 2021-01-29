package cardsAgainstHumanity;

import java.util.ArrayList;

public class CardDeck {
    boolean isQuestions;
    ArrayList<Card> cards = new ArrayList<Card>();
    int cardNumber, lastCard;

    public CardDeck(String type) {
        isQuestions = type.equals("questions");
        cardNumber = 0;
    }

    public void init(ArrayList<String> words) {
        lastCard = words.size() - 1;
        for (String s: words) {
            cards.add(new Card(s, isQuestions));
        }
        shuffle();
        shuffle();
    }

    public void shuffle() {
        ArrayList<Integer> order = new ArrayList<Integer>();
        int random = (int)(Math.random() * lastCard);
        while (order.contains(random)) {
            random = (int)(Math.random() * lastCard);
        }
        ArrayList<Card> placeHolder = new ArrayList<Card>();
        for (Card c: cards) {
            placeHolder.add(c);
        }
        for (int i = cards.size() - 1; i >= 0; i--) {
            if (order.size() == 0) {
                break;
            }
            cards.set(order.get(i), placeHolder.get(i));
            order.remove(i);
        }
    }

    public Card getNextCard() {
        if (cardNumber == lastCard + 1) {
            shuffle();
            cardNumber = 0;
        }
        cardNumber++;
        return cards.get(cardNumber);
    }
}
