package cloud.memome.backend.infra.security.oidc;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import cloud.memome.backend.infra.security.jwt.CreateJwtDto;
import cloud.memome.backend.infra.security.jwt.JwtProvider;
import cloud.memome.backend.infra.security.jwt.TokenType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OidcSuccessHandler implements AuthenticationSuccessHandler {
	private final JwtProvider jwtProvider;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		LoginMemberOidcUser oidcUser = (LoginMemberOidcUser)authentication.getPrincipal();
		Long memberId = oidcUser.getMemberId();

		String jwt = jwtProvider.createJwt(new CreateJwtDto(memberId, TokenType.ACCESS));
		response.setHeader("Authorization", "Bearer " + jwt);
	}
}
