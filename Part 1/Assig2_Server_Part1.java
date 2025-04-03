import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Assig2_Server_Part1 {
	static boolean token = true;
	static String targetID = "";
	static int portNum;
	static Queue<String> IDs = new LinkedList<> ();
	static Queue<String> fullIDs = new LinkedList<> ();
	public static void main(String[] args) {
		DatagramSocket aSocket = null;
		
		try {
			aSocket = new DatagramSocket(25000);
			byte[] buffer = new byte[1000];
			System.out.println("Server is ready and accepting clients' requests ... ");
			while(true) {
				DatagramPacket requestToken = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(requestToken);
				String msg = new String(requestToken.getData(), 0, requestToken.getLength());
				if (msg.equals("done")) {
					System.out.println("Client " + targetID + " released the token");
					token = true;
					if (!IDs.isEmpty()) {
						targetID = IDs.remove();
						portNum = 20000 + Integer.parseInt(targetID.substring(0, 1));
						DatagramPacket grantToken = new DatagramPacket(targetID.getBytes(), targetID.length(), requestToken.getAddress(), portNum);
						aSocket.send(grantToken);
						token = false;
					}
				} else {
					fullIDs.add(msg);
					System.out.println("Q is :" + fullIDs);
					System.out.println("Received a token request from client " + msg);
					if (token) {
						targetID = msg;
						// System.out.println("Message" + msg);
						
						portNum = 20000 + Integer.parseInt(msg.substring(0, 1));
						System.out.println("Sending the token to client " + msg);
						DatagramPacket grantToken = new DatagramPacket(msg.getBytes(), msg.length(), requestToken.getAddress(), portNum);
						aSocket.send(grantToken);
						token = false;
					} else {
						System.out.println("Client " + msg + " added to the Q");
						IDs.add(msg);
					}
				}
			}
		}catch (SocketException e){System.out.println("Error Socket: " + e.getMessage());
	 	}catch (IOException e) {System.out.println("Error IO: " + e.getMessage());
		}finally {
			if(aSocket != null) aSocket.close();
		}
	}
}