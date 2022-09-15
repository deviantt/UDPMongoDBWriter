package org.example;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;




public class App{
    private static DatagramSocket socket;


    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        int threadsQuan = 10;
        try
        {
            socket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            System.out.println("Sending to server...");
            for (int i = 0; i < threadsQuan; i++) {
                long imei = 999999999999999L - i*2;
                executorService.submit(new UDPTask(IPAddress, imei, socket));
                System.out.println("Thread #" + i + " started");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
