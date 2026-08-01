package kg.attractor.payment_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String SECURITY_SCHEME_NAME = "basicAuth";

    @Bean
    public OpenAPI paymentSystemOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment System API")
                        .description("Control work #7")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                        ));
    }
}
