package poker;

public class Point {
	private int player;
	private String hand;
	
	public Point(int player, String hand) {
		this.player = player;
		this.hand = hand;
	}
	
	public String getHand() {
		return hand;
	}
	
	public int getPlayer() {
		return player;
	}
	
	public boolean equals(Point p) {
		return this.getHand().equals(p.getHand()) && this.getPlayer() == p.getPlayer();
	}
	
	public String toString() {
		return String.format("Player %d: %s", player, hand);
	}
}
