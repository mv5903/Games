package poker;
// ♠
public class Bet implements Constants {
	private int balance;
	private static Pot pot = new Pot();
	
	public Bet() {
		balance = INITIAL_AMOUNT;
	}
	
	public void bet(int amount) {
		if (balance < amount) {
			System.out.println("Sorry, you can't bet as you do not have enough credits!");
		} else {
			balance -= amount;
			pot.add(amount);
		}
	}
	
	public void win() {
		balance += pot.winner();
	}
	
	public int getBalance() {
		return balance;
	}
	
	public String toString() {
		return "You have " + balance + " chips";
	}
}
