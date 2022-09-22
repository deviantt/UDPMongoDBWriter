package org.example;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;




public class App{

    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        int threadsQuan = 100;
        try {
            InetAddress IPAddress = InetAddress.getByName("localhost");
            System.out.println("Sending to server...");
            for (int i = 0; i < threadsQuan; i++) {
                long imei = 999999999999999L - i;
                executorService.execute(new UDPTask(IPAddress, imei));
                System.out.println("Thread #" + i + " started");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
