package cloud.memome.backend.infra.security.oidc;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import cloud.memome.backend.application.auth.RefreshTokenService;
import cloud.memome.backend.infra.security.jwt.CreateJwtDto;
import cloud.memome.backend.infra.security.jwt.JwtProvider;
import cloud.memome.backend.infra.security.jwt.TokenType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OidcSuccessHandler implements AuthenticationSuccessHandler {
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		LoginMemberOidcUser oidcUser = (LoginMemberOidcUser)authentication.getPrincipal();
		Long memberId = oidcUser.getMemberId();

		String accessToken = jwtProvider.createJwt(new CreateJwtDto(memberId, TokenType.ACCESS));
		response.setHeader("Authorization", "Bearer " + accessToken);

		String refreshToken = jwtProvider.createJwt(new CreateJwtDto(memberId, TokenType.REFRESH));
		refreshTokenService.saveRefreshToken(memberId, refreshToken);

		Cookie refresh = new Cookie("refresh", refreshToken);
		refresh.setHttpOnly(true);
		refresh.setMaxAge(TokenType.REFRESH.getDurationToSec().intValue());
		refresh.setAttribute("SameSite", "Strict");
		refresh.setPath("/");
		response.addCookie(refresh);
	}
}
