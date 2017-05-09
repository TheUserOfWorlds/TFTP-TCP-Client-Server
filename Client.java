package mypackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/**
 * Coursework for networks
 * @author Vaios
 */

public class Client extends Thread{
    
    InetAddress address;        //The computer we want to write/read from/to
    int targetPort;             //The target computers port
    DatagramSocket socket = new DatagramSocket();
    File file = new File("file1.txt"); //Some Metallica lyrics that the client can transfer to the server
    
    public Client(String ip,int port) throws IOException{
        targetPort = port;
        address = InetAddress.getByName(ip);
    }
 /**
 *When the thread runs, the user enters number 1 or 2 in the console.
 * Depending on the number, the client sends a request packet to the server
 * telling it what it needs to do(write or retrieve file
 */
    public void run(){
         try {
                
                Scanner reader = new Scanner(System.in);
                String request;
                System.out.println("ENTER 1 TO WRITE FILE TO SERVER, OR 2 TO RETRIEVE FILE");
                int choice = reader.nextInt();
                if(choice == 1){
                    request = "write";
                    System.out.println("Client sends request to write to server");
                    byte[] buf = new byte[512];
                    buf = request.getBytes();    
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, targetPort);
                    socket.send(packet);
                    
                    //Wait for confirmation from server that it got the request before start sending the file
                    byte[] temp = new byte[100];
                    DatagramPacket confirmation = new DatagramPacket(temp, temp.length);
                    socket.receive(confirmation);
                    System.out.println("client receives confirmation from server to begin to send file");
                    writeFile();
                    
                }
                else if(choice == 2){
                    //Send retrieve request and immediately start waiting for chunks of the file
                    request = "read";
                    System.out.println("Client sends request to read from server");
                    byte[] buf = new byte[512];
                    buf = request.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, targetPort);
                    socket.send(packet);
                    retrieveFile();
                }
                
                
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    //This method works with the method receiveFile() at the server side
    public void writeFile() throws FileNotFoundException, IOException{
        FileInputStream fis = new FileInputStream(file);
        
        while(fis.available() > 0){
            byte[] bytes = new byte[512];
            fis.read(bytes);
            DatagramPacket packet = new DatagramPacket(bytes,bytes.length,address,targetPort);
            socket.send(packet);
            System.out.println("Client sends chunk of file");
            byte[] foo = new byte[100];
            DatagramPacket confirmation = new DatagramPacket(foo, foo.length);
            socket.receive(confirmation);
            System.out.println("Client receives confirmation from server");
        }
        //Once the file has been sent , we send a packet that tells the server that
        //the transfer is over.
        String finish = "end";
        byte[] bufs = new byte[512];
        bufs = finish.getBytes();  
        DatagramPacket finalPacket = new DatagramPacket(bufs, bufs.length, address, targetPort);
        socket.send(finalPacket);
        System.out.println("Client sends final packet");
    }
    //This method works in combination with the sendFile() method at the server side.
    //The logic is the same as the combination above.( writeFile-client + receiveFile-server )
    public void retrieveFile() throws FileNotFoundException, IOException{
        FileOutputStream file = new FileOutputStream("serverFile.txt");
        boolean loop = true;
        socket.setSoTimeout(10000);
        while(loop){
            byte[] arrival = new byte[512];
            DatagramPacket receivedPacket = new DatagramPacket(arrival,arrival.length);
            System.out.println("waiting for chunk...");
            try{
            socket.receive(receivedPacket);
            } catch(SocketTimeoutException e){
                 String request = "read";
                    System.out.println("Client sends request to read from server");
                    byte[] buf = new byte[512];
                    buf = request.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, targetPort);
                    socket.send(packet);
            }
            System.out.println("received chunk");
            DatagramPacket confirm = new DatagramPacket(arrival, arrival.length, address,targetPort);
            socket.send(confirm);
            String string = new String(receivedPacket.getData(),0,receivedPacket.getLength());
            if(string.equals("end")){
                break;
            }
            arrival = receivedPacket.getData();
            file.write(arrival);
        }
            
        
    }
}
