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
		Info info = new Info()
			.title("MemoMe 서비스 API 명세서")
			.version("V1.0");

		//-------------------- 인가 방식 지정 ---------------------
		SecurityScheme auth = new SecurityScheme()
			.type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name("JSESSIONID");
		SecurityRequirement securityRequirement = new SecurityRequirement().addList("basicAuth");
		return new OpenAPI()
			.components(new Components().addSecuritySchemes("basicAuth", auth))
			.addSecurityItem(securityRequirement)
			.info(info);
	}
}
