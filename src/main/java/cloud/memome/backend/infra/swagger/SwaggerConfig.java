package cloud.memome.backend.infra.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {
		Info info = new Info().title("Memome 서비스 API 명세서")
			.description("This is How API")
			.version("v0.0.1");

		//-------------------- 인가 방식 지정 ---------------------
		SecurityRequirement jwt = new SecurityRequirement().addList("JWT");
		SecurityScheme keyScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP)
			.bearerFormat("JWT")
			.scheme("bearer");

		return new OpenAPI().addSecurityItem(jwt)
			.components(new Components().addSecuritySchemes("JWT", keyScheme))
			.info(info);
	}
}
