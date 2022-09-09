package test.mongo.Models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
public class Coordinates {

    @Field("LON")
    private double lon;
    @Field("LAT")
    private double lat;
    @Field("ALT")
    private double alt;
    @Field("VEL")
    private double velocity;
}
