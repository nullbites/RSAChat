import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;

public class Client {
	
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Scanner scanner;
    private String clientName;
    private RSA rsa;
    private BigInteger[] serverPublicKey;
    private String backup;
    final String EXIT = "{exit}";
    final String NAME = "{name}";
    final String KEYS = "{keys}";
    final String BCKP = "{bckp}";

    public Client() {
    	this.clientName = "Client";
    	this.scanner = new Scanner(System.in);
    	this.backup = "";
    	this.rsa = new RSA();
    	this.serverPublicKey = new BigInteger[2];
    }
    
    public void startClient() {
    	System.out.println("\n--------------------------------<Cryptochat>------------------------------------");
    	System.out.println("                                v1.0 11/2019");
    	System.out.println("                                RSA Chat - Client");
    	System.out.println("\nGlobal functions:");
    	System.out.println("{exit} - terminate connection and close the program");
    	System.out.println("{name} - change the client name");
    	System.out.println("{keys}  - change the pair of keys and share them with the server");
    	System.out.println("{bckp} - creates a .txt with a backup of the sent and receivec messages of the current instance of the chat\n\n");
    	System.out.println("Client interface. Make sure server is running, then configure socket");
        
    	System.out.println("\nEnter the desired IP address [default address is localhost]: ");
        String ip = this.scanner.nextLine();
        if (ip.length() <= 0) ip = "localhost";

        System.out.println("Enter the desired port [default port is 5050]: ");
        String port = this.scanner.nextLine();
        if (port.length() <= 0) port = "5050";
    	this.execConnection(ip, Integer.parseInt(port));
    	this.writeData();
    }
    
    private void changeClientName() {
    	String newName = "";
    	System.out.println("Current client name is: " + this.clientName);
    	do {
    		System.out.println("Write the desired name for the client. It should be at least 3 characters long: ");
    		newName = this.scanner.nextLine();
    	} while(newName.equals("\n") || newName.length() < 3);
    	this.clientName = newName;
    	System.out.println("The client name was changed successfully");
    	System.out.println();
    }
    
    private void openConnection(String ip, int port) {
        try {
            this.socket = new Socket(ip, port);
            System.out.println("\nConnected to: " + this.socket.getInetAddress().getHostName());
            System.out.print("\n[" + clientName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> ");
        } catch (Exception e) {
            System.out.println("\nAn error ocurred while trying to open a connection: " + e.getMessage());
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
            System.out.println("\nAn error ocurred trying to share the server keys message: " + ex.getMessage());
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
	        this.serverPublicKey[0] = new BigInteger(e);
	        this.serverPublicKey[1] = new BigInteger(n);
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
        this.serverPublicKey[0] = new BigInteger(e);
        this.serverPublicKey[1] = new BigInteger(n);
    }

    public void writeData() {
        String input = "";
        while (true) {
            System.out.print("\n[" + clientName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> ");
            input = this.scanner.nextLine();
            if(input.equals(EXIT)) {
            	this.closeConnection();
            } else if(input.equals(NAME)) {
            	String oldName = this.clientName;
            	this.changeClientName();
            	this.send("**" + oldName + " has changed its name to " + this.clientName + "**", 0);
            } else if(input.equals(KEYS)) {
            	RSA newKeys = new RSA();
            	this.sharePublicKey(newKeys.gete(), newKeys.getn());
            	this.rsa = newKeys;
            } else if(input.equals(BCKP)) {
            	try {
            		String fileName = this.clientName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".log";
            	    Files.write(Paths.get(fileName), this.backup.getBytes());
            	    System.out.println("Backup of chat generated");
	            	System.out.println("Backup named: " + this.clientName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")));
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
        	BigInteger e = this.serverPublicKey[0];
        	BigInteger n = this.serverPublicKey[1];
        	this.backup += "\n[" + this.clientName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> " + message + "\n";
        	encryptedMessage += this.rsa.encryptOtherMessageBI(this.clientName + ":" + message, e, n);
        	this.dos.writeUTF(intention + encryptedMessage);
            this.dos.flush();
        } catch (IOException e) {
            System.out.print("An error ocurred while trying to send a message: ");
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
        	this.dis = new DataInputStream(socket.getInputStream());
        	this.dos = new DataOutputStream(socket.getOutputStream());
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
        String serverName;
        boolean nameCaptured;
        boolean firstTime;
        System.out.println();
        System.out.println("Sharing public key");
        this.sharePublicKey();
        System.out.println("Public key shared");
        System.out.println();
        System.out.println("Getting server public key");
        this.receivePublicKey();
        System.out.println("Server public key obtained");
        System.out.println();
        try {
            do {
            	input = decryptedInput = message = serverName = "";
            	nameCaptured = firstTime = false;
                input += (String) this.dis.readUTF();
                char flag = input.charAt(0);
                input = input.substring(1);
                encryptedMessage = new BigInteger(input);
                decryptedInput = this.rsa.decryptMessageBI(encryptedMessage);
                //Indicates the intention of the received message
                //Regular message
                if(flag == '0') {
                	for(int size = 0; size < decryptedInput.length(); size++) {
                    	if(!firstTime) {
    	                	if(decryptedInput.substring(size, size + 1).equals(":")) {
    	                		nameCaptured = firstTime = true;
    	                		size++;
    	                	}
                    	}
                    	if(!nameCaptured) {
                    		serverName += decryptedInput.substring(size, size + 1);
                    	} else {
                    		message += decryptedInput.substring(size, size + 1);
                    	}
                    }
                	this.backup("\n[" + serverName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> " + message);
                //Server changed keys
                } else if(flag == '1') {
                	System.out.println("\n" + serverName + "has changed keys for encryption");
                	System.out.println("Getting server public key");
                    this.receivePublicKey(input);
                    System.out.println("Server public key obtained\n");
                }
                System.out.print("\n[" + this.clientName + "] [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + "] :> ");
            } while (!message.equals(KEYS) || !message.equals(EXIT) || !message.equals(NAME) || !message.equals(BCKP));
        } catch (IOException e) {
        	System.out.println();
        	System.out.println("An error ocurred while trying to communicate with the server: " + e.getMessage());
        	System.out.println("Closing connection...");;
            this.closeConnection();
        }
    }

    public void execConnection(String ip, int port) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    openConnection(ip, port);
                    flux();
                    getInput();
                } finally {
                    closeConnection();
                }
            }
        });
        thread.start();
    }

    public static void main(String[] argumentos) throws IOException {
        Client client = new Client();
        client.startClient();
    }
    
}
