package poker;

import java.io.PrintStream;
import java.util.Scanner;

import org.json.JSONObject;
import org.json.JSONArray;

public class NetIndex {
	static String IPAddress;
	final static int port = 8080;
	static Scanner kbd = new Scanner(System.in);
	static PrintStream charStream;
	
	public static void main(String[] args) {
		System.out.println("Welcome to Poker - Internet Version. Are you hosting?");
		if (kbd.next().equalsIgnoreCase("yes")) {
			System.out.println("Ok. Please follow the instructions on GitHub to learn how to create a server.");
			System.exit(0);
		} else {
			System.out.println("Please enter the IP Address of whom you would like to connect to.");
			IPAddress = kbd.nextLine();
			getPersonalInformation();
		}
	}
	
	public static void getPersonalInformation() {
		System.out.println("Please enter your name: ");
		String name = kbd.next();
		
	}
}
