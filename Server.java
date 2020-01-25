import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;


public class Server {

    private Socket socket;
    private ServerSocket serverSocket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Scanner scanner;
    private String serverName;
    private RSA rsa;
    private BigInteger[] clientPublicKey;
    private String backup;
    final String EXIT = "{exit}";
    final String NAME = "{name}";
    final String KEYS = "{keys}";
    final String BCKP = "{bckp}";

    public Server() {
    	this.serverName = "Server";
    	this.scanner = new Scanner(System.in);
    	this.backup = "";
    	this.rsa = new RSA();
    	this.clientPublicKey = new BigInteger[2];
    }
    
    public void startServer() {
    	System.out.println("\n--------------------------------<Cryptochat>------------------------------------");
    	System.out.println("                                v1.0 11/2019");
    	System.out.println("                                RSA Chat - Server");
    	System.out.println("\nGlobal functions:");
    	System.out.println("{exit} - terminate connection and close the program");
    	System.out.println("{name} - change the server name");
    	System.out.println("{keys}  - change the pair of keys and share them with the client");
    	System.out.println("{bckp} - creates a .txt with a backup of the sent and receivec messages of the current instance of the chat\n\n");
    	System.out.println("Server interface. Configure socket and wait for a connection");
        System.out.println("\nEnter the desired port [default port is 5050]: ");
        String port = this.scanner.nextLine();
        if (port.length() <= 0) port = "5050";
        this.execConnection(Integer.parseInt(port));
        this.writeData();
    }
    
    private void changeClientName() {
    	String newName = "";
    	System.out.println("Current server name is: " + this.serverName);
    	do {
    		System.out.println("Write the desired name for the server. It should be at least 3 characters long: ");
    		newName = this.scanner.nextLine();
    	} while(newName.equals("\n") || newName.length() < 3);
    	this.serverName = newName;
    	System.out.println("The server name was changed successfully");
    	System.out.println();
    }
    
