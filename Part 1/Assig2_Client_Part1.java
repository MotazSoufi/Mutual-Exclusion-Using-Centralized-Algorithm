import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.io.*;

public class Assig2_Client_Part1{   
	
	static String[] arrayLetters = {"0","1","9","a","b","c","^","r","s","t","~","&","%"};
	
	static String filename = "D:\\Eclipse\\Assig2_Part1\\src\\Assig2_Part1.txt";

    public static void main(String args[]) {
    	Random rand = new Random();
        DatagramSocket aSocket = null;
        // args[0] = clientID
        // args[1] = localhost
        // args[2] = 25000
        try {
        	long startTime = 0;
        	long waitingTime = 0;
        	
        	aSocket = new DatagramSocket(20000+Integer.parseInt(args[0]));
        	
        	InetAddress IPaddress = InetAddress.getByName(args[1]);
        	int port = Integer.parseInt(args[2]);
        	
        	int number0fPassowrds = 40;
        	int lengthOfPasswords = 6;
            // generate 100 6-character passwords
        	ArrayList<String> randomPasswords = functions.getRandPasswords(number0fPassowrds, lengthOfPasswords);

        	for(int i = 0; i < number0fPassowrds; i++) {
        		int delay = rand.nextInt(6000) + 1000;
        		System.out.println("Sleeping for " + delay/1000 + " seconds");
                Thread.sleep(delay);
    	    	// REQUEST TOKEN
            	String requestTokenM = args[0] +Integer.toString(i+1);
            	DatagramPacket requestToken = new DatagramPacket(requestTokenM.getBytes(), requestTokenM.length(), IPaddress, port);
    			aSocket.send(requestToken);
    			waitingTime = System.currentTimeMillis();
    			
    			// WAIT TO RECEIVE TOKEN
    			byte[] buffer = new byte[1000];
                DatagramPacket receiveToken = new DatagramPacket(buffer, buffer.length);
            	aSocket.receive(receiveToken);
            	long elapsedWaitingTime = System.currentTimeMillis() - waitingTime;
            	startTime = System.currentTimeMillis();
            	
            	// CRACK PASSWORD
            	System.out.println("New Generated Password is: " + randomPasswords.get(i) + " by client " + (i+1));
        		String generatedPassword = functions.applySha256(randomPasswords.get(i));
        		
        		// Generate the possible passwords
        		ArrayList<String> results = functions.generatePasswords(arrayLetters, "", 6);
        		String password = functions.compareHashes(results, generatedPassword);
				long elapsedTime = System.currentTimeMillis() - startTime;
				// true: append data, false: overwrite data
				
				FileWriter fileWriter = new FileWriter(filename,true);
		        PrintWriter printWriter = new PrintWriter(fileWriter);
		        printWriter.println("Client " + args[0] + " with request number: " +(i+1) + " just modified with a time of : " + elapsedTime/1000 + "secs, Result password is " + password);
		        printWriter.println("Waiting Time: " + elapsedWaitingTime/1000 + " seconds");
		        
		        
		        // RETURN TOKEN    
		        String returnTokenM = "done";
            	DatagramPacket returnToken = new DatagramPacket(returnTokenM.getBytes(), returnTokenM.length(), IPaddress, port);
    			aSocket.send(returnToken);
    			
    			System.out.println("Token returned by Client " + args[0] + " for request: " + (i+1));
    			
    			long elapsedResponseTime = System.currentTimeMillis() - waitingTime;
		        printWriter.println("Response Time: " + elapsedResponseTime/1000 + " seconds");
		        printWriter.println("==================================================");
    			printWriter.close();
        	}	
        }catch (SocketException e){System.out.println("Error Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("Error IO: " + e.getMessage());
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally { 
            if(aSocket != null) aSocket.close();
        }
        
    }
}