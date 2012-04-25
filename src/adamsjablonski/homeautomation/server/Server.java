package adamsjablonski.homeautomation.server;
import java.net.*;
import java.util.*;
import java.io.*;
import adamsjablonski.homeautomation.HomeAutomation;
import com.n1nja.io.ConsoleApplication;
public class Server extends ConsoleApplication {
	public static void main(String[] args) {
		Server server = null;
		if (args.length >= 1) {
			server = new Server(Integer.parseInt(args[0]));
		} else {
			server = new Server();
		}
		server.start();
	}
	private ServerSocket listener;
	private final int port;
	private boolean isStarted;
	private final HashSet<Connection> connections;
	public Server(int port) {
		this.port = port;
		this.connections = new HashSet<Connection>();
		this.printLine("Home Automation Server 0.0.0");
		this.printLine("Copyright 2012 Adam Jablonski");
		super.start();
	}
	public Server() {
		this(HomeAutomation.DEFAULT_PORT);
	}
	@Override
	public void start() {
		this.isStarted = true;
		try {
			this.listener = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		final Server server = this;
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					server.listen();
				}
			}
		).start();
		try {
			this.printLine("Server started at " + InetAddress.getLocalHost().getHostAddress() + ":" + this.listener.getLocalPort());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void stop() {
		this.isStarted = false;
		try {
			this.listener.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Connection c : this.connections) {
			c.stop();
		}
		this.printLine("Server stopped");
	}
	@Override
	public void exit() {
		super.stop();
		this.printLine("Server exited");
		super.exit();
	}
	private void listen() {
		while (!this.listener.isClosed() && this.isStarted) {
			try {
				Connection c = new Connection(this, this.listener.accept());
				this.addConnection(c);
				c.start();
			} catch (SocketException e) {
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void handle(String s) {
		String[] args = s.split(" ");
		String command = args[0].toUpperCase();
		//String[] params = Arrays.copyOfRange(args, 1, args.length);
		if (command.equals("STOP")) {
			this.stop();
		} else if (command.equals("EXIT")) {
			if (this.isStarted) {
				this.stop();
			}
			this.exit();
		} else if (command.equals("START")) {
			this.start();
		} else if (command.equals("RESTART")) {
			this.stop();
			this.start();
		}
	}
	public void addConnection(Connection c) {
		this.connections.add(c);
	}
	public void removeConnection(Connection c) {
		this.connections.remove(c);
	}
}