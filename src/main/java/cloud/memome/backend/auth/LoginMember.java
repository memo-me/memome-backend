package cloud.memome.backend.auth;

import cloud.memome.backend.member.ProviderType;
import lombok.Value;

@Value
public class LoginMember {
	ProviderType providerType;
	String providerId;
}
