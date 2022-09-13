package org.example;

import org.w3c.dom.ls.LSOutput;

import javax.xml.crypto.Data;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.Scanner;
import java.util.*;
import java.util.concurrent.*;


public class App{
    private static DatagramSocket socket;

    public static void main(String[] args) throws IOException {
        long imei = 999999999999999L;
        try
        {
            socket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            System.out.println("Sending to server...");
            while (true) {
                DataPacket dataPacket = new DataPacket(imei);
                DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getDataArray(), dataPacket.getDataArray().length, IPAddress, 12489);
                socket.send(datagramPacket);
//                byte[] receiveData = new byte[1024];
//                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//                socket.receive(receivePacket);
//                System.out.print("Received answer: ");
//                for (int i = 0; i < 10; i++) {
//                    System.out.print(receiveData[i] + " ");
//                }
//                System.out.println("\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
