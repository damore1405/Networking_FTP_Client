import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class FTPDataSession extends ServerSocket{
	public FTPDataSession(int port) throws IOException {
		super(port);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Socket accept() throws IOException {
		// TODO Auto-generated method stub
		return super.accept();
	}

}
