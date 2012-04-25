package adamsjablonski.homeautomation.server;
import java.net.*;
import java.io.*;
public class Connection {
	private final Socket socket;
	private final Server server;
	private final BufferedWriter netWrite;
	private final BufferedReader netRead;
	private boolean isListening;
	public Connection(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		BufferedWriter netWrite = null;
		BufferedReader netRead = null;
		try {
			netWrite = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
			netRead = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.netWrite = netWrite;
		this.netRead = netRead;
		this.printLine("Connected");
	}
	public void start() {
		this.isListening = true;
		final Connection connection = this;
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					connection.listen();
				}
			}
		).start();
	}
	public void stop() {
		this.isListening = false;
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.printLine("Disconnected");
			this.server.removeConnection(this);
		}
	}
	private void listen() {
		String line;
		while (!this.socket.isInputShutdown() && this.isListening) {
			line = this.readLine();
			if (line != null) {
				this.printLine("[READ] " + line);
			}
		}
	}
	public void writeLine(String s) {
		try {
			this.netWrite.write(s + '\n');
			this.netWrite.flush();
		} catch (SocketException e) {
			this.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String readLine() {
		String readLine = null;
		try {
			readLine = this.netRead.readLine();
		} catch (SocketException e) {
			this.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return readLine;
	}
	public void printLine(String s) {
		this.server.printLine("[" + this.socket.getInetAddress().getHostAddress() + "] " + s);
	}
}