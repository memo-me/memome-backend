package cloud.memome.backend.member.dto;

import cloud.memome.backend.auth.LoginMember;
import cloud.memome.backend.member.ProviderType;
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
