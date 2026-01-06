package cloud.memome.backend.member.dto;

import cloud.memome.backend.member.ProviderType;
import lombok.Value;

@Value
public class IdentityDto {
	ProviderType providerType;
	String providerId;
}
