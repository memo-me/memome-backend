package cloud.memome.backend.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProviderTypeTest {
	@DisplayName("valueOfIssuer: GOOGLE 성공")
	@Test
	public void valueOfIssuer_google_success() {
		//given
		String googleIssuer = "https://accounts.google.com";

		//when
		ProviderType providerType = ProviderType.valueOfIssuer(googleIssuer);

		//then
		Assertions.assertThat(providerType).isEqualTo(ProviderType.GOOGLE);
	}

	@DisplayName("valueOfIssuer: KAKAO 성공")
	@Test
	public void valueOfIssuer_kakao_success() {
		//given
		String kakaoIssuer = "https://kauth.kakao.com";

		//when
		ProviderType providerType = ProviderType.valueOfIssuer(kakaoIssuer);

		//then
		Assertions.assertThat(providerType).isEqualTo(ProviderType.KAKAO);
	}

	@DisplayName("valueOfIssuer: issuer == null 일 때")
	@Test
	public void valueOfIssuer_null_fail() {
		//when && then
		Assertions.assertThatThrownBy(() -> ProviderType.valueOfIssuer(null))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("valueOfIssuer: issuer가 빈문자열 일 때")
	@Test
	public void valueOfIssuer_blank_fail() {
		//when && then
		Assertions.assertThatThrownBy(() -> ProviderType.valueOfIssuer("               "))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("valueOfIssuer: 지원하지 않는 issuer일 때")
	@Test
	public void valueOfIssuer_not_support_fail() {
		//when && then
		Assertions.assertThatThrownBy(() -> ProviderType.valueOfIssuer("https://www.naver.com"))
			.isInstanceOf(IllegalArgumentException.class);
	}
}