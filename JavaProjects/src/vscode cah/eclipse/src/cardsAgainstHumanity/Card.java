package cardsAgainstHumanity;

public class Card {
    String contents;
    boolean isQuestions;

    public Card(String contents, boolean isQuestions) {
        this.isQuestions = isQuestions;
        this.contents = contents;
    }

    public String toString() {
        String temp = isQuestions ? "Question: " : "Answer: ";
        return temp + contents;
    }
}
