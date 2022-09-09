package test.mongo.Models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import static test.mongo.Const.Const.*;
import static test.mongo.Models.Frame.*;

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
    private int usedChannels;
    @Field("ACH")
    private Map<Integer, Long> analogs = new HashMap<>();
    @Field("CRD")
    private Coordinates coordinates = new Coordinates();
    @Field("GPS")
    private int gpsSatCount;
    @Field("GLS")
    private int glonassSatCount;
    @Transient
    private int packetSize;

    public Packet(byte[] data) {
        usedChannels = (data[7] >>> 2) & 0x1F;
        packetSize = usedChannels*CHANNEL_SIZE + 19;
        timestamp = Instant.ofEpochSecond(getLongFromByte(data, 0, 4));
        measuredVoltage = (double) (getLongFromByte(data, packetSize-2, 1) + 100) / 10;
        stateFlags = (byte) getLongFromByte(data, packetSize-1, 1);

        long latLong = getLongFromByte(data, packetSize - 10, 4);
        long lonLong = getLongFromByte(data, packetSize - 6, 4);
        long altLong = bytesToLong(shiftRight(Arrays.copyOfRange(data, 4, 6), 1)) & 0x7FFF;
        long velocity = bytesToLong(Arrays.copyOfRange(data, 7, 9)) & 0x03FF;
        coordinates.setLat((double) latLong / 100000.0);
        coordinates.setLon((double) lonLong / 100000.0);
        coordinates.setAlt((double) altLong / 10.0);
        coordinates.setVelocity((double) velocity / 10.0);
        gpsSatCount = ((data[6] << 1) & 0x1F) + ((data[7] >> 7) & 0x1);
        glonassSatCount = ((data[6] >> 4) & 0xF) + ((data[5] << 4) & 0x10);

        int chDataStartAt = 9;
        for (int i = 0; i < usedChannels ; i++) {
            long currChannelData = getLongFromByte(data, chDataStartAt, 8);
            analogs.put(i, currChannelData);
            chDataStartAt +=8;
        }
    }

    private static long getLongFromByte(byte[] data, int start, int length) {
        long result = 0;
        for (int i = 0; i < length; i++) {
            result += (data[start + i] & 0xFF) << (i * 8);
        }
        return result;
    }

}