    private void openConnection(int port) {
        try {
        	this.serverSocket = new ServerSocket(port);
            System.out.println("\nEstablishing connection with port: " + port);
            
            this.socket = this.serverSocket.accept();
            System.out.println("\nEstablished connection with: " + this.socket.getInetAddress().getHostName());
            System.out.println();
            System.out.print("\n[" + this.serverName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> ");
        } catch (Exception e) {
            System.out.println("\nAn error ocurred while establishing a connection: " + e.getMessage());
            System.out.println("\nExiting program...");
            System.exit(0);
        }
    }
    
    private void closeConnection() {
        try {
        	this.dis.close();
        	this.dos.close();
        	this.socket.close();
        } catch (IOException e) {
          System.out.println("\nAn error ocurred while trying to close a connection: " + e.getMessage());
        } finally {
            System.out.println("\nExiting program....");
            System.exit(0);

        }
    }
    
    private void sharePublicKey() {
    	try {
        	BigInteger[] publicKey = this.rsa.sharePublicKey();
        	BigInteger e = publicKey[0];
        	BigInteger n = publicKey[1];
        	this.dos.writeUTF(e + ":" + n);
        	this.dos.flush();
        } catch (Exception ex) {
            System.out.println("\nAn error ocurred trying to share the server keys message: " + ex.getMessage());
        }
    }
    
    private void sharePublicKey(BigInteger e, BigInteger n) {
    	try {
        	this.dos.writeUTF("1" + e + ":" + n);
        	this.dos.flush();
        } catch (Exception ex) {
            System.out.println("An error ocurred trying to share the server keys message: " + ex.getMessage());
        }
    }
    
    private void receivePublicKey() {
    	try {
    		String publicKey, e, n;
    		publicKey = e = n = "";
    		publicKey = (String) this.dis.readUTF();
    		boolean keyCaptured = false;
            for(int size = 0; size < publicKey.length(); size++) {
            	if(publicKey.substring(size, size + 1).equals(":")) {
            		keyCaptured = true;
            		size++;
            	}
            	if(!keyCaptured) {
            		e += publicKey.substring(size, size + 1);
            	} else {
            		n += publicKey.substring(size, size + 1);
            	}
            }
	        this.clientPublicKey[0] = new BigInteger(e);
	        this.clientPublicKey[1] = new BigInteger(n);
        } catch (IOException ex) {
        	System.out.println();
        	System.out.println("\nAn error ocurred while trying to obtain the public key of the client");
        	System.out.println("\nClosing server connection...");
        	this.closeConnection();
        }
    }
    
    private void receivePublicKey(String publicKey) {
		String e, n;
		publicKey = e = n = "";
		boolean keyCaptured = false;
        for(int size = 1; size < publicKey.length(); size++) {
        	if(publicKey.substring(size, size + 1).equals(":")) {
        		keyCaptured = true;
        		size++;
        	}
        	if(!keyCaptured) {
        		e += publicKey.substring(size, size + 1);
        	} else {
        		n += publicKey.substring(size, size + 1);
        	}
        }
        this.clientPublicKey[0] = new BigInteger(e);
        this.clientPublicKey[1] = new BigInteger(n);
    }
    
    public void writeData() {
    	String input = "";
        while (true) {
            System.out.print("\n[" + this.serverName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> ");
            input = this.scanner.nextLine();   
            if(input.equals(EXIT)) {
            	this.closeConnection();
            } else if(input.equals(NAME)) {
            	String oldName = this.serverName;
            	this.changeClientName();
            	this.send("**" + oldName + " has changed its name to " + this.serverName + "**", 0);
            } else if(input.equals(KEYS)) {
            	RSA newKeys = new RSA();
            	this.sharePublicKey(newKeys.gete(), newKeys.getn());
            	this.rsa = newKeys;
            } else if(input.equals(BCKP)) {
            	try {
            		String fileName = this.serverName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".log";
            	    Files.write(Paths.get(fileName), this.backup.getBytes());
            	    System.out.println("Backup of chat generated");
	            	System.out.println("Backup named: " + this.serverName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".log");
	            	System.out.println("The backup has been saved in the same directory where cryptochat is located");
            	} catch (IOException ex) {
            		System.out.println("The backup could not be saved: " + ex.getMessage());
            	}
        	} else if(input.length() > 0 && !input.equals("\n")) {
            	this.send(input, 0);
            }
        }
    }
    
    private void send(String message, int intention) {
        try {
        	String encryptedMessage = "";
        	BigInteger e = this.clientPublicKey[0];
        	BigInteger n = this.clientPublicKey[1];
        	this.backup += "\n[" + this.serverName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> " + message + "\n";
        	encryptedMessage += this.rsa.encryptOtherMessageBI(this.serverName + ":" + message, e, n);
        	this.dos.writeUTF(intention + encryptedMessage);
        	this.dos.flush();
        } catch (Exception e) {
            System.out.print("An error ocurred trying to send a message: ");
            if(e.getMessage().equals(null)) {
            	System.out.println("There is no client connected");
            } else {
            	System.out.println(e.getMessage());
            }
        }
    }
    
    private void backup(String message) {
    	this.backup += message + "\n";
    	System.out.println(message);
    }
    
    private void flux() {
        try {
        	this.dis = new DataInputStream(this.socket.getInputStream());
        	this.dos = new DataOutputStream(this.socket.getOutputStream());
        	this.dos.flush();
        } catch (IOException e) {
            System.out.println("An error ocurred during the flux aperture");
        }
    }

    private void getInput() {
    	BigInteger encryptedMessage;
    	String input;
    	String decryptedInput;
        String message;
        String clientName;
        boolean nameCaptured;
        boolean firstTime;
        System.out.println();
        System.out.println("Sharing public key");
        this.sharePublicKey();
        System.out.println("Public key shared");
        System.out.println();
        System.out.println("Getting client public key");
        this.receivePublicKey();
        System.out.println("Client public key obtained");
        System.out.println();
        try {
            do {
            	input = decryptedInput = message = clientName = "";
            	nameCaptured = firstTime = false;
                input += (String) this.dis.readUTF();
                char flag = input.charAt(0);
                input = input.substring(1);
                encryptedMessage = new BigInteger(input);
                decryptedInput = this.rsa.decryptMessageBI(encryptedMessage);
                if(flag == '0') {
	                for(int size = 0; size < decryptedInput.length(); size++) {
	                	if(!firstTime) {
		                	if(decryptedInput.substring(size, size + 1).equals(":")) {
		                		nameCaptured = firstTime = true;
		                		size++;
		                	}
	                	}
	                	if(!nameCaptured) {
	                		clientName += decryptedInput.substring(size, size + 1);
	                	} else {
	                		message += decryptedInput.substring(size, size + 1);
	                	}
	                }
	                this.backup("\n[" + clientName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> " + message);
                } else if(flag == '1') {
                	System.out.println("\n" + clientName + "has changed keys for encryption");
                	System.out.println("Getting client public key");
                    this.receivePublicKey(input);
                    System.out.println("Client public key obtained\n");
                }
                System.out.print("\n[" + this.serverName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> ");
            } while (!message.equals(KEYS) || !message.equals(EXIT) || !message.equals(NAME) || !message.equals(BCKP));
        } catch (IOException e) {
        	System.out.println();
        	System.out.println("An error ocurred while trying to communicate with the client: " + e.getMessage());
        	//String answer = "";
        	//do {
            //	System.out.println("Do you want to terminate the program? y/n");
        	//	answer = this.scanner.nextLine();
        	//} while(answer.equals("y") || answer.equals("Y") || answer.equals("n") || answer.equals("N"));
        	//if(answer.equals("y") || answer.equals("Y")) {
            	System.out.println("Closing server connection...");;
            	this.closeConnection();
        	//} else if(answer.equals("n") || answer.equals("N")) {
        	//	System.out.println("The server will wait until a new clients connects");
        	//}
        }
    }

    public void execConnection(int port) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        openConnection(port);
                        flux();
                        getInput();
                    } finally {
                        closeConnection();
                    }
                }
            }
        });
        thread.start();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }
    
}
