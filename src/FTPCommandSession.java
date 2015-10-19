import java.net.*;
import java.util.logging.Logger;
import java.io.*;
import java.util.Arrays;
import java.util.Timer;

public class FTPCommandSession{
	
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
	private ServerSocket dataSocket;
	private BufferedReader responseReader;
	private PrintWriter socketWriter;
	

	public FTPCommandSession(String hostname){
		this.hostname = hostname;
		this.commandPort = FTP_DEFAULT_PORT;
	}
	
	public FTPCommandSession(String hostname, int commandport){
		this.hostname = hostname;
		this.commandPort = commandport;
	}
	
	public void openConnection() throws UnknownHostException, IOException{
		//Open the socket, and open up an input and output stream for it
		commandSocket = new Socket(hostname, commandPort);
		log.info("Socket connection to " + hostname + " opened..");
		socketWriter = new PrintWriter(commandSocket.getOutputStream(), true);
		responseReader = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
		log.info(responseReader.readLine());
	}
	
	public void user(String username) throws IOException, FTPException{
		int responseCode;
		String responseString;
		
		socketWriter.print("USER " + username + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
//		log.info(responseString);
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);
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
	
	public int cdup() throws IOException {
		int responseCode;
		String responseString;
		
		socketWriter.print("CDUP " + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		
		return responseCode;
	}
	
	public int cwd(String directory) throws IOException{
		int responseCode;
		String responseString;
		
		socketWriter.print("CDUP "+ directory + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		
		return responseCode;
	}
	
	public int epsv() throws IOException, FTPException{
		int responseCode;
		String responseString;
		
		socketWriter.print("EPSV" + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		

		return responseCode;
	}
	public int pasv() throws IOException, FTPException{
		int responseCode;
		int serverPort;
		String portInfo;
		String responseString;
		
		socketWriter.print("PASV" + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);
		
		portInfo = splitResponse[splitResponse.length - 1];
		portInfo = portInfo.substring(1, portInfo.length() - 2);
		
		serverPort = ( Integer.parseInt(portInfo.split(",")[4]) * 256 ) + Integer.parseInt(portInfo.split(",")[5]);
		
		
		log.info(Integer.toString(serverPort));
		return serverPort;
		
		
	}
	public void port(int port)throws IOException, FTPException{
		int responseCode;
		String responseString;
		String command = "PORT " 
						+ commandSocket.getLocalAddress().getHostAddress().replace(".", ",") + ","
						+ Integer.parseInt(Integer.toHexString(port).substring(0, 2), 16) + ","
						+ Integer.parseInt(Integer.toHexString(port).substring(2, 4), 16) + FTP_END_COMMAND;
		
		log.info(command);
		
		socketWriter.print(command);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
	}
	
	public void list() throws IOException, FTPException{
		int responseCode;
		String responseString;
		
		socketWriter.print("LIST" + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		
	}

	public String pwd() throws IOException{
		int responseCode;
		String responseString;
		
		socketWriter.print("PWD" + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		
		return splitResponse[1].replace("\"", "");
	}
	
	public String help() throws IOException{
		int responseCode;
		String responseString = "";
		
		socketWriter.print("HELP" + FTP_END_COMMAND);
		socketWriter.flush();
		while(!responseString.contains("Help OK")){
			responseString += responseReader.readLine() + "\n";
		}
		log.info(responseString);
//		String[] splitResponse = responseString.split(" ");
//		responseCode = Integer.parseInt(splitResponse[0]);
		
		return responseString;
	}
	public void retr(String filename) throws IOException{
		int responseCode;
		String responseString;
		
		socketWriter.print("RETR " + filename + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		
		return;
	}
	
	public void quit() throws IOException { 
		socketWriter.print("QUIT" + FTP_END_COMMAND);
		socketWriter.flush();
	}
	
	private int processResponseCode(int code) throws FTPException{
//		switch (code) {
//			throw new 
//		}
		return code;
	}
	
}
