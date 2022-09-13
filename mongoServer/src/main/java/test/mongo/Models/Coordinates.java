package test.mongo.Models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Coordinates {

    @Field("LON")
    private Double lon;
    @Field("LAT")
    private Double lat;
    @Field("ALT")
    private Double alt;
    @Field("VEL")
    private Double velocity;
}
