package org.example;

import java.util.zip.Checksum;

public class CRC8 implements Checksum {
    private int crc = 0;

    @Override
    public void update(final byte[] input, final int offset, final int len) {
        for (int i = 0; i < len; i++) {
            update(input[offset + i]);
        }
    }

    public void update(final byte[] input) {
        update(input, 0, input.length);
    }

    private final void update(byte b) {
        int i = 8;
        do {
            if(((b ^ crc) & 0x01) != 0) {
                crc = (byte) (((crc ^ 0x18) >> 1) | 0x80) & 0xFF;
            }
            else {
                crc >>= 1;
            }
            b >>= 1;
        }
        while( --i > 0);
    }



    @Override
    public void update(final int b) {
        update((byte) b);
    }

    @Override
    public long getValue() {
        return (crc & 0xFF);
    }

    @Override
    public void reset() {
        crc = 0;
    }
}
