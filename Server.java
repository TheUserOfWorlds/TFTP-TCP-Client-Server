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

/**
 * Coursework for Networks
 * @author Vaios
 */

public class Server extends Thread {
    
    DatagramSocket socket = null;
    File file = new File("file2.txt"); //Some Metallica lyrics that the server can transfer to the client
    
    public Server() throws IOException{
        socket = new DatagramSocket(4445); 
    }
    
    public void run(){
        try {
                byte[] buf = new byte[512];

                // receive request letting know the server what the client wants to do
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Server receives request to "+request);
                InetAddress origin  = packet.getAddress();
                int port = packet.getPort();
                
                if(request.equals("read")){
                    sendFile(origin,port);
                }
                else if(request.equals("write")){
                    //sending the confirmation packet to signal that its OK to start sending file
                    DatagramPacket confirm = new DatagramPacket(buf, buf.length, origin,port);
                    socket.send(confirm);
                    receiveFile(origin,port);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    //server sends file
    public void sendFile(InetAddress origin,int port) throws FileNotFoundException,IOException{
        InetAddress destination = origin;
        int destPort = port;
        
        FileInputStream fis = new FileInputStream(file);
        
        while(fis.available() > 0){
            byte[] bytes = new byte[512];
            fis.read(bytes);
            DatagramPacket packet = new DatagramPacket(bytes,bytes.length,destination,destPort);
            socket.send(packet);
            System.out.println("Server sends chunk of file");
            byte[] foo = new byte[100];
            DatagramPacket confirmation = new DatagramPacket(foo, foo.length);
            socket.receive(confirmation);
            System.out.println("Server receives confirmation from client");
    }
        
        String finish = "end";
        byte[] bufs = new byte[512];
        bufs = finish.getBytes();  
        DatagramPacket finalPacket = new DatagramPacket(bufs, bufs.length, destination, destPort);
        socket.send(finalPacket);
        System.out.println("Server sends final packet");
    }
    //the server will start receiving the file immediately after receiving a request
    //to 'write' to server and then sending the OK packet that its ok to start sending
    public void receiveFile(InetAddress origin,int port) throws IOException{
        InetAddress destination = origin;
        int destPort = port;
        FileOutputStream file = new FileOutputStream("clientFile.txt");
        boolean loop = true;
        socket.setSoTimeout(10000);
        while(loop){
            byte[] arrival = new byte[512];
            DatagramPacket receivedPacket = new DatagramPacket(arrival,arrival.length);
            System.out.println("waiting for chunk...");
            try{
            socket.receive(receivedPacket);
            } catch(SocketTimeoutException e){
                byte[] buf = new byte[512];
                DatagramPacket confirm = new DatagramPacket(buf, buf.length, origin,port);
                socket.send(confirm);
            }
            System.out.println("received chunk");
            DatagramPacket confirm = new DatagramPacket(arrival, arrival.length, destination,destPort);
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
