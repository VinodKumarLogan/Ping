import java.io.*;
import java.net.*;

public class PingThread {

   public static void main(String [] args)
   {
      MyThread t1 = new MyThread(0);
      MyThread t2 = new MyThread(1);
      t1.start();
      t2.start();
   }
}

class MyThread extends Thread {
   private int c;
   private static int port1,port2;
   public static BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
   public static DatagramSocket socket;
   public static String destinationIP;

   public static void threadedSender() {
      try {
         InetAddress ipaddr = InetAddress.getByName(destinationIP);
         byte[] sendData;
         while(true) {
            System.out.print("Enter the message : ");
            String sentence = inFromUser.readLine();
            sendData = sentence.getBytes();
            DatagramPacket sendPacket =new DatagramPacket(sendData, sendData.length,ipaddr,port2);
            sendPacket.setPort(port2);
            socket.send(sendPacket);
            System.out.println("Packet Sent"+sendPacket.toString());
         }
      }
      catch(Exception e) {
         System.out.print("Sender: "+e);
      }
   }
   
   public static void threadedReceiver() {
      try {
         byte[] receiveData = new byte[1024];
	 //byte[] sendData = new byte[1024];
         while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String sentence = new String(receivePacket.getData(),0, receivePacket.getLength());
            System.out.println("Message Received : "+sentence+" "+receivePacket.getSocketAddress().toString());
            //sendData = sentence.toUpperCase().getBytes();
            //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,receivePacket.getAddress(),receivePacket.getPort());
            //sendPacket.setPort(port2);
            //socket.send(sendPacket);
            //System.out.println("Packet Sent");
         }
      }
      catch(Exception e) {
         System.out.print("Receiver: "+e);
      }
   }
   public MyThread(int c) {
      try {
         this.c = c;
         if(this.c==0) {
            System.out.print("Enter the host port number : ");
            port1 = Integer.parseInt(inFromUser.readLine());
            socket = new DatagramSocket(port1);
         }
         else {
            System.out.print("Enter the destination IP Address : ");
            destinationIP = inFromUser.readLine();
            System.out.print("Enter the destination port number : ");
            port2 = Integer.parseInt(inFromUser.readLine());
         }
      }
      catch(Exception e){
         System.out.print(e);
      }
   }

   @Override
   public void run() {
      if(this.c==1)
         threadedReceiver();
      else
         threadedSender();
   }
}
