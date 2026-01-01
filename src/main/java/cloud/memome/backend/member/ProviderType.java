package cloud.memome.backend.member;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum ProviderType {
	GOOGLE("https://accounts.google.com"),
	KAKAO("https://kauth.kakao.com");

	private final String issuer;

	ProviderType(String issuer) {
		this.issuer = issuer;
	}

	public static ProviderType valueOfIssuer(String issuer) {
		if (issuer == null || issuer.isBlank()) {
			throw new IllegalArgumentException("issuer은 null이거나 빈 문자열일 수 없습니다.");
		}
		return Arrays.stream(ProviderType.values())
			.filter(x -> issuer.equals(x.getIssuer()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 OAuth Issuer 입니다: " + issuer));
	}
}
