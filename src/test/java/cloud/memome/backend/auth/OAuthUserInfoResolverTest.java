package cloud.memome.backend.auth;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cloud.memome.backend.member.ProviderType;

class OAuthUserInfoResolverTest {

	@Test
	@DisplayName("Google Provider - resolve 성공")
	public void resolve_google_success() throws MalformedURLException {
		//given
		Map<String, Object> attributes = Map.of(
			"iss", new URL(ProviderType.GOOGLE.getIssuer()),
			"sub", "0123456789",
			"name", "홍길동",
			"email", "test@email.com"
		);

		//when
		OAuthUserInfo oAuthUserInfo = OAuthUserInfoResolver.resolve(attributes);

		//then
		Assertions.assertThat(oAuthUserInfo.getProviderType()).isEqualTo(ProviderType.GOOGLE);
		Assertions.assertThat(oAuthUserInfo.getProviderId()).isEqualTo("0123456789");
		Assertions.assertThat(oAuthUserInfo.getNickname()).isEqualTo("홍길동");
		Assertions.assertThat(oAuthUserInfo.getEmail()).isEqualTo("test@email.com");
	}

	@Test
	@DisplayName("Kakao Provider - resolve 성공")
	public void resolve_kakao_success() throws MalformedURLException {
		//given
		Map<String, Object> attributes = Map.of(
			"iss", new URL(ProviderType.KAKAO.getIssuer()),
			"sub", "0123456789",
			"nickname", "홍길동",
			"email", "test@email.com"
		);

		//when
		OAuthUserInfo oAuthUserInfo = OAuthUserInfoResolver.resolve(attributes);

		//then
		Assertions.assertThat(oAuthUserInfo.getProviderType()).isEqualTo(ProviderType.KAKAO);
		Assertions.assertThat(oAuthUserInfo.getProviderId()).isEqualTo("0123456789");
		Assertions.assertThat(oAuthUserInfo.getNickname()).isEqualTo("홍길동");
		Assertions.assertThat(oAuthUserInfo.getEmail()).isEqualTo("test@email.com");
	}

	@Test
	@DisplayName("지원하지 않는 Provider - resolve 실패")
	public void resolve_unsupported_provider_fail() throws MalformedURLException {
		//given
		Map<String, Object> attributes = Map.of(
			"iss", new URL("https://naver.com"),
			"sub", "0123456789",
			"nickname", "홍길동",
			"email", "test@email.com"
		);

		//when && then
		Assertions.assertThatThrownBy(() -> OAuthUserInfoResolver.resolve(attributes))
			.isInstanceOf(IllegalArgumentException.class);
	}
}