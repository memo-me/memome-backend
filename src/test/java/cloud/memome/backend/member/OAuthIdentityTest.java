package cloud.memome.backend.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuthIdentityTest {
	@DisplayName("OAuthIdentity 정상 생성")
	@Test
	public void create_OAuthIdentity() {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerId = "1234567890";

		//when
		OAuthIdentity oAuthIdentity = new OAuthIdentity(providerType, providerId);

		//then
		Assertions.assertThat(oAuthIdentity.getProviderType()).isEqualTo(providerType);
		Assertions.assertThat(oAuthIdentity.getProviderId()).isEqualTo(providerId);
	}

	@DisplayName("providerType == null 일 때, OAuthIdentity 생성 실패")
	@Test
	public void create_fail_when_providerType_is_null() {
		//given
		String providerId = "1234567890";

		//when && then
		Assertions.assertThatThrownBy(() -> new OAuthIdentity(null, providerId))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("providerId == null 일 때, OAuthIdentity 생성 실패")
	@Test
	public void create_fail_when_providerId_is_null() {
		//given
		ProviderType providerType = ProviderType.GOOGLE;

		//when && then
		Assertions.assertThatThrownBy(() -> new OAuthIdentity(providerType, null))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("providerId가 \'빈 문자열\'일 때, OAuthIdentity 생성 실패")
	@Test
	public void create_fail_when_providerId_is_empty() {
		//given
		ProviderType providerType = ProviderType.GOOGLE;
		String providerIdIsEmpty = "     ";

		//when && then
		Assertions.assertThatThrownBy(() -> new OAuthIdentity(providerType, providerIdIsEmpty))
			.isInstanceOf(IllegalArgumentException.class);
	}
}