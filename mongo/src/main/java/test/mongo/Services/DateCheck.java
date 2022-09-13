package test.mongo.Services;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import test.mongo.MongoApplication;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
public class DateCheck {

    private final MongoTemplate mongoTemplate;
    private static final Logger logger = LogManager.getLogger(DateCheck.class);

    public DateCheck(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

//    @PostConstruct
//    private void onStartup() {
//        dateCheck();
//    }

    @Scheduled(cron = "0 0 10 * * MON", zone="GMT+5:00")
    private void dateCheck() {
        logger.info("Datecheck executed");

        Query query = Query.query(Criteria.where("TIM").lte(Instant.now().minus(90, ChronoUnit.DAYS)));

        for (String imei : mongoTemplate.getCollectionNames()) {
            try {
                if (!Objects.equals(imei, "Users") && !Objects.equals(imei, "Devices"))
                    mongoTemplate.remove(query, imei);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
