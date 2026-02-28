package cloud.memome.backend.application.member.dto;

import cloud.memome.backend.domain.member.ProviderType;
import lombok.Value;

@Value
public class IdentityDto {
	ProviderType providerType;
	String providerId;
}
