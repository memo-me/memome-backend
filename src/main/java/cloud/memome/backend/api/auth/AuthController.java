package cloud.memome.backend.api.auth;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cloud.memome.backend.api.auth.exception.InvalidRefreshTokenException;
import cloud.memome.backend.application.auth.RefreshTokenService;
import cloud.memome.backend.infra.security.jwt.CreateJwtDto;
import cloud.memome.backend.infra.security.jwt.JwtProvider;
import cloud.memome.backend.infra.security.jwt.TokenType;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	@GetMapping("/refresh")
	public String refreshAccessToken(@CookieValue(required = true) String refresh) {
		Claims claims = jwtProvider.parseJwt(refresh);
		Long memberId = Long.valueOf(claims.getSubject());
		String found = refreshTokenService.findRefreshTokenById(memberId);
		if (!refresh.equals(found)) {
			throw new InvalidRefreshTokenException();
		}
		return jwtProvider.createJwt(new CreateJwtDto(memberId, TokenType.ACCESS));
	}
}
