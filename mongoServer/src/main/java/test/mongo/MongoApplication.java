package test.mongo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import test.mongo.Handlers.Handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static test.mongo.Const.Const.*;


@SpringBootApplication
@EnableScheduling
public class MongoApplication implements CommandLineRunner {

	public static void main(String[] args) {
			SpringApplication.run(MongoApplication.class, args);
	}
	private static final Logger logger = LogManager.getLogger(MongoApplication.class);
	private final MongoTemplate mongoTemplate;

	@Autowired
	public MongoApplication(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public void run(String... args) {
		ExecutorService executorService = Executors.newFixedThreadPool(8);
		try (DatagramSocket serverSocket = new DatagramSocket(RCV_PORT);) {
			logger.info("Listening on udp: " + InetAddress.getLocalHost().getHostAddress() + ":" +  RCV_PORT);
			while (true) {
				byte[] receiveData = new byte[1400];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
					executorService.execute(new Handler(receivePacket, serverSocket, mongoTemplate));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			executorService.shutdown();
		}
	}

}