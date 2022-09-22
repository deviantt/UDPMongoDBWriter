package test.mongo.Handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import test.mongo.MongoApplication;
import test.mongo.Services.CRC8;
import test.mongo.Models.Frame;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Handler implements Runnable {

    private final MongoTemplate mongoTemplate;
    private final DatagramPacket receivePacket;
    private final DatagramSocket serverSocket;
    private Frame receiveFrame;
    private static final Logger logger = LogManager.getLogger(Handler.class);


    public Handler(DatagramPacket receivePacket, DatagramSocket serverSocket, MongoTemplate mongoTemplate) {
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run() {
        CRC8 crc8 = new CRC8();
        receiveFrame = new Frame(receivePacket, serverSocket);
        Query query = Query.query(Criteria.where("IME").is(receiveFrame.getImei()));
        crc8.reset();
        crc8.update(Arrays.copyOfRange(receivePacket.getData(), 0, receiveFrame.getFrameSize()));
        try {
//            if (mongoTemplate.exists(query, "Devices")) {
                if (receivePacket.getData()[receiveFrame.getFrameSize()] == (byte) (crc8.getValue() & 0xff)) {
                    mongoTemplate.indexOps(String.valueOf(receiveFrame.getImei())).ensureIndex(new Index("TIM", Sort.Direction.DESC).unique());
                    mongoTemplate.insert(receiveFrame.getPackets(), String.valueOf(receiveFrame.getImei()));
                    sendAnswer(packAnswer());
                } else logger.info("CRC check failed for device " + receiveFrame.getImei());
//            } else logger.info("Attempt to send data from Device " + receiveFrame.getImei());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendAnswer(DatagramPacket answer) {
        try {
            serverSocket.send(answer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DatagramPacket packAnswer() {
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.put(longToBytes(receiveFrame.getImei()));
        bb.put((byte) 2);
        return new DatagramPacket(bb.array(), bb.array().length, receivePacket.getAddress(), receivePacket.getPort());
    }

    public static byte[] longToBytes (long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }
}
