package test.mongo.Models;

import lombok.Data;
import test.mongo.CRC8;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import static test.mongo.Const.Const.*;

@Data
public class Frame {

    private int frameSize;
    private long imei;
    private int packetsQuan;
    private ArrayList<Packet> packets = new ArrayList<>();
    private DatagramPacket receivedPacket;
    private DatagramSocket socket;

    public Frame(DatagramPacket receivedPacket, DatagramSocket socket) {
        this.receivedPacket = receivedPacket;
        this.socket = socket;
        byte[] data = receivedPacket.getData();
        frameSize = (int) bytesToLong(shiftRight(Arrays.copyOfRange(data,0,2), 5));
        byte[] header = Arrays.copyOfRange(data, 2, 9);
        packetsQuan = data[8] & 0x3F;
        for(byte each : shiftRight(header, 6)) {
            imei = (imei << 8) + (each & 0xFF);
        }
        int cursor = HEADER_SIZE;
        int realPacketSize;
        for (int i = 0; i < packetsQuan; i++) {
            realPacketSize = ((data[cursor+7] & 0x7C) >>> 2)*CHANNEL_SIZE + 19;
            Packet r1 = new Packet(Arrays.copyOfRange(data, cursor, cursor + realPacketSize));
            packets.add(r1);
            cursor += realPacketSize;
        }
    }

    public static long bytesToLong(final byte[] b) {
        long result = 0;
        for (int i = 0; i < 2; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    static byte[] shiftRight(byte[] byteArray, int shiftBitCount) {
        final int shiftMod = shiftBitCount % 8;
        final byte carryMask = (byte) (0xFF << (8 - shiftMod));
        final int offsetBytes = (shiftBitCount / 8);

        int sourceIndex;
        for (int i = byteArray.length - 1; i >= 0; i--) {
            sourceIndex = i - offsetBytes;
            if (sourceIndex < 0) {
                byteArray[i] = 0;
            } else {
                byte src = byteArray[sourceIndex];
                byte dst = (byte) ((0xff & src) >>> shiftMod);
                if (sourceIndex - 1 >= 0) {
                    dst |= byteArray[sourceIndex - 1] << (8 - shiftMod) & carryMask;
                }
                byteArray[i] = dst;
            }
        }
        return byteArray;
    }

}