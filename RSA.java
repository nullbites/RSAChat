import java.math.BigInteger;
import java.util.Random;
import javax.swing.JOptionPane;
 
public class RSA
{
    private BigInteger p, q, n, phi, e, d;
    private int bitlength;
    private Random r;
 
    public RSA() {
        this.bitlength = 1024;
        this.r = new Random();
        this.p = BigInteger.probablePrime(this.bitlength, this.r);
        this.q = BigInteger.probablePrime(this.bitlength, this.r);
        this.n = this.p.multiply(this.q);
        this.phi = this.p.subtract(BigInteger.ONE).multiply(this.q.subtract(BigInteger.ONE));
        this.e = BigInteger.valueOf(65537);
        this.d = this.e.modInverse(this.phi);
    }
    
    public BigInteger gete() {
    	return this.e;
    }
    
    public BigInteger getn() {
    	return this.n;
    }
    
    public BigInteger[] sharePublicKey() {
    	BigInteger[] publicKey = new BigInteger[2];
    	publicKey[0] = this.e;
    	publicKey[1] = this.n;
    	return publicKey;
    }
    
    public void generateNewKeys() {
    	this.r = new Random();
        this.p = BigInteger.probablePrime(this.bitlength, this.r);
        this.q = BigInteger.probablePrime(this.bitlength, this.r);
        this.n = this.p.multiply(q);
        this.phi = this.p.subtract(BigInteger.ONE).multiply(this.q.subtract(BigInteger.ONE));
        this.e = BigInteger.valueOf(65537);
        this.d = this.e.modInverse(this.phi);
    }
    
    public void setBitlength() {
    	int newbitlength = this.bitlength;
		System.out.println("Current this.bitlength is " + newbitlength);
    	do {
	    	newbitlength = Integer.parseInt(JOptionPane.showInputDialog("Enter this.bitlength of "
	    			+ "the primes used for encryption. It must be at least 1024 (the default is 1024): ")); 
    	} while(newbitlength < 1024);
    	this.bitlength = newbitlength;
    	this.generateNewKeys();
    }
    
    public void setPublicExponent() {
    	BigInteger newe = this.e;
		System.out.println("Current this.bitlength is " + newe);
    	do {
	    	newe = BigInteger.valueOf((Long.parseLong(JOptionPane.showInputDialog("Enter the value of "
	    			+ "the public exponent. It must be at least 3 (the default is 65537): ")))); 
    	} while(newe.compareTo(BigInteger.valueOf(3)) == -1);
    	this.e = newe;
    	this.generateNewKeys();
    }
 
    private String bytesToString(byte[] encrypted) {
        String res = "";
        for(byte b : encrypted) {
            res += Byte.toString(b);
        }
        return res;
    }
    
    private byte[] stringToBytes(String encrypted) {
    	int count = 0;
    	for(int size = 0; size < encrypted.length(); size++) {
    		if(encrypted.substring(size, size + 1).equals("-")) count++;
    	}
    	byte[] res = new byte[count+1];
    	count = 0;
    	int lastLetterAfterDash = 0;
    	for(int size = 0; size < encrypted.length(); size++) {
    		if(encrypted.substring(size, size + 1).equals("-")) {
    			res[count] = new Byte(encrypted.substring(lastLetterAfterDash, size));
    			count++;
    			lastLetterAfterDash = size + 1;
    		}
    	}
    	return res;
    }
    
    private byte[] encrypt(byte[] message) {
    	BigInteger encrypted = new BigInteger(message);
    	byte[] encryptedArray = encrypted.modPow(this.e, this.n).toByteArray();
        return encryptedArray;
    }
    
    private byte[] decrypt(byte[] message) {
    	BigInteger decrypted = new BigInteger(message);
    	byte[] decryptedArray = decrypted.modPow(this.d, this.n).toByteArray();
        return decryptedArray;
    }
    
    public String encryptMessage(String message) {
    	byte[] bytes = message.getBytes();
    	System.out.println();
    	System.out.println("Encrypting message: " + message);
    	System.out.println("Encrypting bytes: " + this.bytesToString(bytes));
    	byte[] encrypted = this.encrypt(bytes);
    	System.out.println("Message encrypted: " + this.bytesToString(encrypted));
    	System.out.println();
    	return this.bytesToString(encrypted);
    }
    
    public String decryptMessage(String encrypted) {
    	System.out.println();
    	System.out.println("Decrypting message: " + encrypted);
    	byte[] decrypted = this.decrypt(this.stringToBytes(encrypted));
    	System.out.println("Bytes decrypted: " + this.bytesToString(decrypted));
    	System.out.println("Message decrypted: " + new String(decrypted));
    	System.out.println();
    	return new String(decrypted);
    }
    
    private BigInteger encryptBI(byte[] message) {
    	BigInteger encrypted = new BigInteger(message);
    	return encrypted.modPow(this.e, this.n);
    }
    
    private BigInteger encryptOtherBI(byte[] message, BigInteger e, BigInteger n) {
    	BigInteger encrypted = new BigInteger(message);
    	return encrypted.modPow(e, n);
    }
    
    private byte[] decryptBI(BigInteger message) {
    	BigInteger decrypted = message;
    	byte[] decryptedArray = decrypted.modPow(this.d, this.n).toByteArray();
        return decryptedArray;
    }
    
    public BigInteger encryptMessageBI(String message) {
    	byte[] bytes = message.getBytes();
    	System.out.println();
    	System.out.println("Encrypting message: " + message);
    	System.out.println("Encrypting bytes: " + this.bytesToString(bytes));
    	BigInteger encrypted = this.encryptBI(bytes);
    	System.out.println("Message encrypted: " + encrypted);
    	System.out.println();
    	return encrypted;
    }
    
    public BigInteger encryptOtherMessageBI(String message, BigInteger e, BigInteger n) {
    	byte[] bytes = message.getBytes();
    	System.out.println();
    	System.out.println("Encrypting message: " + message);
    	System.out.println("Encrypting bytes: " + this.bytesToString(bytes));
    	BigInteger encrypted = this.encryptOtherBI(bytes, e, n);
    	System.out.println("Message encrypted: " + encrypted);
    	System.out.println();
    	return encrypted;
    }
    
    public String decryptMessageBI(BigInteger encrypted) {
    	System.out.println();
    	System.out.println("Decrypting message: " + encrypted);
    	byte[] decrypted = this.decryptBI(encrypted);
    	System.out.println("Bytes decrypted: " + this.bytesToString(decrypted));
    	System.out.println("Message decrypted: " + new String(decrypted));
    	System.out.println();
    	return new String(decrypted);
    }
    
    public static void main(String[] args) {
    	/*
    	RSA rsa = new RSA();
        String encriptado = "";
        encriptado += rsa.encryptMessageBI("Server:H[{}]'?/.,<>@#$%^&*()ola");
        BigInteger desencriptado = new BigInteger(encriptado);
        rsa.decryptMessageBI(desencriptado);
        System.out.println();
        
        String teststring = "cochino";
        System.out.println("Encrypting String: " + teststring);
        System.out.println("String in Bytes: " + rsa.bytesToString(teststring.getBytes()));
        byte[] encrypted = rsa.encrypt(teststring.getBytes());
        byte[] decrypted = rsa.decrypt(encrypted);
        System.out.println("Decrypting Bytes: " + rsa.bytesToString(decrypted));
        System.out.println("Decrypted String: " + new String(decrypted));
        */
    }
}