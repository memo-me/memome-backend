package cloud.memome.backend.infra.security.jwt;

import lombok.Value;

@Value
public class CreateJwtDto {
	Long subject;
	TokenType type;
}
