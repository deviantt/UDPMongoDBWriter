package org.example;


import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPTask implements Runnable {

    private InetAddress IPAddress;
    private long imei;

    public UDPTask(InetAddress IPAddress, long imei) {
        this.imei = imei;
        this.IPAddress = IPAddress;
    }

    @Override
    public void run() {
//        BufferedWriter writer = null;
//        try {
//            writer = new BufferedWriter(new FileWriter("D:\\dst\\data.txt"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        while(true) {
            try {
                DatagramSocket socket = new DatagramSocket();
                DataPacket dataPacket = new DataPacket(imei);
                for (int i = 0; i < dataPacket.getDataArray().length; i++) {
//                    System.out.print((Long.toHexString(dataPacket.getDataArray()[i] & 0xFF)) + " ");
//                    writer.write(String.format("%s ", Long.toHexString(dataPacket.getDataArray()[i] & 0xFF)));
                }
//                writer.write(String.format(" | Packets: %d, channels: %d, flag: %s\n", dataPacket.getPacketsQuan(), dataPacket.getUsedChannels(), Integer.toBinaryString(dataPacket.getFlags() & 0xFF)));
                DatagramPacket datagramPacket = new DatagramPacket(dataPacket.getDataArray(), dataPacket.getDataArray().length, IPAddress, 12489);
                socket.send(datagramPacket);
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                System.out.print("Received answer: ");
                for (int i = 0; i < 10; i++) {
                    System.out.print(receiveData[i] + " ");
                }
                System.out.println(" for imei: " + dataPacket.getImei());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
