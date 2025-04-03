import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Assig2_Client_Part2 {
	static String[] arrayLetters = {"0","1","9","a","b","c","^","r","s","t","~","&","%"}; // Password Letters
    static int DELAY_MS = 500;
    static String filename = "TokenFile.txt";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Random rand = new Random();
        DatagramSocket aSocket = null;
        try {
        	// args[0] clientID
        	// args[1] localhost
        	// args[2] 25000
        	
        	// Initialization
        	
        	InetAddress IPaddress = InetAddress.getByName(args[1]);
        	int port = Integer.parseInt(args[2]);
        	long startTime = 0;
        	aSocket = new DatagramSocket(20000+Integer.parseInt(args[0])); // Create client port
        	int number0fPassowrds = 10;
        	int lengthOfPasswords = 6;
        	ArrayList<String> randomPasswords = functions.getRandPasswords(number0fPassowrds, lengthOfPasswords);
        	for(int i = 0; i < number0fPassowrds; i++) {
                
                // REQUEST TOKEN to enter CS
                String requestTokenM = args[0] + Integer.toString(i+1);
            	DatagramPacket requestToken = new DatagramPacket(requestTokenM.getBytes(), requestTokenM.length(), IPaddress, port);
    			aSocket.send(requestToken);
    			
    			// WAIT TO RECEIVE TOKEN
    			byte[] buffer = new byte[1000];
                DatagramPacket receiveToken = new DatagramPacket(buffer, buffer.length);
            	aSocket.receive(receiveToken);
            	String received = new String(receiveToken.getData(), 0, receiveToken.getLength());
            	System.out.println("Received Token from server: " + received);
            	
            	// Create sendMemberAlive thread
            	sendMemberAlive aliveThread = new sendMemberAlive(22000+Integer.parseInt(requestTokenM), 2000, requestTokenM);
            	
            	// Enter CS & CRACK PASSWORD
            	startTime = System.currentTimeMillis();
            	System.out.println("New Generated Password is: " + randomPasswords.get(i) + " by Client: " + args[0] + " Request: "+ (i+1));
        		String generatedPassword = functions.applySha256(randomPasswords.get(i));
        		
        		// Generate the possible passwords
				ArrayList<String> results = functions.generatePasswords(arrayLetters, "", 6);
				String password = functions.compareHashes(results, generatedPassword);
				long elapsedTime = System.currentTimeMillis() - startTime;
				
				// true: append data, false: overwrite data
				FileWriter fileWriter = new FileWriter(filename,true);
		        PrintWriter printWriter = new PrintWriter(fileWriter);
		        printWriter.println("Client " + args[0] + " with request number: " +(i+1) + " just modified with a time of : " + elapsedTime/1000 + "secs, Result password is " + password);
		        printWriter.close();
		        
		        // RETURN TOKEN
		        String returnTokenM = "DONE";
            	DatagramPacket returnToken = new DatagramPacket(returnTokenM.getBytes(), returnTokenM.length(), IPaddress, port);
    			aSocket.send(returnToken);
    			
    			// Delete the sendMemberAlive Thread
    			aliveThread.end();
    			int delay = rand.nextInt(6000) + 1000; // Generate a random delay between 1 and 7 Seconds
        		System.out.println("Sleeping for " + delay/1000 + " seconds .. ");
        		// Adding the random delay
                Thread.sleep(delay);
        	}
        } catch (SocketException e){System.out.println("Error Socket: " + e.getMessage());
        } catch (IOException e){System.out.println("Error IO: " + e.getMessage());
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally { 
            if(aSocket != null) aSocket.close();
        }

	}
	// The sendMemberAlive Thread
	public static class sendMemberAlive extends Thread{
	    private int serverPort; // Port of the server
	    private int delay; // The delay between sending I'm alive messages
	    private String ID; // The target ID (Client Number + Request Number)
	    private boolean running = true; 
	    DatagramSocket aliveSocket;
	    InetAddress address;
	    public sendMemberAlive(int serverPort, int interval, String ID) throws SocketException, UnknownHostException {
	        this.serverPort = serverPort;
	        this.delay = interval;
	        this.ID = ID;
	        this.aliveSocket = new DatagramSocket();
	        address = InetAddress.getByName("localhost");
	        this.start();
	    }

	    public void end(){
	        running = false;
	    }

	    @Override
	    public void run() {
	    	System.out.println("Started thread for the client request");
	    	
	        String aliveData = "Client: " + ID.substring(0, 1) +" Request: " + ID.substring(1)+ " I'm alive ";
	        byte[] message = aliveData.getBytes();
	        DatagramPacket alivePacket = new DatagramPacket(message, message.length, address, serverPort);
	    	while (running) {
	            try {
	                aliveSocket.send(alivePacket);
	                Thread.sleep(delay);
	            } catch (IOException e) {
	                e.printStackTrace();
	            } catch (InterruptedException e) {
	            }
	        }
	    	aliveSocket.close();
	    }
	}

}
