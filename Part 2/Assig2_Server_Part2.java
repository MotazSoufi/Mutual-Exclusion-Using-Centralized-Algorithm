import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

public class Assig2_Server_Part2 {
	static String tokenWith;
    static boolean hasToken = true;
    static Queue<String> queue = new LinkedList<>();
    static receiveMemberAlive clientAlive;
    static DatagramSocket aSocket;
    
    public static void main(String[] args) {
    	try {
    		// args[0] localhost
    		// args[1] 30000
			InetAddress backupIP = InetAddress.getByName(args[0]);
			int backupPort = Integer.parseInt(args[1]);
			//
			aSocket = new DatagramSocket(25000);
			byte[] buffer = new byte[1000];
			System.out.println("Server is ready and accepting clients' requests ... ");
			// Creates the sendServerAlive thread
			new sendServerAlive(backupIP,backupPort,500);
			while (true) {
				
				DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(receivePacket);
				String receivedMSG = new String(receivePacket.getData(), 0, receivePacket.getLength());
				String clientID = receivedMSG.substring(0, 1);
        		String clientReq = receivedMSG.substring(1);
                System.out.println("Server received: " + receivedMSG);
                if (receivedMSG.equals("DONE")) {
                	// Receive The Token from Pi
                	System.out.println("Client: " + tokenWith.substring(0, 1) + " Request: " + tokenWith.substring(1) + " Returned the Token");
                	clientAlive.requestStop(); 
                } else {
                	// Receive the RequestCS from Pi
                	if (hasToken) {
                		System.out.println("Client: " + clientID + ", with request: " + clientReq + ", requested the Token");
                		sendToken(clientID, clientReq, receivePacket.getAddress(), receivePacket.getPort());    
                	} else {
                		System.out.println("Currently Token is with Client: " + tokenWith.substring(0, 1) + ", request: " + tokenWith.substring(1));
                		System.out.println("Client: " + clientID + ", with request: " + clientReq + " will be added to the waiting Queue");
                		String added = receivedMSG + receivePacket.getAddress() + "/" + receivePacket.getPort();
                		// Adds Pi to the waiting Queue
                		queue.add(added);
                		update(backupIP, backupPort);
                	}
                }
                System.out.println("----------------------------------------------------------------");
                
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public static void sendToken(String clientId,String clientReq, InetAddress address,int port) throws IOException {
    	// Grants the Token to Pi
    	InetAddress backupIP = InetAddress.getByName("localhost");
    	update(backupIP, 30000);
        byte[] sendData = "Granted Token".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        aSocket.send(sendPacket);
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
        // Updates "hasToken" & "tokenWith"
        hasToken = false;
        tokenWith = clientId+clientReq;
    }
    public static void nextClient() {
    	// Client has finished or crashed, removes the head of the queue
    	InetAddress backupIP = null;
		try {
			backupIP = InetAddress.getByName("localhost");
			update(backupIP, 30000);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("Moving to the next request .. ");
    	String waiting = queue.poll();
    	StringTokenizer str = new StringTokenizer(waiting, "/");
    	String targetID = str.nextToken();
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
    // Creating receiveMemberAlive Thread (delay, Pi), it waits to receive alive messages from Pi each delay period.
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
            while (running) {
                try {
                    aliveSocket.setSoTimeout(delay);
                    aliveSocket.receive(receivePacket);
                    String aliveMSG = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("receiveMemberAlive Received: " + aliveMSG);
                } catch (SocketTimeoutException e) {
                	if (running) {
                		System.out.println("Client has crashed.");
                		
                        running=false;
                        // If the Queue is not empty go to the next Client
                        if (!queue.isEmpty()) {
                    		nextClient();
                    	}
                	}
                } catch (EOFException e) {
                    System.out.println("Error EOF:" + e.getMessage());
                } catch (IOException e) {
                    System.out.println("Error readline:" + e.getMessage());
                }
            }
            aliveSocket.close();
        }

        public void requestStop() {
        	running=false;
        	hasToken = true;
        	// If the Queue is not empty go to the next Client
        	if (!queue.isEmpty()) {
        		nextClient();
        	}
        }
    }
    
    public static void update(InetAddress address, int port) {
    	StringBuilder result = new StringBuilder();
      
	      Iterator<String> iterator = queue.iterator();
	      while (iterator.hasNext()) {
	          String element = iterator.next();
	          result.append(element);
	          if (iterator.hasNext()) {
	          	result.append(':');
	          }
	      }
	     
	      String msg = hasToken + "*" + tokenWith + "*" + result;
          byte[] message = msg.getBytes();
          DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
          try {
			aSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    // Creates the sendServerAlive thread to the Backup
    public static class sendServerAlive extends Thread{
        private InetAddress backupAddress;
        private int backupPort;
        private int delay;

        public sendServerAlive(InetAddress backupAddress, int backupPort, int delay){
            this.backupAddress = backupAddress;
            this.backupPort = backupPort;
            this.delay = delay;
            this.start();
        }

        public void run() {
            try {
            	// Keeps updating the backup with current changes
                DatagramSocket socket = new DatagramSocket();
                while (!Thread.interrupted()) {
//                	StringBuilder result = new StringBuilder();
//                    
//                    Iterator<String> iterator = queue.iterator();
//                    while (iterator.hasNext()) {
//                        String element = iterator.next();
//                        result.append(element);
//                        if (iterator.hasNext()) {
//                        	result.append(':');
//                        }
//                    }
//
//                    String msg = hasToken + "*" + tokenWith + "*" + result;
//                    byte[] message = msg.getBytes();
//                    DatagramPacket packet = new DatagramPacket(message, message.length, backupAddress, backupPort);
//                    socket.send(packet);
//                    // Adds a 0.5 seconds delay
//                    Thread.sleep(delay);
                	String msg = "Alive";
                	byte[] message = msg.getBytes();
                	DatagramPacket packet = new DatagramPacket(message, message.length, backupAddress, backupPort);
                	socket.send(packet);
                	Thread.sleep(delay);
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
