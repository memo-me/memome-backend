package cloud.memome.backend.auth;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import cloud.memome.backend.member.ProviderType;

public class OAuthUserInfoResolver {
	public static OAuthUserInfo resolve(OidcUser oidcUser) {
		OAuthUserInfo oAuthUserInfo = null;
		ProviderType providerType = ProviderType.valueOfIssuer(oidcUser.getIssuer().toString());
		switch (providerType) {
			case GOOGLE:
				oAuthUserInfo = resolveGoogle(oidcUser);
				break;
			case KAKAO:
				oAuthUserInfo = resolveKakao(oidcUser);
				break;
		}
		return oAuthUserInfo;
	}

	private static OAuthUserInfo resolveGoogle(OidcUser oidcUser) {
		return new OAuthUserInfo(
			ProviderType.GOOGLE,
			oidcUser.getSubject(),
			oidcUser.getName(),
			oidcUser.getEmail()
		);
	}

	private static OAuthUserInfo resolveKakao(OidcUser oidcUser) {
		return new OAuthUserInfo(
			ProviderType.KAKAO,
			oidcUser.getSubject(),
			oidcUser.getNickName(),
			oidcUser.getEmail()
		);
	}
}
