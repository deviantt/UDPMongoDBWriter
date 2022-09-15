package org.example;



import java.time.Instant;
import java.util.*;


public class DataPacket {
    private final boolean isRandomData = true;
    private final int usedChannels = isRandomData ? (int) rnd(16.0, 32.0) : 16;
    private final int packetsQuan = 1;
    private final byte[] packetData = new byte[18+usedChannels*8];
    private final byte[] dataArray = new byte[7+packetsQuan*(18+usedChannels*8)+1];
    private CRC8 crc = new CRC8();
    private final byte flags = (byte) (0b00000000 & 0xFF);


    public byte[] getDataArray() {
        return dataArray;
    }

    public DataPacket(long imei) throws InterruptedException {
        crc.reset();
        int command = 5;
        packHeader(imei, command, packetsQuan);
        Runnable task = () -> {
            for (int i = 0; i < packetsQuan; i++) {
                try {
                    buildData();
                    System.arraycopy(packetData, 0, dataArray, 7 + (i * (usedChannels * 8 + 18)), usedChannels * 8 + 18);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        thread.join();
        if ((flags & 0x80) == 0) {
            if ((flags & 0x10) == 0) {
                crc.update(Arrays.copyOfRange(dataArray, 0, 7 + packetsQuan*(18+usedChannels*8)));
                dataArray[7 + packetsQuan*(18+usedChannels*8)] = (byte) (crc.getValue() & 0xff);
                //0 0
            } else {
                crc.update(Arrays.copyOfRange(dataArray, 0, 7 + packetsQuan*(7+usedChannels*8)));
                dataArray[7 + packetsQuan*(7+usedChannels*8)] = (byte) (crc.getValue() & 0xff);
                // 0 1
            }
        } else if ((flags & 0x10) == 0) {
            crc.update(Arrays.copyOfRange(dataArray, 0, 7 + packetsQuan*18));
            dataArray[7 + packetsQuan*18] = (byte) (crc.getValue() & 0xff);
            // 1 0
        } else {
            crc.update(Arrays.copyOfRange(dataArray, 0, 7 + packetsQuan*7));
            dataArray[7 + packetsQuan*7] = (byte) (crc.getValue() & 0xff);
            // 1 1
        }
        long imei1 = 0;
        byte[] asd = Arrays.copyOfRange(dataArray, 0, 7);
        for(byte each : shiftRight(asd, 6)) {
            imei1 = (imei1 << 8) + (each & 0xFF);
        }
        System.out.println("Send IMEI: " + imei1 + " Packets quantity: " + (dataArray[6] & 0x07));
        System.out.println("Send size: " + (7+packetsQuan*(18+usedChannels*8)));
    }


    private void buildData() {
        packTimestamp();
        packDeviceFlags();
        packVoltage();
        packUsedChannels();
        packSpeed();
        packLat();
        packLon();
        packAlt();
        packChannelsData();
    }


    private void packTimestamp () {
        long timeSec = Instant.now().getEpochSecond();
        for (int c = 0; c < 4; c++) {
            packetData[c] = (byte)(timeSec >> (c * 8));
        }
    }

    private void packDeviceFlags () {
        packetData[4] = flags;
    }

    private void packVoltage () {
        packetData[5] = isRandomData ? (byte) (rnd(23.0, 26.0) * 10 - 100) : (byte) (140);
    }

    private void packUsedChannels () {
        if ((flags & 0x80) == 0) packetData[6] |= (byte) (usedChannels << 3);
    }

    private void packChannelsData() {
        int chDataStart = 0;
        if ((flags & 0x80) == 0) {
            if ((flags & 0x10) == 0) chDataStart = 18;
            else chDataStart = 7;
            for (int i = 0; i < usedChannels; i++) {
                int channelShift = i * 8;
                for (int nByte = 0; nByte < 8; nByte++) {
                    packetData[chDataStart + channelShift + nByte] = isRandomData ? (byte) (rnd(-127.0, 127.0)) : (byte) 1;
                }
            }
        }
    }
    private void packLat () {
        if ((flags & 0x10) == 0) {
            int rndLat = isRandomData ? (int) (rnd(54.0, 56.0) * 100000.0) : (int) (55.0 * 100000.0);
            for (int i = 0; i < 4; i++) {
                packetData[8 + i] = (byte) (rndLat >> (i * 8));
            }
        }
    }
    private void packLon () {
        if ((flags & 0x10) == 0) {
            int rndLon = isRandomData ? (int) (rnd(60.0, 62.0) * 100000.0) : (int) (61.0 * 100000.0);
            for (int i = 0; i < 4; i++) {
                packetData[8 + 4 + i] = (byte) (rndLon >> (i * 8));
            }
        }
    }

    private void packAlt () {
        if ((flags & 0x10) == 0) {
            long rndAlt = isRandomData ? (long) (rnd(0, 2000.0) * 10.0) : 10000L;
            packetData[17] = (byte) rndAlt;
            packetData[16] = (byte) (rndAlt >>> 8);
        }
    }

    private void packSpeed () {
        if ((flags & 0x10) == 0) {
            long rndSpeed = isRandomData ? (long) (rnd(0, 100.0) * 10.0) : 500L;
            packetData[7] = (byte) rndSpeed;
            packetData[6] |= (byte) (rndSpeed >>> 8);
        }
    }

    private void packHeader (long imei, int command, int packetsQuan) {
        byte[] imeiArr = shiftLeft(longToByte(imei), 6);
        System.arraycopy(imeiArr, 1, dataArray, 0, 7);
        dataArray[6] |= packetsQuan;
        dataArray[6] |= command << 3;
    }


    private static byte[] shiftRight(byte[] byteArray, int shiftBitCount) {
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

    private static long bytesToLong(final byte[] b) {
        long result = 0;
        for (int i = 0; i < 2; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
    private static double rnd (double min, double max) {
        return Math.random() * (max - min) + min;
    }

    private static byte[] longToByte (long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }


    private static byte[] shiftLeft(byte[] byteArray, int shiftBitCount) {
        final int shiftMod = shiftBitCount % 8;
        final byte carryMask = (byte) ((1 << shiftMod) - 1);
        final int offsetBytes = (shiftBitCount / 8);

        int sourceIndex;
        for (int i = 0; i < byteArray.length; i++) {
            sourceIndex = i + offsetBytes;
            if (sourceIndex >= byteArray.length) {
                byteArray[i] = 0;
            } else {
                byte src = byteArray[sourceIndex];
                byte dst = (byte) (src << shiftMod);
                if (sourceIndex + 1 < byteArray.length) {
                    dst |= byteArray[sourceIndex + 1] >>> (8 - shiftMod) & carryMask;
                }
                byteArray[i] = dst;
            }
        }
        return byteArray;
    }
}
