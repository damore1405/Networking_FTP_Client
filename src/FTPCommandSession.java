import java.net.*;
import java.util.logging.Logger;
import java.io.*;
import java.util.Arrays;
import java.util.Timer;

public class FTPCommandSession{

	private static final Logger log = Logger.getLogger("log");
	private static final int FTP_DEFAULT_PORT = 21;
	private static final String FTP_END_COMMAND = "\r\n";
	
	private String hostname;
	private int commandPort;
	private Socket commandSocket;
	private BufferedReader responseReader;
	private PrintWriter socketWriter;

	/**
	 * Constructor to be used with just a hostname, sets the command port to its default value of 21
	 * @param hostname
	 */
	public FTPCommandSession(String hostname){
		this.hostname = hostname;
		this.commandPort = FTP_DEFAULT_PORT;
	}

	/**
	 *	Constructor to be used when the custom port opeion is set, initialized the object like normal
	 *	but with the additional port setting
	 * @param hostname
	 * @param commandport
	 */
	public FTPCommandSession(String hostname, int commandport){
		this.hostname = hostname;
		this.commandPort = commandport;
	}

	/**
	 *
	 * @throws IOException
	 */
	public void openConnection() throws  IOException{
		//Open the socket, and open up an input and output stream for it
		commandSocket = new Socket(hostname, commandPort);

		//Log that the socket to the host has been opened
		log.info("Socket connection to " + hostname + " opened..");

		//Initialize the socket writer and response reader to send and receive information from the socket
		socketWriter = new PrintWriter(commandSocket.getOutputStream(), true);
		responseReader = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));

		log.info(responseReader.readLine());
	}

	/**
	 *	the USER command to be used for user authentication
	 *
	 * @param username the username of the user
	 * @throws IOException
	 * @throws FTPException
	 */
	public void user(String username) throws IOException, FTPException{
		int responseCode;
		String responseString;
		
		socketWriter.print("USER " + username + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);
	}

	/**
	 * The password command to be used after sending a USER request
	 *
	 *
	 * @param password The user password
	 * @return
	 * @throws IOException
	 */
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

	/**
	 *	Implementation of the FTP CDUP command which simply moved up a directory much like cd ..
	 *
	 * @throws IOException
	 * @throws FTPException
	 */
	public void cdup() throws IOException, FTPException {
		int responseCode;
		String responseString;
		
		socketWriter.print("CDUP " + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);
		return;
	}

	/**
	 *	implementation of the cwd command to be used for calling directorys
	 *
	 * @param directory The directory to be called for navigation
	 * @throws IOException
	 * @throws FTPException
	 */
	public void cwd(String directory) throws IOException, FTPException{
		int responseCode;
		String responseString;
		
		socketWriter.print("CDUP "+ directory + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);
		return;
	}

	/**
	 *	The ipv6 implementation of the pasv command
	 *
	 * @return
	 * @throws IOException
	 * @throws FTPException
	 */
	public int epsv() throws IOException, FTPException{
		int responseCode;
		String responseString;
		int responsePort;
		
		socketWriter.print("EPSV" + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);

		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		String passiveResponse = splitResponse[5];

		processResponseCode(responseCode);

		// Parse out the port from the server response by ridding it of parentheses and bars.
		responsePort = Integer.parseInt(passiveResponse.replace("(","").replace(")","").replace("|",""));

		return responsePort;
	}

	/**
	 *	the ipv6 implementation of the port command.
	 *
	 * @param port the port that is to be used for the active connection
	 * @throws IOException
	 * @throws FTPException
	 */
	public void eprt(int port) throws IOException, FTPException{
		int responseCode;
		String responseString;
		String command = "EPRT |2|"+commandSocket.getInetAddress().getHostAddress()+"|"+port+"|"+FTP_END_COMMAND;

		log.info(command);

		socketWriter.print(command);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);
	}

	/**
	 * Implementation of the ipv4 pasv to ask the server during a passive connection to procure a port for use
	 *
	 * @return Returns the port that the server informed the client to use
	 * @throws IOException
	 * @throws FTPException
	 */
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

		//Fiddling with substrings to pull out the information i need, get just the list of ip and ports
		portInfo = splitResponse[splitResponse.length - 1];
		portInfo = portInfo.substring(1, portInfo.length() - 2);

		//Do the math on the p1 and p2 arguments to get the correct port number...
		serverPort = ( Integer.parseInt(portInfo.split(",")[4]) * 256 ) + Integer.parseInt(portInfo.split(",")[5]);
		
		
		log.info(Integer.toString(serverPort));
		return serverPort;
		
		
	}

	/**
	 *
	 * @param port the port to be used to inform the server to use
	 * @throws IOException
	 * @throws FTPException
	 *
	 * constructs the ipv4 PORT FTP request to the server for an active connection.
	 */
	public void port(int port)throws IOException, FTPException{
		int responseCode;
		String responseString;
		/*
			Build the port command by splitting the port number into two hex trings then parsing it down into the
			correct format to be sent over the PORT command.
		*/
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
		processResponseCode(responseCode);
	}


	/**
	 *
	 * @throws IOException
	 * @throws FTPException
	 *
	 * Implementation of the LIST command, lists out the contents of the current directory.
	 */
	public void list() throws IOException, FTPException{
		int responseCode;
		String responseString;
		
		socketWriter.print("LIST" + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);

	}

	/**
	 *
	 * @return The string representation of the current working directory
	 * @throws IOException
	 * @throws FTPException
	 *
	 * Implementaiton of the PWD ftp command, used to get the current working directory that the server is currently using
	 *
	 */
	public String pwd() throws IOException, FTPException{
		int responseCode;
		String responseString;
		
		socketWriter.print("PWD" + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);
		
		return splitResponse[1].replace("\"", "");
	}

	/**
	 *
	 * @return The help string returned from the server.
	 * @throws IOException
	 * @throws FTPException
	 *
	 * Implementation of the HELP FTP command to return the avialable commands that the server can use,
	 * not too usefill in this case when everything is predefined.
	 *
	 */
	public String help() throws IOException, FTPException{
		int responseCode;

		String responseString = "";
		socketWriter.print("HELP" + FTP_END_COMMAND);
		socketWriter.flush();

		for (int i = 0; i <= 5 && !responseReader.ready(); ++i){
			if(i == 5) throw new FTPException("Response Timeout");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		while(responseReader.ready()){
			responseString += responseReader.readLine();
		}

		log.info(responseString);
		String[] splitResponse = responseString.split("-");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);

		return responseString;
	}

	/**
	 *
	 * @param filename the name of the file being requested from the server
	 * @throws IOException
	 * @throws FTPException
	 *
	 * Implementation of the RETR request to be sent to the FTP server
	 *
	 */
	public void retr(String filename) throws IOException, FTPException{
		int responseCode;
		String responseString;
		
		socketWriter.print("RETR " + filename + FTP_END_COMMAND);
		socketWriter.flush();
		responseString = responseReader.readLine();
		log.info(responseString);
		String[] splitResponse = responseString.split(" ");
		responseCode = Integer.parseInt(splitResponse[0]);
		processResponseCode(responseCode);
		return;
	}

	/**
	 *
	 * @throws IOException
	 *
	 * Sends the quit command to the FTP server to shut down the connection cleanly
	 */
	public void quit() throws IOException { 
		socketWriter.print("QUIT" + FTP_END_COMMAND);
		socketWriter.flush();
	}

	/**
	 *
	 * @param responseCode The response code from the request to be passed to the method for analyses
	 * @throws FTPException
	 *
	 * Collection of the response codes and exceptions that they may cause to be called after the end of each request.
	 */
	private void processResponseCode(int responseCode) throws FTPException{
		switch (responseCode) {
			case 500:
				//Syntax error
				throw new FTPException("Syntax Error");
			case 501:
				//Syntax error in params
				throw new FTPException("Syntax Error");
			case 421:
				//Service not available error
				throw new FTPException("Server not available");
			case 502:
				//Command not implemented
				throw new FTPException("Command not implemented");
			case 425:
				//Cant open data connection
				throw new FTPException("Cannot open data connection");
			case 426:
				//Connection closed
				throw new FTPException("Connection closed");
			case 451:
				//Local error in processing
				throw new FTPException("Local Error in processing");
			case 450:
				//File Busy
				throw new FTPException("File Unavailable");
			case 550:
				//File Not Found
				throw new FTPException("File not found");
			case 530:
				//Not logged in
				throw new FTPException("not logged in");
			default:
				return;

		}
	}

	/**
	 * Flushes the reader after a data connection to get the confirmations.
	 * @throws IOException
	 * @throws FTPException
	 */
	public void flushReader() throws IOException, FTPException {

		for (int i = 0; i <= 5 && !responseReader.ready(); ++i){
			if(i == 5) throw new FTPException("Response Timeout");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		while(responseReader.ready()){
			log.info(responseReader.readLine());
		}
	}
	
}
