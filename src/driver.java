import java.net.*;
import java.io.Console;
import java.io.IOException;
import java.util.logging.*;

public class driver {
	//Init the logger
	private static final Logger log = Logger.getLogger(driver.class.getName());
	
	public static void main(String[] args) {
		
		//TODO: Maybe make this into a ftp sessions. 
		int portNumber = 21;
		String hostname = null;
		String logFilePath = null;
		
		switch(args.length){
			case 0:
			case 1: printUsage();
				break;
			case 2: 
				//We have a server name and a log file
				hostname = args[0];
				logFilePath = args[1];
				break;
			case 3:
				//Now we also have a port number, So set the port...
				hostname = args[0];
				logFilePath = args[1];
				portNumber = Integer.parseInt(args[3]);
				break;
			default: System.err.println("Invalid arguments");
		}
		
		try {
			FileHandler fh = new FileHandler (logFilePath, true);
			SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  
			log.addHandler(fh);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("testing testing 123, burp");
		
		//Valid arguments, ask now as the user for their username and password
		
		
		System.out.println();
	}

	/*
	 * Prints the usage of the FTP interface when no input is given,
	 * or when invalid input is given. 
	 */
	private static void printUsage(){
		System.out.println("This will eventually be the help script :3");
		System.exit(1);
	}

}