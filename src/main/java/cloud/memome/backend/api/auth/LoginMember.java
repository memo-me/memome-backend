package cloud.memome.backend.api.auth;

import cloud.memome.backend.domain.member.ProviderType;
import lombok.Value;

@Value
public class LoginMember {
	ProviderType providerType;
	String providerId;
}
