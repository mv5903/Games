
//♠
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A deck of Cards are handled in this class accordingly.
 * 
 * @author Matthew Vandenberg
 *
 */
public class DeckOfCards {
	private Card[] deck = new Card[52];
	private ArrayList<Integer> newOrder = new ArrayList<Integer>(); // used for creating a new order for shuffling cards
	private int cardNumber = 0;

	public DeckOfCards() {
		deck = createCardArray();
		shuffleDeck();
	}

	/**
	 * Creates a new array of cards in numerical/suit order.
	 * 
	 * @return The card array in order
	 */
	public Card[] createCardArray() {
		Card[] temp = new Card[52];
		int counter = 0;
		for (int i = 0; i < 4; i++) {
			for (int j = 2; j <= 14; j++) {
				temp[counter] = new Card(j, i);
				counter++;
			}
		}
		return temp;
	}

	public ArrayList<Integer> getOrder() {
		return newOrder;
	}
	
	public List<Card> getAllCards() {
		List<Card> temp = Arrays.asList(deck);
		return temp;
	}

	public void shuffleDeck() {
		Card[] newCards = new Card[52];
		generateNewOrder();
		for (int i = 0; i < 52; i++) {
			newCards[i] = deck[newOrder.get(i)];
		}
		deck = newCards;
	}

	/**
	 * Randomly generates a list of indeces which will then cause {@link #deck} to
	 * be reordered according to this.
	 */
	private void generateNewOrder() {
		for (int i = 0; i < 52; i++) {
			int newNum = (int) (Math.random() * 52);
			while (newOrder.contains(newNum)) {
				newNum = (int) (Math.random() * 52);
			}
			newOrder.add(newNum);
		}
	}

	public Card getNextCard() {
		if (cardNumber == 52) {
			cardNumber = 0;
			this.shuffleDeck();
		}
		Card c = deck[cardNumber];
		cardNumber++;
		return c;
	}

	/**
	 * Aligns all the cards into a 2D array, which will then be displayed in columns
	 * of 13. Not used for BlackJack, only for testing purposes.
	 * 
	 * @return All of the cards as they appear in order, in columns of 13
	 * 
	 */
	public String toString() {
		String temp = "";
		int count = 0;
		Card[][] print = new Card[4][13];
		for (int i = 0; i < print.length; i++) {
			for (int j = 0; j < print[0].length; j++) {
				print[i][j] = deck[count];
				count++;
			}
		}

		for (int i = 0; i < print[0].length; i++) {
			for (int j = 0; j < print.length; j++) {
				temp += print[j][i] + "\t";
			}
			temp += "\n";
		}
		return temp;
	}
}
