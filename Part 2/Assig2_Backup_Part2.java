import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;


public class Assig2_Backup_Part2 {
	// Initialization
	static receiveMemberAlive clientAlive;
	static String tokenWith;
    static boolean hasToken = true;
    static DatagramSocket serverSocket;
    // static receiveServerAlive update;
    static Queue<String> queue = new LinkedList<>();
    public static void main(String[] args) {

        System.out.println("Backup is ready ...");
        // Creates the receive server Alive thread on Port 30000
        new receiveServerAlive(30000);
    }
    public static void serverCrashed() {
    	
    	// Receive serverAlive timed out ..
    	try {
    		System.out.println("Backup server became the main server ...");
    		System.out.println("Queue is " + queue);
    		System.out.println("Token with " + tokenWith);
    		byte[] receiveData = new byte[1024];
    		serverSocket = new DatagramSocket(25000);
    		if (!hasToken) {
    			clientAlive = new receiveMemberAlive(9000, Integer.parseInt(tokenWith));
    		}
    		while (true) {
    			DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivedPacket);
                String receivedMSG = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                System.out.println("Server received: " + receivedMSG);
                if (receivedMSG.equals("DONE")) {
                	System.out.println("Client: " + tokenWith.substring(0, 1) + " Request: " + tokenWith.substring(1) + " Returned the Token");
                	if (clientAlive != null) {
                		clientAlive.requestStop();
                	}
                } else {
                	String clientID = receivedMSG.substring(0, 1);
            		String clientReq = receivedMSG.substring(1);
                	if (hasToken) {
                		System.out.println("Client: " + clientID + ", with request: " + clientReq + ", requested the Token");
                		sendToken(clientID, clientReq, receivedPacket.getAddress(), receivedPacket.getPort());
                	} else {
                		System.out.println("Currently Token is with Client: " + tokenWith.substring(0, 1) + ", request: " + tokenWith.substring(1));
                		System.out.println("Client: " + clientID + ", with request: " + clientReq + " will be added to the waiting Queue");
                		String added = receivedMSG + receivedPacket.getAddress() + "/" + receivedPacket.getPort();
                		queue.add(added);
                	}
                }
                System.out.println("----------------------------------------------------------------");
    		}
    	}catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public static void sendToken(String clientId,String clientReq, InetAddress address,int port) throws IOException {
        byte[] sendData = "Granted Token".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        serverSocket.send(sendPacket);
        System.out.println("Sending the token to Client: " + clientId + ", with request: " + clientReq);
        try {
			clientAlive = new receiveMemberAlive(9000, Integer.parseInt(clientId+clientReq));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        hasToken = false;
        tokenWith = clientId+clientReq;
    }
public static void nextClient() {
	    System.out.println("Moving to the next request .. ");
    	String waiting = queue.poll();
    	StringTokenizer str = new StringTokenizer(waiting, "/");
    	String targetID = str.nextToken();
    	// System.out.println("Client info " + clientInfo);
    	try {
			InetAddress clientAddress = InetAddress.getByName(str.nextToken());
			int clientPort = Integer.parseInt(str.nextToken());
			sendToken(targetID.substring(0,1),targetID.substring(1),clientAddress,clientPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
public static class receiveServerAlive extends Thread{
    private static final int BUFFER_SIZE = 1024;
    private DatagramSocket socket;
    private int port;
    boolean running = true;
    


    public receiveServerAlive(int port){
        this.port = port;
        this.run();
    }
    public void requestStop() {
    	running = false;
    }
    public void run() {
        try {
            socket = new DatagramSocket(port);
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket changes = new DatagramPacket(buffer, BUFFER_SIZE);

            while (running) {
            	// Receive (<Queued, P>) and (<tokenWith, P>)
                socket.receive(changes);
                socket.setSoTimeout(650);
            
                // The server is Alive and the backup is updating its variables
                String receiveMsg = new String(changes.getData(), 0, changes.getLength());
                if(!receiveMsg.equals("Alive")) {
                	System.out.println("Received message " + receiveMsg);
                	StringTokenizer argument1 = new StringTokenizer(receiveMsg,"*");
                    hasToken = Boolean.parseBoolean(argument1.nextToken());
                    tokenWith = argument1.nextToken();
                    if(argument1.hasMoreTokens()) {
                    	String waitingQueue = argument1.nextToken();
                        // Updating the queue and clearing it
                        queue.clear();
                        StringTokenizer argument2 = new StringTokenizer(waitingQueue, ":");
                        while (argument2.hasMoreTokens()) {
                            String element = argument2.nextToken();
                            // Adding waiting requests 
                            queue.add(element);
                        }
                    }
                }
                //System.out.println("Server is Alive .. ");
                // Splitting the "tokenWith", "hasToken" and the "Queue"
                
            }
        } catch (Exception e) {
                System.out.println("Primary server crashed!");
                running = false;
                serverCrashed();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
//Creating receiveMemberAlive Thread (delay, Pi), it waits to receive alive messages from Pi each delay period.
    public static class receiveMemberAlive extends Thread {
        int alivePort = 22000;
        DatagramSocket aliveSocket;
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket;
        int delay;
        int Pi;
        boolean running = true;

        public receiveMemberAlive(int delay, int Pi) throws InterruptedException {
            try {
                this.delay = delay;
                this.Pi = Pi;
                aliveSocket = new DatagramSocket(alivePort+Pi);
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                this.start();
            } catch (IOException e) {
                System.out.println("Error Connection: " + e.getMessage());
            }
        }
        public void run() {
            try {
                aliveSocket.setSoTimeout(delay);
                while (running) {
                    aliveSocket.receive(receivePacket);
                    String aliveMSG = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("receiveMemberAlive Received: " + aliveMSG);
                }
            } catch (SocketTimeoutException e) {
                if (running) {
                    System.out.println("Client has crashed.");
                    nextClient();
                    running=false;
                }
            } catch (EOFException e) {
                System.out.println("Error EOF:" + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error readline:" + e.getMessage());
            }
        }


        public void requestStop() {
            running=false;
            hasToken = true;
            if (!queue.isEmpty()) {
                nextClient();
            }
        }

    }
}
