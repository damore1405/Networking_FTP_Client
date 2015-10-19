import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.logging.Logger;


public class FTPClient {
	
	private enum FTPState{ auth , passiveCommand , activeCommand }
	private enum IPState{ ipv4 , ipv6 }
	private String host;
	private FTPState state = FTPState.auth;
	private IPState ipState;
	private static final Logger log = Logger.getLogger("log");
	protected FTPCommandSession session;
	

	public FTPClient(String hostname) throws UnknownHostException, IOException{
		ipState = getIpAddressType(hostname);
		host = hostname;
		session = new FTPCommandSession(hostname);
		session.openConnection();
	}
	
	public FTPClient(String hostname, int port) throws UnknownHostException, IOException{
		ipState = getIpAddressType(hostname);
		host = hostname;
		session = new FTPCommandSession(hostname, port);
		session.openConnection();
	}
	
	public void login(String username, String password) throws IOException, FTPException{
		
		session.user(username);
		session.pass(password);
		state = FTPState.passiveCommand;
		log.info("Login of user " + username + " successful");
		
	}
	
	public void ls() throws IOException, FTPException{
		
		if(state == FTPState.passiveCommand){
			session.help();
			String currentDir = session.pwd();
			int port = session.pasv();
			Socket listSocket = new Socket(host, port);
			log.info("Socket at " + port + " opened");
			session.list();
			BufferedReader dirReader = new BufferedReader(new InputStreamReader(listSocket.getInputStream()));
			System.out.println("Contents of directory: " + currentDir);
			while(dirReader.ready()){
				System.out.println(dirReader.readLine());
			}
			listSocket.close();
		}
		
		else if(state == FTPState.activeCommand){
			
			String currentDir = session.pwd();
			ServerSocket serverSocket = new ServerSocket(0);
			
			session.port(serverSocket.getLocalPort());
			System.out.println(serverSocket.getLocalPort());
			session.list();
			Socket listSocket = serverSocket.accept();
			BufferedReader dirReader = new BufferedReader(new InputStreamReader(listSocket.getInputStream()));
			System.out.println("Contents of directory: " + currentDir);
			while(dirReader.ready()){
				System.out.println(dirReader.readLine());
			}
			listSocket.close();
			serverSocket.close();
		}
		else{
			System.out.println("Please login first!");
		}
	}
	
	public void getFile(String filename) throws IOException, FTPException{
		
		File file = new File(filename);
		if(file.exists()){
			throw new IOException("File already exists, cannot overwrite");
		}else if(!file.exists()){
			file.createNewFile();
		}

		FileWriter fileWriter = new FileWriter(filename);
		if(ipState == IPState.ipv4){
			
			if(state == FTPState.passiveCommand){
				int port = session.pasv();
				Socket listSocket = new Socket(host, port);
				session.retr(filename);
				BufferedReader dirReader = new BufferedReader(new InputStreamReader(listSocket.getInputStream()));
				
				while(dirReader.ready()){
					fileWriter.write( dirReader.readLine());
					fileWriter.flush();
				}
				
				fileWriter.close();
				listSocket.close();
				return;
			}
			
			else if(state == FTPState.activeCommand){
	
				ServerSocket serverSocket = new ServerSocket(0);
				session.port(serverSocket.getLocalPort());
				session.retr(filename);
				Socket listSocket = serverSocket.accept();
				BufferedReader dirReader = new BufferedReader(new InputStreamReader(listSocket.getInputStream()));
				
				while(dirReader.ready()){
					fileWriter.write( dirReader.readLine());
					fileWriter.flush();
				}
				
				listSocket.close();
				serverSocket.close();
				fileWriter.close();
				return;
			}
			
			else{
				file.delete();
				fileWriter.close();
				return;
			}
			
		}
		if(ipState == IPState.ipv6){
			
		}
		else{
			file.delete();
			fileWriter.close();
		}
	}
	
	public void passive(){
		if(state == FTPState.passiveCommand){
			state = FTPState.activeCommand;
			System.out.println("State has changed to active...");
			log.info("State has changed to active...");
			return;
		}
		else if(state == FTPState.activeCommand){
			state = FTPState.passiveCommand;
			System.out.println("State has changed to passive...");
			log.info("State has changed to passive");
			return;
		}
		else{
			return;
		}
	}
	private IPState getIpAddressType(String hostname) throws UnknownHostException{
		InetAddress ipVersion = InetAddress.getByName(hostname);
		if (ipVersion instanceof Inet6Address){
			return IPState.ipv6;
		}else if (ipVersion instanceof Inet4Address){
			return IPState.ipv4;
		}
		return null;
	}
}
