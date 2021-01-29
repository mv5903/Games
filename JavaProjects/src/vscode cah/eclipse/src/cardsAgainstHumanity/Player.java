package cardsAgainstHumanity;

import java.util.ArrayList;

public class Player {
    ArrayList<Card> cards;
    String name;
    int wins = 0;

    public Player(String name) {
        this.name = name;
        cards = new ArrayList<Card>();
    }
}
