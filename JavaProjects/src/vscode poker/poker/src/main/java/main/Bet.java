// ♠
/**
 * A bet is created for each player, and various events in the
 * game will determine how their balance changes.
 * @author matt
 *
 */
@SuppressWarnings("unused")
public class Bet implements Constants {
	private int balance;
	private int roundBet;
	public static Pot pot = new Pot();
	
	public Bet() {
		balance = INITIAL_AMOUNT;
		roundBet = 0;
	}
	/**
	 * When someone makes a bet, the amount they bet gets removed from
	 * their total and added to the pot for the round.
	 * @param amount The amount placed in the pot from the player.
	 */
	public void bet(int amount) {
		if (balance < amount) {
			System.out.println("Sorry, you can't bet as you do not have enough credits!");
		} else {
			balance -= amount;
			pot.add(amount);
			roundBet+=amount;
		}
	}
	/**
	 * If the player wins, the amount in the pot gets reset to 0 and
	 * all the money from the pot gets added to that player's balance.
	 */
	public void win() {
		balance += pot.winner();
	}
	
	public void resetRoundBet() {
		roundBet = 0;
	}
	
	public int getBalance() {
		return balance;
	}
	
	public void updateBalance(int balance) {
		this.balance = balance;
	}
	
	public String toString() {
		return "You have " + balance + " chips";
	}
}
