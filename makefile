build: JavaFtpClient.java FTPClient.java FTPCommandSession.java FTPException.java
	javac JavaFtpClient.java

run: build JavaFtpClient.class FTPClient.class FTPCommandSession.class FTPException.class

ifndef $(Port)
	java JavaFtpClient $(Host) $(LogFilePath)
else
	java JavaFtpClient $(Host) $(LogFilePath) $(Port)
endif

clean:
	rm *.class