package main;
// ♠
/**
 * Defines what is shown for each suit, and the starting amount for
 * each player.
 * @author matt
 *
 */
public interface Constants {
	final int INITIAL_AMOUNT = 1500;
	final int BIG_BLIND = 20;
	final int SMALL_BLIND = 10;
	final char SPADES = '\u2660';
	final char HEARTS = '\u2665';
	final char CLUBS = '\u2663';
	final char DIAMONDS = '\u2666';
	final String DUPLICATE_NAME_ERROR = " Someone with that name already exists. Please join again with another username.";
    final String adminPassword = "";
    final String DEFAULT_CENTER = "[ ? , ? , ? , ? , ? ]";
    final String DEFAULT_HAND = "[ ? , ? ]";
}
