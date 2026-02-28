package cloud.memome.backend.application.member.dto;

import cloud.memome.backend.api.auth.LoginMember;
import cloud.memome.backend.domain.member.ProviderType;
import lombok.Value;

@Value
public class IdentityDto {
	ProviderType providerType;
	String providerId;

	public static IdentityDto create(LoginMember loginMember) {
		return new IdentityDto(
			loginMember.getProviderType(),
			loginMember.getProviderId()
		);
	}
}
