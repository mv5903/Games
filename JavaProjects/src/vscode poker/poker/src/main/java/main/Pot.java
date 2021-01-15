package main;
//♠
/**
 * Keeps track of the pot that players place their bets in.
 * @author matt
 *
 */
public class Pot {
	private int amount;
	
	public Pot() {
		amount = 0;
	}
	
	public void add(int amount) {
		this.amount+=amount;
	}
	
	public void subtract(int amount) {
		this.amount-=amount;
	}
	
	public int winner() {
		int winAmount = amount;
		amount = 0;
		return winAmount;
	}
	
	public String toString() {
		return Integer.toString(amount);
	}
}
