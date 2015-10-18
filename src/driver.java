import java.net.*;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.*;

public class driver {
	//Init the logger
	private static final Logger log = Logger.getLogger("log");
	
	public static void main(String[] args) {
		
		//TODO: Maybe make this into a ftp sessions. 
		FTPSession ftpSession;
		String logFilePath = null;
		
		switch(args.length){
			case 2: 
				//We have a server name and a log file
				setUpLogger(args[1]);  /* The singleton logger needs to be set up first to be used in the ftp Session */
				ftpSession = new FTPSession(args[0]);
				break;
			case 3:
				//Now we also have a port number, So set the port...
				setUpLogger(args[1]);
				ftpSession = new FTPSession(args[0], Integer.parseInt(args[3]));
				break;
			default: printUsage();
		}
		
		
//		Okey now we have all we need to test out sending a user request
		try {
			Socket socket = new Socket("localhost", 21);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.print("USER matt\r\n");
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println(in.readLine());
			out.print("USER matt\r\n");
			out.flush();
			System.out.println(in.readLine());
			out.print("QUIT\r\n");
			out.flush();
			socket.close();
		
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		//Valid arguments, ask now as the user for their username and password
		
		
		System.out.println();
	}

	/*
	 * Prints the usage of the FTP interface when no input is given,
	 * or when invalid input is given. 
	 */
	private static void printUsage(){
		log.warning("Invalid input entered!");
		System.out.println("This will eventually be the help script :3");
		System.exit(1);
	}
	
	
	public static void setUpLogger(String logFilePath){
		try {
			FileHandler fh = new FileHandler (logFilePath, true);
			SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  
			log.addHandler(fh);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}