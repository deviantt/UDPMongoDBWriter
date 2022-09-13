package test.mongo.Models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import static test.mongo.Const.Const.*;
import static test.mongo.Models.Frame.*;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;

@Data
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Packet {
    @Field("TIM")
    private Instant timestamp;
    @Field("VLT")
    private double measuredVoltage;
    @Field("FLG")
    private byte stateFlags;
    @Field("UCH")
    private Integer usedChannels;
    @Field("ACH")
    private Map<Integer, Long> analogs = new HashMap<>();
    @Field("CRD")
    private Coordinates coordinates = new Coordinates();
    @Transient
    private int packetSize;

    public Packet(byte[] data) {
        packetSize = data.length;
        timestamp = Instant.ofEpochSecond(getLongFromByte(data, 0, 4));
        stateFlags = data[4];
        measuredVoltage = (double) ((data[5] & 0xFF) + 100) / 10;
        int chDataStartAt;
        if ((stateFlags & 0x10) > 0) {
            coordinates = null;
            chDataStartAt = 7;
            //GPS 1
        } else {
            int latLong = (int) getLongFromByte(data, 8, 4);
            int lonLong = (int) getLongFromByte(data, 12, 4);
            short altLong = (short) bytesToLong(Arrays.copyOfRange(data, 16, 18), 2);
            short velocity = (short) (bytesToLong(Arrays.copyOfRange(data, 6, 8), 2) & 0x07FF);
            coordinates.setLat((double) latLong / 100000.0);
            coordinates.setLon((double) lonLong / 100000.0);
            coordinates.setAlt((double) altLong / 10.0);
            coordinates.setVelocity((double) velocity / 10.0);
            chDataStartAt = 18;
            // GPS 0
        }
        if ((stateFlags & 0x80) > 0) {
            usedChannels = null;
            analogs = null;
            // CH 1
        }
        else {
            usedChannels = ((data[6] >> 3) & 0x1F);
            for (int i = 0; i < usedChannels ; i++) {
                long currChannelData = bytesToLong(Arrays.copyOfRange(data, chDataStartAt, chDataStartAt + 8), 8);
                analogs.put(i, currChannelData);
                chDataStartAt += 8;
            }
            // CH 0
        }

    }

    private static long bytesToLong(final byte[] b, int bytes) {
        long result = 0;
        for (int i = 0; i < bytes; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    private static long getLongFromByte(byte[] data, int start, int length) {
        long result = 0;
        for (int i = 0; i < length; i++) {
            result += (data[start + i] & 0xFF) << (i * 8);
        }
        return result;
    }

}
