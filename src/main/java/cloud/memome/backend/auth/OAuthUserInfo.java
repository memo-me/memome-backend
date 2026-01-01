package cloud.memome.backend.auth;

import cloud.memome.backend.member.ProviderType;
import lombok.Getter;

@Getter
public class OAuthUserInfo {
	private final ProviderType providerType;
	private final String providerId;
	private final String nickname;
	private final String email;

	public OAuthUserInfo(ProviderType providerType, String providerId, String nickname, String email) {
		validate(providerType, providerId, nickname, email);
		this.providerType = providerType;
		this.providerId = providerId;
		this.nickname = nickname;
		this.email = email;
	}

	private static void validate(ProviderType providerType, String providerId, String nickname, String email) {
		if (providerType == null || providerId == null || providerId.isBlank()) {
			throw new IllegalArgumentException("providerType 또는 providerId는 null이거나 빈 문자열일 수 없습니다");
		}
	}
}
