package test.mongo.Models;

import lombok.Data;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
        int allPacketsSize = 0;
        this.receivedPacket = receivedPacket;
        this.socket = socket;
        byte[] data = receivedPacket.getData();
        byte[] header = Arrays.copyOfRange(data, 0, HEADER_SIZE);
        packetsQuan = data[6] & 0x07;
        for(byte each : shiftRight(header, 6)) {
            imei = (imei << 8) + (each & 0xFF);
        }
        int cursor = HEADER_SIZE;
        int realPacketSize;
        for (int i = 0; i < packetsQuan; i++) {
            int stateFlag = data[cursor + 4];
            if ((stateFlag & CHANNELS_FLAG) == 0) {
                if ((stateFlag & GPS_FLAG) == 0) {
                    realPacketSize = 18 + ((((data[cursor + 6] >>> 3) & 0x1F) + 1) * CHANNEL_SIZE);
                    //0 0
                } else {
                    realPacketSize = 7 + ((((data[cursor + 6] >>> 3) & 0x1F) + 1) * CHANNEL_SIZE);
                    // 0 1
                }
            } else if ((stateFlag & GPS_FLAG) == 0) {
                realPacketSize = 18;
                // 1 0
            } else {
                realPacketSize = 6;
                // 1 1
            }
            realPacketSize = (stateFlag & 0x04) > 0 ? realPacketSize + 4 : realPacketSize;
            Packet r1 = new Packet(Arrays.copyOfRange(data, cursor, cursor + realPacketSize));
            allPacketsSize += r1.getPacketSize();
            packets.add(r1);
            cursor += realPacketSize;
        }
        frameSize = HEADER_SIZE + allPacketsSize;
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