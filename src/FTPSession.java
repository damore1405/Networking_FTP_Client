import java.net.*;
import java.util.logging.Logger;
import java.io.*;

public class FTPSession{
	
	private enum state{
		AUTH,Transfer
	}
	private enum FTPCommands{
		
	}
	
	private static final Logger log = Logger.getLogger("log");
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
	}
	
	public FTPSession(String hostname, int commandport){
		this.hostname = hostname;
		this.commandPort = commandport;
	}
	
	public void openConnection() throws UnknownHostException, IOException{
		//Open the socket, and open up an input and output stream for it
		commandSocket = new Socket(hostname, commandPort);
		log.info("Socket connection to " + hostname + " opened..");
		socketWriter = new PrintWriter(commandSocket.getOutputStream(), true);
		responseReader = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
	}
	
	public int user(String username) throws IOException{
		int responseCode;
		String responseString;
		
		socketWriter.print("USER " + username + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		
		return responseCode;
	}
	
	public int pass(String password) throws IOException {
		int responseCode;
		String responseString;
		
		socketWriter.print("PASS " + password + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		
		return responseCode;
	}
	public void quit() throws IOException { 
		socketWriter.print("QUIT" + FTP_END_COMMAND);
		socketWriter.flush();
	}
	
	
}
