package cloud.memome.backend.infra.security.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import cloud.memome.backend.application.auth.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return;
		}

		String accessToken = authorization.substring("Bearer ".length());
		if (accessToken.equals("null")) {
			return;
		}

		Claims claims = jwtProvider.parseJwt(accessToken);
		refreshTokenService.deleteRefreshToken(Long.valueOf(claims.getSubject()));

		Cookie cookie = new Cookie("refresh", "");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
}
