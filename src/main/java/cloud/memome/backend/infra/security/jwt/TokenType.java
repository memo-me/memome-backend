package cloud.memome.backend.infra.security.jwt;

import java.time.Duration;

public enum TokenType {
	ACCESS(Duration.ofMinutes(30)),
	REFRESH(Duration.ofDays(14));

	private final Duration duration;

	TokenType(Duration duration) {
		this.duration = duration;
	}

	public Long getDurationToSec() {
		return this.duration.toSeconds();
	}

	public Long getDurationToMilli() {
		return this.duration.toMillis();
	}
}
