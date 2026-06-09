package cloud.memome.backend.infra.security.jwt;

import java.time.Duration;

public enum TokenType {
	ACCESS(Duration.ofMinutes(30)),
	REFRESH(Duration.ofDays(14));

	private final Duration duraion;

	TokenType(Duration duraion) {
		this.duraion = duraion;
	}

	public Long getDurationToSec() {
		return this.duraion.toSeconds();
	}

	public Long getDurationToMilli() {
		return this.duraion.toMillis();
	}
}
