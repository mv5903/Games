import java.util.Vector;

abstract class MultiplayerServer {
    Vector<String> users = new Vector<String>();
    Vector<HandleClient> clients = new Vector<HandleClient>();
    String response = "";
}