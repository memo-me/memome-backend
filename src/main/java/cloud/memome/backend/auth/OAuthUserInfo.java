package cloud.memome.backend.auth;

import cloud.memome.backend.member.ProviderType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class OAuthUserInfo {
	private final ProviderType providerType;
	private final String providerId;
	private final String nickname;
	private final String email;
}
