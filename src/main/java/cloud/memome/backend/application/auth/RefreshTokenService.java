package cloud.memome.backend.application.auth;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import cloud.memome.backend.infra.security.jwt.TokenType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final String keyPrefix = "refresh:member:";
	private final RedisTemplate<String, String> redisTemplate;

	public void saveRefreshToken(Long memberId, String refreshToken) {
		redisTemplate.opsForValue()
			.set(getKey(memberId), refreshToken,
				Duration.ofMillis(TokenType.REFRESH.getDurationToMilli()));
	}

	public void deleteRefreshToken(Long memberId) {
		String key = getKey(memberId);
		if (redisTemplate.hasKey(key)) {
			redisTemplate.delete(key);
		}
	}

	public String findRefreshTokenById(Long memberId) {
		String key = getKey(memberId);
		return redisTemplate.opsForValue().get(key);
	}

	private String getKey(Long id) {
		return keyPrefix + id;
	}
}
