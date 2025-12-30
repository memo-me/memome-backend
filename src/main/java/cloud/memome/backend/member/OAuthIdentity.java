package cloud.memome.backend.member;

import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthIdentity {
	@Enumerated(EnumType.STRING)
	private ProviderType providerType;
	private String providerId;

	public OAuthIdentity(ProviderType providerType, String providerId) {
		if (providerType == null || providerId == null || providerId.isBlank()) {
			throw new IllegalArgumentException("providerType 또는 providerId는 null이거나 공백일 수 없습니다");
		}
		this.providerType = providerType;
		this.providerId = providerId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		OAuthIdentity that = (OAuthIdentity)obj;
		return providerType == that.providerType && Objects.equals(providerId, that.providerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(providerType, providerId);
	}

	@Override
	public String toString() {
		return providerType.toString() + "(" + providerId + ")";
	}
}
