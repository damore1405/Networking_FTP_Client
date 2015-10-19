import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class driver {
    //Init the logger
    private static final Logger log = Logger.getLogger("log");

    public static void main(String[] args) {

        // Init the scanner for input, and the usrname and password
        Scanner in = new Scanner(System.in);
        String username;
        String password;
        FTPClient ftpClient = null;

        try {
            // Preform argument verification and preform actions accordingly
            switch (args.length) {
                case 2:
                    //We have a server name and a log file
                    setUpLogger(args[1]);  /* The singleton logger needs to be set up first to be used in the ftp Session */
                    try {
                        ftpClient = new FTPClient(args[0]);
                    }catch (UnknownHostException e){
                        System.err.println("Host not found! exiting...");
                        System.exit(1);
                    }

                    break;
                case 3:
                    //Now we also have a port number, So set the port...
                    setUpLogger(args[1]);
                    try {
                        ftpClient = new FTPClient(args[0], Integer.parseInt(args[3]));
                    }catch (UnknownHostException e){
                        System.err.println("Host not found! exiting...");
                        System.exit(1);
                    }
                    break;
                //When the arguments are not given correctly, simply print the usage
                default:
                    printUsage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


			System.out.println("Please enter username");
			username = in.nextLine();
			System.out.println("Please enter password");
			password = in.nextLine();

        try {
//            ftpClient.login("msd88", "Ophirril594");
            ftpClient.login(username,password);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FTPException e) {
            System.err.println(e.getMessage());
        }

        while (in.hasNextLine()) {
            String command = in.nextLine();
            String[] splitCommand = command.split(" ");
            try {
                switch (splitCommand[0]) {
                    case "ls":
                        try {
                            ftpClient.ls();
                        } catch (FTPException e) {
                            System.err.println(e.getMessage());
                            continue;
                        }
                        break;
                    case "pwd":
                        try {
                            ftpClient.pwd();
                        } catch (FTPException e) {
                            System.err.println(e.getMessage());
                            continue;
                        }
                        break;
                    case "help":
                        try {
                            ftpClient.help();
                        } catch (FTPException e) {
                            System.err.println(e.getMessage());
                            continue;
                        }
                        break;
                    case "passive":
                        ftpClient.passive();
                        break;
                    case "get":
                        try {
                            ftpClient.getFile(splitCommand[1]);
                        } catch (FTPException e) {
                            System.err.println(e.getMessage());
                            continue;
                        }
                        break;
                    case "cd":
                        try {
                            ftpClient.cd(splitCommand[1]);
                        } catch (FTPException e) {
                            System.err.println(e.getMessage());
                            continue;
                        }
                        break;
                    case "cdup":
                        try {
                            ftpClient.cdup();
                        } catch (FTPException e) {
                            System.err.println(e.getMessage());
                            continue;
                        }
                        break;
                    case "quit":
                        ftpClient.quit();
                        System.exit(1);
                    default:
                        System.out.println("invalid command!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /*
     * Prints the usage of the FTP interface when no input is given,
     * or when invalid input is given.
     */
    private static void printUsage() {
        log.warning("Invalid input entered!");
        System.out.println(
                "ls- Prints the listing of the current directory\n" +
                "pwd- Prints the path of the current directory\n" +
                "help- Gets and prints out the servers help command\n" +
                "passive- Swaps the mode from passive to active and vice versa\n" +
                "get- Downlaods a given file from the server\n" +
                "cd- calls the given directory\n" +
                "cdup- moves up one directory on the file server\n" +
                "quit- quits the ftp client and sends the appropriate command to the server");
        System.exit(1);
    }


    public static void setUpLogger(String logFilePath) {
        try {
            FileHandler fh = new FileHandler(logFilePath, true);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            log.addHandler(fh);
        } catch (SecurityException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}