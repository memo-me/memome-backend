package cloud.memome.backend.auth;

import java.net.URL;
import java.util.Map;

import cloud.memome.backend.member.ProviderType;

public class OAuthUserInfoResolver {
	public static OAuthUserInfo resolve(Map<String, Object> attributes) {
		OAuthUserInfo oAuthUserInfo = null;
		ProviderType providerType = ProviderType.valueOfIssuer(((URL)attributes.get("iss")).toString());
		switch (providerType) {
			case GOOGLE:
				oAuthUserInfo = resolveGoogle(attributes);
				break;
			case KAKAO:
				oAuthUserInfo = resolveKakao(attributes);
				break;
		}
		return oAuthUserInfo;
	}

	private static OAuthUserInfo resolveGoogle(Map<String, Object> attributes) {
		return new OAuthUserInfo(
			ProviderType.GOOGLE,
			(String)attributes.get("sub"),
			(String)attributes.get("name"),
			(String)attributes.get("email")
		);
	}

	private static OAuthUserInfo resolveKakao(Map<String, Object> attributes) {
		return new OAuthUserInfo(
			ProviderType.KAKAO,
			(String)attributes.get("sub"),
			(String)attributes.get("nickname"),
			(String)attributes.get("email")
		);
	}
}
