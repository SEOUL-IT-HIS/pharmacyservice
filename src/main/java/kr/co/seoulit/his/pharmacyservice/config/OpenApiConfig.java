package kr.co.seoulit.his.pharmacyservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pharmacyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("pharmacyservice API")
                        .description("약제(PHM) 서비스 API 문서")
                        .version("v1"));
    }
}
