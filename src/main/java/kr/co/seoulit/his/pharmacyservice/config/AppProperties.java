package kr.co.seoulit.his.pharmacyservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOriginPatterns = new ArrayList<>();
    }
}
