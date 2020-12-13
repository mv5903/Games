package poker;
// ♠
public class Bet implements Constants {
	private int balance;
	private static Pot pot = new Pot();
	
	public Bet() {
		balance = INITIAL_AMOUNT;
	}
	
	public void bet(int amount) {
		balance -= amount;
		pot.add(amount);
	}
	
	public void win() {
		balance += pot.winner();
	}
	
	public String toString() {
		return "You have " + balance + " chips";
	}
}
