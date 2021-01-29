package cardsAgainstHumanity;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
	private static final long serialVersionUID = -3518166377757486607L;
	String subject, message;
	boolean simple;
	ArrayList<String> list;
	
	Message(String subject, ArrayList<String> list) {
		this.subject = subject;
		this.list = list;
		simple = false;
	}
	
	Message(String subject, String message) {
		this.message = message;
		this.subject = subject;
		simple = true;
	}
	
	public String toString() {
		if (simple) {
			return subject + "\n" + message;
		}
		return subject + "\n" + list;
	}
}
