package example.anagram;

import ssobjects.Stopwatch;
import ssobjects.telnet.TelnetServerSocket;
import ssobjects.telnet.TelnetServerThreaded;
import ssobjects.telnet.threads.TelnetServerHandler;

import java.io.IOException;
import java.util.*;

/**
 * Example server that provides the ability to login, query who else is
 * connected to the server, perform a couple operations, and shut down the
 * server.
 * 
 * Three important things to note:
 * 
 * 1) The server is able to send continuous updates to one or more connections.
 * 2) The server is able to handle long running operations, by utilizing
 * handlers on a different thread. 3) The server is able to cleanly shutdown.
 * 
 * @author lbpatterson
 */
public class AnagramServer extends TelnetServerThreaded {
	public static int PORT = 4444;
	protected Map<TelnetServerSocket, AnagramUser> userMap = new HashMap<TelnetServerSocket, AnagramUser>();
	protected Stopwatch timer = new Stopwatch();
	protected Map<String, AnagramGame> gameMap = new HashMap<String, AnagramGame>();

	public static void main(String[] args) throws Exception {
		AnagramServer server = new AnagramServer(PORT);
		server.idleTime = 2000;
		server.info("Anagram Server running on port " + PORT);
		server.runServer();
	}

	public AnagramServer(int port) throws IOException {
		super(null, port);
	}

	@Override
	protected TelnetServerHandler createHandler(int id) {
		return new AnagramHandler(this, "anagram handler " + id);
	}

	@Override
	public void connectionAccepted(TelnetServerSocket sock) {
		try {
			AnagramUser u = new AnagramUser(sock);
			addUser(sock, u);

			info("MonitorServerThreaded New connection from ["
					+ sock.getHostAddress() + "]");
			sock.println("Login:");
		} catch (Exception e) {
			e.printStackTrace();
			close(sock);
		}
	}

	@Override
	public void connectionClosed(TelnetServerSocket sock, IOException e) {
		info("Connection from [" + sock.getHostAddress() + "] closed");
		sock.close();
		removeUser(sock);
	}

	@Override
	public void idle(long deltaTime) {
		// info("MonitorServerThreaded Idle ["+deltaTime+"]");
		long now = System.currentTimeMillis();
		printlnAllTickingUsers("tick [" + now + "]");
		Iterator<AnagramGame> it = gameMap.values().iterator();
		while (it.hasNext()) {
			AnagramGame game = it.next();
			game.tick(this);
		}
	}

	public synchronized void printlnAllTickingUsers(String s) {
		Iterator<AnagramUser> it = userMap.values().iterator();
		while (it.hasNext()) {
			AnagramUser u = it.next();
			if (u.isLoggedIn() && u.isTicking()) {
				try {
					u.sock.println(s);
				} catch (IOException e) {
					close(u);
				}
			}
		}
	}

	public void close(AnagramUser u) {
		removeUser(u.sock);
		u.sock.close();
	}

	protected synchronized void addUser(TelnetServerSocket sock, AnagramUser u) {
		userMap.put(sock, u);
	}

	public synchronized void removeUser(TelnetServerSocket sock) {
		AnagramUser u = userMap.get(sock);
		userMap.remove(u.sock);
		AnagramGame g = u.currentGame;
		if (g != null) {
			g.removeUser(u);
			g.println(u.username+" has left");
		}
	}

	protected synchronized AnagramUser getUser(TelnetServerSocket sock) {
		return userMap.get(sock);
	}

	/**
	 * Grabs a copy of the user list. Since it is not save to iterate through a
	 * list in multiple threads, grab a copy and work with that list.
	 * 
	 * @return user list, or empty list if no users.
	 */
	protected synchronized List<AnagramUser> getUserList() {
		List<AnagramUser> users = new ArrayList<AnagramUser>();
		Iterator<AnagramUser> it = userMap.values().iterator();
		while (it.hasNext()) {
			users.add(it.next());
		}
		return users;
	}

	/** @return copy of the game list. */
	public synchronized List<AnagramGame> getGames() {
		List<AnagramGame> names = new ArrayList<AnagramGame>();
		Iterator<AnagramGame> it = gameMap.values().iterator();
		while (it.hasNext()) {
			names.add(it.next());
		}
		return names;
	}

	/**
	 * Create a new game named gameName, and adds it to the game map.
	 * 
	 * @param gameName
	 *            User defined game name
	 * @return Game object that was just created.
	 */
	public synchronized AnagramGame createGame(String gameName) {
		if (gameMap.get(gameName) != null)
			return null; // game already exists

		AnagramGame game = new AnagramGame(gameName,this);
		gameMap.put(gameName, game);
		return game;
	}

	/**
	 * Remove the given game, returns the game object you removed.
	 * 
	 * @return The game you just removed, or null if none was found.
	 */
	public synchronized AnagramGame removeGame(String gameName) {
		AnagramGame g = gameMap.get(gameName);
		if (g == null) {
			gameMap.remove(gameName);
		}
		return g;
	}

	/**
	 * Finds the game of gameName.
	 * 
	 * @return The game that matches, or null if none found.
	 */
	public synchronized AnagramGame findGame(String gameName) {
		AnagramGame g = gameMap.get(gameName);
		return g;
	}

}
