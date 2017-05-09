package mypackage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientStarter {
    
    
    public static void main(String[] args) throws IOException {
        
        new Client("127.0.0.1",4445).start();
    }
    
}
