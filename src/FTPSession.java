import java.net.*;
import java.util.logging.Logger;
import java.io.*;

public class FTPSession{
	
	private static final Logger log = Logger.getLogger(driver.class.getName());
	private static final int FTP_DEFAULT_PORT = 21;
	private static final String FTP_END_COMMAND = "\r\n";
	
	private String hostname;
	private int commandPort;
	private int dataPort;
	private Socket commandSocket;
	private Socket dataSocket;
	private BufferedReader responseReader;
	private PrintWriter socketWriter;
	

	public FTPSession(String hostname){
		this.hostname = hostname;
		this.dataPort = FTP_DEFAULT_PORT;
		log.info("TESTING TESTING");
	}
	
	public FTPSession(String hostname, int commandport){
		this.hostname = hostname;
		this.commandPort = commandport;
	}
	
	public void openConnection() throws UnknownHostException, IOException{
		//Open the socket, and open up an input and output stream for it
		commandSocket = new Socket(hostname, commandPort);
		socketWriter = new PrintWriter(commandSocket.getOutputStream(), true);
		responseReader = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
		//TODO Log that the port has been opened
	}
	
	//TODO Implement the send command
	public String sendCommand() {
		return null;
	}
	
	
	
}
