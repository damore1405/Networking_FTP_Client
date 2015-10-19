import java.io.*;
import java.net.*;
import java.util.logging.Logger;


public class FTPClient {
    private static final int TIMEOUT_TICK = 250;
    private enum FTPState {auth, passiveCommand, activeCommand}
    private enum IPState {ipv4, ipv6}
    private static final Logger log = Logger.getLogger("log");
    protected FTPCommandSession session;
    private String host;
    private FTPState state = FTPState.auth;
    private IPState ipState;

    /**
     *
     * @param hostname
     * @throws IOException
     * @throws UnknownHostException
     *
     * FTPClient constructor, Creates a client and ftp session to be used by a driver, taking in only
     * a host name and using the default port of 21
     */
    public FTPClient(String hostname) throws IOException, UnknownHostException {
        ipState = getIpAddressType(hostname);
        host = hostname;
        session = new FTPCommandSession(hostname);
        session.openConnection();
    }

    /**
     *
     * @param hostname
     * @param port
     * @throws IOException
     * @throws UnknownHostException
     *
     * FTPClient constructor, Creates a client and ftp session to be used by a driver, taking in a host
     * as well as a custom port to be used by the session.
     *
     */
    public FTPClient(String hostname, int port) throws IOException, UnknownHostException {
        ipState = getIpAddressType(hostname);
        host = hostname;
        session = new FTPCommandSession(hostname, port);
        session.openConnection();
    }

    /**
     *
     * @param username The users username
     * @param password The users password
     * @throws IOException
     * @throws FTPException
     *
     * The login method for the ftp session, called when the connection is first made to athenticate the user
     *
     */
    public void login(String username, String password) throws IOException, FTPException {

        session.user(username);
        session.pass(password);
        state = FTPState.passiveCommand;
        log.info("Login of user " + username + " successful");

    }

    /**
     *
     * @throws IOException
     * @throws FTPException
     *
     * Lists out the files in the current directory on the server depending on the PWD.
     *
     */
    public void ls() throws IOException, FTPException {

        //Initialize the port to a dummy number to be overwritton
        int port = -1;

        //Check to see what passive state the client is in
        if (state == FTPState.passiveCommand) {
            String currentDir = session.pwd();

            //Make a decision to use IPv4 or v6 depending on the mode set from the constructor
            port = passIpConn();

            //Begin listening on the socket
            Socket listSocket = new Socket(host, port);
            log.info("Socket at " + port + " opened");

            //Call the LIST command on the underlying session object
            session.list();

            BufferedReader dirReader = new BufferedReader(new InputStreamReader(listSocket.getInputStream()));
            System.out.println("Contents of directory: " + currentDir); /* Let the user know the contents of the current directory  */

            //Implementation of a timeout, wait 5 seconds for the buffered reader to receive a response, if not, throw a new exception
            for(int i = 0; i <= 10 && !dirReader.ready(); ++i){
                // Wait 5 seconds max while waiting for a response
                if(i == 10) throw new FTPException("Timeout");
                try {
                    Thread.sleep(TIMEOUT_TICK);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            }

            while (dirReader.ready()) { /* While there's something to read */
                System.out.println(dirReader.readLine());
            }
            // Flush the buffer, close the socket, and return.
            session.flushReader();
            listSocket.close();
            return;

        }
        else if (state == FTPState.activeCommand) {
            String currentDir = session.pwd();
            ServerSocket serverSocket = new ServerSocket(0);

            //Check for ipv4 vs ipv6
            actvIpConn(serverSocket.getLocalPort());

            System.out.println(serverSocket.getLocalPort());
            session.list();
            Socket listSocket = serverSocket.accept();
            BufferedReader dirReader = new BufferedReader(new InputStreamReader(listSocket.getInputStream()));
            System.out.println("Contents of directory: " + currentDir);

            for(int i = 0; i <= 10 && !dirReader.ready(); ++i){
//              Wait 5 seconds max while waiting for a response
                if(i == 10) throw new FTPException("Timeout");
                try {
                    Thread.sleep(TIMEOUT_TICK);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            while (dirReader.ready()) {
                System.out.println(dirReader.readLine());
            }

            session.flushReader();
            listSocket.close();
            serverSocket.close();

        }
    }

    public void getFile(String filename) throws IOException, FTPException {
        int port = -1;
        File file = new File(filename);
        if (file.exists()) {
            throw new IOException("File already exists, cannot overwrite");
        } else if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(filename);

        if (state == FTPState.passiveCommand) {

            port = passIpConn();
            Socket listSocket = new Socket(host, port);
            session.retr(filename);
            BufferedReader dirReader = new BufferedReader(new InputStreamReader(listSocket.getInputStream()));

            while (dirReader.ready()) {
                fileWriter.write(dirReader.readLine());
                fileWriter.write("\n");
                fileWriter.flush();
            }

            session.flushReader();
            fileWriter.close();
            listSocket.close();
            return;

        } else if (state == FTPState.activeCommand) {

            ServerSocket serverSocket = new ServerSocket(0);
            actvIpConn(serverSocket.getLocalPort());

            session.retr(filename);
            Socket listSocket = serverSocket.accept();
            BufferedReader dirReader = new BufferedReader(new InputStreamReader(listSocket.getInputStream()));

            while (dirReader.ready()) {
                fileWriter.write(dirReader.readLine());
                fileWriter.write("\n");
                fileWriter.flush();
            }

            listSocket.close();
            serverSocket.close();
            fileWriter.close();
            session.flushReader();
            return;

        } else {
            file.delete();
            fileWriter.close();
            return;
        }
    }

    public void passive() {
        if (state == FTPState.passiveCommand) {
            state = FTPState.activeCommand;
            System.out.println("State has changed to active...");
            log.info("State has changed to active...");
            return;
        } else if (state == FTPState.activeCommand) {
            state = FTPState.passiveCommand;
            System.out.println("State has changed to passive...");
            log.info("State has changed to passive");
            return;
        } else {
            return;
        }
    }
    public void pwd() throws IOException, FTPException {
        System.out.print(session.pwd());
    }

    public void help() throws IOException, FTPException {
        String response = session.help();
        System.out.println(response);
    }
    public void quit() throws IOException {
        session.quit();
    }
    public void cd(String dirName) throws IOException, FTPException {
        session.cwd(dirName);
    }
    public void cdup() throws IOException, FTPException {
        session.cdup();
    }

    private IPState getIpAddressType(String hostname) throws UnknownHostException {
        InetAddress ipVersion = InetAddress.getByName(hostname);
        if (ipVersion instanceof Inet6Address) {
            return IPState.ipv6;
        } else if (ipVersion instanceof Inet4Address) {
            return IPState.ipv4;
        }
        return null;
    }
    private int passIpConn() throws IOException, FTPException {
        int port = -1;
        if(ipState == IPState.ipv4){
            port = session.pasv();
        }
        else if (ipState == IPState.ipv6){
            port = session.epsv();
        }
        return port;
    }
    private void actvIpConn(int port) throws IOException, FTPException {
        if(ipState == IPState.ipv4) {
            session.port(port);
        }else if(ipState == IPState.ipv6){
            session.eprt(port);
        }
    }




}
